package com.kdy_soft.truck.ui.home

import com.kdy_soft.truck.data.model.DeliveryModel
import com.kdy_soft.truck.util.TruckUtils
import java.util.*

class DeliveryItemViewModel(delivery: DeliveryModel) {
    val departure: String =
        if (delivery.departure.placeName.isBlank()) {
            delivery.departure.addressName
        } else {
            delivery.departure.placeName
        }
    val destination: String =
        if (delivery.destination.placeName.isBlank()) {
            delivery.destination.addressName
        } else {
            delivery.destination.placeName
        }
    val productImageUrl: String = delivery.photoUrl

    //TODO 초가 바뀔때 마다 값을 바꿔줌
    var deadline: String = TruckUtils.timeString(
        ((delivery.deadline - System.currentTimeMillis()) / 1000).toInt(),
        forRestTime = true,
        forDate = true
    ) + " 남음"
    val approxTime: String = TruckUtils.timeString(delivery.approxTime)
    val distance: String = TruckUtils.distanceString(delivery.distance)
    val cartons: String = "${delivery.cartons}Cts"
    val distanceFromMe: String = TruckUtils.distanceString(delivery.distanceFromMe)
    val price = delivery.priceString
}