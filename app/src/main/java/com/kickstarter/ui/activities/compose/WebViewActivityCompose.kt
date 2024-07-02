package com.kickstarter.ui.activities.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.compose.WebViewScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.finishWithAnimation

class WebViewActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbarTitle = intent.getStringExtra(IntentKey.TOOLBAR_TITLE)
        val url = intent.getStringExtra(IntentKey.URL)

        getEnvironment()?.let { env ->
            setContent {
                KickstarterApp(
                    useDarkTheme = this.isDarkModeEnabled(env = env),
                ) {
                    WebViewScreen(
                        onBackButtonClicked = { finishWithAnimation() },
                        toolbarTitle = toolbarTitle ?: "",
                        url = url
                    )
                }
            }
        }
    }
}