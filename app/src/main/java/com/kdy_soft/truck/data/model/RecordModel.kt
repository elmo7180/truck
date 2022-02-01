package com.kdy_soft.truck.data.model

import java.util.*

data class RecordModel(
    val id: String = "",
    val startDate: Date = Date(),
    val completeDate: Date = Date(),
    val distance: Int = 0,
    val deliveryId: String = "",
    val price: Int = 0,
    val departure: Address = Address.FAIL,
    val destination: Address = Address.FAIL
)