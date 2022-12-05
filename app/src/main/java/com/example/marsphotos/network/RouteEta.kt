package com.example.marsphotos.network

import kotlinx.serialization.Serializable

//@Serializable
//data class MarsPhoto(
//    val id: String,
//    @SerialName(value = "img_src")
//    val img_src:String
//)

@Serializable
data class RouteEta(
    val type: String = "", //The corresponding API that returns the data
    val version: String = "",//The version number of the JSON returned.
    val generated_timestamp: String = "",//The timestamp of the initial generated time of the response before it is cached.
    val data: List<Eta> = emptyList(),
)

@Serializable
data class Eta(
    val co: String = "KMB",//The bus company
    val route: String = "", //The bus route number of the requested bus company
    val dir: String = "",//The direction of the bus route
    val service_type: Int = 0,//The service type of the bus route.
    val seq: Int = 0, //The stop sequence number of a bus route
    val dest_tc: String = "",//The destination of a bus route in Traditional Chinese
    val dest_sc: String = "",//The destination of a bus route in Simplified Chinese.
    val dest_en: String = "",//The destination of a bus route in English
    val eta_seq: Int = 0,//The sequence number of ETA
    val eta: String? = "-",//The timestamp of the next ETA
    val rmk_tc: String = "",//The remark of an ETA in Traditional Chinese
    val rmk_sc: String = "",//The remark of an ETA in Simplified Chinese.
    val rmk_en: String = "",//The remark of an ETA in English
    val data_timestamp: String = "",//The timestamp of the data when it was initially
)

@Serializable
data class RouteStationList(
    val type: String = "", //The corresponding API that returns the data
    val version: String = "",//The version number of the JSON returned.
    val generated_timestamp: String = "",//The timestamp of the initial generated time of the response before it is cached.
    val data: List<RouteStation> = emptyList(),
)

@Serializable
data class RouteStation(
    val route: String = "",
    val bound: String = "",
    val service_type: String = "",
    val seq: String = "",
    val stop: String = "",
)

@Serializable
data class StopInformationList(
    val type: String,
    val version: String,
    val generated_timestamp: String,
    val data: List<StopInformation>,
)

@Serializable
data class StopInformation(
    val stop: String = "",
    val name_en: String = "",
    val name_tc: String = "",
    val name_sc: String = "",
    val lat: String = "",
    val long: String = "",
)

@Serializable
data class RouteListData(
    val type: String = "", //The corresponding API that returns the data
    val version: String = "",//The version number of the JSON returned.
    val generated_timestamp: String = "",//The timestamp of the initial generated time of the response before it is cached.
    val data : List<RouteData>
)

@Serializable
data class RouteData(
    val route: String = "",
    val bound: String = "",
    val service_type: String = "",
    val orig_en: String = "",
    val orig_tc: String = "",
    val orig_sc: String = "",
    val dest_en: String = "",
    val dest_tc: String = "",
    val dest_sc: String = "",
)
