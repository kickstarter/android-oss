package com.kickstarter.ui.compose.designsystem.videoplayer

import android.graphics.Matrix
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.initializeExoplayer
import com.kickstarter.ui.compose.designsystem.KSControlIcon
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSVideoScrubBar
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Play
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay

enum class KSVideoPlayerTestTag {
    VIDEO_PLAYER_SURFACE,
    VIDEO_PLAYER_CONTROLS,
    VIDEO_PLAYER_PLAY_BUTTON,
    VIDEO_PLAYER_FORWARD_BUTTON,
    VIDEO_PLAYER_REWIND_BUTTON,
    VIDEO_PLAYER_PROGRESS_BAR,
    VIDEO_PLAYER_POSTER
}

/** How many times the on-screen player will re-prepare after a *recoverable* error before giving up.
* Bounded so a device that is genuinely over its decoder budget. */
private const val MAX_PLAYBACK_RECOVERY_ATTEMPTS = 3

/** A reclaimed decoder or a transient network failure can succeed on a re-prepare;
* format/source/DRM errors cannot, so we never retry (or loop on) those.*/
private fun PlaybackException.isRecoverable(): Boolean = when (errorCode) {
    PlaybackException.ERROR_CODE_DECODING_RESOURCES_RECLAIMED,
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> true
    else -> false
}

/**
 * Applies a transformation matrix to the [TextureView] to emulate a "Center Crop" (RESIZE_MODE_ZOOM)
 * aspect ratio. This ensures the video fills the entire view area by scaling the smaller dimension
 * to fit, while cropping the overflow.
 *
 * @this textureView The [TextureView] to which the transformation matrix will be applied.
 * @param videoWidth The intrinsic width of the video source.
 * @param videoHeight The intrinsic height of the video source.
 * @param matrix A [Matrix] instance to write the transform into. Callers on a hot path (e.g. per
 * layout frame during the HideUI animation) should pass a reused instance to avoid per-frame
 * allocations; the [TextureView] copies the values internally, so the same instance is safe to reuse.
 */
fun TextureView.applyZoomMatrix(videoWidth: Int, videoHeight: Int, matrix: Matrix = Matrix()) {
    // - Guard against invalid dimensions to prevent division by zero
    if (videoWidth <= 0 || videoHeight <= 0 || width <= 0 || height <= 0) return

    val viewWidth = width.toFloat()
    val viewHeight = height.toFloat()

    val videoRatio = videoWidth.toFloat() / videoHeight
    val viewRatio = viewWidth / viewHeight

    val (scaleX, scaleY) = if (videoRatio > viewRatio) {
        // - Video is wider than the view: scale X to overflow (crop sides)
        (videoRatio / viewRatio) to 1f
    } else {
        // - Video is taller than the view: scale Y to overflow (crop top/bottom)
        1f to (viewRatio / videoRatio)
    }

    // - Optimization: Only apply if a transformation is actually needed
    if (scaleX == 1f && scaleY == 1f) {
        setTransform(null)
        return
    }

    // - Use internal center point for scaling. setScale replaces the matrix contents, so a reused
    //   instance needs no explicit reset.
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)

    setTransform(matrix)
}

/**
 * Applies a transformation matrix to the [TextureView] to emulate a "Fit / Contain"
 * (RESIZE_MODE_FIT) aspect ratio. The whole video frame stays visible, preserving its aspect
 * ratio, and any leftover space becomes letterbox bars. Used by the video feed's HideUI mode so
 * captions baked into the edges of the frame are never cropped.
 *
 * @this textureView The [TextureView] to which the transformation matrix will be applied.
 * @param videoWidth The intrinsic width of the video source.
 * @param videoHeight The intrinsic height of the video source.
 * @param matrix A [Matrix] instance to write the transform into. Callers on a hot path (e.g. per
 * layout frame during the HideUI animation) should pass a reused instance to avoid per-frame
 * allocations; the [TextureView] copies the values internally, so the same instance is safe to reuse.
 */
fun TextureView.applyFitMatrix(videoWidth: Int, videoHeight: Int, matrix: Matrix = Matrix()) {
    // - Guard against invalid dimensions to prevent division by zero
    if (videoWidth <= 0 || videoHeight <= 0 || width <= 0 || height <= 0) return

    val viewWidth = width.toFloat()
    val viewHeight = height.toFloat()

    val videoRatio = videoWidth.toFloat() / videoHeight
    val viewRatio = viewWidth / viewHeight

    // A [TextureView] stretches its surface to fill the view, so we scale the overflowing dimension
    // back down to restore the video's real aspect ratio (letterboxing the remainder).
    val (scaleX, scaleY) = if (videoRatio > viewRatio) {
        // - Video is wider than the view: keep full width, shrink height (letterbox top/bottom)
        1f to (viewRatio / videoRatio)
    } else {
        // - Video is taller than the view: keep full height, shrink width (letterbox sides)
        (videoRatio / viewRatio) to 1f
    }

    // - Optimization: Only apply if a transformation is actually needed
    if (scaleX == 1f && scaleY == 1f) {
        setTransform(null)
        return
    }

    // setScale replaces the matrix contents, so a reused instance needs no explicit reset.
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)

    setTransform(matrix)
}

/**
 * Holds the listeners attached to the player's [TextureView] so both can be detached together when
 * the [AndroidView] is released, avoiding a dangling [View.OnLayoutChangeListener].
 */
private class TextureViewListeners(
    val playerListener: Player.Listener,
    val layoutListener: View.OnLayoutChangeListener
)

/**
 * A full-screen video player component that utilizes [ExoPlayer] to render video content.
 * It supports automatic playback based on lifecycle/visibility, interactive playback controls,
 * and a custom progress bar.
 *
 * The player uses a [TextureView] combined with a "Center Crop" transformation to ensure
 * the video fills the available surface area. It also integrates with a glassmorphism (Haze)
 * effect for the UI overlays.
 *
 * @param videoUrl The remote URL of the video to be played. If empty, the component renders nothing.
 * @param isActive A boolean flag indicating if the video should be playing. When true, the video
 * starts/resumes; when false, it pauses.
 * @param hideUi When true, the player animates into "HideUI" mode: the video surface shrinks into a
 * centered, rounded 9:16 card and switches from center-crop to fit (so the whole frame, including
 * captions, stays visible). When false it fills the container center-cropped as usual.
 * @param modifier The [Modifier] to be applied to the player's outer container.
 * @param previewImageUrl Optional poster (first frame) of the video. When provided, it is shown
 * center-cropped over the surface while the stream buffers and fades out once the player renders
 * its first frame, avoiding a black flash on each page.
 * //TODO will potentially change in future versions to not create internally any instance
 * @param player An optional, pre-configured [ExoPlayer] instance. If null, a default instance
 * is created and managed internally, then released when the Composable is disposed.
 * @param overlayContent A slot for adding custom UI elements on top of the video player (e.g., Badges,
 * titles, actionButtons). These elements are placed in a [BoxScope] and are drawn above the video and its controls.
 */
@Composable
fun KSVideoPlayer(
    videoUrl: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    hideUi: Boolean = false,
    previewImageUrl: String? = null,
    player: ExoPlayer? = null,
    overlayContent: @Composable BoxScope.(HazeState) -> Unit = {},
    onPlayPauseToggle: (isPlaying: Boolean) -> Unit = {},
    onProgressBarInteraction: (currentProgress: Float) -> Unit = {},
    onBecameInactive: (watchTimeMs: Long, videoDurationMs: Long) -> Unit = { _, _ -> },
    onPlaybackError: (error: PlaybackException, isActive: Boolean) -> Unit = { _, _ -> }
) {
    if (videoUrl.isEmpty()) return // TODO: Check video format of the url on the VM
    val context = LocalContext.current

    val exoPlayer = remember(player ?: videoUrl) {
        player ?: context.initializeExoplayer().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }

    // Tracks whether the player has drawn its first frame. Until then we keep the poster visible
    // to cover the black surface while the stream buffers. Reset when the player instance changes.
    var firstFrameRendered by remember(player ?: videoUrl) { mutableStateOf(false) }

    var progress by remember { mutableFloatStateOf(0f) }
    var isScrubbing by remember { mutableStateOf(false) }

    // Intrinsic video dimensions, published by the player once known. Drives the crop/fit matrix,
    // which is re-applied reactively (and on every layout pass while the HideUI card animates).
    var videoWidth by remember(player ?: videoUrl) { mutableIntStateOf(0) }
    var videoHeight by remember(player ?: videoUrl) { mutableIntStateOf(0) }
    val hideUiState = rememberUpdatedState(hideUi)
    // Reused across every crop/fit application so the per-layout-frame updates during the HideUI
    // animation don't allocate a new Matrix each time.
    val transformMatrix = remember { Matrix() }

    var showControls by remember { mutableStateOf(false) }
    val hazeState = rememberHazeState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAppInForeground by remember { mutableStateOf(true) }

    // Keep references to the latest callback lambdas so lambdas inside remember(exoPlayer)
    // blocks always invoke the current version even if the parent recomposes.
    val onPlayPauseToggleState = rememberUpdatedState(onPlayPauseToggle)
    val onProgressBarInteractionState = rememberUpdatedState(onProgressBarInteraction)
    val onBecameInactiveState = rememberUpdatedState(onBecameInactive)
    val onPlaybackErrorState = rememberUpdatedState(onPlaybackError)
    val isActiveState = rememberUpdatedState(isActive)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isAppInForeground = true
                Lifecycle.Event.ON_PAUSE -> isAppInForeground = false
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // - Updated progress bar only when active and not scrubbing
    LaunchedEffect(isActive, isAppInForeground) {
        val shouldPlay = isActive && isAppInForeground
        exoPlayer.playWhenReady = shouldPlay
        if (shouldPlay) {
            showControls = false
            while (true) {
                if (!isScrubbing) {
                    val duration = exoPlayer.duration
                    if (duration > 0) {
                        progress = exoPlayer.currentPosition.toFloat() / duration
                    }
                }
                delay(500)
            }
        }
    }

    // - control functions are wrapped in remember(exoPlayer).
    // This is a performance optimization: it ensures these functions are only recreated if the exoPlayer instance changes.
    // If the UI recomposes for other reasons (like a timer for the progressBar), these functions remain stable in memory.
    val onToggleControls = remember(exoPlayer) {
        {
            showControls = !showControls
            if (showControls) exoPlayer.pause()
            else exoPlayer.play()
            // showControls=true means paused; !showControls = isPlaying
            onPlayPauseToggleState.value(!showControls)
        }
    }

    val onSeek = remember(exoPlayer) {
        { newProgress: Float ->
            progress = newProgress
            val duration = exoPlayer.duration
            if (duration > 0) {
                exoPlayer.seekTo((duration * newProgress).toLong())
            }
        }
    }

    val onScrubStart = remember(exoPlayer) {
        {
            isScrubbing = true
            exoPlayer.pause()
            val progressAtInteraction = if (exoPlayer.duration > 0L) {
                exoPlayer.currentPosition.toFloat() / exoPlayer.duration
            } else 0f
            onProgressBarInteractionState.value(progressAtInteraction)
        }
    }

    val onScrubEnd = remember(exoPlayer) {
        {
            isScrubbing = false
            if (!showControls) exoPlayer.play()
        }
    }

    // Full screen player surface
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_SURFACE.name)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Remove ripple for the background tap
                onClickLabel = stringResource(id = if (showControls) R.string.accessibility_discovery_buttons_close else R.string.Play)
            ) {
                onToggleControls()
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // - HideUI card geometry: the video surface animates between full-bleed and a centered,
            //   rounded 9:16 card that sits between the top icon row and the bottom CTA.
            val hideUiProgress by animateFloatAsState(
                targetValue = if (hideUi) 1f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "hideUiProgress"
            )

            val fullWidth = maxWidth
            val fullHeight = maxHeight

            val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val iconRowTop = maxOf(dimensions.videoFeedCloseButtonTopPadding, statusBarInset + dimensions.paddingSmall)
            val topReserve = iconRowTop + (dimensions.videoFeedHiddenModeTopReserve - dimensions.videoFeedCloseButtonTopPadding)
            val bottomReserve = maxOf(dimensions.videoFeedScrubBarBottomPadding, navBarInset) +
                (dimensions.videoFeedHiddenModeBottomReserve - dimensions.videoFeedScrubBarBottomPadding)
            val availableHeight = (fullHeight - topReserve - bottomReserve).coerceAtLeast(0.dp)
            val inset = dimensions.videoFeedHiddenModeHorizontalInset
            val widthConstrained = (fullWidth - inset * 2).coerceAtLeast(0.dp)
            val nineToSixteenHeight = widthConstrained * (16f / 9f)
            val (cardWidth, cardHeight) = if (nineToSixteenHeight <= availableHeight) {
                widthConstrained to nineToSixteenHeight
            } else {
                (availableHeight * (9f / 16f)) to availableHeight
            }

            val surfaceWidth = lerp(fullWidth, cardWidth, hideUiProgress)
            val surfaceHeight = lerp(fullHeight, cardHeight, hideUiProgress)
            val cornerRadius = lerp(0.dp, dimensions.videoFeedHiddenModeCornerRadius, hideUiProgress)
            // - Bias the card slightly upward (toward the reserved band centre) so its bottom edge
            //   clears the CTA button.
            val cardCenterY = topReserve + availableHeight / 2f
            val verticalShift = lerp(0.dp, cardCenterY - fullHeight / 2f, hideUiProgress)

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = verticalShift)
                    .size(width = surfaceWidth, height = surfaceHeight)
                    .clip(RoundedCornerShape(cornerRadius))
            ) {
                AndroidView(
                    // - Required TextureView to work in tandem with haze to achieve glassmorphism on control buttons/badges
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            val playerListener = object : Player.Listener {
                                override fun onVideoSizeChanged(videoSize: VideoSize) {
                                    if (videoSize.width > 0 && videoSize.height > 0) {
                                        videoWidth = videoSize.width
                                        videoHeight = videoSize.height
                                    }
                                }

                                override fun onRenderedFirstFrame() {
                                    firstFrameRendered = true
                                }
                            }
                            // - Re-apply the crop/fit matrix on every layout pass so it stays correct
                            //   while the card resizes during the HideUI animation.
                            val layoutListener = View.OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                                val textureView = v as? TextureView ?: return@OnLayoutChangeListener
                                if (videoWidth > 0 && videoHeight > 0) {
                                    if (hideUiState.value) textureView.applyFitMatrix(videoWidth, videoHeight, transformMatrix)
                                    else textureView.applyZoomMatrix(videoWidth, videoHeight, transformMatrix)
                                }
                            }
                            tag = TextureViewListeners(playerListener, layoutListener)
                            exoPlayer.setVideoTextureView(this)
                            exoPlayer.addListener(playerListener)
                            addOnLayoutChangeListener(layoutListener)
                        }
                    },
                    update = { view ->
                        if (videoWidth > 0 && videoHeight > 0) {
                            if (hideUi) view.applyFitMatrix(videoWidth, videoHeight, transformMatrix)
                            else view.applyZoomMatrix(videoWidth, videoHeight, transformMatrix)
                        }
                    },
                    onRelease = { view ->
                        (view.tag as? TextureViewListeners)?.let {
                            exoPlayer.removeListener(it.playerListener)
                            view.removeOnLayoutChangeListener(it.layoutListener)
                        }
                        exoPlayer.clearVideoTextureView(view)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState)
                )

                // Poster shown over the surface until the player renders its first frame, eliminating the
                // black flash while the HLS stream buffers (most noticeable on fast swipes / slow networks).
                AnimatedVisibility(
                    visible = !firstFrameRendered && !previewImageUrl.isNullOrEmpty(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = remember(previewImageUrl) {
                            ImageRequest.Builder(context)
                                .data(previewImageUrl)
                                .crossfade(true)
                                .build()
                        },
                        contentDescription = null,
                        contentScale = if (hideUi) ContentScale.Fit else ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_POSTER.name)
                    )
                }
            }
        }

        ControlsContainer(
            modifier = Modifier.align(Alignment.Center),
            showControls = showControls,
            hazeState = hazeState,
            playPauseCallback = onToggleControls
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                overlayContent(hazeState)
            }

            if (hideUi) {
                Spacer(modifier = Modifier.height(dimensions.videoFeedHiddenModeScrubBarSpacing))
            }

            ProgressBarContainer(
                modifier = Modifier,
                progressProvider = { progress },
                onSeek = onSeek,
                onScrubStart = onScrubStart,
                onScrubEnd = onScrubEnd,
                showThumb = showControls
            )
        }
    }

    // Key on the player instance: release only when the internal player is replaced or disposed.
    // External (pool) players are never released here — the pool owns their lifecycle.
    DisposableEffect(exoPlayer) {
        onDispose { if (player == null) exoPlayer.release() }
    }

    // Playback-error telemetry + bounded self-recovery for the on-screen video. We always report the error (telemetry) and, additionally, re-prepare to recover —
    //  - only the on-screen video recovers ([isActive]);
    //  - only recoverable errors are retried, never broken media
    //  - retries are bounded, and the budget refills only once the video genuinely plays again
    DisposableEffect(exoPlayer) {
        var recoveryAttempts = 0
        val errorListener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                onPlaybackErrorState.value(error, isActiveState.value)
                if (isActiveState.value && error.isRecoverable() && recoveryAttempts < MAX_PLAYBACK_RECOVERY_ATTEMPTS) {
                    recoveryAttempts++
                    exoPlayer.prepare()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) recoveryAttempts = 0
            }
        }
        exoPlayer.addListener(errorListener)
        onDispose { exoPlayer.removeListener(errorListener) }
    }

    DisposableEffect(isActive) {
        onDispose {
            if (isActive) {
                onBecameInactiveState.value(
                    exoPlayer.currentPosition,
                    exoPlayer.duration.coerceAtLeast(0L)
                )
            }
        }
    }
}

/**
 * A composable that displays a scrub bar for the video player with a draggable playhead.
 *
 * It uses [KSVideoScrubBar] to provide a progress track with a visible thumb circle
 * that supports both tap-to-seek and drag-to-scrub gestures.
 *
 * @param modifier The [Modifier] to be applied to the container.
 * @param progressProvider A lambda that returns the current video progress as a [Float] between 0.0 and 1.0.
 * @param onSeek A callback invoked when the user seeks to a new position, providing the new progress value.
 */
@Composable
private fun ProgressBarContainer(
    modifier: Modifier,
    progressProvider: () -> Float,
    onSeek: (Float) -> Unit = {},
    onScrubStart: () -> Unit = {},
    onScrubEnd: () -> Unit = {},
    showThumb: Boolean = true
) {
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    KSVideoScrubBar(
        progress = progressProvider(),
        onSeek = onSeek,
        onScrubStart = onScrubStart,
        onScrubEnd = onScrubEnd,
        modifier = modifier
            .padding(bottom = maxOf(dimensions.videoFeedScrubBarBottomPadding, navBarInset))
            .padding(horizontal = dimensions.paddingMedium)
            .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name),
        activeColor = Color.White,
        trackColor = KSTheme.colors.grey_05,
        showThumb = showThumb
    )
}

@Composable
private fun ControlsContainer(
    modifier: Modifier,
    showControls: Boolean,
    hazeState: HazeState? = null,
    playPauseCallback: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = showControls,
        enter = fadeIn() + scaleIn(
            initialScale = 0.6f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 300f
            )
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_CONTROLS.name)
                .pointerInput(Unit) {} // Stops click propagation
        ) {
            KSControlIcon(
                icon = Play,
                size = 80.dp,
                onClick = {
                    playPauseCallback.invoke()
                },
                hazeState = hazeState,
                modifier = Modifier.testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PLAY_BUTTON.name),
                contentDescription = stringResource(id = R.string.Play)
            )
        }
    }
}

@Composable
@Preview(widthDp = 300, heightDp = 200)
fun ControlsPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KSTheme.colors.kds_black),
    ) {
        ControlsContainer(
            modifier = Modifier.align(Alignment.Center),
            showControls = true
        )
    }
}

@Composable
@Preview(widthDp = 300, heightDp = 200)
fun ProgressBarPreview() {
    KSTheme {
        Column(
            modifier = Modifier.padding(dimensions.paddingSmall),
        ) {
            ProgressBarContainer(
                modifier = Modifier.fillMaxWidth(),
                progressProvider = { 0.1f }
            )
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            ProgressBarContainer(
                modifier = Modifier.fillMaxWidth(),
                progressProvider = { 0.5f }
            )
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            ProgressBarContainer(
                modifier = Modifier.fillMaxWidth(),
                progressProvider = { 0.9f }
            )
        }
    }
}
