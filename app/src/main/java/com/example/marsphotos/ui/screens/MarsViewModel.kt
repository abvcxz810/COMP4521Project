/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.network.*
import com.example.marsphotos.ui.kmbScreen
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

sealed interface MarsUiState {
    data class Success(
        val routeDataList: List<RouteData>,
    ) : MarsUiState

    object Error : MarsUiState
    object Loading : MarsUiState
}

sealed interface RouteEtaUiState {
    data class Success(
        val etaList: List<EtaDataWithBusStopName>,
        val bound: String,
        val inboundList: List<RouteStation>,
        val outboundList: List<RouteStation>) :
        RouteEtaUiState

    object Error : RouteEtaUiState
    object Loading : RouteEtaUiState

}

sealed interface StopEtaUiState {
    data class Success(
        val bookmarkList: List<StopEtaWithName>) :
        StopEtaUiState

    object Error : StopEtaUiState
    object Loading : StopEtaUiState

}

class MarsViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    var routeEtaUiState: RouteEtaUiState by mutableStateOf(RouteEtaUiState.Loading)
        private set

    var topBarUiState : String by mutableStateOf("Welcome")
        private set

    var stopEtaUiState: StopEtaUiState by mutableStateOf(StopEtaUiState.Loading)
        private set

    private var allStopInformation: StopInformationList =
        StopInformationList("", "", "", emptyList())

    init {
        getAllRouteData()
        getAllStationInformation()
    }


    fun getRouteEtaAndStationId(route: String, bound: String) {
        Log.d("insideFunction2", allStopInformation.toString())
        routeEtaUiState = RouteEtaUiState.Loading
        updateTopBarUIByPassingString("Loading")
        viewModelScope.launch {
            try {
                val listOfCurrentRouteEtaWithStationName: MutableList<EtaDataWithBusStopName> =
                    mutableListOf()
                val currentRouteInboundList: MutableList<StopInformation> = mutableListOf()
                val currentRouteOutboundList: MutableList<StopInformation> = mutableListOf()
                val routeEtaList = MarsApi.retrofitService.getRouteEta(route)
                Log.d("Route", routeEtaList.toString())
                val stationInboundList = MarsApi.retrofitService.getRouteStation(route, "inbound")
                val stationOutBoundList = MarsApi.retrofitService.getRouteStation(route, "outbound")
                stationInboundList.data.forEach { station ->
                    currentRouteInboundList.add(allStopInformation.data.find { it.stop == station.stop }!!)
                }
                stationOutBoundList.data.forEach { station ->
                    currentRouteOutboundList.add(allStopInformation.data.find { it.stop == station.stop }!!)
                }
                routeEtaList.data.forEach { eta ->
                    if (eta.dir == "I" && eta.dir == bound) {
                        listOfCurrentRouteEtaWithStationName.add(EtaDataWithBusStopName(
                            currentRouteInboundList[eta.seq - 1].name_tc,
                            eta))
                    } else if (eta.dir == "O" && eta.dir == bound) {
                        listOfCurrentRouteEtaWithStationName.add(EtaDataWithBusStopName(
                            currentRouteOutboundList[eta.seq - 1].name_tc,
                            eta))
                    }
                }
                Log.d("Tag", routeEtaList.toString()) //print debug message with "Tag" tag

                routeEtaUiState =
                    RouteEtaUiState.Success(listOfCurrentRouteEtaWithStationName, bound, stationInboundList.data, stationOutBoundList.data)
                updateTopBarUIByPassingString((routeEtaUiState as RouteEtaUiState.Success).etaList[0].eta.route)
            } catch (e: IOException) {
                routeEtaUiState = RouteEtaUiState.Error
            } catch (e: Exception) {
                routeEtaUiState = RouteEtaUiState.Error
            }
        }
    }

    fun getAllStationInformation() {
        viewModelScope.launch {
            try {
                allStopInformation = MarsApi.retrofitService.getAllStationInfo()
                Log.d("getAllStationInfo", allStopInformation.toString())
            } catch (e: Exception) {
                marsUiState = MarsUiState.Error
            }
        }
    }

    fun getAllRouteData() {
        viewModelScope.launch {
            val allRouteData = RouteListData("", "", "", emptyList())
            try {
                val allRouteData = MarsApi.retrofitService.getRouteListData()
                Log.d("AllRouteData", allRouteData.data.toString())
                marsUiState =
                    MarsUiState.Success(allRouteData.data.filter { it.service_type == "1" })

            } catch (e: Exception) {
                marsUiState = MarsUiState.Error
            }
            Log.d("AllRouteData", allRouteData.data.toString())
            Log.d("AllRouteDataSize", allRouteData.data.size.toString())

        }
    }

    fun updateTopBarUIByPassingString(message: String){
        topBarUiState = message
    }

    // Function to get the ETA for bookmarked items
    fun getBookmarkEta() {
        stopEtaUiState = StopEtaUiState.Loading
        updateTopBarUIByPassingString("Loading")
        viewModelScope.launch {
            try {
                // For bookmarks
                val stopEtaList: MutableList<StopEtaWithName> = mutableListOf()
                if (markedStops.isNotEmpty()){
                    for (stops in markedStops){
                        val getETA = MarsApi.retrofitService.getStopETA(stops.routeStation.stop, stops.routeStation.route)
                        stopEtaList.add(StopEtaWithName(stops.busStopName,getETA.data))
                    }
                }

                Log.i("test bookmark", stopEtaList.toString())
                stopEtaUiState = StopEtaUiState.Success(stopEtaList)

            } catch (e: IOException) {
                stopEtaUiState = StopEtaUiState.Error
            } catch (e: Exception) {
                stopEtaUiState = StopEtaUiState.Error
            }
        }
    }
}

class StationInfoWithName(
    val busStopName: String,
    val routeStation: RouteStation
)

class StopEtaWithName(
    val busStopName: String,
    val stopEtaList: List<StopEta>
)

class EtaDataWithBusStopName(
    val busStopName: String,
    val eta: Eta,
)

fun extractTime(origin: String): String {
    return origin.substring(11, 19)
}

fun getTimeDiff(eta: String) : String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    val date: Date = format.parse(eta)
    val diff = date.getTime() - Date().getTime()
    val min_diff = diff/1000/60
    if(min_diff <= 0.01){
        return "-"
    }
    else return min_diff.toString()
}