package com.kdy_soft.truck.data.repository.source

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.contract.DELIVERY_TABLE
import com.kdy_soft.truck.data.entity.Delivery
import com.kdy_soft.truck.data.model.Address
import com.kdy_soft.truck.data.model.DeliveryModel
import com.kdy_soft.truck.data.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

private const val TAG = "FirebaseSource"

@Singleton
class DeliveryFirebaseSource @Inject constructor(

) : DeliveryDataSource {
    private val database = Firebase.database


    override fun getDelivery(
        id: String,
        callback: (Delivery) -> Unit
    ) {
        database.getReference(DELIVERY_TABLE)
            .child(id)
            .get()
            .addOnCompleteListener {
                val delivery = if (it.isSuccessful) {
                    it.result.getValue(Delivery::class.java) ?: Delivery()
                } else {
                    Delivery()
                }
                callback.invoke(delivery)
            }
    }

    override fun getDeliveryList(
        current: LatLng,
        callback: (List<Delivery>) -> Unit
    ) {
        //TODO use current value
        database
            .getReference(DELIVERY_TABLE)
            .orderByChild("deadline")
            .startAt(Date().time.toDouble())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val children = it.result.children
                    val list = mutableListOf<Delivery>()

                    CoroutineScope(Dispatchers.Default).launch {
                        children.filter { child ->
                            child.getValue(Delivery::class.java)?.driverId?.isBlank() == true
                        }.toList()
                            .forEach { child ->
                                val delivery = child.getValue(Delivery::class.java)
                                delivery?.let {
                                    list.add(delivery)
                                }
                            }
                        callback.invoke(list)
                    }
                } else {
                    callback.invoke(emptyList())
                }
            }
    }

}