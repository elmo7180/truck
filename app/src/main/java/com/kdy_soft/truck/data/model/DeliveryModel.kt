package com.kdy_soft.truck.data.model

import com.google.android.gms.maps.model.LatLng
import com.kdy_soft.truck.data.entity.Delivery
import com.kdy_soft.truck.data.entity.VehicleKinds
import com.kdy_soft.truck.util.TruckUtils
import java.util.*

data class DeliveryModel(
    val id: String = "",
    val ownerId: String = "",
    val photoUrl: String = "",
    val vehicleKinds: VehicleKinds = VehicleKinds.TOP1,
    val claim: String = "",
    val departure: Address = Address(),
    val destination: Address = Address(),
    val distance: Int = 0,
    val distanceFromMe: Int = 0,
    val approxTime: Int = 0,
    val cartons: Int = 0,
    val deadline: Long = TruckUtils.tomorrow(),
    val price: Int = 0,
    val priceString: String = TruckUtils.priceString(price)
) {
    private val date = Date(deadline)

    val deadlineString: String
        get() = TruckUtils.getDateString(date)

    val deadlineTimeString: String
        get() = TruckUtils.getTimeString(date)

    companion object {
        fun fromDelivery(
            delivery: Delivery,
            departure: Address = Address.FAIL,
            destination: Address = Address.FAIL,
            distance: Int = 0,
            distanceFromMe: Int = 0,
            approxTime: Int = 0
        ) = DeliveryModel(
            id = delivery.id,
            ownerId = delivery.ownerId,
            photoUrl = delivery.photoUrl,
            vehicleKinds = delivery.vehicleKinds,
            claim = delivery.claim,
            departure = departure,
            destination = destination,
            cartons = delivery.cartons,
            deadline = delivery.deadline,
            price = delivery.price,
            approxTime = approxTime,
            distance = distance,
            distanceFromMe = distanceFromMe
        )
    }
}

