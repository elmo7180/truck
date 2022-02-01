package com.kdy_soft.truck.data.entity

import java.util.*

data class ChatRoom(
    val id: String = UUID.randomUUID().toString(),
    val deliveryId: String = "",
    val deliveryComplete: Boolean = false,
    val participantIds: Map<String, Boolean> = emptyMap(),
    val createDate: Long = Date().time,
    val recentTime: Long = 0,
    val recentMsg: String = ""
)
