package com.kdy_soft.truck.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.kdy_soft.truck.data.rest.response.LocationByKeywordResponse

data class Address(
    val placeName: String = "",
    val addressName: String = "",
    val latLng: LatLng = LatLng(0.0, 0.0)
) : Parcelable {
    constructor(parcel: Parcel) : this() {
        Address(
            placeName = parcel.readString() ?: "",
            addressName = parcel.readString() ?: "",
            latLng = parcel.readParcelable(LatLng::class.java.classLoader)
                ?: LatLng(0.0, 0.0)
        )
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(placeName)
        parcel.writeString(addressName)
        parcel.writeParcelable(latLng, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Address> {
        override fun createFromParcel(parcel: Parcel): Address {
            return Address(parcel)
        }

        override fun newArray(size: Int): Array<Address?> {
            return arrayOfNulls(size)
        }
        val FAIL = Address()
    }
}