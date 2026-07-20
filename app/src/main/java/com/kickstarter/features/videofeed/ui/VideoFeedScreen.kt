package com.kickstarter.features.videofeed.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.PlaybackException
import com.kickstarter.R
import com.kickstarter.features.socialshare.AndroidSocialShareService
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.ui.LocalSocialShareViewModel
import com.kickstarter.features.socialshare.ui.SocialShareSheet
import com.kickstarter.features.socialshare.viewmodel.SocialShareViewModel
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.features.videofeed.ui.components.KSVideoActionsColumn
import com.kickstarter.features.videofeed.ui.components.KSVideoBadgesRow
import com.kickstarter.features.videofeed.ui.components.KSVideoCampaignCard
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.toCompactFormat
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSVideoFeedSnackbar
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Close
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Collapse
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Expand
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

enum class VideoFeedScreenTestTag {
    VIDEO_FEED_PAGER,
    VIDEO_FEED_OVERLAY_CONTAINER,
    VIDEO_FEED_CLOSE_BUTTON,
    VIDEO_FEED_HIDE_UI_BUTTON,
    VIDEO_FEED_LOADING_PAGE
}

// Stable pager key for the trailing pagination loading page. Project ids are positive,
// so this sentinel never collides with a real item key.
private const val LOADING_PAGE_KEY = Long.MIN_VALUE

@Composable
fun VideoFeedScreen(
    items: List<VideoFeedItem>,
    environment: Environment,
    errorSnackBarHostState: SnackbarHostState = SnackbarHostState(),
    hasMore: Boolean = true,
    isLoading: Boolean = false,
    onLoadMore: () -> Unit = {},
    onReachedLastVideo: () -> Unit = {},
    onClose: () -> Unit = {},
    onProfileClick: (project: Project) -> Unit = { _ -> },
    onBookmarkClick: (project: Project, index: Int) -> Unit = { _, _ -> },
    onShareIntentReady: (Intent) -> Unit = {},
    preLaunchedCallback: (project: Project, refTag: RefTag) -> Unit = { _, _ -> },
    projectCallback: (project: Project, refTag: RefTag) -> Unit = { _, _ -> },
    onVideoImpression: (item: VideoFeedItem, position: Int) -> Unit = { _, _ -> },
    onVideoPageSettled: (videoFeedItem: VideoFeedItem, toPosition: Int, fromVideoFeedItem: VideoFeedItem, watchTimeMs: Long?, videoDurationMs: Long?) -> Unit = { _, _, _, _, _ -> },
    onPlayPauseTap: (project: Project, isPlaying: Boolean) -> Unit = { _, _ -> },
    onProgressBarTap: (item: VideoFeedItem, progress: Float) -> Unit = { _, _ -> },
    onShareCTAClick: (project: Project) -> Unit = { _ -> },
    onVideoPlaybackError: (item: VideoFeedItem, position: Int, error: PlaybackException, isActive: Boolean) -> Unit = { _, _, _, _ -> }
) {
    // Append a trailing loading page while the next page is being fetched. The prefetch in the
    // pagination LaunchedEffect usually completes before the user reaches the end, so this is only
    // seen when the user out-swipes the request (or during the very first load).
    val showLoadingPage = isLoading && hasMore
    val pagerState = rememberPagerState(pageCount = { items.size + if (showLoadingPage) 1 else 0 })

    var previousSettledPage by remember { mutableStateOf(-1) }
    var hasTriggeredReview by remember { mutableStateOf(false) }
    // Stores (watchTimeMs, videoDurationMs) per page index as each player deactivates.
    // Written by KSVideoPlayer.onBecameInactive during the swipe animation; read when
    // settledPage fires after the animation completes, so the data is always ready.
    val watchTimeByPage = remember { mutableMapOf<Int, Pair<Long, Long>>() }
    var shareData: SocialShareData? by remember { mutableStateOf(null) }

    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val iconTopPadding = maxOf(dimensions.videoFeedCloseButtonTopPadding, statusBarInset + dimensions.paddingSmall)

    // - Pagination:
    // Threshold: items.size - (beyondViewportPageCount + 2)
    // Triggers before the pager pre-renders the last page, keeping at least one rendered while the next page loads.
    LaunchedEffect(pagerState.currentPage, items.size) {
        if (items.isNotEmpty() && pagerState.currentPage >= items.size - 3) {
            onLoadMore()
        }
    }

    // - In-app review: fire once when the user settles on the last video and there are no more pages.
    LaunchedEffect(pagerState.settledPage, items.size, hasMore) {
        if (!hasTriggeredReview && items.isNotEmpty() && !hasMore && pagerState.settledPage == items.size - 1) {
//            Timber.d("VideoFeedScreen: ratingDialog triggering — user reached last video (page=${pagerState.settledPage})")
            hasTriggeredReview = true
            onReachedLastVideo()
        }
    }

    // - Analytics:
    // - Impression fires on every settled page, including first load.
    // - Page-viewed (swipe + watch data) fires only when navigating from a previous video.
    LaunchedEffect(pagerState.settledPage, items.isEmpty()) {
        val currentPage = pagerState.settledPage
        if (items.isEmpty() || currentPage >= items.size) return@LaunchedEffect

        onVideoImpression(items[currentPage], currentPage)

        if (previousSettledPage in items.indices && previousSettledPage != currentPage) {
            val watchData = watchTimeByPage.remove(previousSettledPage)
            onVideoPageSettled(
                items[currentPage],
                currentPage,
                items[previousSettledPage],
                watchData?.first,
                watchData?.second
            )
        }

        previousSettledPage = currentPage
    }

    val screenHazeState = rememberHazeState()

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = screenHazeState)
                .testTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name),
            state = pagerState,
            beyondViewportPageCount = 1,
            key = { index -> if (index < items.size) items[index].project.id() else LOADING_PAGE_KEY }
        ) { page ->

            if (page >= items.size) {
                VideoFeedLoadingPage()
                return@VerticalPager
            }

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
                    if (pagerState.settledPage == page) project.percentFunded()?.toFloat() ?: 0f else 0f
                }
            }

            // HideUI mode is per-page and defaults to off, so every video starts with the chrome
            // visible. remember(page) only re-initialises when this page is disposed and recomposed
            // (2+ pages away, given beyondViewportPageCount = 1); the LaunchedEffect additionally
            // resets it the moment this page stops being current, so an adjacent page that stays
            // composed is still reset when swiped back to.
            var hideUi by remember(page) { mutableStateOf(false) }
            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage != page) hideUi = false
            }

            Box(modifier = Modifier.fillMaxSize()) {
                KSVideoPlayer(
                    videoUrl = videoUrl,
                    isActive = pagerState.currentPage == page,
                    hideUi = hideUi,
                    previewImageUrl = item.previewImageUrl,
                    onPlayPauseToggle = { isPlaying -> onPlayPauseTap(project, isPlaying) },
                    onProgressBarInteraction = { currentProgress -> onProgressBarTap(item, currentProgress) },
                    onBecameInactive = { watchTimeMs, videoDurationMs ->
                        watchTimeByPage[page] = Pair(watchTimeMs, videoDurationMs)
                    },
                    onPlaybackError = { error, active -> onVideoPlaybackError(item, page, error, active) },
                    overlayContent = { hazeState ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_${project.id()}")
                        ) {
                            // The action rail and badges fade out entirely in HideUI mode; the
                            // campaign card keeps its CTA button but hides its details (see below).
                            // Seed the transition from the current value so it doesn't animate in on
                            // first composition (only on an actual toggle).
                            val chromeVisible = remember { MutableTransitionState(!hideUi) }
                            chromeVisible.targetState = !hideUi
                            AnimatedVisibility(
                                visibleState = chromeVisible,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
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
                                        onShareClick = {
                                            shareData = SocialShareData(
                                                projectName = project.name() ?: "",
                                                projectUrl = project.urls()?.web()?.project() ?: "",
                                                imageUrl = project.photo()?.full() ?: "",
                                                creatorName = project.creator()?.name() ?: ""
                                            )
                                            onShareCTAClick(project)
                                        },
                                        onMoreOptionsClick = {} // - Hiden for phase 1 of VideoFeed
                                    )

                                    Spacer(modifier = Modifier.height(dimensions.paddingLarge))

                                    KSVideoBadgesRow(
                                        badges = item.badges,
                                        hazeState = hazeState
                                    )
                                }
                            }

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
                                progress = percentageFounded,
                                detailsVisible = !hideUi
                            )
                        }
                    }
                )

                Image(
                    imageVector = Close,
                    contentDescription = stringResource(id = R.string.accessibility_discovery_buttons_close),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = dimensions.paddingMediumSmall, top = iconTopPadding)
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

                val hideUiLabel = stringResource(
                    id = if (hideUi) R.string.fpo_Show_details else R.string.fpo_Hide_details
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = dimensions.paddingMediumSmall, top = iconTopPadding)
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
                            onClick = { hideUi = !hideUi },
                            onClickLabel = hideUiLabel,
                            role = Role.Button
                        )
                        .semantics(mergeDescendants = true) { }
                        .testTag("${VideoFeedScreenTestTag.VIDEO_FEED_HIDE_UI_BUTTON.name}_${project.id()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = if (hideUi) Expand else Collapse,
                        contentDescription = hideUiLabel,
                        modifier = Modifier.size(dimensions.videoFeedHideUiIconSize)
                    )
                }
            }
        }

        shareData?.let { data ->
            val context = LocalContext.current
            val shareViewModel: SocialShareViewModel = viewModel(
                key = data.projectUrl,
                factory = SocialShareViewModel.Factory(
                    environment = environment,
                    service = AndroidSocialShareService(context.applicationContext),
                    shareData = data,
                    contextPage = ContextPageName.VIDEO_FEED
                )
            )
            CompositionLocalProvider(LocalSocialShareViewModel provides shareViewModel) {
                SocialShareSheet(
                    shareData = data,
                    isVisible = true,
                    onDismiss = { shareData = null },
                    onIntentReady = onShareIntentReady,
                    snackbarHostState = errorSnackBarHostState
                )
            }
        }

        SnackbarHost(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = dimensions.videoFeedSnackbarTopPadding,
                    start = dimensions.videoFeedSnackbarHorizontalMargin,
                    end = dimensions.videoFeedSnackbarHorizontalMargin
                ),
            hostState = errorSnackBarHostState,
            snackbar = { data ->
                KSVideoFeedSnackbar(text = data.visuals.message, hazeState = screenHazeState)
            }
        )
    }
}

/**
 * Full-screen loading page rendered as the trailing page of the feed while the next page is
 * being fetched. A centered spinner over the video background keeps the look consistent with the
 * player while the user waits for more videos to load.
 */
@Composable
fun VideoFeedLoadingPage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KSTheme.colors.videoPlayer.background)
            .testTag(VideoFeedScreenTestTag.VIDEO_FEED_LOADING_PAGE.name),
        contentAlignment = Alignment.Center
    ) {
        KSCircularProgressIndicator(color = KSTheme.colors.videoPlayer.content)
    }
}

@Preview
@Composable
fun VideoFeedLoadingPagePreview() {
    KSTheme {
        VideoFeedLoadingPage()
    }
}

@Composable
fun setUpVideoFeedErrorActions(snackbarHostState: SnackbarHostState): (String?) -> Unit {
    val scope = rememberCoroutineScope()
    val defaultErrorMessage = stringResource(R.string.Something_went_wrong_please_try_again)
    return { message: String? ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
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
    val previewEnv = Environment.builder().build()
    KSTheme {
        VideoFeedScreen(
            environment = previewEnv,
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
