package com.kdy_soft.truck.data.rest.response

import com.google.gson.annotations.SerializedName

data class DirectionResponse(
    @SerializedName("trans_id") val transId: String = "",
    val routes: List<Route> = emptyList()
) {
    companion object {
        data class Route(
            @SerializedName("result_code") val resultCode: Int = 0,
            @SerializedName("result_message") val resultMessage: String = "",
            val summary: Summary = Summary(),
            val sections: List<Section> = emptyList()
        )

        data class Summary(
            val origin: Loc = Loc(),
            val destination: Loc = Loc(),
            val waypoints: List<Loc> = emptyList(),
            val distance: Int = 0, //meter
            val duration: Int = 0, //seconds
        )

        data class Loc(
            val name: String = "",
            val x: Double = 0.0,
            val y: Double = 0.0
        )

        data class Section(
            val distance: Int = 0,
            val duration: Int = 0,
            val roads: List<Road> = emptyList()
        )

        data class Road(
            val name: String = "",
            val distance: Int = 0,
            val duration: Int = 0,
            @SerializedName("traffic_speed") val trafficSpeed: Int = 0,
            @SerializedName("traffic_state") val trafficState: Int = 0,
            val vertexes: List<Double> = emptyList()
        )
    }
}