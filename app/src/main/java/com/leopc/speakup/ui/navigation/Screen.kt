package com.leopc.speakup.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Stats : Screen("stats")
    object Profile : Screen("profile")
}
