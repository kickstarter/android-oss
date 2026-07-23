package com.kickstarter.features.videofeed.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.features.videofeed.viewmodel.VideoFeedViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventContextValues.CtaContextName
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.showRatingDialogWidget
import com.kickstarter.ui.extensions.startCreatorBioWebViewActivity
import com.kickstarter.ui.extensions.startPreLaunchProjectActivity
import com.kickstarter.ui.extensions.startProjectActivity

class VideoFeedActivity : ComponentActivity() {

    private lateinit var videoFeedFactory: VideoFeedViewModel.Factory
    private val viewModel: VideoFeedViewModel by viewModels { videoFeedFactory }
    private lateinit var env: Environment
    private var pendingBookmark: Pair<Project, Int>? = null

    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingBookmark?.let { (project, index) -> viewModel.bookmarkProject(project, index) }
        }
        pendingBookmark = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        this.getEnvironment()?.let {
            env = it
            val entrySurface = intent.getStringExtra(IntentKey.PREVIOUS_SCREEN) ?: ""
            videoFeedFactory = VideoFeedViewModel.Factory(env, entrySurface)
        }

        setContent {
            KSTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val errorAction = setUpVideoFeedErrorActions(snackbarHostState)
                viewModel.provideErrorAction { message -> errorAction.invoke(message) }

                val uiState by viewModel.videoFeedUIState.collectAsStateWithLifecycle()
                VideoFeedScreen(
                    items = uiState.items,
                    environment = env,
                    errorSnackBarHostState = snackbarHostState,
                    hasMore = uiState.hasMore,
                    isLoading = uiState.isLoading,
                    onLoadMore = {
                        viewModel.loadVideoFeed()
                    },
                    onReachedLastVideo = {
                        showRatingDialogWidget()
                    },
                    onClose = { onBackPressedDispatcher.onBackPressed() },
                    onProfileClick = { project ->
                        viewModel.onCTAClicked(project, CtaContextName.VIDEO_CREATOR)
                        startCreatorBioWebViewActivity(project)
                    },
                    onBookmarkClick = { project, index ->
                        viewModel.onCTAClicked(project, CtaContextName.VIDEO_SAVE)
                        if (viewModel.isUserLoggedIn.value) {
                            viewModel.bookmarkProject(project, index)
                        } else {
                            // - Execute bookmark after returning back from login successful
                            pendingBookmark = Pair(project, index)
                            startLoginToutActivity()
                        }
                    },
                    onShareIntentReady = { intent -> startActivity(intent) },
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
                    },
                    onVideoImpression = { videoFeedItem, position ->
                        viewModel.onVideoImpression(videoFeedItem, position)
                    },
                    onVideoPageSettled = { videoFeedItem, toPosition, fromVideoFeedItem, watchTimeMs, videoDurationMs ->
                        viewModel.onVideoPageSettled(videoFeedItem, toPosition, fromVideoFeedItem, watchTimeMs, videoDurationMs)
                    },
                    onPlayPauseTap = { project, isPlaying ->
                        val cta = if (isPlaying) CtaContextName.VIDEO_PLAY else CtaContextName.VIDEO_PAUSE
                        viewModel.onCTAClicked(project, cta)
                    },
                    onProgressBarTap = { videoFeedItem, progress ->
                        viewModel.onProgressBarTapped(videoFeedItem, progress)
                    },
                    onShareCTAClick = { project ->
                        viewModel.onCTAClicked(project, CtaContextName.VIDEO_SHARE)
                    },
                    onVideoPlaybackError = { videoFeedItem, position, error, isActive ->
                        viewModel.onVideoPlaybackError(videoFeedItem, position, error, isActive)
                    }
                )
            }
        }
    }

    private fun startLoginToutActivity() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.STAR_PROJECT)
        loginLauncher.launch(intent)
    }
}
