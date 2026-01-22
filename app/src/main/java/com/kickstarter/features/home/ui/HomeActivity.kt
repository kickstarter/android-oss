package com.kickstarter.features.home.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kickstarter.features.home.data.Tab
import com.kickstarter.features.home.ui.components.FloatingBottomNav
import com.kickstarter.features.home.viewmodel.HomeScreenViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.features.search.viewmodel.SearchAndFilterViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.ui.activities.compose.search.SearchAndFilterScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.startPreLaunchProjectActivity
import com.kickstarter.ui.extensions.startProjectActivity
import com.kickstarter.ui.extensions.transition
import kotlin.getValue
import kotlin.random.Random

class HomeActivity : ComponentActivity() {

    private lateinit var environment: Environment
    private lateinit var viewModelFactory: HomeScreenViewModel.Factory
    private val viewModel: HomeScreenViewModel by viewModels { viewModelFactory }

    // Search related VM's
    private lateinit var searchVMFactory: SearchAndFilterViewModel.Factory
    private lateinit var filterMenuViewModelFactory: FilterMenuViewModel.Factory
    private val searchVM: SearchAndFilterViewModel by viewModels { searchVMFactory }
    private val filterMenuVM: FilterMenuViewModel by viewModels { filterMenuViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpConnectivityStatusCheck(lifecycle)
        enableEdgeToEdge()

        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = HomeScreenViewModel.Factory(env)
            searchVMFactory = SearchAndFilterViewModel.Factory(environment)
            filterMenuViewModelFactory = FilterMenuViewModel.Factory(environment)
            filterMenuVM.getRootCategories()
        }

        setContent {
            val darModeEnabled = this.isDarkModeEnabled(env = environment)
            val homeUIState by viewModel.homeUIState.collectAsStateWithLifecycle()

            val tabs = remember(homeUIState.isLoggedInUser) {
                listOf(
                    Tab.Home(),
                    Tab.Search(),
                    if (homeUIState.isLoggedInUser) Tab.Profile(homeUIState.userAvatarUrl) else Tab.LogIn()
                )
            }

            KickstarterApp(useDarkTheme = darModeEnabled) {
                val navController = rememberNavController()
                val shouldShowBottomNav = remember { mutableStateOf(true) }
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route

                val tabsUpdated = tabs.map { tab ->
                    // - Inject the Navigation logic into each one
                    when (tab) {
                        is Tab.Home -> tab.copy(onClick = { navController.navWithDefaults(tab.route) })
                        is Tab.Search -> tab.copy(onClick = { navController.navWithDefaults(tab.route) })
                        is Tab.LogIn -> tab.copy(onClick = { navController.navWithDefaults(tab.route) })
                        is Tab.Profile -> tab.copy(onClick = { navController.navWithDefaults(tab.route) })
                    }
                }

                val activeTab = tabsUpdated.find { it.route == currentRoute } ?: tabs.first()
                Scaffold(
                    modifier = Modifier.systemBarsPadding(),
                    bottomBar = {
                        if (shouldShowBottomNav.value) {
                            FloatingBottomNav(tabs = tabsUpdated, activeTab = activeTab)
                        }
                    }
                ) { inner ->
                    NavHost(
                        navController = navController,
                        startDestination = tabs.first().route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = inner.calculateTopPadding())
                    ) {
                        tabs.map { tab ->
                            when (tab) {
                                is Tab.Search -> {
                                    composable(tab.route) {
                                        SearchAndFilterScreen(
                                            env = environment,
                                            searchViewModel = searchVM,
                                            filterMenuVM = filterMenuVM,
                                            onBackClicked = { },
                                            preLaunchedCallback = { project, tag ->
                                                startPreLaunchProjectActivity(
                                                    project = project,
                                                    previousScreen = ThirdPartyEventValues.ScreenName.SEARCH.value,
                                                    refTag = tag
                                                )
                                            },
                                            projectCallback = { projectAndRef ->
                                                startProjectActivity(
                                                    project = projectAndRef.first,
                                                    refTag = projectAndRef.second,
                                                    previousScreen = ThirdPartyEventValues.ScreenName.SEARCH.value
                                                )
                                            },
                                        )
                                    }
                                }
                                else -> {
                                    composable(tab.route) {
                                        ScreenStub(tab.route)
                                    }
                                }
                            }
                        }
                    }
                }
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

/**
 * Navigates to a specified route using the standard default configuration for bottom navigation.
 *
 * This helper ensures that:
 * 1. The back stack is popped up to the start destination to avoid a large stack of screens.
 * 2. State is saved and restored when switching between tabs.
 * 3. Only a single instance of a destination is launched (launchSingleTop) to prevent multiple
 *    copies of the same screen when re-selecting a tab.
 *
 * @param route The destination route to navigate to.
 */
private fun NavHostController.navWithDefaults(route: String) {
    this.navigate(route) {
        popUpTo(this@navWithDefaults.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
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
