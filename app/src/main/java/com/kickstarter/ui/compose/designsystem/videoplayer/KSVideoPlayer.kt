package com.kickstarter.ui.compose.designsystem.videoplayer

import android.graphics.Matrix
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.kickstarter.libs.utils.extensions.initializeExoplayer
import com.kickstarter.ui.compose.designsystem.KSControlIcon
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Forward
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Play
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Rewind
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
    VIDEO_PLAYER_PROGRESS_BAR
}

/**
 * Applies a transformation matrix to the [TextureView] to emulate a "Center Crop" (RESIZE_MODE_ZOOM)
 * aspect ratio. This ensures the video fills the entire view area by scaling the smaller dimension
 * to fit, while cropping the overflow.
 *
 * @this textureView The [TextureView] to which the transformation matrix will be applied.
 * @param videoWidth The intrinsic width of the video source.
 * @param videoHeight The intrinsic height of the video source.
 */
fun TextureView.applyZoomMatrix(videoWidth: Int, videoHeight: Int) {
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

    val matrix = Matrix().apply {
        // - Use internal center point for scaling
        setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
    }

    setTransform(matrix)
}

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
 * @param modifier The [Modifier] to be applied to the player's outer container.
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
    player: ExoPlayer? = null,
    overlayContent: @Composable BoxScope.(HazeState) -> Unit = {}
) {
    if (videoUrl.isEmpty()) return // TODO: Check video format of the url on the VM
    val context = LocalContext.current
    // When an external player is provided, key on the player instance so that the pool can
    // recycle it across pages without triggering spurious recomputations. When managed
    // internally, key on videoUrl so the player is replaced if the URL changes.
    val exoPlayer = remember(player ?: videoUrl) {
        player ?: context.initializeExoplayer().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }

    var progress by remember { mutableFloatStateOf(0f) }

    var showControls by remember { mutableStateOf(false) }
    val hazeState = rememberHazeState()

    // - Updated progress bar only when active
    LaunchedEffect(isActive) {
        exoPlayer.playWhenReady = isActive
        if (isActive) {
            while (true) {
                val duration = exoPlayer.duration
                if (duration > 0) {
                    progress = exoPlayer.currentPosition.toFloat() / duration
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
        }
    }

    val onRewind = remember(exoPlayer) {
        {
            exoPlayer.seekTo(exoPlayer.currentPosition - 5000)
        }
    }

    val onForward = remember(exoPlayer) {
        {
            exoPlayer.seekTo(exoPlayer.currentPosition + 5000)
        }
    }

    val onSeek = remember(exoPlayer) {
        { newProgress: Float ->
            val duration = exoPlayer.duration
            if (duration > 0) {
                exoPlayer.seekTo((duration * newProgress).toLong())
            }
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
                indication = null // Remove ripple for the background tap
            ) {
                onToggleControls()
            }
    ) {
        AndroidView(
            // - Required TextureView to work in tandem with haze to achieve glassmorphism on control buttons/badges
            factory = { ctx ->
                TextureView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val listener = object : Player.Listener {
                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            if (videoSize.width > 0 && videoSize.height > 0) {
                                this@apply.applyZoomMatrix(videoSize.width, videoSize.height)
                            }
                        }
                    }
                    tag = listener
                    exoPlayer.setVideoTextureView(this)
                    exoPlayer.addListener(listener)
                }
            },
            onRelease = { view ->
                (view.tag as? Player.Listener)?.let { exoPlayer.removeListener(it) }
                exoPlayer.clearVideoTextureView(view)
            },
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        )

        ControlsContainer(
            modifier = Modifier.align(Alignment.Center),
            showControls = showControls,
            hazeState = hazeState,
            playPauseCallback = onToggleControls,
            rewindCallback = onRewind,
            forwardCallback = onForward
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

            ProgressBarContainer(
                modifier = Modifier,
                progressProvider = { progress },
                onSeek = onSeek
            )
        }
    }

    // Key on the player instance: release only when the internal player is replaced or disposed.
    // External (pool) players are never released here — the pool owns their lifecycle.
    DisposableEffect(exoPlayer) {
        onDispose { if (player == null) exoPlayer.release() }
    }
}

/**
 * A composable that displays a progress bar for the video player and handles user seeking.
 *
 * It uses a [KSLinearProgressIndicator] to visualize the current progress and wraps it in a larger
 * touch target [Box] to detect tap gestures for seeking to specific timestamps.
 *
 * @param modifier The [Modifier] to be applied to the container.
 * @param progressProvider A lambda that returns the current video progress as a [Float] between 0.0 and 1.0.
 * Passing a lambda instead of a direct value is a performance optimization to defer reading the state
 * until the draw phase, preventing unnecessary recompositions of the parent player.
 * @param onSeek A callback invoked when the user taps the progress bar, providing the new progress value.
 */
@Composable
private fun ProgressBarContainer(
    modifier: Modifier,
    progressProvider: () -> Float,
    onSeek: (Float) -> Unit = {}
) {
    Box(
        modifier = modifier
            .padding(bottom = 24.dp)
            .padding(horizontal = dimensions.paddingMedium)
            .fillMaxWidth()
            .height(48.dp) // Standard touch target height
            .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val tappedProgress = offset.x / size.width
                    onSeek(tappedProgress.coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        KSLinearProgressIndicator(
            progress = progressProvider(),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = Color.White,
            trackColor = KSTheme.colors.grey_05,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun ControlsContainer(
    modifier: Modifier,
    showControls: Boolean,
    hazeState: HazeState? = null,
    playPauseCallback: () -> Unit = {},
    forwardCallback: () -> Unit = {},
    rewindCallback: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = showControls,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_CONTROLS.name)
                .pointerInput(Unit) {} // Stops click propagation
        ) {
            KSControlIcon(
                icon = Rewind,
                size = 36.dp,
                onClick = {
                    rewindCallback.invoke()
                },
                hazeState = hazeState,
                modifier = Modifier.testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_REWIND_BUTTON.name)
            )

            KSControlIcon(
                icon = Play,
                size = 62.dp,
                onClick = {
                    playPauseCallback.invoke()
                },
                hazeState = hazeState,
                modifier = Modifier.testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PLAY_BUTTON.name)
            )

            KSControlIcon(
                icon = Forward,
                size = 36.dp,
                onClick = {
                    forwardCallback.invoke()
                },
                hazeState = hazeState,
                modifier = Modifier.testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_FORWARD_BUTTON.name)
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
