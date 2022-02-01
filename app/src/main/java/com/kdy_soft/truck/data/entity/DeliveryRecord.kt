package com.kdy_soft.truck.data.entity

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.util.*

data class DeliveryRecord(
    val id: String = UUID.randomUUID().toString(),
    val ownerId:String = "",
    val driverId: String = "",
    val deliveryId: String = "",
    val chatRoomId: String = "",
    val departTime: Long = 0,
    val arriveTime: Long = 0,
    val departLocation: MyLatLng = MyLatLng(0.0, 0.0),
    val destLocation: MyLatLng = MyLatLng(0.0, 0.0),
    val distance: Int = 0,
    val cartons: Int = 0,
    val price: Int = 0
)