package com.example.marsphotos.ui.screens

import android.util.Log
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson

@Composable
fun RouteEtaScreen(routeEtaUiState: RouteEtaUiState, modifier: Modifier = Modifier) {
    when (routeEtaUiState) {
        is RouteEtaUiState.Success -> RouteEtaResultScreen(
            routeEtaUiState.etaList,
            routeEtaUiState.bound,
            modifier)
        is RouteEtaUiState.Loading -> LoadingScreen(modifier)
        is RouteEtaUiState.Error -> ErrorScreen(modifier)
    }
}

/**
 * The home screen displaying result of fetching photos.
 */
@Composable
fun RouteEtaResultScreen(
    etaList: List<EtaDataWithBusStopName>,
    bound: String,
    modifier: Modifier = Modifier,
) {
    EtaList(etaList, bound)
}

@Composable
fun EtaList(etas: List<EtaDataWithBusStopName>, bound: String) {
    LazyColumn {
        items(etas) { eta ->
            if (eta.eta.eta_seq == 1 && eta.eta.dir == bound) ETAItem(eta = eta)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ETAItem(
    eta: EtaDataWithBusStopName,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 2.dp,
        backgroundColor = Color.Gray,
        // Adds the item to the bookmark stop list in Bookmark.kt
        onClick = {
            var contains = false
            if (markedStops.isNotEmpty()){
                for (stops in markedStops){
                    if (eta.eta.route == stops.eta.route &&
                        eta.eta.dir == stops.eta.dir &&
                        eta.eta.seq == stops.eta.seq){
                        contains = true
                    }
                }
            }
            if (!contains) {
                markedStops.add(eta)
                saveData(context)
                Toast.makeText(context, "Stop bookmarked", Toast.LENGTH_SHORT).show()
            }
            else {
                for (stops in markedStops){
                    if (eta.eta.route == stops.eta.route &&
                        eta.eta.dir == stops.eta.dir &&
                        eta.eta.seq == stops.eta.seq){
                        markedStops.remove(stops)
                        break
                    }
                }
                saveData(context)
                Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f)) {
                Text(text = (eta.eta.seq).toString(), style = MaterialTheme.typography.body1)
                Text(text = eta.busStopName, style = MaterialTheme.typography.body1)
            }
            Column(modifier = Modifier.weight(2f)) {
                Text(text = "${eta.eta.eta?.let { extractTime(it) }?:eta.eta.rmk_tc}mins", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "${eta.eta.eta?.let { getTimeDiff(it) }?:"-"}mins",style = MaterialTheme.typography.body1)
            }
        }
    }
}


fun saveData(context: Context){
    val sharedPreferences = context.getSharedPreferences("shared preference", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val json = gson.toJson(markedStops)
    editor.putString("stop list",json)
    editor.apply()
}
