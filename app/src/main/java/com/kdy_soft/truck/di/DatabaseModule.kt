package com.kdy_soft.truck.di

import com.kdy_soft.truck.data.repository.source.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {
    @Binds
    abstract fun chatDataSource(
        chatFireSource: ChatFireSource
    ): ChatDataSource

    @Binds
    abstract fun deliveryDataSource(
        dataSourceDelivery: DeliveryFirebaseSource
    ): DeliveryDataSource

    @Binds
    abstract fun recordDataSource(
        dataSource: RecordFireSource
    ): RecordDataSource
}