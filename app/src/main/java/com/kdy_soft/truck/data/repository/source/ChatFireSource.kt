package com.kdy_soft.truck.data.repository.source

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.contract.CHAT_ROOM_TABLE
import com.kdy_soft.truck.data.contract.CHAT_TABLE
import com.kdy_soft.truck.data.entity.Chat
import com.kdy_soft.truck.data.entity.ChatRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton

private const val CHAT_PER_PAGE = 100

@Singleton
class ChatFireSource @Inject constructor() : ChatDataSource {
    private val database = Firebase.database

    override fun getChatRooms(uid: String, valueEventListener: ValueEventListener) {
        database.getReference(CHAT_ROOM_TABLE)
            .child(uid)
            .addValueEventListener(valueEventListener)
    }

    override fun getChats(
        roomId: String,
        childEventListener: ChildEventListener,
        page: Int
    ) {
        database.getReference(CHAT_TABLE)
            .child(roomId)
            .orderByChild("time")
            .limitToLast(CHAT_PER_PAGE * page)
            .addChildEventListener(childEventListener)
    }

    override fun removeEventListener(
        roomId: String,
        childEventListener: ChildEventListener,
        page: Int
    ) {
        database
            .getReference(CHAT_TABLE)
            .child(roomId)
            .orderByChild("time")
            .limitToLast(CHAT_PER_PAGE * page)
            .removeEventListener(childEventListener)
    }

}