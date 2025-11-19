package com.kickstarter.features.home.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Tab(val route: String, val icon: ImageVector) {
    data object Home : Tab("home", Icons.Outlined.Home)
    data object Search : Tab("search", Icons.Outlined.Search)
    data object Profile : Tab("profile", Icons.Outlined.Person)
}

// Hardcoded for now, could try to potentially load configuration from backend (SDUI approach or easiest version try remote config configuration)
val tabs = listOf(Tab.Home, Tab.Search, Tab.Profile)
