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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.marsphotos.R
import com.example.marsphotos.network.RouteData

@Composable
fun HomeScreen(
    marsUiState: MarsUiState,
    onRouteItemClicked: (String,String)->Unit,
    modifier: Modifier = Modifier,
) {
    when (marsUiState) {
        is MarsUiState.Success -> ResultScreen1( 
            onRouteItemClicked,
            marsUiState.routeDataList,
            modifier)
        is MarsUiState.Loading -> LoadingScreen(modifier)
        is MarsUiState.Error -> ErrorScreen(modifier)
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        Image(modifier = Modifier.size(200.dp),
            painter = painterResource(id = R.drawable.loading_img),
            contentDescription = stringResource(
                id = R.string.loading))
    }
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        Text(text = stringResource(id = R.string.loading_failed))
    }
}

//ResultScreen for Route List Data
@Composable
fun ResultScreen1(
    onRouteItemClicked: (String,String)->Unit,
    routeDataList: List<RouteData>,
    modifier: Modifier = Modifier,
) {
    val textState = remember { mutableStateOf(TextFieldValue("")) }
    Column {
        SearchView(state = textState)
        RouteDataList(routeDataList, onRouteItemClicked, textState)
    }
}

// Search bar
@Composable
fun SearchView(
    modifier: Modifier = Modifier,
    state: MutableState<TextFieldValue>
) {
    val focusManager = LocalFocusManager.current
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = state.value,
        onValueChange = { value ->
            state.value = value
        },

        // Search icon in front
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "",
                modifier = Modifier
                    .padding(15.dp)
                    .size(24.dp)
            )
        },

        // X icon for clearing search
        trailingIcon = {
            if (state.value != TextFieldValue("")) {
                IconButton(
                    // Clear search text on click
                    onClick = {
                        state.value = TextFieldValue("")
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(15.dp)
                            .size(24.dp)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {focusManager.clearFocus()})
    )
}

@Composable
fun RouteDataList(routeDataList: List<RouteData>, onRouteItemClicked: (String,String)->Unit,  state: MutableState<TextFieldValue>) {
    var filteredRoutes: ArrayList<RouteData>
    LazyColumn {
        val searchedRoute = state.value.text
        filteredRoutes =
            if (searchedRoute.isEmpty()) {ArrayList(routeDataList)}
            else{
                val resultList = ArrayList<RouteData>()
                for (route in routeDataList){
                    if (route.route.startsWith(searchedRoute,ignoreCase = true) ||
                        route.dest_en.contains(searchedRoute, ignoreCase = true) ||
                        route.dest_tc.contains(searchedRoute, ignoreCase = true) ||
                        route.dest_sc.contains(searchedRoute, ignoreCase = true) ||
                        route.orig_en.contains(searchedRoute, ignoreCase = true) ||
                        route.orig_tc.contains(searchedRoute, ignoreCase = true) ||
                        route.orig_sc.contains(searchedRoute, ignoreCase = true)){
                        resultList.add(route)
                    }
            }
                resultList
            }

        items(filteredRoutes) { routeData ->
            Surface(modifier = Modifier.clickable { onRouteItemClicked(routeData.route,routeData.bound) }) {
                RouteDataItem(routeData.route,routeData.orig_tc,routeData.orig_en,routeData.dest_tc,routeData.dest_en)
            }
        }
    }
}

@Composable
fun RouteDataItem(
    route: String,
    orig_tc: String,
    orig_en: String,
    dest_tc:String,
    dest_en: String,
    modifier: Modifier = Modifier,
) {

    Card(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 2.dp,
        backgroundColor = Color.Gray
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = modifier,  verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                Text(text = "$route ",
                    style = MaterialTheme.typography.h3,
                    )
            }
            Spacer(modifier = Modifier.width(5.dp))
            Column(modifier = Modifier
                .padding(end = 16.dp)
                ) {
                Text(text = "??? $orig_tc")
                Text(text = "??? $dest_tc")
                Text(text = "From $orig_en")
                Text(text = "To /${dest_en}")
            }
        }
    }
}