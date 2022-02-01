package com.kdy_soft.truck.data.repository.source

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.contract.DELIVERY_RECORD_TABLE
import com.kdy_soft.truck.data.contract.RECORD_TABLE
import com.kdy_soft.truck.data.entity.DeliveryRecord
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RecordFireSource"

@Singleton
class RecordFireSource @Inject constructor() : RecordDataSource {
    private val database = Firebase.database

    override fun getRecordList(
        uid: String,
        calendar: Calendar,
        callback: (List<DeliveryRecord>) -> Unit
    ) {
        database.getReference(DELIVERY_RECORD_TABLE)
            .orderByChild("driverId")
            .equalTo(uid)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val list = mutableListOf<DeliveryRecord>()

                    Log.d(TAG, "success")

                    val result = it.result
                    val children = result.children

                    Log.d(TAG, "success:$children")
                    children.forEach { child ->
                        val deliveryRecord =
                            child.getValue(DeliveryRecord::class.java) ?: return@forEach
                        list.add(deliveryRecord)
                        Log.d(TAG, "${deliveryRecord}}")
                    }
                    callback.invoke(list)
                } else {
                    Log.d(TAG,"Fail")
                    callback.invoke(emptyList())
                }
            }

    }

    /*
    * if Primary object is returned, that is FAIL to load data
    * */
    override fun getRecord(uid: String, recordId: String, callback: (DeliveryRecord) -> Unit) {
        database.getReference(DELIVERY_RECORD_TABLE)
            .child(recordId)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val record = it.result.getValue(DeliveryRecord::class.java)
                    callback.invoke(record ?: DeliveryRecord())
                } else {
                    callback.invoke(DeliveryRecord())
                }
            }

    }
}