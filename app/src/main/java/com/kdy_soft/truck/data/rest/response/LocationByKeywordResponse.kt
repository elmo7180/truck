package com.kdy_soft.truck.data.rest.response

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field

data class LocationByKeywordResponse(
    val meta: Meta = Meta(),
    val documents: List<Document> = emptyList()
) {
    data class Meta(
        @SerializedName("total_count") val totalCount: Int = 0,
        @SerializedName("pageable_count") val pageableCount: Int = 0,
        @SerializedName("is_end") val isEnd: Boolean = false
    )

    data class Document(
        @SerializedName("place_name") val placeName: String = "",
        val distance: String = "0",
        @SerializedName("place_url") val placeUrl: String = "",
        @SerializedName("category_name") val categoryName: String = "",
        @SerializedName("address_name") val addressName: String = "",
        @SerializedName("road_address_name") val roadAddressName: String = "",
        val id: String = "",
        val phone: String = "",
        val x: String = "",
        val y: String = ""
    )
}
