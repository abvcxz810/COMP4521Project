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
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface MarsUiState {
    data class Success(
        val etaList: List<EtaDataWithBusStopName>,
        val routeDataList: List<RouteData>,
    ) : MarsUiState

    object Error : MarsUiState
    object Loading : MarsUiState
}

sealed interface RouteEtaUiState {
    data class Success(val etaList: List<EtaDataWithBusStopName>) : RouteEtaUiState
    object Error : RouteEtaUiState
    object Loading : RouteEtaUiState

}

class MarsViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    var routeEtaUiState: RouteEtaUiState by mutableStateOf(RouteEtaUiState.Loading)
        private set

    private var allStopInformation: StopInformationList =
        StopInformationList("", "", "", emptyList())

    init {
        getRouteEtaAndStationId("91M")
        getAllRouteData()
    }


    fun getRouteEtaAndStationId(route: String) {
        Log.d("insideFunction2", allStopInformation.toString())
        viewModelScope.launch {
            try {
                Log.d("getAllStatInfo", allStopInformation.toString())
                allStopInformation = MarsApi.retrofitService.getAllStationInfo()
                val listOfCurrentRouteEtaWithStationName: MutableList<EtaDataWithBusStopName> =
                    mutableListOf()
                val currentRouteInboundList: MutableList<StopInformation> = mutableListOf()
                val currentRouteOutboundList: MutableList<StopInformation> = mutableListOf()
                val routeEtaList = MarsApi.retrofitService.getRouteEta(route)
                val stationInboundList = MarsApi.retrofitService.getInboundStation(route)
                val stationOutBoundList = MarsApi.retrofitService.getOutboundStation(route)
                //allStopInformation = MarsApi.retrofitService.getAllStationInfo()
                Log.d("getAllStatInfo", allStopInformation.toString())
                stationInboundList.data.forEach { station ->
                    currentRouteInboundList.add(allStopInformation.data.find { it.stop == station.stop }!!)
                }
                stationOutBoundList.data.forEach { station ->
                    currentRouteOutboundList.add(allStopInformation.data.find { it.stop == station.stop }!!)
                }
                routeEtaList.data.forEach { eta ->
                    if (eta.dir == "I") {
                        listOfCurrentRouteEtaWithStationName.add(EtaDataWithBusStopName(
                            currentRouteInboundList[eta.seq - 1].name_tc,
                            eta))
                    } else {
                        listOfCurrentRouteEtaWithStationName.add(EtaDataWithBusStopName(
                            currentRouteOutboundList[eta.seq - 1].name_tc,
                            eta))
                    }
                }
                Log.d("Tag", routeEtaList.toString()) //print debug message with "Tag" tag
                routeEtaUiState = RouteEtaUiState.Success(listOfCurrentRouteEtaWithStationName)
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
                marsUiState = MarsUiState.Success(emptyList(), allRouteData.data)

            } catch (e: Exception) {
                marsUiState = MarsUiState.Error
            }
            Log.d("AllRouteData", allRouteData.data.toString())
            Log.d("AllRouteDataSize", allRouteData.data.size.toString())

        }
    }
}

class EtaDataWithBusStopName(
    val busStopName: String,
    val eta: Eta,
)


