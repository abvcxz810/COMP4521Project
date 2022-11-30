package com.example.marsphotos.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path


private const val BASE_URL = "https://data.etabus.gov.hk"
private const val BASE_URL2 = "https://android-kotlin-fun-mars-server.appspot.com"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
    .baseUrl(BASE_URL)
    .build()

interface MarsApiService{
    //@GET("photos")
    @GET("v1/transport/kmb/eta/6D582F3C571E5E9C/91M/1")
    suspend fun getPhotos() : MarsPhoto

    @GET("v1/transport/kmb/route-eta/{route}/1")
    suspend fun getRouteEta(@Path("route") route:String ) : MarsPhoto

    @GET("v1/transport/kmb/route-stop/{route}/inbound/1")
    suspend fun getInboundStation(@Path("route") route: String) : RouteStationList

    @GET("v1/transport/kmb/route-stop/{route}/outbound/1")
    suspend fun getOutboundStation(@Path("route") route :String) : RouteStationList

    @GET("v1/transport/kmb/stop")
    suspend fun getAllStationInfo() : StopInformationList

}

object MarsApi{
    val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }
}

