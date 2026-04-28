package com.kickstarter.features.videofeed.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.features.videofeed.viewmodel.VideoFeedViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.ui.extensions.startCreatorBioWebViewActivity
import com.kickstarter.ui.extensions.startPreLaunchProjectActivity
import com.kickstarter.ui.extensions.startProjectActivity

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
                    onClose = { onBackPressedDispatcher.onBackPressed() },
                    onProfileClick = { project ->
                        startCreatorBioWebViewActivity(project)
                    },
                    onBookmarkClick = { project ->
                        if (viewModel.isUserLoggedIn.value) {
                            viewModel.bookmarkProject(project)
                        } else {
                            startLoginToutActivity()
                        }
                    },
                    preLaunchedCallback = { project, refTag ->
                        startPreLaunchProjectActivity(
                            project = project,
                            previousScreen = ThirdPartyEventValues.ScreenName.DISCOVERY.value,
                            refTag = refTag
                        )
                    },
                    projectCallback = { project, refTag ->
                        startProjectActivity(
                            project = project,
                            refTag = refTag,
                            previousScreen = ThirdPartyEventValues.ScreenName.DISCOVERY.value
                        )
                    }
                )
            }
        }
    }

    private fun startLoginToutActivity() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
