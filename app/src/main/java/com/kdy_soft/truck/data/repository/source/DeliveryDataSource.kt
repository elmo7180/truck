package com.kdy_soft.truck.data.repository.source

import com.google.android.gms.maps.model.LatLng
import com.kdy_soft.truck.data.entity.Delivery
import com.kdy_soft.truck.data.model.DeliveryModel

interface DeliveryDataSource {
    fun getDelivery(id:String, callback:(Delivery)->Unit)
    fun getDeliveryList(current: LatLng, callback:(List<Delivery>)->Unit)
}