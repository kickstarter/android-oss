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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.kickstarter.R
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.features.videofeed.ui.components.KSVideoActionsColumn
import com.kickstarter.features.videofeed.ui.components.KSVideoBadgesRow
import com.kickstarter.features.videofeed.ui.components.KSVideoCampaignCard
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Close

enum class VideoFeedScreenTestTag {
    VIDEO_FEED_PAGER,
    VIDEO_FEED_OVERLAY_CONTAINER,
    VIDEO_FEED_CLOSE_BUTTON
}

@Composable
fun VideoFeedScreen(
    items: List<VideoFeedItem>,
    onClose: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            modifier = Modifier
                .fillMaxSize()
                .testTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name),
            state = pagerState,
            beyondViewportPageCount = 1,
            key = { index -> items[index].project.id() }
        ) { page ->

            val item = items[page]
            val project = item.project
            val videoUrl = item.hlsUrl ?: ""
            val profileImage = project.creator()?.avatar()?.medium() ?: ""
            val projectTitle = project.name()
            val percentageFounded by remember(page) {
                derivedStateOf {
                    if (pagerState.settledPage == page) project.percentageFunded() else 0f
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
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

                            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

                            KSVideoBadgesRow(
                                badges = item.badges,
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

                Image(
                    imageVector = Close,
                    contentDescription = stringResource(id = R.string.accessibility_discovery_buttons_close),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = dimensions.paddingMediumSmall, top = dimensions.videoFeedCloseButtonTopPadding)
                        .size(dimensions.videoFeedCloseButtonSize)
                        .dropShadow(
                            shape = CircleShape,
                            shadow = Shadow(
                                radius = dimensions.videoPlayerShadowBlur,
                                color = KSTheme.colors.videoPlayer.iconShadow,
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
                        .testTag("${VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name}_${project.id()}")
                )
            }
        }
    }
}

@Preview
@Composable
fun VideoFeedScreenPreview() {
    KSTheme {
        VideoFeedScreen(
            items = listOf(
                VideoFeedItem(
                    badges = listOf(KSVideoBadgeType.ProjectWeLove, KSVideoBadgeType.DaysLeft("3 days left")),
                    project = ProjectFactory.project(),
                    hlsUrl = null
                ),
                VideoFeedItem(
                    badges = listOf(KSVideoBadgeType.JustLaunched),
                    project = ProjectFactory.caProject(),
                    hlsUrl = null
                ),
                VideoFeedItem(
                    badges = listOf(KSVideoBadgeType.Trending),
                    project = ProjectFactory.ukProject(),
                    hlsUrl = null
                )
            )
        )
    }
}
