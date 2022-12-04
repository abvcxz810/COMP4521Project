package com.example.marsphotos.ui.screens


import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


var markedStops = mutableListOf<EtaDataWithBusStopName>()


@Composable
fun Bookmark(routeEtaUiState: RouteEtaUiState){
    when (routeEtaUiState){
        is RouteEtaUiState.Success -> BookmarkList(routeEtaUiState.etaList)
        is RouteEtaUiState.Loading -> LoadingScreen()
        is RouteEtaUiState.Error -> ErrorScreen()
    }

}

@Composable
fun BookmarkList(etaList: List<EtaDataWithBusStopName>){
    // Find the bookmarked stop ETA info and put in column
    if (markedStops.isNotEmpty() && etaList.isNotEmpty()) {
        val searchedStops = mutableListOf<EtaDataWithBusStopName>()

        markedStops.forEach { stops ->
            searchedStops.add(etaList.find{
                it.eta.route == stops.eta.route &&
                        it.eta.dir == stops.eta.dir &&
                        it.eta.seq == stops.eta.seq &&
                        it.eta.eta_seq == 1
            }!!)
        }
        LazyColumn {
            if (searchedStops.isNotEmpty()) {
                items(searchedStops) { eta ->
                    ETAItem(eta = eta)
                }
            }
        }
    }

    // Display text when nothing is bookmarked
    else
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Click a stop information card to bookmark it!",
                textAlign = TextAlign.Center
            )
        }
}


fun loadData(context: Context){
    val sharedPreferences = context.getSharedPreferences("shared preference", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPreferences.getString("stop list", null)

    markedStops = if (json == null){
        mutableListOf<EtaDataWithBusStopName>()
    }
    else{
        gson.fromJson(json)
    }

}

inline fun <reified T> Gson.fromJson(json: String): T = fromJson<T>(json, object: TypeToken<T>() {}.type)