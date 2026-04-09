package com.kickstarter.features.videofeed.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kickstarter.R
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
    VIDEO_FEED_OVERLAY_CONTAINER,
    VIDEO_FEED_CLOSE_BUTTON
}

@Composable
fun VideoFeedScreen(
    projectsList: List<Project>,
    onClose: () -> Unit = {}
) {
    // TODO: In future tickets this hardcoded list will be substituted by the result of a query
    val badges = listOf(
        KSVideoBadgeType.ProjectWeLove,
        KSVideoBadgeType.DaysLeft("3 days left"),
        KSVideoBadgeType.JustLaunched,
        KSVideoBadgeType.Trending
    )

    val pagerState = rememberPagerState(pageCount = { projectsList.size })

    Box(modifier = Modifier.fillMaxSize()) {
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
        // Derive progress per-page: only recomposes this page when its own settled state flips
        val percentageFounded by remember(page) {
            derivedStateOf {
                if (pagerState.settledPage == page) project.percentageFunded() else 0f
            }
        }

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

        Image(
            painter = painterResource(id = R.drawable.close),
            contentDescription = stringResource(id = R.string.accessibility_discovery_buttons_close),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 66.dp)
                .size(40.dp)
                .dropShadow(
                    shape = CircleShape,
                    shadow = Shadow(
                        radius = dimensions.videoPlayerShadowBlur,
                        color = KSTheme.colors.videoPlayerIconShadow,
                        offset = DpOffset.Zero
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose,
                    onClickLabel = stringResource(id = R.string.accessibility_discovery_buttons_close),
                    role = Role.Button
                )
                .semantics {
                    role = Role.Button
                }
                .testTag(VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name)
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
