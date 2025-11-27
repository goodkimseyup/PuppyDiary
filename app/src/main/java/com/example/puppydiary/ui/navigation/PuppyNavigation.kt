package com.example.puppydiary.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.puppydiary.ui.components.BottomNavigation
import com.example.puppydiary.ui.screens.*
import com.example.puppydiary.viewmodel.PuppyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuppyNavigation(
    viewModel: PuppyViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val puppyData by viewModel.puppyData.collectAsState()

    // 강아지가 등록되어 있는지 확인
    val startDestination = if (puppyData == null) "register" else "home"

    // puppyData가 변경되면 화면 전환
    LaunchedEffect(puppyData) {
        if (puppyData != null && navController.currentDestination?.route == "register") {
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            // 등록 화면에서는 하단 네비게이션 숨김
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != "register") {
                BottomNavigation(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable("register") {
                RegisterPuppyScreen(
                    viewModel = viewModel,
                    onRegistrationComplete = {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(viewModel = viewModel, navController = navController)
            }
            composable("stats") {
                StatsScreen(viewModel = viewModel)
            }
            composable("diary") {
                DiaryScreen(viewModel = viewModel)
            }
            composable("gallery") {
                PhotoGalleryScreen(viewModel = viewModel)
            }
        }
    }
}
