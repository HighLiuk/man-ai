package com.highliuk.manai.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.highliuk.manai.ui.home.HomeScreen
import com.highliuk.manai.ui.reader.ReaderScreen
import com.highliuk.manai.ui.settings.SettingsScreen

private const val NAV_ANIM_DURATION = 300

@Composable
fun ManAiNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION))
        },
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onMangaClick = { mangaId ->
                    navController.navigate(Screen.Reader.createRoute(mangaId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable(
            route = Screen.Reader.route,
            arguments = listOf(navArgument("mangaId") { type = NavType.LongType }),
        ) {
            ReaderScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
