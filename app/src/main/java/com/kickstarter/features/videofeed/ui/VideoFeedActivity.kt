package com.kickstarter.features.videofeed.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.features.videofeed.viewmodel.VideoFeedViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.KSTheme

class VideoFeedActivity : ComponentActivity() {

    private lateinit var videoFeedFactory: VideoFeedViewModel.Factory
    private val viewModel: VideoFeedViewModel by viewModels { videoFeedFactory }
    private lateinit var env: Environment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let {
            env = it
            videoFeedFactory = VideoFeedViewModel.Factory(env)
        }

        viewModel.provideErrorAction { message ->
            // TODO: surface error to the user (snackbar / error state)
        }

        setContent {
            KSTheme {
                val uiState by viewModel.videoFeedUIState.collectAsStateWithLifecycle()
                VideoFeedScreen(
                    items = uiState.items,
                    onClose = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}
