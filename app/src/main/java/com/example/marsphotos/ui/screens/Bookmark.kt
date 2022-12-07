package com.example.marsphotos.ui.screens


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


var markedStops = mutableListOf<StationInfoWithName>()


@Composable
fun Bookmark(marsViewModel: MarsViewModel){

    when (marsViewModel.stopEtaUiState){
        is StopEtaUiState.Success -> BookmarkList((marsViewModel.stopEtaUiState as StopEtaUiState.Success).bookmarkList)
        is StopEtaUiState.Loading -> LoadingScreen()
        is StopEtaUiState.Error -> ErrorScreen()
    }
}

@SuppressLint("CoroutineCreationDuringComposition", "MutableCollectionMutableState")
@Composable
fun BookmarkList(bookmarkList: List<StopEtaWithName>){
    // Find the bookmarked stop ETA info and put in column
    if (markedStops.isNotEmpty()){
        LazyColumn {
            if (bookmarkList.isNotEmpty()) {
                items(bookmarkList) { eta ->
                    BookmarkItem(eta = eta)
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookmarkItem(
    eta: StopEtaWithName,
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

        // Removes the clicked item
        onClick = {
            for (stops in markedStops){
                if (eta.stopEta.route == stops.routeStation.route &&
                    eta.stopEta.dir == stops.routeStation.bound &&
                    eta.stopEta.seq.toString() == stops.routeStation.seq){
                    markedStops.remove(stops)
                    saveData(context)
                    Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                    break
                }
            }
        }

    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f)) {
                Text(text = (eta.stopEta.route), style = MaterialTheme.typography.h6)
                Text(text = eta.busStopName, style = MaterialTheme.typography.body1)
            }
            Column(modifier = Modifier.weight(2f)) {
                Text(text = "往${eta.stopEta.dest_tc}", style = MaterialTheme.typography.body1)
                Text(text = "To ${eta.stopEta.dest_en}", style = MaterialTheme.typography.body1)
                Text(text = "${eta.stopEta.eta?.let { getTimeDiff(it) }?:"-"} 分鐘/mins (${eta.stopEta.eta?.let { extractTime(it) }?:eta.stopEta.rmk_tc})",style = MaterialTheme.typography.body1)
            }
        }
    }
}

fun loadData(context: Context){
    val sharedPreferences = context.getSharedPreferences("shared preference", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPreferences.getString("stop list", null)

    markedStops = if (json == null){
        mutableListOf<StationInfoWithName>()
    }
    else{
        gson.fromJson(json)
    }

}

inline fun <reified T> Gson.fromJson(json: String): T = fromJson<T>(json, object: TypeToken<T>() {}.type)