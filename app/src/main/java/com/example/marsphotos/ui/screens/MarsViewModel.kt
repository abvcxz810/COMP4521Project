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
import com.example.marsphotos.network.Eta
import com.example.marsphotos.network.MarsApi
import com.example.marsphotos.network.RouteStationList
import com.example.marsphotos.network.StopInformation
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface MarsUiState {
    data class Success(val photos: String, val etaList: List<EtaDataWithBusStopName>) :
        MarsUiState

    object Error : MarsUiState
    object Loading : MarsUiState
}

class MarsViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    /**
     * Call getMarsPhotos() on init so we can display status immediately.
     */
    init {
//        getMarsPhotos()
        getRouteEtaAndStationId()
    }

    /**
     * Gets Mars photos information from the Mars API
     */
//    fun getMarsPhotos() {
//        val Route = "74X"
//        viewModelScope.launch {
//            marsUiState = try {
//                //val listResult = MarsApi.retrofitService.getPhotos()
//                val listResult = MarsApi.retrofitService.getRouteEta(Route)
//                val stationlistResult = MarsApi.retrofitService.getInboundStation(Route)
//                MarsUiState.Success("Success: ${listResult} Mars photo retrieved")
//            } catch (e: Exception) {
//                MarsUiState.Error
//            }
//        }
//    }

    fun getRouteEtaAndStationId() {
        viewModelScope.launch {
            try {
                val route = "74X"
                val listOfCurrentRouteEtaWithStationName: MutableList<EtaDataWithBusStopName> = mutableListOf()
                val currentRouteInboundList: MutableList<StopInformation> = mutableListOf()
                val currentRouteOutboundList: MutableList<StopInformation> = mutableListOf()
                val routeEtaList = MarsApi.retrofitService.getRouteEta(route)
                val stationInboundList = MarsApi.retrofitService.getInboundStation(route)
                val stationOutBoundList = MarsApi.retrofitService.getOutboundStation(route)
                val allStopInformation = MarsApi.retrofitService.getAllStationInfo()
                stationInboundList.data.forEach { station ->
                    currentRouteInboundList.add(allStopInformation.data.find { it.stop == station.stop }!!)
                }
                stationOutBoundList.data.forEach { station ->
                    currentRouteOutboundList.add(allStopInformation.data.find { it.stop == station.stop }!!)
                }
                routeEtaList.data.forEach {
                    eta ->
                    if(eta.dir == "I"){
                        listOfCurrentRouteEtaWithStationName.add(EtaDataWithBusStopName(currentRouteInboundList[eta.seq-1].name_tc,eta))
                    }
                    else{
                        listOfCurrentRouteEtaWithStationName.add(EtaDataWithBusStopName(currentRouteOutboundList[eta.seq-1].name_tc,eta))
                    }
                }
                marsUiState = MarsUiState.Success("finally Success",listOfCurrentRouteEtaWithStationName)
            } catch (e: IOException) {
                marsUiState = MarsUiState.Error
            }
            catch (e: Exception){
                marsUiState = MarsUiState.Error
            }
        }
    }
}

class EtaDataWithBusStopName(
    val busStopName : String,
    val eta: Eta
)


