package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import com.kickstarter.R
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPreLaunchProjectActivity
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.isTrimmedEmpty
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.search.SearchScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.viewmodels.SearchViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay

class SearchActivity : ComponentActivity() {
    private lateinit var viewModelFactory: SearchViewModel.Factory
    val viewModel: SearchViewModel.SearchViewModel by viewModels { viewModelFactory }

    private var darkModeEnabled: Boolean = false
    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        disposables = CompositeDisposable()

        val env = this.getEnvironment()?.let { env ->
            viewModelFactory = SearchViewModel.Factory(env, intent = intent)
            darkModeEnabled =
                env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            env
        }

        setContent {
            var currentSearchTerm by remember { mutableStateOf("") }

            var popularProjects =
                viewModel.popularProjects().subscribeAsState(initial = listOf()).value

            var searchedProjects =
                viewModel.searchProjects().subscribeAsState(initial = listOf()).value

            var isLoading = viewModel.isFetchingProjects().subscribeAsState(initial = false).value

            val lazyListState = rememberLazyListState()

            val shouldStatePaginate = remember {
                derivedStateOf {
                    (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: -4) >= lazyListState.layoutInfo.totalItemsCount - 9
                }
            }

            KickstarterApp(useDarkTheme = if (darkModeEnabled) isSystemInDarkTheme() else false) {
                SearchScreen(
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    scaffoldState = rememberScaffoldState(),
                    isLoading = isLoading,
                    isPopularList = currentSearchTerm.isTrimmedEmpty(),
                    itemsList = if (currentSearchTerm.isTrimmedEmpty()) {
                        popularProjects
                    } else {
                        searchedProjects
                    },
                    lazyColumnListState = lazyListState,
                    showEmptyView = false,
                    onSearchTermChanged = { searchTerm ->
                        if (searchTerm.isEmpty()) viewModel.clearSearchedProjects()
                        currentSearchTerm = searchTerm
                    },
                    onItemClicked = { project ->
                        viewModel.projectClicked(project = project)
                    }
                )
            }

            LaunchedEffect(key1 = currentSearchTerm) {
                if (currentSearchTerm.isTrimmedEmpty()) return@LaunchedEffect

                delay(500)
                viewModel.search(currentSearchTerm)
            }

            LaunchedEffect(key1 = shouldStatePaginate.value) {
                if (shouldStatePaginate.value) viewModel.inputs.nextPage()
            }
        }

        viewModel.outputs.startProjectActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startProjectActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startPreLaunchProjectActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startPreLaunchProjectActivity(it.first, it.second) }
            .addToDisposable(disposables)
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

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
