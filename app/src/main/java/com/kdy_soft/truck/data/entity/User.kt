package com.kdy_soft.truck.data.entity

data class User(
    val uid: String = "",
    val name: String = "",
    val mobile: String = "",
    val vehicleNumber: String = "",
    val vehicleKinds: String = "",
    val deliveryId: String = "",
    val profileUrl: String = "",
    val fcmToken: String? = null
)

enum class VehicleKinds {
    TOP1,
    TOP1_5,
    TOP2_5,
    TOP5,
    TOP5_PLUS,
    TOP11,
    TOP18,
    TOP25,
    TOP24;

    fun indexOf(): Int {
        return values().indexOf(this)
    }
}
