package com.kickstarter.ui.activities

import OnboardingScreen
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.ui.compose.designsystem.KickstarterApp

class OnboardingFlowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KickstarterApp {
                OnboardingScreen()
            }
        }
    }
}
