package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

class ReportProjectCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopToolBar(
                            title = stringResource(id = R.string.FPO_report_project_title)
                        )
                    },
                    content = { paddingValue ->

                        var shouldNavigate by rememberSaveable { mutableStateOf(false) }
                        if (!shouldNavigate)
                            ReportProjectCategoryScreen(padding = paddingValue, { shouldNavigate = true })
                        else
                            FormularyScreen()
                    }
                )
            }
        }
    }
}
