package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.KickstarterApp

// ---------- Routes ----------
sealed interface Screen {
    val route: String
    data object Home : Screen { override val route = "home" }
    data object Search : Screen { override val route = "search" }
    data object Profile : Screen { override val route = "profile" }

    // Non–bottom-bar destinations
    data object Details : Screen {
        override val route = "details/{id}"
        fun createRoute(id: String) = "details/$id"
    }
    data object Settings : Screen { override val route = "settings" }
}

data class TopLevelDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val TopLevelDestinations = listOf(
    TopLevelDestination(Screen.Home, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    TopLevelDestination(Screen.Search, "Search", Icons.Outlined.Search, Icons.Filled.Search),
    TopLevelDestination(Screen.Profile, "Profile", Icons.Outlined.Person, Icons.Filled.Person)
)

private val BottomBarRoutes = TopLevelDestinations.map { it.screen.route }

class BottomNavWithNavigation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        this.getEnvironment()?.let { env ->
            setContent {
                KickstarterApp(useDarkTheme = true) {
                    val nav = rememberNavController()
                    val backStack by nav.currentBackStackEntryAsState()
                    val currentDestination = backStack?.destination

                    val showBottomBar = currentDestination
                        ?.hierarchy
                        ?.any { dest -> BottomBarRoutes.contains(dest.route) } == true

                    Scaffold(
                        topBar = {
                            // topBar here
                        },
                        bottomBar = {
                            AnimatedVisibility(
                                visible = showBottomBar,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut()
                            ) {
                                AppBottomBar(nav)
                            }
                        },
                        contentWindowInsets = WindowInsets.systemBars
                    ) { inner ->
                        NavHost(
                            navController = nav,
                            startDestination = Screen.Home.route,
                            modifier = Modifier.padding(inner)
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    onOpenDetails = { id ->
                                        nav.navigate(
                                            Screen.Details.createRoute(
                                                id
                                            )
                                        )
                                    }
                                )
                            }
                            composable(Screen.Search.route) {
                                SearchScreen(onOpenDetails = { id ->
                                    nav.navigate(
                                        Screen.Details.createRoute(id)
                                    )
                                })
                            }
                            composable(Screen.Profile.route) {
                                ProfileScreen(onOpenSettings = {
                                    nav.navigate(
                                        Screen.Settings.route
                                    )
                                })
                            }

                            composable(
                                route = Screen.Details.route,
                                arguments = listOf(navArgument("id") { type = NavType.StringType })
                            ) { entry ->
                                DetailsScreen(id = entry.arguments?.getString("id").orEmpty())
                            }

                            composable(Screen.Settings.route) { SettingsScreen() }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AppBottomBar(nav: NavHostController) {
        val backStack by nav.currentBackStackEntryAsState()
        val currentDestination = backStack?.destination

        NavigationBar {
            TopLevelDestinations.forEach { item ->
                val selected = currentDestination
                    ?.hierarchy
                    ?.any { it.route == item.screen.route } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        nav.navigate(item.screen.route) {
                            popUpTo(nav.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            if (selected) item.selectedIcon else item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = { Text(item.label) }
                )
            }
        }
    }

    @Composable
    fun HomeScreen(onOpenDetails: (String) -> Unit) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Settings")
        }
    }

    @Composable
    fun SearchScreen(onOpenDetails: (String) -> Unit) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { onOpenDetails("99") }) { Text("Open details (hide bottom bar)") }
        }
    }

    @Composable
    fun ProfileScreen(onOpenSettings: () -> Unit) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = onOpenSettings) { Text("Open Settings (hide bottom bar)") }
        }
    }

    @Composable
    fun DetailsScreen(id: String) {
        // No bottom bar here
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Details for item $id")
        }
    }

    @Composable
    fun SettingsScreen() {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Settings")
        }
    }
}
