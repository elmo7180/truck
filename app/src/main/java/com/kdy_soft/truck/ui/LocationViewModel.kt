package com.kdy_soft.truck.ui

import android.location.Location
import android.os.HandlerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.kdy_soft.truck.data.model.Addresses
import com.kdy_soft.truck.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    private var boundaryDistance = 20_000.0 // 20km
    private val _addresses = MutableLiveData<Addresses>()

    val location = MutableLiveData<Location>()
    val position = MutableLiveData<LatLng?>()
    val addresses: LiveData<Addresses> = _addresses

    val locationPermissionEnabled = locationRepository.checkSelfPermission()

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            location.value = result.lastLocation
        }
    }
    private val thread = HandlerThread("LocationThread")

    init {
        requestLastLocation()
        locationRepository.requestUpdates(
            locationCallback = locationCallback,
            looper = thread.looper
        )
    }

    override fun onCleared() {
        super.onCleared()
        locationRepository.removeRequestUpdates(locationCallback)
        thread.quit()
    }

    private fun requestLastLocation() {
        locationRepository.requestLastLocation { result ->
            position.postValue(result)
        }
    }

    fun searchAddressByKeyword(keyword: String) {
        locationRepository.getAddressByKeyword(keyword) {
            _addresses.postValue(it)
        }
    }

    fun outOfBoundary(latLng: LatLng): Boolean {
        val currentTarget = position.value
        val dist = SphericalUtil.computeDistanceBetween(currentTarget!!, latLng)
        return dist > boundaryDistance
    }
}