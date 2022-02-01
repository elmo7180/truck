package com.kdy_soft.truck.data.model

data class Addresses(
    val metaData: MetaData = MetaData(),
    val addresses: List<Address> = emptyList()
) {
    data class MetaData(
        val page: Int = 1,
        val totalCount: Int = 0
    )
}

