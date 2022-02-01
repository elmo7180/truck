package com.kdy_soft.truck.data.repository

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.kdy_soft.truck.data.contract.CHAT_ROOM_TABLE
import com.kdy_soft.truck.data.contract.CHAT_TABLE
import com.kdy_soft.truck.data.entity.Chat
import com.kdy_soft.truck.data.entity.ChatRoom
import com.kdy_soft.truck.data.repository.source.ChatDataSource
import com.kdy_soft.truck.service.FCMService
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ChatRepository"
@Singleton
class ChatRepository @Inject constructor(
    private val chatDataSource: ChatDataSource
) {
    private val database = Firebase.database

    fun getChatRooms(uid: String, eventListener: ValueEventListener) {
        chatDataSource.getChatRooms(uid, eventListener)
    }

    fun getChats(
        chatId: String,
        childEventListener: ChildEventListener,
        page: Int = 1
    ) {
        chatDataSource.getChats(chatId, childEventListener, page)
    }

    fun sendMessage(roomId: String, msg: String, uid: String) {
        val chat = Chat(
            roomId = roomId,
            uid = uid,
            msg = msg
        )

        database.reference
            .updateChildren(
                mapOf(
                    "${CHAT_TABLE}/${roomId}/${chat.id}" to chat,
                    "${CHAT_ROOM_TABLE}/${uid}/${roomId}/recentTime" to chat.time,
                    "${CHAT_ROOM_TABLE}/${uid}/${roomId}/recentMsg" to chat.msg,
                    "${CHAT_ROOM_TABLE}/${roomId}/recentTime" to chat.time,
                    "${CHAT_ROOM_TABLE}/${roomId}/recentMsg" to chat.msg

                )
            )
            .addOnCompleteListener {
                Firebase.database.getReference("token")
                    .child(uid)
                    .get()
                    .addOnSuccessListener {
                        val token = it.getValue(String::class.java) ?: return@addOnSuccessListener

                        Log.d(TAG, " onSuccess get Token ")

                        val remoteMessage = RemoteMessage.Builder("${token}@fcm.googleapis.com")
                            .setData(
                                mapOf(
                                    "sender" to uid,
                                    "message" to msg
                                )
                            )
                            .setMessageId(roomId)
                            .build()

                        Log.d(TAG, " onSuccess get Token before [Send]")
                        Firebase.messaging.send(remoteMessage)
                    }

            }
    }
}