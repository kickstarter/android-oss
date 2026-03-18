package com.kickstarter.features.videofeed.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.ui.components.KSVideoActionsColumn
import com.kickstarter.features.videofeed.ui.components.KSVideoBadgesRow
import com.kickstarter.features.videofeed.ui.components.KSVideoCampaignCard
import com.kickstarter.features.videofeed.viewmodel.VideoFeedViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer
import kotlin.getValue

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

        setContent {
            KSTheme {
                // TODO: In future tickets this hardcoded list will be substituted by the result of a query
                val badges = listOf(
                    KSVideoBadgeType.ProjectWeLove,
                    KSVideoBadgeType.DaysLeft("3 days left"),
                    KSVideoBadgeType.JustLaunched,
                    KSVideoBadgeType.Trending
                )

                // TODO: Following vm lines are for qa/demo purposes as of now!!! aiming here to avoid videoURL expiration real VM still to come
                val videoFeedUIState = viewModel.videoFeedUIState.collectAsStateWithLifecycle()
                val videoUrl = videoFeedUIState.value.project?.video()?.hls() ?: ""
                val profileImage = videoFeedUIState.value.project?.creator()?.avatar()?.medium() ?: ""
                val projectTitle = videoFeedUIState.value.project?.name() ?: "Ringo Move - The Ultimate Workout Bottle"
                val isBacked = videoFeedUIState.value.project?.isBacking() ?: true

                KSVideoPlayer(
                    videoUrl = videoUrl,
                    isActive = true,
                    overlayContent = { hazeState ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            KSVideoActionsColumn(
                                modifier = Modifier.align(Alignment.End),
                                profileImageUrl = profileImage,
                                bookmarkCount = "1k",
                                shareCount = "50",
                                onProfileClick = { },
                                onBookmarkClick = { },
                                onShareClick = { },
                                onMoreOptionsClick = { }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            KSVideoBadgesRow(
                                badges = badges,
                                hazeState = hazeState
                            )

                            KSVideoCampaignCard(
                                title = projectTitle,
                                subtitle = "$50,134 pledged • Join 431 backers",
                                buttonText = "Back this project",
                                onButtonClick = { },
                                isBacked = isBacked,
                                hazeState = hazeState
                            )
                        }
                    }
                )
            }
        }
    }
}
