package com.kdy_soft.truck.data.model

import java.util.*

data class ChatRoomModel(
    val id: String ="",
    val profileUrl: String = "",
    val name : String = "",
    val recentMessage: String = "",
    val recentDate: Date = Date()
)