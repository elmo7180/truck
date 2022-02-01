package com.kdy_soft.truck.ui.home

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.R
import com.kdy_soft.truck.data.entity.Delivery
import com.kdy_soft.truck.data.entity.VehicleKinds
import com.kdy_soft.truck.data.entity.fromLatLng
import com.kdy_soft.truck.data.model.Address
import com.kdy_soft.truck.data.model.DeliveryModel
import com.kdy_soft.truck.data.repository.DeliveryRepository
import com.kdy_soft.truck.data.repository.LocationRepository
import com.kdy_soft.truck.data.repository.UserRepository
import com.kdy_soft.truck.util.TruckUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

private const val TAG = "DeliveryDetailViewModel"

@HiltViewModel
class DeliveryDetailViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    //TODO 필요한 데이터가 많이 없음 리팩토링 해야할듯 ?
    private val _delivery: MediatorLiveData<DeliveryModel> = MediatorLiveData()
    val delivery: LiveData<DeliveryModel> = _delivery
    private val _editing = MutableLiveData(false)
    private val _loading = MutableLiveData(false)

    var location = LatLng(0.0, 0.0)
    private val uid = Firebase.auth.currentUser?.uid ?: ""
    val loading: LiveData<Boolean> = _loading
    val id = MutableLiveData<String>()
    val editing: LiveData<Boolean> = _editing
    var myCarKinds = VehicleKinds.TOP25

    val destination = MutableLiveData<Address>()
    val departure = MutableLiveData<Address>()
    val deadline = MutableLiveData<Long>(0)
    val cartons = MutableLiveData("1")
    val price = MutableLiveData("0")
    val vehicleKinds = MutableLiveData(0)
    val claims = MutableLiveData("")

    val deliveryPhotoFile: File?
        get() = if (_delivery.value == null) {
            null
        } else {
            deliveryRepository.getDeliveryFile(_delivery.value!!)
        }

    val photoUri: Uri?
        get() = if (deliveryPhotoFile == null) {
            null
        } else {
            deliveryRepository.getPhotoUri(deliveryPhotoFile!!)
        }

    val canDrive = Transformations.map(_delivery) { delivery ->
        Log.d(TAG, "left${vehicleKinds.value},right:${myCarKinds.indexOf()}")
        if (delivery == null) {
            false
        } else {
            (editing.value == false) && (delivery.ownerId != uid)
                    && (delivery.deadline > Date().time)
                    && (vehicleKinds.value == myCarKinds.indexOf())
        }
    }

    fun edit() {
        if (uid == _delivery.value?.ownerId) {
            _editing.value = true
        }
    }

    fun apply(view: View) {
        if (validate(view)) {
            _loading.value = true

            if (delivery.value == null) {
                //TODO notify to deliveryModel is null
                Toast
                    .makeText(view.context.applicationContext, "오류발생", Toast.LENGTH_SHORT)
                    .show()
                view.findNavController().navigateUp()
                return
            }

            delivery.value!!.let {
                getPhotoUrlFromUri { downloadUrl ->
                    val result = Delivery(
                        id = it.id,
                        ownerId = uid,
                        driverId = "",
                        photoUrl = downloadUrl,
                        vehicleKinds = VehicleKinds.values()[vehicleKinds.value!!],
                        claim = claims.value!!,
                        departure = departure.value!!.latLng.fromLatLng(),
                        destination = destination.value!!.latLng.fromLatLng(),
                        cartons = cartons.value!!.toInt(),
                        price = price.value!!.toInt(),
                        deadline = deadline.value!!
                    )

                    Log.d(TAG, "result:$result")

                    viewModelScope.launch {
                        deliveryRepository.setDelivery(result) {
                            if (it.isSuccessful) {
                                Log.d(TAG, "insert is successful")
                            } else {
                                //TODO notify failed to insert delivery
                                Log.d(TAG, "insert is fail")
                            }
                            _loading.postValue(false)
                            view.findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    fun doDelivery(navController: NavController) {
        if (canDrive.value == true) {
            val result = Delivery(
                id = _delivery.value!!.id,
                ownerId = _delivery.value!!.ownerId,
                driverId = uid,
                photoUrl = _delivery.value!!.photoUrl,
                vehicleKinds = _delivery.value!!.vehicleKinds,
                claim = _delivery.value!!.claim,
                departure = _delivery.value!!.departure.latLng.fromLatLng(),
                destination = _delivery.value!!.destination.latLng.fromLatLng(),
                cartons = _delivery.value!!.cartons,
                price = _delivery.value!!.price,
                deadline = _delivery.value!!.deadline
            )

            _loading.value = true
            viewModelScope.launch {
                deliveryRepository
                    .delivery(result, uid) {
                        _loading.postValue(false)
                        navController.navigateUp()
                    }
            }
        }

    }

    private fun validate(view: View): Boolean {
        if (departure.value == null) {
            Toast.makeText(view.context, R.string.error_departure, Toast.LENGTH_SHORT).show()
            return false
        }
        if (destination.value == null) {
            Toast.makeText(view.context, R.string.error_destination, Toast.LENGTH_SHORT).show()
            return false
        }

        val dist = TruckUtils
            .getDistance(departure.value!!.latLng, destination.value!!.latLng)

        if (!validateDeadline(dist)) {
            Toast.makeText(view.context, R.string.error_deadline, Toast.LENGTH_SHORT).show()
            return false
        }

        if ((cartons.value?.toIntOrNull() ?: 0) < 1) {
            Toast.makeText(view.context, R.string.error_cartons, Toast.LENGTH_SHORT).show()
            return false
        }

        if ((price.value?.toIntOrNull() ?: 0) < 50_000) {
            Toast.makeText(view.context, R.string.error_price, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateDeadline(dist: Int): Boolean {
        return true
    }

    private fun getPhotoUrlFromUri(callback: (String) -> Unit) {
        deliveryRepository.uploadPhoto(deliveryPhotoFile, callback)
    }

    private fun onGetDelivery(delivery: DeliveryModel) {
        delivery.let {
            //TODO departure, destination
            Log.d(TAG, "deliveryModelDeadline: ${it.deadline}")
            departure.postValue(it.departure)
            destination.postValue(it.destination)
            vehicleKinds.postValue(it.vehicleKinds.indexOf())
            claims.postValue(it.claim)
            deadline.postValue(it.deadline)
            cartons.postValue(it.cartons.toString())
            price.postValue(it.price.toString())
        }
    }

    private fun addSources() {
        _delivery.apply {
            addSource(id) { deliveryId ->
                if (deliveryId?.isNotBlank() == true) {
                    viewModelScope.launch {
                        _loading.value = true
                        deliveryRepository.getDelivery(deliveryId) { delivery ->
                            //TODO delivery convert To deliveryModel
                            convertDeliveryModel(
                                delivery,
                                currentLocation = location
                            ) { model ->
                                postValue(model)
                                onGetDelivery(model)
                                _loading.postValue(false)
                            }

                        }
                    }
                } else {
                    value = DeliveryModel(
                        id = UUID.randomUUID().toString(),
                        ownerId = uid,
                        cartons = 1
                    )
                    onGetDelivery(value!!)
                    _editing.value = true
                }
            }

            addSource(deadline) { time ->
                Log.d(TAG, "onDeadlineChanged:$time")
                time?.let {
                    value = value?.copy(deadline = it)
                }
            }
        }
    }

    init {
        addSources()
    }

    private fun deletePhoto() {
        deliveryPhotoFile?.let {
            if (it.exists()) {
                viewModelScope.launch {
                    it.delete()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        deletePhoto()
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