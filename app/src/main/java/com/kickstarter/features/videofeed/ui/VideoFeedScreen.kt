package com.kickstarter.features.videofeed.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.ui.components.KSVideoActionsColumn
import com.kickstarter.features.videofeed.ui.components.KSVideoBadgesRow
import com.kickstarter.features.videofeed.ui.components.KSVideoCampaignCard
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer

enum class VideoFeedScreenTestTag {
    VIDEO_FEED_PAGER,
    VIDEO_FEED_OVERLAY_CONTAINER
}

@Composable
fun VideoFeedScreen(
    projectsList: List<Project>
) {
    // TODO: In future tickets this hardcoded list will be substituted by the result of a query
    val badges = listOf(
        KSVideoBadgeType.ProjectWeLove,
        KSVideoBadgeType.DaysLeft("3 days left"),
        KSVideoBadgeType.JustLaunched,
        KSVideoBadgeType.Trending
    )

    val pagerState = rememberPagerState(pageCount = { projectsList.size })

    VerticalPager(
        modifier = Modifier
            .fillMaxSize()
            .testTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name),
        state = pagerState,
        beyondViewportPageCount = 1,
        key = { index -> projectsList[index].id() }
    ) { page ->

        val project = projectsList[page]
        val videoUrl = project.video()?.hls() ?: ""
        val profileImage = project.creator().avatar().medium()
        val projectTitle = project.name()
        val percentageFounded = project.percentageFunded()

        KSVideoPlayer(
            videoUrl = videoUrl,
            isActive = pagerState.currentPage == page,
            overlayContent = { hazeState ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_${project.id()}")
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

@Preview
@Composable
fun VideoFeedScreenPreview() {
    KSTheme {
        VideoFeedScreen(
            projectsList = listOf(
                ProjectFactory.project(),
                ProjectFactory.caProject(),
                ProjectFactory.ukProject()
            )
        )
    }
}
