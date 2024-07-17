package com.kickstarter.features.pledgedprojectsoverview.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.features.pledgedprojectsoverview.viewmodel.PledgedProjectsOverviewViewModel
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.AppThemes
import com.kickstarter.ui.activities.ProfileActivity
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.startCreatorMessageActivity
import com.kickstarter.ui.extensions.transition
import kotlinx.coroutines.launch

class PledgedProjectsOverviewActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: PledgedProjectsOverviewViewModel.Factory
    private val viewModel: PledgedProjectsOverviewViewModel by viewModels { viewModelFactory }
    private var theme = AppThemes.MATCH_SYSTEM.ordinal
    private var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getBooleanExtra(IntentKey.FIX_PAYMENT_SUCCESS, false)
            data?.let {
                if (it.isTrue()) {
                    viewModel.getPledgedProjects(
                        PledgedProjectsOverviewQueryData(
                            25, null, null, null
                        )
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            setContent {
                viewModelFactory = PledgedProjectsOverviewViewModel.Factory(env)

                theme = env.sharedPreferences()
                    ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                    ?: AppThemes.MATCH_SYSTEM.ordinal

                val ppoUIState by viewModel.ppoUIState.collectAsStateWithLifecycle()

                val lazyListState = rememberLazyListState()
                val snackbarHostState = remember { SnackbarHostState() }

                val ppoCardPagingSource = viewModel.ppoCardsState.collectAsLazyPagingItems()
                val totalAlerts = ppoUIState.totalAlerts
                val isLoading = ppoUIState.isLoading || !ppoCardPagingSource.loadState.isIdle
                val isErrored = ppoUIState.isErrored || ppoCardPagingSource.loadState.hasError

                KickstarterApp(
                    useDarkTheme = this.isDarkModeEnabled(env = env)
                ) {
                    PledgedProjectsOverviewScreen(
                        modifier = Modifier,
                        onBackPressed = { onBackPressedDispatcher.onBackPressed() },
                        lazyColumnListState = lazyListState,
                        errorSnackBarHostState = snackbarHostState,
                        ppoCards = ppoCardPagingSource,
                        totalAlerts = totalAlerts,
                        onAddressConfirmed = { viewModel.showSnackbarAndRefreshCardsList() },
                        onProjectPledgeSummaryClick = { url -> openBackingDetailsWebView(url) },
                        onSendMessageClick = { projectName -> viewModel.onMessageCreatorClicked(projectName) },
                        isLoading = isLoading,
                        isErrored = isErrored,
                        onSeeAllBackedProjectsClick = { startProfileActivity() },
                        pullRefreshCallback = {
                            // TODO call viewmodel.getPledgedProjects() here
                        },
                        onFixPaymentClick = { projectSlug -> openManagePledge(projectSlug, resultLauncher = startForResult) }
                    )
                }

                LaunchedEffect(Unit) {
                    viewModel.projectFlow
                        .collect {
                            startCreatorMessageActivity(project = it, previousScreen = MessagePreviousScreenType.PLEDGED_PROJECTS_OVERVIEW)
                        }
                }

                viewModel.provideSnackbarMessage {
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar(getString(it))
                    }
                }

                onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        finish()
                        this@PledgedProjectsOverviewActivity.transition(TransitionUtils.slideInFromLeft())
                    }
                })
            }
        }
    }

    private fun openBackingDetailsWebView(url: String) {
        val intent = Intent(this, BackingDetailsActivity::class.java)
            .putExtra(IntentKey.URL, url)
        startActivity(intent)
    }

    fun startProfileActivity() {
        startActivity(
            Intent(this, ProfileActivity::class.java)
        )
        this.let {
            TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
        }
    }

    private fun openManagePledge(projectSlug: String, resultLauncher: ActivityResultLauncher<Intent>) {
        resultLauncher.launch(
            Intent().getProjectIntent(this)
                .putExtra(IntentKey.PROJECT_PARAM, projectSlug)
                .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
                .putExtra(IntentKey.REF_TAG, RefTag.activity())
        )
        this.let {
            TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
        }
    }
}
