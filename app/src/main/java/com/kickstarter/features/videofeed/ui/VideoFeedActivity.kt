package com.kickstarter.features.videofeed.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
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
                val projectsList = videoFeedUIState.value.projects

                val context = LocalContext.current
                val pool = remember { VideoPlayerPool(context) }
                DisposableEffect(pool) { onDispose { pool.releaseAll() } }

                val pagerState = rememberPagerState(pageCount = { projectsList.size })

                // Proactively buffer current±2 — outside the composition window — so the user
                // can scroll back two pages instantly without waiting for re-buffering.
                LaunchedEffect(pagerState.currentPage) {
                    val current = pagerState.currentPage
                    projectsList.getOrNull(current + 2)?.video()?.hls()?.takeIf { it.isNotEmpty() }?.let { url ->
                        pool.preload(current + 2, url, current)
                    }
                    projectsList.getOrNull(current - 2)?.video()?.hls()?.takeIf { it.isNotEmpty() }?.let { url ->
                        pool.preload(current - 2, url, current)
                    }
                }

                VerticalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    key = { index -> projectsList[index].id() }
                ) { page ->

                    val project = projectsList[page]
                    val videoUrl = project.video()?.hls() ?: ""
                    val profileImage = project.creator()?.avatar()?.medium() ?: ""
                    val projectTitle = project.name()
                    val percentageFounded = project.percentageFunded()

                    val player = remember(page, videoUrl) {
                        pool.getPlayer(page, videoUrl, pagerState.currentPage)
                    }

                    KSVideoPlayer(
                        videoUrl = videoUrl,
                        player = player,
                        isActive = pagerState.currentPage == page,
                        overlayContent = { hazeState ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                KSVideoActionsColumn(
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(end = dimensions.paddingMediumLarge),
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
                                    progress = percentageFounded
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
