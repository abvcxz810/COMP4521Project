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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
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
import com.example.marsphotos.ui.screens.HomeScreen
import com.example.marsphotos.ui.screens.MarsViewModel
import com.example.marsphotos.ui.screens.RouteEtaScreen

enum class kmbScreen() {
    AllRouteList,
    OneRoute,
    Search
}

@Composable
fun kmbTopAppBar(modifier: Modifier = Modifier) {
    TopAppBar(title = {
        Text(stringResource(R.string.app_name))
    },
        modifier = modifier
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
        topBar = { kmbTopAppBar() },
        bottomBar = { BottomBar({ navController.navigate(kmbScreen.OneRoute.name) }) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = kmbScreen.AllRouteList.name,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = kmbScreen.AllRouteList.name) {
                HomeScreen(
                    marsUiState = marsViewModel.marsUiState,
                    onRouteItemClicked = { route, bound->
                        marsViewModel.getRouteEtaAndStationId(route,bound)
                        navController.navigate(kmbScreen.OneRoute.name)
                    }
                )
            }
            composable(route = kmbScreen.OneRoute.name) {
                RouteEtaScreen(routeEtaUiState = marsViewModel.routeEtaUiState)
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
                Text(text = "bye", modifier = modifier.fillMaxSize())
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
