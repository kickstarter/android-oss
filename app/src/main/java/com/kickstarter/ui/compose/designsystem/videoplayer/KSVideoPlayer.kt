package com.kickstarter.ui.compose.designsystem.videoplayer

import androidx.annotation.DrawableRes
import com.kickstarter.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kickstarter.ui.compose.designsystem.KSTheme
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

    // Create the HazeState to track background pixels
    var progress by remember { mutableFloatStateOf(0f) }

    var showControls by remember { mutableStateOf(false) }

    LaunchedEffect(isActive) {
        exoPlayer.playWhenReady = isActive
    }

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

    // Main Container with Click Toggle
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

        // 1. Central Controls (Hidden by default, shown on tap)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.pointerInput(Unit) {} // Stops click propagation
            ) {
                ControlIcon(
                    iconRes = R.drawable.rewind,
                    size = 36.dp,
                    onClick = { exoPlayer.seekTo(exoPlayer.currentPosition - 5000) }
                )

                // Play/Pause Center Button
                ControlIcon(
                    iconRes = R.drawable.play,
                    size = 62.dp,
                    onClick = {
                        showControls = !showControls
                        if (showControls) exoPlayer.pause()
                        else exoPlayer.play()
                    }
                )

                ControlIcon(
                    iconRes = R.drawable.forward,
                    size = 36.dp,
                    onClick = { exoPlayer.seekTo(exoPlayer.currentPosition + 5000) }
                )
            }
        }

        // 2. Progress Bar (Always visible while video is playing)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { progress },
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

    DisposableEffect(videoUrl) {
        onDispose { exoPlayer.release() }
    }
}

/**
 * Icons that try to match Glassmorphism Effects
 * take a look as reference here: https://androidengineers.substack.com/p/creating-stunning-glassmorphism-effects
 */
@Composable
private fun ControlIcon(
    @DrawableRes iconRes: Int,
    size: Dp,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        // - layer 1 Glass Surface
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2B2B2D).copy(alpha = 0.15f), // Top-left shine
                            Color(0xFF2B2B2D).copy(alpha = 0.35f), // Middle tint
                            Color(0xFF2B2B2D).copy(alpha = 0.5f)   // Bottom-right shadow
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
                .blur(50.dp)
        )

        // - layer 2 Reflective border
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.38.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f), // Bright reflection
                            Color.White.copy(alpha = 0.1f), // Faded side
                            Color.White.copy(alpha = 0.05f) // Bottom dark side
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    ),
                    shape = CircleShape
                )
        )

        // - layer 3 icon
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White
        )
    }
}