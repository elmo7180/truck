package com.kdy_soft.truck.ui.home

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.kdy_soft.truck.data.entity.Delivery
import com.kdy_soft.truck.data.entity.DeliveryRecord
import com.kdy_soft.truck.data.model.Address
import com.kdy_soft.truck.data.model.DeliveryModel
import com.kdy_soft.truck.data.repository.DeliveryRepository
import com.kdy_soft.truck.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

private const val TAG = "DeliveryListviewModel"

@HiltViewModel
class DeliveryListViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val _loadDelivery = MutableLiveData(false)

    val drivingId = MutableLiveData("")
    private val _drivingDelivery = MutableLiveData<Delivery?>()
    val drivingDelivery: LiveData<Delivery?> = _drivingDelivery

    private val _restTime = MutableLiveData(0)
    private val _restDistance = MutableLiveData(0)
    val restTime: LiveData<Int> = _restTime
    val restDistance: LiveData<Int> = _restDistance

    private val _completeRecord = MutableLiveData<DeliveryRecord?>(null)
    val completedRecord: LiveData<DeliveryRecord?> = _completeRecord
    val loadDelivery: LiveData<Boolean> = _loadDelivery
    val currentLocation = MutableLiveData<LatLng?>(null)
    val location = MutableLiveData<LatLng?>(null)
    val velocity = MutableLiveData(0)

    private val _paths = MediatorLiveData<List<LatLng>>()
        .apply {
            addSource(drivingId) { id ->
                if (id.isNotBlank()) {
                    viewModelScope.launch {
                        deliveryRepository.getDelivery(id) { delivery ->
                            _drivingDelivery.postValue(delivery)

                            currentLocation.value?.let { from ->
                                val to = delivery.destination.toLatLng()
                                addPath(from, to)
                            }
                        }
                    }
                }
            }

            addSource(currentLocation) { loc ->
                val delivery = drivingDelivery.value
                if (loc != null && delivery != null) {
                    val to = delivery.destination.toLatLng()
                    addPath(loc, to)
                }
            }

        }

    val paths: LiveData<List<LatLng>> = _paths

    val deliveryList = MediatorLiveData<List<DeliveryModel>>().apply {
        addSource(drivingId) {
            if (it.isNotBlank()) {
                postValue(emptyList())
            } else {
                loadByLocation(location.value, this)
            }
        }

        addSource(location) { loc ->
            if (drivingId.value?.isNotBlank() != true) {
                Log.d(TAG, "Transformations.loc->$loc")
                loadByLocation(loc, this)
            }
        }
    }

    fun addPath(from: LatLng, to: LatLng) {
        locationRepository.getNavigation(from, to) {
            it?.let {
                _restDistance.postValue(
                    it.routes.getOrNull(0)?.summary?.distance ?: 0
                )
                _restTime.postValue(
                    it.routes.getOrNull(0)?.summary?.duration ?: 0
                )

                viewModelScope.launch(Dispatchers.Default) {
                    val result = mutableListOf<LatLng>()
                    val sections = it.routes.getOrNull(0)?.sections
                    sections?.let {
                        sections.getOrNull(0)?.roads?.forEach { road ->
                            val repeat = road.vertexes.size / 2

                            for (index in 0 until repeat) {
                                val x = road.vertexes[index * 2]
                                val y = road.vertexes[(index * 2 + 1)]
                                result.add(LatLng(y, x))
                            }
                        }
                    }
                    _paths.postValue(result)
                }
            }
        }
    }

    private fun loadByLocation(loc: LatLng?, data: MutableLiveData<List<DeliveryModel>>) {
        if (loc != null) {
            Log.d(TAG, "loc is not null")
            _loadDelivery.value = true
            viewModelScope.launch {
                deliveryRepository.getDeliveryList(loc) { deliveryList ->
                    val latch = CountDownLatch(deliveryList.size)
                    val list = mutableListOf<DeliveryModel>()
                    Log.d(TAG, "deliveryList' size:${deliveryList.size}")

                    deliveryList.forEach { delivery ->
                        convertDeliveryModel(delivery, loc) { model ->
                            list.add(model)
                            latch.countDown()
                            data.postValue(list)
                        }
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        latch.await()
                        Log.d(TAG, "list:$list")
                        data.postValue(list)
                        _loadDelivery.postValue(false)
                    }
                }
            }
        }
    }

    val focusPosition = MutableLiveData(LatLng(0.0, 0.0))

    fun completeDelivery(
        onComplete: (DeliveryRecord?) -> Unit = {}
    ) {
        val delivery = drivingDelivery.value ?: return
        deliveryRepository.deliveryComplete(
            delivery,
            delivery.driverId
        ) { record ->
            onComplete.invoke(record)
        }

    }

    private fun convertDeliveryModel(
        delivery: Delivery,
        currentLocation: LatLng,
        callback: (DeliveryModel) -> Unit
    ) {
        val localLatch = CountDownLatch(4)
        var departure = Address.FAIL
        var destination = Address.FAIL
        var distance = 0
        var distanceFromMe = 0
        var seconds = 0

        locationRepository.getNavigation(
            delivery.departure.toLatLng(), delivery.destination.toLatLng()
        ) {
            it?.let { direction ->
                distance = direction.routes.getOrNull(0)?.summary?.distance ?: 0
                seconds = direction.routes.getOrNull(0)?.summary?.duration ?: 0
            }
            localLatch.countDown()
        }

        locationRepository.getNavigation(currentLocation, delivery.departure.toLatLng()) {
            it?.let { dir ->
                distanceFromMe = dir.routes.getOrNull(0)?.summary?.distance ?: 0
            }
            localLatch.countDown()
        }

        locationRepository.getAddressByCoordination(
            delivery.departure.toLatLng(),
            isRoadAddress = true
        ) {
            Log.d(TAG, it.addressName)
            departure = it
            localLatch.countDown()
        }

        locationRepository.getAddressByCoordination(
            delivery.destination.toLatLng(),
            isRoadAddress = true
        ) {
            Log.d(TAG, it.placeName)
            destination = it
            localLatch.countDown()
        }

        CoroutineScope(Dispatchers.IO).launch {
            localLatch.await()
            callback.invoke(
                DeliveryModel.fromDelivery(
                    delivery = delivery,
                    departure = departure,
                    destination = destination,
                    distance = distance,
                    distanceFromMe = distanceFromMe,
                    approxTime = seconds
                )
            )
        }
    }
}