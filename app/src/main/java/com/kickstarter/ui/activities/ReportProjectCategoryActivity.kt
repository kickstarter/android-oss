package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.ui.activities.compose.FormularyScreen
import com.kickstarter.ui.activities.compose.ReportProjectCategoryScreen
import com.kickstarter.ui.compose.TopToolBar

class ReportProjectCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var shouldNavigate by rememberSaveable { mutableStateOf(false) }

                // - Back gesture and icon navigation
                BackHandler {
                    if (shouldNavigate)
                        shouldNavigate = false
                    else {
                        finish()
                        TransitionUtils.transition(this, TransitionUtils.slideInFromLeft())
                    }
                }

                Scaffold(
                    topBar = {
                        TopToolBar(
                            title = stringResource(id = R.string.FPO_report_project_title),
                            leftOnClickAction = { shouldNavigate = false }
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
