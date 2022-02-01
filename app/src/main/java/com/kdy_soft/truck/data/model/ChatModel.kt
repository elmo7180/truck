package com.kdy_soft.truck.data.model

import com.kdy_soft.truck.data.entity.Chat
import java.util.*

data class ChatModel(
    val id: String = "",
    val isMe: Boolean = false,
    val name: String = "",
    val msg: String = "",
    val date: Date = Date(),
    val profileUrl: String = ""
)