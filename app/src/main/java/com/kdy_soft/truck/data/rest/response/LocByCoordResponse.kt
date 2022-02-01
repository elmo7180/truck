package com.kdy_soft.truck.data.rest.response

import com.google.gson.annotations.SerializedName

data class LocByCoordResponse(
    val documents: List<TotalDocuments>
) {
    companion object {
        data class TotalDocuments(
            val address: Address,
            @SerializedName("road_address") val roadAddress: RoadAddress?
        ) {
            data class Address(
                @SerializedName("address_name") val addressName: String
            )

            data class RoadAddress(
                @SerializedName("address_name") val addressName: String,
                @SerializedName("building_name") val buildingName: String
            )
        }
    }
}
