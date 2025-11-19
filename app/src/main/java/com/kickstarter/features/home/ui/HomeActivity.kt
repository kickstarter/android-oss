package com.kickstarter.features.home.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kickstarter.features.home.data.Tab
import com.kickstarter.features.home.data.tabs
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.transition
import kotlin.random.Random

class HomeActivity : ComponentActivity() {

    private lateinit var environment: Environment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpConnectivityStatusCheck(lifecycle)
        enableEdgeToEdge()

        this.getEnvironment()?.let { env ->
            environment = env
        }

        setContent {
            val darModeEnabled = this.isDarkModeEnabled(env = environment)
            KickstarterApp(useDarkTheme = darModeEnabled) {
                App()
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                this@HomeActivity.transition(TransitionUtils.slideInFromLeft())
            }
        })
    }
}

@Composable
fun App() {
    val nav = rememberNavController()
    val shouldShowBottomNav = remember { mutableStateOf(true) }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (shouldShowBottomNav.value)
                FloatingCenterPill(nav, shouldShowBottomNav)
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Tab.Home.route,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(Tab.Home.route) { ScreenStub("Home") }
            composable(Tab.Search.route) { ScreenStub("Search") }
            composable(Tab.Profile.route) { ScreenStub("Profile") }
        }
    }
}

@Composable
private fun FloatingCenterPill(nav: NavHostController, shouldShowBottomNav: MutableState<Boolean>) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Box(
        Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(152.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            NavigationBar(
                modifier = Modifier
                    .height(64.dp) // - This potentially loses responsiveness for large screens
                    .widthIn(min = 260.dp)
                    .padding(horizontal = 8.dp),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                tabs.forEach { tab ->
                    val selected = current == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (tab == Tab.Profile) shouldShowBottomNav.value = false
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        icon = {
                            if (selected) {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFB6F36C))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        tab.icon,
                                        contentDescription = tab.route
                                    )
                                }
                            } else {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.route
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenStub(title: String) {
    Box(Modifier.fillMaxSize().background(Color.Gray), contentAlignment = Alignment.Center) {

        val state = rememberLazyListState()
        val count = 100

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(count = count, key = { it }) { index ->
                val heightPx = remember(index) { Random(index).nextInt(220, 420) }

                Card(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heightPx.dp)
                        .padding(horizontal = 16.dp)
                ) {
                }
            }
        }
    }
}
