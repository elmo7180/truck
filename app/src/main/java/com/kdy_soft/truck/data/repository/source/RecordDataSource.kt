package com.kdy_soft.truck.data.repository.source

import com.kdy_soft.truck.data.entity.DeliveryRecord
import com.kdy_soft.truck.data.entity.Record
import com.kdy_soft.truck.data.model.RecordModel
import kotlinx.coroutines.flow.Flow
import java.util.*
import java.util.concurrent.Future

interface RecordDataSource {
    fun getRecordList(uid: String, calendar: Calendar, callback: (List<DeliveryRecord>) -> Unit)
    fun getRecord(uid: String, recordId: String, callback: (DeliveryRecord) -> Unit)
}