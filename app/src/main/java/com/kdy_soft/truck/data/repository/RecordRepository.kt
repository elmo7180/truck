package com.kdy_soft.truck.data.repository

import android.os.Build
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.entity.DeliveryRecord
import com.kdy_soft.truck.data.entity.Record
import com.kdy_soft.truck.data.model.RecordModel
import com.kdy_soft.truck.data.repository.source.RecordDataSource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepository @Inject constructor(
    private val dataSource: RecordDataSource
) {
    fun getRecordList(
        uid: String,
        calendar: Calendar,
        callback: (List<DeliveryRecord>) -> Unit
    ) {
        dataSource.getRecordList(uid, calendar, callback)
    }

    fun getRecord(uid: String, recordId: String, callback: (DeliveryRecord) -> Unit) {
        dataSource.getRecord(uid, recordId, callback)
    }

}