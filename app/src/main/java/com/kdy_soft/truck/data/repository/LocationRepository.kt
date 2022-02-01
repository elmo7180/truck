package com.kdy_soft.truck.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationRequestCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.kdy_soft.truck.data.model.Address
import com.kdy_soft.truck.data.model.Addresses
import com.kdy_soft.truck.data.rest.response.DirectionResponse
import com.kdy_soft.truck.data.rest.response.LocByCoordResponse
import com.kdy_soft.truck.data.rest.service.LocalService
import com.kdy_soft.truck.data.rest.response.LocationByKeywordResponse
import com.kdy_soft.truck.data.rest.service.DirectionService
import com.kdy_soft.truck.util.TruckUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LocationRepository"

@Singleton
class LocationRepository @Inject constructor(
    private val localService: LocalService,
    private val directionService: DirectionService,
    @ApplicationContext private val context: Context
) {
    private val flp: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val checkPermission = checkSelfPermission()

    fun getAddressByKeyword(
        query: String,
        isRoadAddress: Boolean = false,
        page: Int = 1,
        callback: (Addresses) -> Unit
    ) {
        localService.searchAddressByKeyword(query).enqueue(
            object : Callback<LocationByKeywordResponse> {
                override fun onResponse(
                    call: Call<LocationByKeywordResponse>,
                    response: Response<LocationByKeywordResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "${response.body()}")
                        val body = response.body() ?: return
                        val addresses = mutableListOf<Address>()

                        body.documents.forEach {
                            val name = if (isRoadAddress) it.roadAddressName else it.addressName
                            val address = Address(
                                placeName = it.placeName,
                                addressName = name,
                                latLng = LatLng(
                                    it.y.toDoubleOrNull() ?: 0.0,
                                    it.x.toDoubleOrNull() ?: 0.0
                                )
                            )
                            addresses.add(address)
                        }
                        val metaData = Addresses.MetaData(
                            totalCount = body.meta.totalCount, page = page
                        )
                        callback.invoke(Addresses(metaData, addresses))
                    }
                }

                override fun onFailure(call: Call<LocationByKeywordResponse>, t: Throwable) {
                    Log.e(TAG, t.localizedMessage, t)
                }

            }
        )
    }

    fun getAddressByCoordination(
        latLng: LatLng,
        isRoadAddress: Boolean = true,
        callback: (Address) -> Unit
    ) {
        val latitude = latLng.latitude
        val longitude = latLng.longitude
        val call = localService.searchAddressByLatLng(
            longitude.toString(),
            latitude.toString()
        )

        call.enqueue(object : Callback<LocByCoordResponse> {
            override fun onResponse(
                call: Call<LocByCoordResponse>,
                response: Response<LocByCoordResponse>
            ) {
                val body = response.body()
                val doc = body?.documents
                val addresses = doc?.getOrNull(0)

                if (addresses == null) {
                    callback.invoke(Address.FAIL)
                } else {
                    val result = if (isRoadAddress && addresses.roadAddress != null) {
                        Address(
                            addresses.roadAddress.buildingName,
                            addresses.roadAddress.addressName,
                            latLng
                        )
                    } else {
                        Address(
                            "",
                            addresses.address.addressName,
                            latLng
                        )
                    }
                    callback.invoke(result)
                }
            }

            override fun onFailure(call: Call<LocByCoordResponse>, t: Throwable) {
                Log.e(TAG, "response.body:${t.localizedMessage}", t)
                callback.invoke(Address.FAIL)
            }

        })
    }

    fun getNavigation(
        origin: LatLng,
        destination: LatLng,
        callback: (DirectionResponse?) -> Unit = {}
    ) {
        val originString = DirectionService.convertString(origin)
        val destString = DirectionService.convertString(destination)
        val call = directionService.getDirections(originString, destString)

        call.enqueue(object : Callback<DirectionResponse> {
            override fun onResponse(
                call: Call<DirectionResponse>,
                response: Response<DirectionResponse>
            ) {
                Log.d(TAG, "onResponse:${response.code()}")
                val body = response.body()
                Log.d(TAG, "body: $body")
                callback.invoke(body)
            }

            override fun onFailure(call: Call<DirectionResponse>, t: Throwable) {
                Log.e(TAG, "onFailure", t)
            }

        })
    }

    @SuppressLint("MissingPermission")
    fun requestLastLocation(callback: (LatLng) -> Unit) {
        if (checkPermission) {
            flp.lastLocation
                .addOnFailureListener {
                    callback.invoke(TruckUtils.Locations.SEOUL)
                }
                .addOnSuccessListener { lastLocation: Location? ->
                    if (lastLocation == null) {
                        callback.invoke(TruckUtils.Locations.SEOUL)
                    } else {
                        val result = LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        )
                        callback.invoke(result)
                    }

                }
        } else {
            callback.invoke(TruckUtils.Locations.SEOUL)
        }
    }

    fun checkSelfPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun requestUpdates(
        locationRequest: LocationRequest? = null,
        locationCallback: LocationCallback,
        looper: Looper? = null
    ) {
        if (checkPermission) {
            val request = locationRequest ?: LocationRequest
                .create()
                .apply {
                    fastestInterval = 5000
                    interval = 10000
                }

            flp.requestLocationUpdates(
                request,
                locationCallback,
                looper ?: Looper.getMainLooper()
            )
        }
    }

    fun removeRequestUpdates(locationCallback: LocationCallback) {
        flp.removeLocationUpdates(locationCallback)
    }
}