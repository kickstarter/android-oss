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
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import kotlinx.coroutines.launch
import com.kickstarter.ui.activities.compose.search.SearchAndFilterScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp

class SearchAndFilterActivity : ComponentActivity() {

    lateinit var viewModelFactory: SearchAndFilterViewModel.Factory
    lateinit var filterMenuViewModelFactory: FilterMenuViewModel.Factory
    val viewModel: SearchAndFilterViewModel by viewModels { viewModelFactory }
    val filterMenuViewModel: FilterMenuViewModel by viewModels { filterMenuViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpConnectivityStatusCheck(lifecycle)

        this.getEnvironment()?.let { env ->

            viewModelFactory = SearchAndFilterViewModel.Factory(env)
            filterMenuViewModelFactory = FilterMenuViewModel.Factory(env)

            setContent {
                KickstarterApp(useDarkTheme = true) {
                    SearchAndFilterScreen(
                        env = env,
                        sfVm = viewModel,
                        fmVm = filterMenuViewModel,
                        intent = intent
                    )
                }
            }
        }
    }

    @Composable
    fun SetUpErrorActions(
        snackbarHostState: SnackbarHostState,
        sfVm: SearchAndFilterViewModel,
        fmVm: FilterMenuViewModel
    ) {
        val errorAction = { message: String? ->
            lifecycleScope.launch { // TODO: store SnackbarResult on VM to consult it before showing new  one, in case there is multiple enqueue snackbars.
                snackbarHostState.showSnackbar(
                    message = message ?: getString(R.string.Something_went_wrong_please_try_again),
                    actionLabel = KSSnackbarTypes.KS_ERROR.name,
                    duration = SnackbarDuration.Long
                )
            }
        }
        sfVm.provideErrorAction { message ->
            errorAction.invoke(message)
        }

        fmVm.provideErrorAction { message ->
            errorAction.invoke(message)
        }
    }

    fun startPreLaunchProjectActivity(project: Project, refTag: RefTag) {
        val intent = Intent().getPreLaunchProjectActivity(this, project.slug())
            .putExtra(IntentKey.REF_TAG, refTag)
            .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.SEARCH.value)
        startActivity(intent)
        TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
    }

    fun startProjectActivity(projectAndRefTagAndIsFfEnabled: Pair<Project, RefTag>) {
        val intent = Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT, projectAndRefTagAndIsFfEnabled.first)
            .putExtra(IntentKey.REF_TAG, projectAndRefTagAndIsFfEnabled.second)
            .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.SEARCH.value)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
