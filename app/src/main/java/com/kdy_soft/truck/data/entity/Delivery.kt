package com.kdy_soft.truck.data.entity

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.util.*

data class Delivery(
    val id: String= UUID.randomUUID().toString(),
    val chatRoomId: String = "",
    val ownerId: String = "",
    val driverId: String = "",
    val photoUrl: String="",
    val vehicleKinds: VehicleKinds= VehicleKinds.TOP1,
    val claim: String="",
    val departure: MyLatLng = MyLatLng(0.0,0.0),
    val destination: MyLatLng = MyLatLng(0.0,0.0),
    val cartons: Int = 0,
    val price: Int = 0,
    val deadline: Long = 0,
    val departTime: Long = 0L,
    val arriveTime: Long = 0L
){
}

fun LatLng.fromLatLng() = MyLatLng(latitude, longitude)

class MyLatLng(
    val latitude: Double = 0.0,
    val longitude:Double = 0.0
){
    fun toLatLng() = LatLng(latitude, longitude)
}
