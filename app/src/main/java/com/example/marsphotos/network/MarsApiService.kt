package com.example.marsphotos.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path


private const val BASE_URL = "https://data.etabus.gov.hk"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
    .baseUrl(BASE_URL)
    .build()

interface MarsApiService{
    @GET("v1/transport/kmb/eta/6D582F3C571E5E9C/91M/1")
    suspend fun getPhotos() : RouteEta

    @GET("v1/transport/kmb/route-eta/{route}/1")
    suspend fun getRouteEta(@Path("route") route:String ) : RouteEta

    @GET("v1/transport/kmb/route-stop/{route}/{bound}/1")
    suspend fun getRouteStation(@Path("route") route: String, @Path("bound") bound :String) : RouteStationList

    @GET("v1/transport/kmb/stop")
    suspend fun getAllStationInfo() : StopInformationList

    @GET("v1/transport/kmb/route/")
    suspend fun getRouteListData() : RouteListData

}

object MarsApi{
    val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }
}

