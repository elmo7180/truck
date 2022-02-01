package com.kdy_soft.truck.ui.record

import android.annotation.SuppressLint
import androidx.lifecycle.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.entity.DeliveryRecord
import com.kdy_soft.truck.data.model.Address
import com.kdy_soft.truck.data.model.RecordModel
import com.kdy_soft.truck.data.repository.LocationRepository
import com.kdy_soft.truck.data.repository.RecordRepository
import com.kdy_soft.truck.util.TruckUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.stream.Collectors
import javax.inject.Inject

@HiltViewModel
class DeliveryRecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val uid: String = Firebase.auth.uid ?: ""
    private val selectedMonth = MutableLiveData(TruckUtils.getCurrentMonth(Date()))

    @SuppressLint("NewApi")
    val recordList = Transformations.switchMap(selectedMonth) { calendar ->
        val data = MutableLiveData<List<RecordModel>>(emptyList())
        recordRepository.getRecordList(uid, calendar) { records ->
            viewModelScope.launch(Dispatchers.IO) {
                val list = records.parallelStream()
                    .map { convertRecordAsync(it) }
                    .collect(Collectors.toList())

                data.postValue(list)
            }
        }
        data
    }

    fun selectMonth(month: Calendar) {
        selectedMonth.value = month
    }

    private fun convertRecordAsync(record: DeliveryRecord): RecordModel {
        val destAndDepartCount = 2
        val countDownLatch = CountDownLatch(destAndDepartCount)

        var departure = Address.FAIL
        var destination = Address.FAIL

        locationRepository.getAddressByCoordination(
            latLng = record.departLocation.toLatLng()
        ) { result ->
            departure = result
            countDownLatch.countDown()
        }

        locationRepository.getAddressByCoordination(
            latLng = record.destLocation.toLatLng()
        ) { result ->
            destination = result
            countDownLatch.countDown()
        }

        countDownLatch.await()

        return RecordModel(
            id = record.id,
            startDate = Date(record.departTime),
            completeDate = Date(record.arriveTime),
            distance = record.distance,
            price = record.price,
            deliveryId = record.deliveryId,
            destination = destination,
            departure = departure
        )
    }
}