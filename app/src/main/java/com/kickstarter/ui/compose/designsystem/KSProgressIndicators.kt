package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Check
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun IndicatorsPreview() {
    KSTheme {
        Column(Modifier.padding(dimensions.paddingSmall)) {
            KSLinearProgressIndicator()

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSLinearProgressIndicator(progress = 0.5f)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSCircularProgressIndicator()

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSCircularProgressIndicator(progress = 0.5f)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSVideoProgressIndicator(progress = 0.5f, text = "50")

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSVideoProgressIndicator(progress = 1f, icon = Check)
        }
    }
}

@Composable
fun KSLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = colors.kds_create_700,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap
) {
    if (progress != null) {
        LinearProgressIndicator(
            modifier = modifier,
            color = color,
            progress = { progress },
            trackColor = trackColor,
            strokeCap = strokeCap
        )
    } else {
        LinearProgressIndicator(modifier = modifier, color = color)
    }
}

@Composable
fun KSCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = colors.kds_create_700
        )
    } else {
        CircularProgressIndicator(modifier = modifier, color = colors.kds_create_700)
    }
}

/**
 * Progress indicator designed for use within the Video Player.
 * Matches the Lottie animation (https://www.lottielab.com/editor?project=c2eeb8ac-3d34-4e47-9519-39f8d5ad9e85) frame-for-frame.
 *
 * Lottie timeline (100fps, 252 frames):
 *   Frames 0-23:   Arc fades in (opacity 0→1)
 *   Frames 0-130:  Arc sweeps 0→100%
 *   Frames 130-187: Stroke turns white→green
 *   Frames 125-150: Circle pulses up (638→680)
 *   Frames 143-216: Checkmark draws in via trim path
 *   Frames 216-252: Settle — green→white, circle 680→638
 */
@Composable
fun KSVideoProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String = "",
    contentDescription: String = "",
    baseColor: Color = colors.videoPlayerProgressBase,
    completeColor: Color = colors.videoPlayerProgressComplete,
    trackColor: Color = colors.videoPlayerProgressTrack
) {
    val arcSweepEasing = CubicBezierEasing(0.741f, 0f, 0.545f, 1f)
    val fadeInEasing = CubicBezierEasing(0.5f, 0f, 0f, 1f)
    val scaleUpEasing = CubicBezierEasing(0.5f, -0.5f, 0.2f, 1f)
    val scaleDownEasing = CubicBezierEasing(0.8f, 0f, 0.5f, 1.5f)
    val checkDrawEasing = CubicBezierEasing(0.271f, 0.307f, 0.153f, 1f)

    val arcOpacity = remember { Animatable(0f) }
    val circleScale = remember { Animatable(1f) }
    val colorPhase = remember { Animatable(0f) }
    val checkTrim = remember { Animatable(0f) }

    var phase by remember { mutableIntStateOf(0) }
    var targetProgress by remember { mutableFloatStateOf(0f) }

    // Pre-allocate path objects to avoid GC pressure in DrawScope
    val checkPath = remember { Path() }
    val trimmedPath = remember { Path() }
    val pathMeasure = remember { PathMeasure() }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1300, easing = arcSweepEasing),
        label = "ProgressSweep"
    )

    // Arc fades in over 230ms on mount (Lottie frames 0-23)
    LaunchedEffect(Unit) {
        arcOpacity.animateTo(1f, tween(230, easing = fadeInEasing))
    }

    LaunchedEffect(progress) {
        targetProgress = progress
    }

    // Completion choreography — matches Lottie frames 130-252
    LaunchedEffect(animatedProgress >= 1f) {
        if (animatedProgress >= 1f) {
            phase = 1
            // ── Success phase (frames 130-216, ~860ms) ──
            coroutineScope {
                launch { circleScale.animateTo(680f / 638f, tween(200, easing = scaleUpEasing)) }
                launch { colorPhase.animateTo(1f, tween(570, easing = fadeInEasing)) }
                launch {
                    delay(130)
                    checkTrim.animateTo(1f, tween(730, easing = checkDrawEasing))
                }
            }
            // ── Settle phase (frames 216-252, ~360ms) ──
            phase = 2
            coroutineScope {
                launch { circleScale.animateTo(1f, tween(360, easing = scaleDownEasing)) }
                launch {
                    delay(30)
                    colorPhase.animateTo(0f, tween(330, easing = fadeInEasing))
                }
            }
        } else {
            phase = 0
            circleScale.snapTo(1f)
            colorPhase.snapTo(0f)
            checkTrim.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
                this.stateDescription = text
                this.progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
            },
        contentAlignment = Alignment.Center
    ) {
        // Scale to fit max pulsed outer circle (680 + 120 = 800 Lottie units)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val currentColor = Color(
                red = lerp(baseColor.red, completeColor.red, colorPhase.value),
                green = lerp(baseColor.green, completeColor.green, colorPhase.value),
                blue = lerp(baseColor.blue, completeColor.blue, colorPhase.value),
                alpha = lerp(baseColor.alpha, completeColor.alpha, colorPhase.value)
            )

            val scale = size.minDimension / 800f
            val strokeWidthPx = 120f * scale
            val baseRadius = 319f * scale
            val currentRadius = baseRadius * circleScale.value

            val arcSize = Size(currentRadius * 2, currentRadius * 2)
            val arcTopLeft = Offset(center.x - currentRadius, center.y - currentRadius)

            // TRACK (Lottie layer 3): solid gray circle, always visible
            drawCircle(
                color = trackColor,
                radius = currentRadius,
                center = center,
                style = Stroke(width = strokeWidthPx)
            )

            // PROGRESS ARC (Lottie layer 2): sweeps with fade-in and color transition
            if (animatedProgress > 0f) {
                drawArc(
                    color = currentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                    alpha = arcOpacity.value
                )
            }

            // CHECKMARK (Lottie layer 1): trim-path reveal
            if (icon != null && checkTrim.value > 0f) {
                val d = baseRadius * 2

                checkPath.reset()
                checkPath.moveTo(center.x - 0.1478f * d, center.y + 0.0004f * d)
                checkPath.lineTo(center.x - 0.0424f * d, center.y + 0.1074f * d)
                checkPath.lineTo(center.x + 0.1477f * d, center.y - 0.1060f * d)

                pathMeasure.setPath(checkPath, false)
                trimmedPath.reset()
                pathMeasure.getSegment(0f, pathMeasure.length * checkTrim.value, trimmedPath, true)

                drawPath(
                    path = trimmedPath,
                    color = currentColor,
                    style = Stroke(
                        width = d * 0.06295f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Text overlay (shown during progress phase only)
        if (phase == 0 && text.isNotEmpty()) {
            Text(
                text = text,
                color = baseColor,
                style = typographyV2.bodyBoldXS.copy(fontSize = 12.sp)
            )
        }
    }
}

enum class KSVideoScrubBarTestTag {
    SCRUB_BAR_CONTAINER,
    SCRUB_BAR_THUMB
}

/**
 * A video scrub bar with a draggable playhead thumb for seeking through video content.
 *
 * Features:
 * - Displays a linear progress track with a circular thumb indicator
 * - Supports tap-to-seek and horizontal drag-to-scrub gestures
 * - The thumb scales up with a spring animation while actively scrubbing for tactile feedback
 * - All motion uses spring-based physics for smooth, fluid interactions
 *
 * @param progress The current video progress as a [Float] between 0.0 and 1.0.
 * @param onSeek A callback invoked when the user seeks to a new position, providing the new progress value.
 * @param onScrubStart A callback invoked when the user begins a scrub gesture (touch down).
 * @param onScrubEnd A callback invoked when the user finishes a scrub gesture (touch released).
 * @param modifier The [Modifier] to be applied to the scrub bar container.
 * @param trackHeight The height of the progress track.
 * @param thumbSize The diameter of the playhead thumb circle.
 * @param thumbScaleOnDrag The scale multiplier applied to the thumb while actively dragging.
 * @param activeColor The color of the filled progress track and the thumb.
 * @param trackColor The color of the unfilled portion of the track.
 */
@Composable
fun KSVideoScrubBar(
    modifier: Modifier = Modifier,
    progress: Float,
    onSeek: (Float) -> Unit,
    onScrubStart: () -> Unit = {},
    onScrubEnd: () -> Unit = {},
    trackHeight: Dp = 4.dp,
    thumbSize: Dp = 16.dp,
    thumbScaleOnDrag: Float = 1.4f,
    activeColor: Color = Color.White,
    trackColor: Color = colors.grey_05
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }
    var trackWidthPx by remember { mutableFloatStateOf(0f) }

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) thumbScaleOnDrag else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "thumbScale"
    )

    val displayProgress = if (isDragging) dragProgress else progress

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp) // Standard touch target
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .testTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(displayProgress, 0f..1f)
                setProgress { targetProgress ->
                    onSeek(targetProgress)
                    true
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    // - detect touch down for scale animation
                    val down = awaitFirstDown()
                    isDragging = true
                    onScrubStart()
                    dragProgress = (down.position.x / size.width).coerceIn(0f, 1f)
                    onSeek(dragProgress)

                    // - Track drag movement until pointer is released
                    drag(down.id) { change ->
                        change.consume()
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek(dragProgress)
                    }

                    isDragging = false
                    onScrubEnd()
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // - Progress track
        KSLinearProgressIndicator(
            progress = displayProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(CircleShape),
            color = activeColor,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )

        // playhead
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .testTag(KSVideoScrubBarTestTag.SCRUB_BAR_THUMB.name)
                    .size(thumbSize)
                    .graphicsLayer {
                        scaleX = thumbScale
                        scaleY = thumbScale
                        translationX = displayProgress * trackWidthPx - (size.width / 2f)
                    }
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(activeColor)
            )
        }
    }
}

@Composable
@Preview(name = "Scrub Bar States", widthDp = 300)
fun KSVideoScrubBarPreview() {
    KSTheme {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(dimensions.paddingMedium)
        ) {
            KSVideoScrubBar(
                progress = 0.0f,
                onSeek = {}
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSVideoScrubBar(
                progress = 0.35f,
                onSeek = {}
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSVideoScrubBar(
                progress = 0.75f,
                onSeek = {}
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSVideoScrubBar(
                progress = 1.0f,
                onSeek = {}
            )
        }
    }
}
