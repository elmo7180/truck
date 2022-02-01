package com.kdy_soft.truck.data.repository.source

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ValueEventListener
import com.kdy_soft.truck.data.entity.Chat
import com.kdy_soft.truck.data.entity.ChatRoom
import com.kdy_soft.truck.data.model.ChatModel
import com.kdy_soft.truck.data.model.ChatRoomModel

interface ChatDataSource {
    fun getChatRooms(uid: String, valueEventListener: ValueEventListener)
    fun getChats(roomId: String, childEventListener: ChildEventListener, page: Int)
    fun removeEventListener(roomId: String, childEventListener: ChildEventListener, page: Int)
}