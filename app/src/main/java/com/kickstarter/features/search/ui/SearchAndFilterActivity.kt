package com.kickstarter.features.search.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.staticCompositionLocalOf
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.features.search.viewmodel.SearchAndFilterViewModel
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.activities.compose.search.SearchAndFilterScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.startPreLaunchProjectActivity
import com.kickstarter.ui.extensions.startProjectActivity

// TODO: Improve error message
val LocalFilterMenuViewModel = staticCompositionLocalOf<FilterMenuViewModel> {
    error("No FilterMenuViewModel provided")
}

class SearchAndFilterActivity : ComponentActivity() {

    private lateinit var viewModelFactory: SearchAndFilterViewModel.Factory
    private lateinit var filterMenuViewModelFactory: FilterMenuViewModel.Factory
    private val viewModel: SearchAndFilterViewModel by viewModels { viewModelFactory }
    private val filterMenuViewModel: FilterMenuViewModel by viewModels { filterMenuViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpConnectivityStatusCheck(lifecycle)

        this.getEnvironment()?.let { env ->
            viewModelFactory = SearchAndFilterViewModel.Factory(env)
            filterMenuViewModelFactory = FilterMenuViewModel.Factory(env)
            filterMenuViewModel.getRootCategories()

            setContent {
                KickstarterApp {
                    SearchAndFilterScreen(
                        env = env,
                        searchViewModel = viewModel,
                        filterMenuVM = filterMenuViewModel,
                        onBackClicked = { onBackPressedDispatcher.onBackPressed() },
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
        }
    }
}
