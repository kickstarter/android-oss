package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.FormularyScreen
import com.kickstarter.ui.activities.compose.ReportProjectCategoryScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.toolbars.compose.TopToolBar
import com.kickstarter.viewmodels.ReportProjectViewModel

class ReportProjectActivity : ComponentActivity() {

    private lateinit var viewModelFactory: ReportProjectViewModel.Factory
    private val viewModel: ReportProjectViewModel.ReportProjectViewModel by viewModels { viewModelFactory }
    private lateinit var environment: Environment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = ReportProjectViewModel.Factory(environment, arguments = intent.extras)
        }

        setContent {
            val darModeEnabled = this.isDarkModeEnabled(env = environment)
            KickstarterApp(useDarkTheme = darModeEnabled) {
                OpenExternalLink()

                var shouldNavigate by rememberSaveable { mutableStateOf(false) }
                val onBack = {
                    if (shouldNavigate)
                        shouldNavigate = false
                    else
                        finishWithAnimation()
                }

                // - Detect back gesture
                BackHandler {
                    onBack()
                }

                Scaffold(
                    modifier = Modifier.systemBarsPadding(),
                    topBar = {
                        TopToolBar(
                            title = stringResource(id = R.string.Report_this_project),
                            leftOnClickAction = onBack
                        )
                    },
                    content = { paddingValue ->
                        if (!shouldNavigate)
                            ReportProjectCategoryScreen(
                                padding = paddingValue,
                                navigationAction = {
                                    viewModel.inputs.kind(it)
                                    shouldNavigate = true
                                },
                                inputs = viewModel.inputs
                            )
                        else {
                            val finishResult = viewModel.outputs.finish().subscribeAsState(
                                initial =
                                ReportProjectViewModel.ReportProjectViewModel.NavigationResult(false, "")
                            ).value.flaggingKind

                            FormularyScreen(
                                padding = paddingValue,
                                outputs = viewModel.outputs,
                                inputs = viewModel.inputs,
                                callback = {
                                    finishWithAnimation(finishResult, IntentKey.FLAGGINGKIND)
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun OpenExternalLink() {
        val openUrl =
            this.viewModel.openExternalBrowserWithUrl().subscribeAsState(initial = "").value
        if (openUrl.isNotEmpty()) {
            ApplicationUtils.openUrlExternally(this, openUrl)
        }
    }
}
