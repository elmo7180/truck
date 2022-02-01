package com.kdy_soft.truck.data.entity

import java.util.*

data class Chat(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String = "",
    val uid: String = "",
    val msg: String = "",
    val time: Long = System.currentTimeMillis()
)