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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.ui.activities.compose.FormularyScreen
import com.kickstarter.ui.activities.compose.ReportProjectCategoryScreen
import com.kickstarter.ui.compose.TopToolBar
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.viewmodels.ReportProjectViewModel

class ReportProjectActivity : ComponentActivity() {

    private lateinit var viewModelFactory: ReportProjectViewModel.Factory
    private val viewModel: ReportProjectViewModel.ReportProjectViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                            title = stringResource(id = R.string.FPO_report_project_title),
                            leftOnClickAction = onBack
                        )
                    },
                    content = { paddingValue ->

                        if (!shouldNavigate)
                            ReportProjectCategoryScreen(
                                padding = paddingValue,
                                navigationAction = { shouldNavigate = true }
                            )
                        else
                            FormularyScreen()
                    }
                )
            }
        }
    }
}
