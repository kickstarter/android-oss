package com.kickstarter.features.pledgedprojectsoverview.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.ui.compose.WebViewScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.viewmodels.BackingDetailsViewModel

class BackingDetailsActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: BackingDetailsViewModel.Factory
    private val viewModel: BackingDetailsViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = BackingDetailsViewModel.Factory(env, intent = intent)
            setContent {
                KickstarterApp(
                    useDarkTheme = this.isDarkModeEnabled(env = env),
                ) {
                    val urlState by viewModel.url.collectAsStateWithLifecycle()
                    WebViewScreen(
                        onBackButtonClicked = { finishWithAnimation() },
                        toolbarTitle = stringResource(id = R.string.backing_details_fpo),
                        url = urlState
                    )
                }
            }
        }
    }
}
