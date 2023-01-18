package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.ui.compose.TopToolBar

class ReportProjectCategoryActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopToolBar(
                            title = stringResource(id = R.string.FPO_report_project_title),
                            leftOnClickAction = { finish() }
                        )
                    },
                    content = { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(paddingValues = innerPadding)
                                .background(color = Color.Green)
                        ) {
                            Text(text = "Some Text")
                        }
                    }
                )
            }
        }
    }
}
