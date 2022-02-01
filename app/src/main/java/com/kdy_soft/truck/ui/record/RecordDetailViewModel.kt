package com.kdy_soft.truck.ui.record

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.entity.Chat
import com.kdy_soft.truck.data.entity.DeliveryRecord
import com.kdy_soft.truck.data.model.Address
import com.kdy_soft.truck.data.model.ChatModel
import com.kdy_soft.truck.data.repository.ChatRepository
import com.kdy_soft.truck.data.repository.LocationRepository
import com.kdy_soft.truck.data.repository.RecordRepository
import com.kdy_soft.truck.data.repository.UserRepository
import com.kdy_soft.truck.ui.home.ChatViewModel
import com.kdy_soft.truck.util.TruckUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val locationRepository: LocationRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val uid = Firebase.auth.uid ?: ""
    val recordId = MutableLiveData("")

    val record = Transformations.switchMap(recordId) { id ->
        val data = MutableLiveData<DeliveryRecord>()
        getRecord(id, data)
        data
    }

    val departure = Transformations.switchMap(record) {
        val data = MutableLiveData<Address>()
        locationRepository.getAddressByCoordination(it.departLocation.toLatLng()) { address ->
            data.postValue(address)
        }
        data
    }

    val destination = Transformations.switchMap(record) {
        val data = MutableLiveData<Address>()
        locationRepository.getAddressByCoordination(it.destLocation.toLatLng()) { address ->
            data.postValue(address)
        }
        data
    }

    val timeString = Transformations.map(record) {
        val lastTime = it.arriveTime - it.departTime
        val seconds = (lastTime / 1000).toInt()
        TruckUtils.timeString(seconds, forDate = true)
    }

    val distanceString = Transformations.map(record) {
        TruckUtils.distanceString(it.distance)
    }

    val path = Transformations.switchMap(record) {
        val data = MutableLiveData<List<LatLng>>()
        locationRepository.getNavigation(
            it.destLocation.toLatLng(),
            it.departLocation.toLatLng()
        ) { response ->

            response?.let {
                viewModelScope.launch(Dispatchers.Default) {
                    val result = mutableListOf<LatLng>()
                    val sections = it.routes.getOrNull(0)?.sections
                    sections?.let {
                        sections.getOrNull(0)?.roads?.forEach { road ->
                            val repeat = road.vertexes.size / 2

                            for (index in 0 until repeat) {
                                val x = road.vertexes[index * 2]
                                val y = road.vertexes[(index * 2 + 1)]
                                result.add(LatLng(y, x))
                            }
                        }
                    }
                    data.postValue(result)
                }
            }
            data.postValue(emptyList())
        }
        data
    }

    val chats = Transformations.switchMap(record) {
        val data = MutableLiveData<List<ChatModel>>(emptyList())
        val list = mutableListOf<ChatModel>()
        val chatRoomId = it.chatRoomId

        var selectedChatOpponent = mapOf(
            UserRepository.KEY_PROFILE to "",
            UserRepository.KEY_NAME to ""
        )

        val opponentId = if (uid == it.driverId) {
            it.ownerId
        } else {
            it.driverId
        }

        val countDownLatch = CountDownLatch(1)

        userRepository.getProfile(opponentId) { profileMap ->
            selectedChatOpponent = profileMap
            countDownLatch.countDown()
        }

        viewModelScope.launch(Dispatchers.IO) {
            countDownLatch.await(5000L, TimeUnit.MILLISECONDS)

            data.apply {
                val chatListener = object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val chat = snapshot.getValue(Chat::class.java)
                        chat?.let {
                            val chatModel =
                                convertChatModel(
                                    chat,
                                    selectedChatOpponent
                                )
                            list.add(chatModel)
                            data.postValue(list)
                        }

                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
                chatRepository.getChats(chatRoomId, chatListener, page = 1)
            }
        }

        data
    }

    private fun convertChatModel(
        chat: Chat,
        profileMap: Map<String, String>
    ): ChatModel {
        return ChatModel(
            id = chat.id,
            isMe = chat.uid == uid,
            name = profileMap[UserRepository.KEY_NAME] ?: "",
            profileUrl = profileMap[UserRepository.KEY_PROFILE] ?: "",
            msg = chat.msg,
            date = Date(chat.time)
        )
    }

    private fun getRecord(id: String, data: MutableLiveData<DeliveryRecord>) {
        if (id.isNotBlank() && uid.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                recordRepository.getRecord(uid, id) {
                    data.postValue(it)
                }
            }
        }
    }
}