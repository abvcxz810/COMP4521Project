package com.example.marsphotos.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.marsphotos.network.RouteStation
import com.google.gson.Gson
import java.util.*

@Composable
fun RouteEtaScreen(routeEtaUiState: RouteEtaUiState, modifier: Modifier = Modifier) {
    when (routeEtaUiState) {
        is RouteEtaUiState.Success -> RouteEtaResultScreen(
            routeEtaUiState.etaList,
            routeEtaUiState.bound,
            if (routeEtaUiState.bound == "O") routeEtaUiState.outboundList else routeEtaUiState.inboundList,
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
    stationList: List<RouteStation>,
    modifier: Modifier = Modifier,
) {
    EtaList(etaList, bound, stationList)
}

@Composable
fun EtaList(etas: List<EtaDataWithBusStopName>, bound: String, stationList: List<RouteStation>) {
    var i = 0
    val temp = etas.partition { it.eta.dir == bound }
    val trytry = temp.first.groupBy { it.eta.seq }.values.toList()
    trytry.forEach {
        Log.d("Size", it.size.toString())
    }
    LazyColumn {
        items(trytry) { eta ->
            ETAItem(eta = eta[0], eta_seq2 = try {
                eta[1].eta.eta
            } catch (e: java.lang.IndexOutOfBoundsException) {
                null
            }, eta_seq3 = try {
                eta[2].eta.eta
            } catch (e: java.lang.IndexOutOfBoundsException) {
                null
            }, stationList = stationList)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ETAItem(
    eta: EtaDataWithBusStopName,
    eta_seq2: String?,
    eta_seq3: String?,
    stationList: List<RouteStation>,
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
            if (markedStops.isNotEmpty()) {
                for (stops in markedStops) {
                    if (eta.eta.route == stops.routeStation.route &&
                        eta.eta.dir == stops.routeStation.bound &&
                        eta.eta.seq.toString() == stops.routeStation.seq
                    ) {
                        contains = true
                        markedStops.remove(stops)
                        break
                    }
                }
            }
            if (!contains) {
                markedStops.add(StationInfoWithName(eta.busStopName, stationList.find {
                    it.route == eta.eta.route && it.bound == eta.eta.dir && it.seq == eta.eta.seq.toString()
                }!!))
                saveData(context)
                Toast.makeText(context, "Stop bookmarked", Toast.LENGTH_SHORT).show()
            } else {
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
                Text(text = eta.busStopName, style = MaterialTheme.typography.h6)
            }
            Column(modifier = Modifier.weight(2f)) {
                Text(text = "${eta.eta.eta?.let { getTimeDiff(it) } ?: "-"} 分鐘/mins (${
                    eta.eta.eta?.let {
                        extractTime(it)
                    } ?: eta.eta.rmk_tc
                })", style = MaterialTheme.typography.body1)
                Text(text = "${eta_seq2?.let { getTimeDiff(it) } ?: "-"} 分鐘/mins (${
                    eta_seq2?.let {
                        extractTime(it)
                    } ?: ""
                })", style = MaterialTheme.typography.body1)
                Text(text = "${eta_seq3?.let { getTimeDiff(it) } ?: "-"} 分鐘/mins (${
                    eta_seq3?.let {
                        extractTime(it)
                    } ?: ""
                })", style = MaterialTheme.typography.body1)
            }
            var bookmarked = false
            if (markedStops.isNotEmpty()) {
                for (stops in markedStops) {
                    if (eta.eta.route == stops.routeStation.route &&
                        eta.eta.dir == stops.routeStation.bound &&
                        eta.eta.seq.toString() == stops.routeStation.seq
                    ) {
                        bookmarked = true
                        break
                    }
                }
            }
            Icon(imageVector = Icons.Outlined.Star, contentDescription = "Icon", Modifier.alpha(if (!bookmarked) 0.0F else 1.0F))
        }
    }
}


fun saveData(context: Context) {
    val sharedPreferences = context.getSharedPreferences("shared preference", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val json = gson.toJson(markedStops)
    editor.putString("stop list", json)
    editor.apply()
}
