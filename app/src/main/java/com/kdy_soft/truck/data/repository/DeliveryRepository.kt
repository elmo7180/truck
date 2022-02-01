package com.kdy_soft.truck.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.ktx.storage
import com.kdy_soft.truck.data.contract.*
import com.kdy_soft.truck.data.entity.*
import com.kdy_soft.truck.data.model.DeliveryModel
import com.kdy_soft.truck.data.repository.source.DeliveryDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryRepository @Inject constructor(
    private val deliveryDataSource: DeliveryDataSource,
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository
) {
    private val database = Firebase.database
    private val deliveryPhotos = Firebase.storage.getReference("images/delivery/")
    private val filesDir = context.applicationContext.filesDir

    fun getDelivery(id: String, callback: (Delivery) -> Unit) {
        deliveryDataSource.getDelivery(id, callback)
    }

    fun getDeliveryList(current: LatLng, callback: (List<Delivery>) -> Unit) {
        deliveryDataSource.getDeliveryList(current, callback)
    }

    fun getDeliveryFile(deliveryModel: DeliveryModel) =
        File(filesDir, "Img_${deliveryModel.id}.jpg")

    fun getPhotoUri(file: File): Uri = FileProvider.getUriForFile(
        context,
        "com.kdy_soft.truck.fileprovider",
        file
    )

    fun uploadPhoto(photoFile: File?, urlCallback: (String) -> Unit) {
        if (photoFile != null) {
            val ref = deliveryPhotos.child(photoFile.name)
            if (!photoFile.exists()) {
                urlCallback.invoke("")
                return
            }

            ref.putFile(getPhotoUri(photoFile))
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        ref.downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val url = task.result
                                urlCallback.invoke(url.toString())
                            } else {
                                urlCallback.invoke("")
                            }
                        }
                    } else {
                        urlCallback.invoke("")
                    }
                }
        } else {
            urlCallback.invoke("")
        }

    }

    fun setDelivery(
        delivery: Delivery,
        listener: (Task<Void>) -> Unit = {}
    ) {
        database.getReference(DELIVERY_TABLE)
            .child(delivery.id)
            .setValue(delivery)
            .addOnCompleteListener(listener)
    }

    fun delivery(
        delivery: Delivery,
        uid: String,
        onComplete: (Task<Void>) -> Unit = {}
    ) {
        //TODO notify to owner

        val origin = DeliveryPath(
            deliveryId = delivery.id,
            x = delivery.departure.longitude,
            y = delivery.departure.latitude,
            timeInMillis = System.currentTimeMillis()
        )

        val chat = Chat(
            roomId = UUID.randomUUID().toString(),
            uid = uid,
            msg = "운송이 배정되었습니다."
        )

        val chatRoom = ChatRoom(
            id = chat.roomId,
            deliveryId = delivery.id,
            participantIds = mapOf(
                delivery.ownerId to true,
                uid to true
            ),
            recentMsg = chat.msg,
            recentTime = chat.time
        )

        database.reference
            .updateChildren(
                mapOf(
                    "${CHAT_ROOM_TABLE}/${uid}/${chatRoom.id}" to chatRoom,
                    "${CHAT_ROOM_TABLE}/${delivery.ownerId}/${chatRoom.id}" to chatRoom,
                    "${CHAT_ROOM_TABLE}/${chatRoom.id}" to chatRoom,

                    "${CHAT_TABLE}/${chatRoom.id}/${chat.id}" to chat,

                    "${USER_TABLE}/$uid/deliveryId" to delivery.id,

                    "${DELIVERY_PATH_TABLE}/${delivery.id}/${origin.timeInMillis}" to origin,

                    "${DELIVERY_TABLE}/${delivery.id}/departTime" to origin.timeInMillis,
                    "${DELIVERY_TABLE}/${delivery.id}/chatRoomId" to chatRoom.id,
                    "${DELIVERY_TABLE}/${delivery.id}/driverId" to uid
                )
            ).addOnCompleteListener(onComplete)
            .addOnSuccessListener {
                val roomId = chatRoom.id
                subscribeChatRoom(roomId)
            }
    }

    fun getDeliveryPaths() {

    }

    fun addDeliveryPath(path: DeliveryPath) {

    }

    fun deliveryComplete(
        delivery: Delivery,
        uid: String,
        onComplete: (DeliveryRecord?) -> Unit = {}
    ) {
        val countDown = CountDownLatch(1)
        var dist = 0

        CoroutineScope(Dispatchers.IO).launch {
            locationRepository.getNavigation(
                delivery.departure.toLatLng(),
                delivery.destination.toLatLng()
            ) {
                it?.let {
                    dist = it.routes.getOrNull(0)?.summary?.distance ?: 0
                }
                countDown.countDown()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            countDown.await()

            val deliveryRecord = DeliveryRecord(
                arriveTime = System.currentTimeMillis(),
                departTime = delivery.departTime,
                departLocation = delivery.departure,
                destLocation = delivery.destination,
                deliveryId = delivery.id,
                chatRoomId = delivery.chatRoomId,
                price = delivery.price,
                cartons = delivery.cartons,
                driverId = uid,
                ownerId = delivery.ownerId,
                distance = dist
            )

            val record = Record(
                id = deliveryRecord.id,
                startTime = deliveryRecord.departTime,
                completeTime = deliveryRecord.arriveTime,
                price = deliveryRecord.price,
                driverId = delivery.driverId
            )

            val chat = Chat(
                roomId = delivery.chatRoomId,
                msg = "운송이 완료되었습니다.",
                uid = uid
            )

            database.reference
                .updateChildren(
                    mapOf(
                        //채팅방에서 나가기 설정
                        "${CHAT_ROOM_TABLE}/${uid}/${delivery.chatRoomId}" to null,
                        "${CHAT_ROOM_TABLE}/${delivery.ownerId}/${delivery.chatRoomId}" to null,
                        "${CHAT_ROOM_TABLE}/${chat.roomId}/recentMsg" to chat.msg,
                        "${CHAT_ROOM_TABLE}/${chat.roomId}/recentTime" to chat.time,
                        //배송 완료 메세지 전송
                        "${CHAT_TABLE}/${chat.roomId}/${chat.id}" to chat,

                        //user's 운송 아이디 삭제
                        "${USER_TABLE}/$uid/deliveryId" to "",
                        //채팅방의 운송 완료 설정
                        "${CHAT_ROOM_TABLE}/${delivery.chatRoomId}/deliveryComplete" to true,
                        //운송의 마감기한을 현재로
                        "${DELIVERY_TABLE}/${delivery.id}/deadline" to System.currentTimeMillis(),
                        //도착시간 설정
                        "${DELIVERY_TABLE}/${delivery.id}/arriveTime" to record.completeTime,
                        //운송자와 소유자의 기록 추가
                        "${RECORD_TABLE}/${uid}/${record.id}" to record,
                        "${RECORD_TABLE}/${delivery.ownerId}/${record.id}" to record,
                        //상세 기록
                        "${DELIVERY_RECORD_TABLE}/${deliveryRecord.id}/" to deliveryRecord
                    )
                )
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        unsubscribeRoomId(delivery.chatRoomId)
                        onComplete.invoke(deliveryRecord)
                    } else {
                        onComplete.invoke(null)
                    }
                }
        }


    }

    private fun unsubscribeRoomId(roomId: String) {
        Firebase.messaging.unsubscribeFromTopic(roomId)
    }

    private fun subscribeChatRoom(roomId: String) {
        Firebase.messaging.subscribeToTopic(roomId)
    }
}