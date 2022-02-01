package com.kdy_soft.truck.ui.viewModel

import android.os.HandlerThread
import androidx.lifecycle.*
import com.kdy_soft.truck.data.entity.Delivery
import com.kdy_soft.truck.data.model.DeliveryModel
import com.kdy_soft.truck.data.repository.LocationRepository
import com.kdy_soft.truck.util.TruckUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverHelperViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    val delivery = MutableLiveData<Delivery?>(null)

    val deadline: MutableLiveData<String> = MediatorLiveData<String>().apply {
        value = ""
        addSource(delivery) {
            it?.let {
                val result = TruckUtils.getRestTimeString(it.deadline)
                value = result

                viewModelScope.launch(Dispatchers.Default) {
                    var time = System.currentTimeMillis()

                    while (it.deadline > System.currentTimeMillis()) {
                        val current = System.currentTimeMillis()
                        if (current - time > 1000) {
                            time = current
                            postValue(TruckUtils.getRestTimeString(it.deadline))
                        }
                    }
                }
            }
        }

    }

    val departure: LiveData<String> =
        Transformations.switchMap(delivery) {
            val data = MutableLiveData("")
            it?.let { delivery ->
                viewModelScope.launch(Dispatchers.IO) {
                    locationRepository.getAddressByCoordination(
                        delivery.departure.toLatLng(),
                        true
                    ) { address ->
                        val result = if (address.placeName.isNotBlank()) {
                            StringBuilder(address.placeName)
                                .append("\n")
                                .append(address.addressName)
                                .toString()
                        } else {
                            address.addressName
                        }

                        data.postValue(result)
                    }
                }
            }
            data
        }

    val destination: LiveData<String> = Transformations.switchMap(delivery) {
        val data = MutableLiveData("")
        it?.let { delivery ->
            viewModelScope.launch(Dispatchers.IO) {
                locationRepository.getAddressByCoordination(
                    delivery.destination.toLatLng(),
                    true
                ) { address ->
                    val result = if (address.placeName.isNotBlank()) {
                        StringBuilder(address.placeName)
                            .append("\n")
                            .append(address.addressName)
                            .toString()
                    } else {
                        address.addressName
                    }

                    data.postValue(result)
                }
            }
        }
        data
    }

    val restDistance = MutableLiveData("86km")
    val approxTime = MutableLiveData("14:30")
}