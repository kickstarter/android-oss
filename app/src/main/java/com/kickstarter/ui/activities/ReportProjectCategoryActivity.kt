package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import com.kickstarter.ui.activities.compose.ReportProjectCategoryScreen

class ReportProjectCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ReportProjectCategoryScreen()
            }
        }
    }
}
