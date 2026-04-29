package com.kickstarter.features.videofeed.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.features.videofeed.viewmodel.VideoFeedViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.data.LoginReason
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

        this.getEnvironment()?.let {
            env = it
            videoFeedFactory = VideoFeedViewModel.Factory(env)
        }

        setContent {
            KSTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val errorAction = setUpVideoFeedErrorActions(snackbarHostState)
                viewModel.provideErrorAction { message -> errorAction.invoke(message) }

                viewModel.loadVideoFeed()

                val uiState by viewModel.videoFeedUIState.collectAsStateWithLifecycle()
                VideoFeedScreen(
                    items = uiState.items,
                    errorSnackBarHostState = snackbarHostState,
                    onLoadMore = { viewModel.loadVideoFeed() },
                    onClose = { onBackPressedDispatcher.onBackPressed() },
                    onProfileClick = { project ->
                        startCreatorBioWebViewActivity(project)
                    },
                    onBookmarkClick = { project, index ->
                        if (viewModel.isUserLoggedIn.value) {
                            viewModel.bookmarkProject(project, index)
                        } else {
                            // - Execute bookmark after returning back from login successful
                            pendingBookmark = Pair(project, index)
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
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.STAR_PROJECT)
        loginLauncher.launch(intent)
    }
}
