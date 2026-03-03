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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
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
    val hazeState = remember { HazeState() }
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
            modifier = Modifier.fillMaxSize().hazeSource(hazeState)
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
                    hazeState = hazeState,
                    onClick = { exoPlayer.seekTo(exoPlayer.currentPosition - 5000) }
                )

                // Play/Pause Center Button
                ControlIcon(
                    iconRes = R.drawable.play,
                    size = 62.dp,
                    hazeState = hazeState,
                    onClick = {
                        showControls = !showControls
                        if (showControls) exoPlayer.pause()
                        else exoPlayer.play()
                    }
                )

                ControlIcon(
                    iconRes = R.drawable.forward,
                    size = 36.dp,
                    hazeState = hazeState,
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

@Composable
private fun ControlIcon(
    @DrawableRes iconRes: Int,
    size: Dp,
    hazeState: HazeState,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    tint = HazeTint(Color(0xFF2B2B2D).copy(alpha = 0.25f)),
                    blurRadius = 27.68.dp, // Figma 27.68px blur
                )
            )
            .border(
                width = 1.38.dp, // Figma Spec
                color = Color.White.copy(alpha = 0.25f), // Figma Spec
                shape = CircleShape
            )
            .clickable(onClick = onClick)

    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null, // TODO: add content description
            tint = Color.White,
        )
    }
}