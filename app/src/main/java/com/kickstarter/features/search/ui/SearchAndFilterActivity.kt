package com.kickstarter.features.search.ui

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.kickstarter.R
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.features.search.viewmodel.SearchAndFilterViewModel
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPreLaunchProjectActivity
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.libs.utils.extensions.isTrimmedEmpty
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.search.SearchScreen
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import kotlinx.coroutines.launch

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

            val phaseff = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_SEARCH_FILTER) ?: false

            setContent {
                var currentSearchTerm by rememberSaveable { mutableStateOf("") }
                val lazyListState = rememberLazyListState()
                val snackbarHostState = remember { SnackbarHostState() }

                val searchUIState by viewModel.searchUIState.collectAsStateWithLifecycle()
                val popularProjects = searchUIState.popularProjectsList
                val searchedProjects = searchUIState.searchList
                val isLoading = searchUIState.isLoading
                val hasMorePages = searchUIState.hasMore

                val categoriesState by filterMenuViewModel.filterMenuUIState.collectAsStateWithLifecycle()
                val categories = categoriesState.categoriesList

                SetUpErrorActions(snackbarHostState)

                val darModeEnabled = this.isDarkModeEnabled(env = env)
                KickstarterApp(useDarkTheme = darModeEnabled) {
                    CompositionLocalProvider(LocalFilterMenuViewModel provides filterMenuViewModel) {
                        SearchScreen(
                            environment = env,
                            onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                            scaffoldState = rememberScaffoldState(),
                            errorSnackBarHostState = snackbarHostState,
                            isLoading = isLoading,
                            isDefaultList = currentSearchTerm.isTrimmedEmpty(),
                            itemsList = if (currentSearchTerm.isTrimmedEmpty()) {
                                popularProjects
                            } else {
                                searchedProjects
                            },
                            lazyColumnListState = lazyListState,
                            showEmptyView = !isLoading && (searchedProjects.isEmpty() && popularProjects.isEmpty()),
                            categories = categories,
                            onSearchTermChanged = { searchTerm ->
                                currentSearchTerm = searchTerm
                                viewModel.updateSearchTerm(searchTerm)
                            },
                            onItemClicked = { project ->
                                val projAndRef = viewModel.getProjectAndRefTag(project)
                                if (project.displayPrelaunch().isTrue()) {
                                    startPreLaunchProjectActivity(project, projAndRef.second)
                                } else {
                                    startProjectActivity(projAndRef)
                                }
                            },
                            onDismissBottomSheet = { category, sort, projectState, percentageBucket, location, amountRaisedBucket, goalBucket ->
                                viewModel.updateParamsToSearchWith(
                                    category = category,
                                    projectSort = sort
                                        ?: DiscoveryParams.Sort.MAGIC, // magic is the default sort
                                    projectState = projectState,
                                    percentageBucket = percentageBucket,
                                    location = location,
                                    amountBucket = amountRaisedBucket,
                                    goalBucket = goalBucket
                                )
                            },
                            shouldShowPhase = phaseff
                        )
                    }
                }

                // Load more when scroll to the end
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val layoutInfo = lazyListState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

                        lastVisibleItemIndex >= (totalItems - 5) && totalItems > 0
                    }
                }

                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(shouldLoadMore, lifecycleOwner.lifecycle.currentState, isLoading, hasMorePages) {
                    if (shouldLoadMore && lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED && !isLoading && hasMorePages) {
                        viewModel.loadMore()
                    }
                }
            }
        }
    }

    @Composable
    private fun SetUpErrorActions(snackbarHostState: SnackbarHostState) {
        val errorAction = { message: String? ->
            lifecycleScope.launch { // TODO: store SnackbarResult on VM to consult it before showing new  one, in case there is multiple enqueue snackbars.
                snackbarHostState.showSnackbar(
                    message = message ?: getString(R.string.Something_went_wrong_please_try_again),
                    actionLabel = KSSnackbarTypes.KS_ERROR.name,
                    duration = SnackbarDuration.Long
                )
            }
        }
        viewModel.provideErrorAction { message ->
            errorAction.invoke(message)
        }

        filterMenuViewModel.provideErrorAction { message ->
            errorAction.invoke(message)
        }
    }

    private fun startPreLaunchProjectActivity(project: Project, refTag: RefTag) {
        val intent = Intent().getPreLaunchProjectActivity(this, project.slug())
            .putExtra(IntentKey.REF_TAG, refTag)
            .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.SEARCH.value)
        startActivity(intent)
        TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
    }

    private fun startProjectActivity(projectAndRefTagAndIsFfEnabled: Pair<Project, RefTag>) {
        val intent = Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT, projectAndRefTagAndIsFfEnabled.first)
            .putExtra(IntentKey.REF_TAG, projectAndRefTagAndIsFfEnabled.second)
            .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.SEARCH.value)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
