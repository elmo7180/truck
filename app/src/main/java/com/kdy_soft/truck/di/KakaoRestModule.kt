package com.kdy_soft.truck.di

import com.kdy_soft.truck.data.rest.service.DirectionService
import com.kdy_soft.truck.data.rest.service.LocalService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient

private const val REST_API_KEY = "b93c8414b21af6a9b27d821930af31c3"

@Module
@InstallIn(SingletonComponent::class)
class KakaoRestModule {
    @Provides
    fun localService(): LocalService {
        return LocalService.create(client)
    }

    @Provides
    fun directionService(): DirectionService {
        return DirectionService.create(client)
    }

    companion object {
        private val interceptor = Interceptor {
            val request = it.request()
                .newBuilder()
                .addHeader("Connection", "Keep-Alive")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Authorization", "KakaoAK $REST_API_KEY")
                .build()
            it.proceed(request)
        }

        private val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }
}
