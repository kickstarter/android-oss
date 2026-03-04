package com.kickstarter.ui.compose.designsystem.videoplayer

import Forward
import Play
import Rewind
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kickstarter.ui.compose.designsystem.KSControlIcon
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import kotlinx.coroutines.delay

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun KSVideoPlayer(
    videoUrl: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (videoUrl.isEmpty()) return // TODO: Check video format of the url on the VM
    val context = LocalContext.current
    val exoPlayer = remember(videoUrl) { // - TODO will be extracted to a videoplayer pool, and the pool will be pass as dependency
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }

    var progress by remember { mutableFloatStateOf(0f) }

    var showControls by remember { mutableStateOf(false) }

    LaunchedEffect(isActive) {
        exoPlayer.playWhenReady = isActive
    }

    // - Updated progress bar only when active
    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                val duration = exoPlayer.duration
                if (duration > 0) {
                    progress = exoPlayer.currentPosition.toFloat() / duration
                }
                delay(250)
            }
        }
    }

    // Full screen player surface
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Remove ripple for the background tap
            ) {
                showControls = !showControls
                if (showControls) exoPlayer.pause()
                else exoPlayer.play()
            }
    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        ControlsContainer(
            modifier = Modifier.align(Alignment.Center),
            showControls = showControls,
            pauseCallback = { exoPlayer.pause() },
            playCallback = { exoPlayer.play() },
            rewindCallback = {
                exoPlayer.seekTo(exoPlayer.currentPosition - 5000)
            },
            forwardCallback = {
                exoPlayer.seekTo(exoPlayer.currentPosition + 5000)
            }
        )

        ProgressBarContainer(
            modifier = Modifier.align(Alignment.BottomCenter),
            progress = progress
        )
    }

    DisposableEffect(videoUrl) {
        onDispose { exoPlayer.release() }
    }
}

@Composable
private fun ProgressBarContainer(
    modifier: Modifier,
    progress: Float
) {
    Box(
        modifier = modifier
            .padding(bottom = 24.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        KSLinearProgressIndicator(
            progress = progress,
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
    pauseCallback: () -> Unit = {},
    playCallback: () -> Unit = {},
    forwardCallback: () -> Unit = {},
    rewindCallback: () -> Unit = {},
) {
    var showControls1 = showControls
    AnimatedVisibility(
        visible = showControls1,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.pointerInput(Unit) {} // Stops click propagation
        ) {
            KSControlIcon(
                icon = Rewind,
                size = 36.dp,
                onClick = {
                    rewindCallback.invoke()
                }
            )

            KSControlIcon(
                icon = Play,
                size = 62.dp,
                onClick = {
                    showControls1 = !showControls1
                    if (showControls1) pauseCallback.invoke()
                    else playCallback.invoke()
                }
            )

            KSControlIcon(
                icon = Forward,
                size = 36.dp,
                onClick = {
                    forwardCallback.invoke()
                }
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
                progress = 0.1f
            )
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            ProgressBarContainer(
                modifier = Modifier.fillMaxWidth(),
                progress = 0.5f
            )
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            ProgressBarContainer(
                modifier = Modifier.fillMaxWidth(),
                progress = 0.9f
            )
        }
    }
}
