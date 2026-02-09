package com.highliuk.manai.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object Reader : Screen("reader/{mangaId}") {
        fun createRoute(mangaId: Long): String = "reader/$mangaId"
    }
}
