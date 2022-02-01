package com.kdy_soft.truck.data.rest.service

import com.google.android.gms.maps.model.LatLng
import com.kdy_soft.truck.data.rest.response.DirectionResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionService {
    @GET("v1/directions")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") dest: String
    ): Call<DirectionResponse>

    companion object {
        private const val BASE_URL = "https://apis-navi.kakaomobility.com/"

        fun convertString(latLng: LatLng) = "${latLng.longitude},${latLng.latitude}"

        fun create(httpClient: OkHttpClient? = null): DirectionService {
            val client = httpClient ?: OkHttpClient.Builder().build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(client)
                .build()

            return retrofit.create(DirectionService::class.java)
        }
    }

}