package com.kdy_soft.truck.data.rest.service

import com.kdy_soft.truck.data.rest.response.LocByCoordResponse
import com.kdy_soft.truck.data.rest.response.LocationByKeywordResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LocalService {
    @GET("search/keyword.json")
    fun searchAddressByKeyword(@Query("query") query: String): Call<LocationByKeywordResponse>

    @GET("geo/coord2address.json")
    fun searchAddressByLatLng(
        @Query("x") longitude: String,
        @Query("y") latitude: String
    ): Call<LocByCoordResponse>

    companion object {
        /*apis-navi.kakaomobility.com*/
        private const val BASE_URL = "https://dapi.kakao.com/v2/local/"

        fun create(httpClient: OkHttpClient? = null): LocalService {
            val client = httpClient ?: OkHttpClient.Builder().build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(client)
                .build()

            return retrofit.create(LocalService::class.java)
        }
    }

}