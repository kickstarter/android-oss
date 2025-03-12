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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.kickstarter.R
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.features.search.viewmodel.SearchAndFilterViewModel
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPreLaunchProjectActivity
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.libs.utils.extensions.isTrimmedEmpty
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.toDiscoveryParam
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.models.Project
import com.kickstarter.type.ProjectSort
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.search.SearchScreen
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import kotlinx.coroutines.launch

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

            setContent {
                val searchUIState by viewModel.searchUIState.collectAsStateWithLifecycle()

                var currentSearchTerm by rememberSaveable { mutableStateOf("") }

                val popularProjects = searchUIState.popularProjectsList

                val searchedProjects = searchUIState.searchList

                val isLoading = searchUIState.isLoading

                val isTyping by remember { mutableStateOf(false) }

                val lazyListState = rememberLazyListState()

                val snackbarHostState = remember { SnackbarHostState() }

                val categoriesState by filterMenuViewModel.filterMenuUIState.collectAsStateWithLifecycle()

                // TODO: send the list of categories to the BottomSheet coordinate with MBL-2171
                val categories = categoriesState.categoriesList

                SetUpErrorActions(snackbarHostState)

                val darModeEnabled = this.isDarkModeEnabled(env = env)
                KickstarterApp(useDarkTheme = darModeEnabled) {
                    SearchScreen(
                        environment = env,
                        onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                        scaffoldState = rememberScaffoldState(),
                        errorSnackBarHostState = snackbarHostState,
                        isLoading = isLoading,
                        isPopularList = currentSearchTerm.isTrimmedEmpty(),
                        itemsList = if (currentSearchTerm.isTrimmedEmpty()) {
                            popularProjects
                        } else {
                            searchedProjects
                        },
                        lazyColumnListState = lazyListState,
                        showEmptyView = !isLoading &&
                            !isTyping &&
                            !currentSearchTerm.isTrimmedEmpty() &&
                            searchedProjects.isEmpty(),
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
                        onDismissBottomSheet = {
                            viewModel.updateParamsToSearchWith(
                                CategoryFactory.gamesCategory(),
                                ProjectSort.MOST_FUNDED.toDiscoveryParam()
                            )
                        }
                    )
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
