package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.activities.compose.FormularyScreen
import com.kickstarter.ui.activities.compose.ReportProjectCategoryScreen
import com.kickstarter.ui.toolbars.compose.TopToolBar
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.viewmodels.ReportProjectViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ReportProjectActivity : ComponentActivity() {

    private lateinit var viewModelFactory: ReportProjectViewModel.Factory
    private val viewModel: ReportProjectViewModel.ReportProjectViewModel by viewModels { viewModelFactory }
    private var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = ReportProjectViewModel.Factory(env, arguments = intent.extras)
        }

        disposables.add(
            this.viewModel.outputs.openExternalBrowserWithUrl()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    ApplicationUtils.openUrlExternally(this, it)
                }
        )

        setContent {
            MaterialTheme {
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
                                outputs = viewModel.outputs,
                                inputs = viewModel.inputs,
                                callback = {
                                    finishWithAnimation(finishResult)
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
