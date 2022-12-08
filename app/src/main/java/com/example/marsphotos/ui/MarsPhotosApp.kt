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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
    currentScreen: kmbScreen,
    refreshRoute: () -> Unit,
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
        },
        actions = {
            if (currentScreen == kmbScreen.OneRoute) {
                IconButton(onClick = refreshRoute) {
                    Icon(imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh")
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
                currentScreen = currentScreen,
                refreshRoute = {
                    when (marsViewModel.routeEtaUiState) {
                        is RouteEtaUiState.Success -> {
                            marsViewModel.getRouteEtaAndStationId((marsViewModel.routeEtaUiState as RouteEtaUiState.Success).etaList[0].eta.route,
                                (marsViewModel.routeEtaUiState as RouteEtaUiState.Success).bound)
                        }
                        is RouteEtaUiState.Loading -> {}
                        is RouteEtaUiState.Error -> {}
                    }
                }
            )
        },
        bottomBar = {
            BottomBar({
                if (currentScreen != kmbScreen.Bookmark) navController.navigate(kmbScreen.Bookmark.name)
                else {
                    navController.popBackStack(kmbScreen.AllRouteList.name, false)
                }
            }, currentScreen)
        }
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
                    onRouteItemClicked = { route, bound ->
                        marsViewModel.getRouteEtaAndStationId(route, bound)
                        navController.navigate(kmbScreen.OneRoute.name)
                    }
                )
            }
            composable(route = kmbScreen.OneRoute.name) {
                marsViewModel.updateTopBarUIByPassingString(when (marsViewModel.routeEtaUiState) {
                    is RouteEtaUiState.Success -> "${(marsViewModel.routeEtaUiState as RouteEtaUiState.Success).etaList[0].eta.route} 往 ${(marsViewModel.routeEtaUiState as RouteEtaUiState.Success).etaList[0].eta.dest_tc}"
                    is RouteEtaUiState.Loading -> "Loading"
                    is RouteEtaUiState.Error -> "Error"
                })
                RouteEtaScreen(routeEtaUiState = marsViewModel.routeEtaUiState)
            }

            composable(route = kmbScreen.Bookmark.name) {
                marsViewModel.updateTopBarUIByPassingString("Bookmark")
                if (markedStops.isNotEmpty()) {marsViewModel.getBookmarkEta()
                Bookmark(marsViewModel)}
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
        }
    }
}

@Composable
fun BottomBar(navigateUp: () -> Unit, currentScreen: kmbScreen, modifier: Modifier = Modifier) {
    //BottomAppBar Composable
    BottomAppBar(backgroundColor = Color(0xFF0F9D58)) {
        Row(modifier = modifier
            .fillMaxSize()
            .align(Alignment.CenterVertically)) {
            Button(onClick = navigateUp,
                modifier
                    .fillMaxSize()
                    .weight(1f)) {
                if (currentScreen == kmbScreen.Bookmark) {
                    Text(text = "Home", textAlign = TextAlign.Center)
                } else {
                    Icon(imageVector = Icons.Outlined.Star, contentDescription = "Icon")
                    Text(text = "Bookmarks", textAlign = TextAlign.Center)
                }
            }
        }
    }
}
