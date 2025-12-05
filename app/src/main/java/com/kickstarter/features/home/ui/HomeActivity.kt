package com.kickstarter.features.home.ui

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kickstarter.features.home.data.Tab
import com.kickstarter.features.home.ui.compose.FloatingCenterBottomNav
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.transition
import kotlin.random.Random

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AppPreview() {
    KickstarterApp {
        App()
    }
}

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
            App()
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
                FloatingCenterBottomNav(nav)
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Tab.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = inner.calculateTopPadding())
        ) {
            composable(Tab.Home.route) { ScreenStub("Home") }
            composable(Tab.Search.route) { ScreenStub("Search") }
            composable(Tab.Profile.route) { ScreenStub("Profile") }
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
