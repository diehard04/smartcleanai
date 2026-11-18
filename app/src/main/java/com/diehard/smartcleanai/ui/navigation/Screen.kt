package com.diehard.smartcleanai.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Scanning : Screen("scanning")
    data object Review : Screen("review/{category}") {
        fun createRoute(categoryName: String) = "review/$categoryName"
    }
    data object Summary : Screen("summary/{freedBytes}/{itemCount}") {
        fun createRoute(freedBytes: Long, itemCount: Int) = "summary/$freedBytes/$itemCount"
    }
    data object Settings : Screen("settings")
    data object IgnoredMedia : Screen("ignored_media")
}
