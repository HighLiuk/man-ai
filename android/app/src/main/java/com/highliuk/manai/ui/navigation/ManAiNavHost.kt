package com.highliuk.manai.ui.navigation

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.highliuk.manai.ui.home.HomeScreen
import com.highliuk.manai.ui.home.HomeViewModel
import com.highliuk.manai.ui.settings.SettingsScreen
import com.highliuk.manai.ui.settings.SettingsViewModel

@Composable
fun ManAiNavHost(
    onImportClick: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable("home") {
            val viewModel: HomeViewModel = hiltViewModel()
            val mangaList by viewModel.mangaList.collectAsState()
            val gridColumns by viewModel.gridColumns.collectAsState()

            HomeScreen(
                mangaList = mangaList,
                gridColumns = gridColumns,
                onImportClick = onImportClick,
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            val viewModel: SettingsViewModel = hiltViewModel()
            val gridColumns by viewModel.gridColumns.collectAsState()

            SettingsScreen(
                gridColumns = gridColumns,
                onGridColumnsChange = { viewModel.setGridColumns(it) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
