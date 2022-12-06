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

package com.example.marsphotos.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.marsphotos.R
import com.example.marsphotos.ui.screens.*

enum class kmbScreen() {
    AllRouteList,
    OneRoute,
    Bookmark
}

@Composable
fun KmbAppBar(
    canNavigateBack: Boolean,
    topBarUiState: String,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(topBarUiState) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun MarsPhotosApp(
    marsViewModel: MarsViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen =
        kmbScreen.valueOf(backStackEntry?.destination?.route ?: kmbScreen.AllRouteList.name)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
                KmbAppBar(
                    canNavigateBack = navController.previousBackStackEntry != null,
                    topBarUiState = marsViewModel.topBarUiState,
                    navigateUp = { navController.navigateUp() },
                )
        },
        bottomBar = { BottomBar({
            if (currentScreen != kmbScreen.Bookmark) navController.navigate(kmbScreen.Bookmark.name)
            else navController.navigate(kmbScreen.AllRouteList.name)
        }) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = kmbScreen.AllRouteList.name,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = kmbScreen.AllRouteList.name) {
                marsViewModel.updateTopBarUIByPassingString("Welcome")
                HomeScreen(
                    marsUiState = marsViewModel.marsUiState,
                    onRouteItemClicked = { route, bound->
                        marsViewModel.getRouteEtaAndStationId(route,bound)
                        navController.navigate(kmbScreen.OneRoute.name)
                    }
                )
            }
            composable(route = kmbScreen.OneRoute.name) {
                marsViewModel.updateTopBarUIByPassingString(when (marsViewModel.routeEtaUiState) {
                    is RouteEtaUiState.Success -> "${(marsViewModel.routeEtaUiState as RouteEtaUiState.Success).etaList[0].eta.route} 往 ${(marsViewModel.routeEtaUiState as RouteEtaUiState.Success).etaList[0].eta.dest_tc}"
                    is RouteEtaUiState.Loading ->  "Loading"
                    is RouteEtaUiState.Error -> "Error"
                })
                RouteEtaScreen(routeEtaUiState = marsViewModel.routeEtaUiState)
            }

            composable(route = kmbScreen.Bookmark.name){
                if (markedStops.isNotEmpty()) marsViewModel.getBookmarkEta()
                marsViewModel.updateTopBarUIByPassingString("Bookmark")
                Bookmark(marsViewModel)
            }
        }
    }
}

@Composable
fun BottomBar(navigateUp: () -> Unit, modifier: Modifier = Modifier) {
    //BottomAppBar Composable
    BottomAppBar(backgroundColor = Color(0xFF0F9D58)) {
        Row(modifier = modifier.fillMaxSize()) {
            Button(onClick = navigateUp,
                modifier
                    .fillMaxSize()
                    .weight(1f)) {
                Icon(imageVector = Icons.Outlined.Star, contentDescription = "Icon")
                Text(text = "Bookmarks")
            }
            Button(onClick = { /*TODO*/ },
                modifier
                    .fillMaxSize()
                    .weight(1f)) {
                Text(text = "hi", modifier = modifier.fillMaxSize())
            }
        }
    }
}
