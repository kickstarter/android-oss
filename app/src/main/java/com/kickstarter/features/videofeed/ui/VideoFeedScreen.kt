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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.toCompactFormat
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHeadsupSnackbar
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Close
import kotlinx.coroutines.launch

enum class VideoFeedScreenTestTag {
    VIDEO_FEED_PAGER,
    VIDEO_FEED_OVERLAY_CONTAINER,
    VIDEO_FEED_CLOSE_BUTTON
}

@Composable
fun VideoFeedScreen(
    items: List<VideoFeedItem>,
    errorSnackBarHostState: SnackbarHostState = SnackbarHostState(),
    onLoadMore: () -> Unit = {},
    onClose: () -> Unit = {},
    onProfileClick: (project: Project) -> Unit = { _ -> },
    onBookmarkClick: (project: Project, index: Int) -> Unit = { _, _ -> },
    preLaunchedCallback: (project: Project, refTag: RefTag) -> Unit = { _, _ -> },
    projectCallback: (project: Project, refTag: RefTag) -> Unit = { _, _ -> },
    onVideoImpression: (project: Project, position: Int) -> Unit = { _, _ -> },
    onVideoPageSettled: (toProject: Project, toPosition: Int, fromProject: Project, watchTimeMs: Long?, videoDurationMs: Long?) -> Unit = { _, _, _, _, _ -> },
    onPlayPauseTap: (project: Project, isPlaying: Boolean) -> Unit = { _, _ -> },
    onProgressBarTap: (project: Project, progress: Float) -> Unit = { _, _ -> },
    onShareCTAClick: (project: Project) -> Unit = { _ -> }
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    var previousSettledPage by remember { mutableStateOf(-1) }
    // Stores (watchTimeMs, videoDurationMs) per page index as each player deactivates.
    // Written by KSVideoPlayer.onBecameInactive during the swipe animation; read when
    // settledPage fires after the animation completes, so the data is always ready.
    val watchTimeByPage = remember { mutableMapOf<Int, Pair<Long, Long>>() }

    // - Threshold: items.size - (beyondViewportPageCount + 2)
    // Triggers before the pager pre-renders the last page, keeping at least one rendered while the next page loads.
    LaunchedEffect(pagerState.currentPage, items.size) {
        if (items.isNotEmpty() && pagerState.currentPage >= items.size - 3) {
            onLoadMore()
        }
    }

    // - Impression fires on every settled page, including first load.
    // - Page-viewed (swipe + watch data) fires only when navigating from a previous video.
    LaunchedEffect(pagerState.settledPage, items.size) {
        val currentPage = pagerState.settledPage
        if (items.isEmpty() || currentPage >= items.size) return@LaunchedEffect

        onVideoImpression(items[currentPage].project, currentPage)

        if (previousSettledPage in items.indices && previousSettledPage != currentPage) {
            val watchData = watchTimeByPage.remove(previousSettledPage)
            onVideoPageSettled(
                items[currentPage].project,
                currentPage,
                items[previousSettledPage].project,
                watchData?.first,
                watchData?.second
            )
        }

        previousSettledPage = currentPage
    }

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
            val bookmarkCount = remember(project) { project.watchesCount().toCompactFormat() }
            val shareCount = remember(project) { project.sharesCount().toCompactFormat() }
            val subtitle = remember(project) {
                val pledged = "${project.currencySymbol()}${NumberUtils.format(project.pledged().toInt())}"
                val backers = NumberUtils.format(project.backersCount())
                "$pledged pledged • Join $backers backers"
            }
            val percentageFounded by remember(page) {
                derivedStateOf {
                    if (pagerState.settledPage == page) project.percentageFunded() else 0f
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                KSVideoPlayer(
                    videoUrl = videoUrl,
                    isActive = pagerState.currentPage == page,
                    onPlayPauseToggle = { isPlaying -> onPlayPauseTap(project, isPlaying) },
                    onProgressBarInteraction = { currentProgress -> onProgressBarTap(project, currentProgress) },
                    onBecameInactive = { watchTimeMs, videoDurationMs ->
                        watchTimeByPage[page] = Pair(watchTimeMs, videoDurationMs)
                    },
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
                                bookmarkCount = bookmarkCount,
                                isBookmarked = project.isStarred(),
                                shareCount = shareCount,
                                onProfileClick = { onProfileClick(project) },
                                onBookmarkClick = { onBookmarkClick(project, page) },
                                onShareClick = { onShareCTAClick(project) },
                                onMoreOptionsClick = {} // - Hiden for phase 1 of VideoFeed
                            )

                            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

                            KSVideoBadgesRow(
                                badges = item.badges,
                                hazeState = hazeState
                            )

                            KSVideoCampaignCard(
                                title = projectTitle,
                                subtitle = subtitle,
                                buttonText = stringResource(R.string.project_back_button),
                                onButtonClick = {
                                    val refTag = RefTag.videoFeed()
                                    if (project.displayPrelaunch().isTrue()) {
                                        preLaunchedCallback(project, refTag)
                                    } else {
                                        projectCallback(project, refTag)
                                    }
                                },
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

        SnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            hostState = errorSnackBarHostState,
            snackbar = { data ->
                if (data.visuals.actionLabel == KSSnackbarTypes.KS_ERROR.name) {
                    KSErrorSnackbar(text = data.visuals.message)
                } else {
                    KSHeadsupSnackbar(text = data.visuals.message)
                }
            }
        )
    }
}

@Composable
fun setUpVideoFeedErrorActions(snackbarHostState: SnackbarHostState): (String?) -> Unit {
    val scope = rememberCoroutineScope()
    val defaultErrorMessage = stringResource(R.string.Something_went_wrong_please_try_again)
    return { message: String? ->
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message ?: defaultErrorMessage,
                actionLabel = KSSnackbarTypes.KS_ERROR.name,
                duration = SnackbarDuration.Long
            )
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
