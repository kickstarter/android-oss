package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Check

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
 */
@Composable
fun KSVideoProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String = "",
    contentDescription: String = "",
    baseColor: Color = Color.White,
    completeColor: Color = Color(0xFF8CE71A), // [0.55, 0.906, 0.1] from JSON
    trackColor: Color = Color.White.copy(alpha = 0.2f)
) {
    val lottieEasing = CubicBezierEasing(0.15f, 0f, 0.27f, 1f)

    // Animation States
    var targetProgressValue by remember { mutableFloatStateOf(0f) }
    val pulseScale = remember { Animatable(1f) }

    // 0: Initial, 1: Success (Green), 2: Settle (White)
    var completionPhase by remember { mutableIntStateOf(0) }

    LaunchedEffect(progress) {
        targetProgressValue = progress
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgressValue.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = lottieEasing),
        label = "ProgressSweep"
    )

    // Sequence Trigger: Handles the Green -> White transition from frames 216-252
    LaunchedEffect(animatedProgress >= 1f) {
        if (animatedProgress >= 1f) {
            // Step 1: Hit Success Phase (Green + Pulse Up)
            completionPhase = 1
            pulseScale.animateTo(1.06f, tween(300, easing = lottieEasing))

            // Step 2: Settle Phase (Back to White + Scale Down)
            completionPhase = 2
            pulseScale.animateTo(1f, tween(300, easing = lottieEasing))
        } else {
            completionPhase = 0
            pulseScale.snapTo(1f)
        }
    }

    // Color logic mapping to the JSON keyframes
    val animatedColor by animateColorAsState(
        targetValue = when (completionPhase) {
            1 -> completeColor // Success Green
            2 -> baseColor // Settles back to White
            else -> baseColor
        },
        animationSpec = tween(500),
        label = "ColorPhase"
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer(scaleX = pulseScale.value, scaleY = pulseScale.value)
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
                this.progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Calculate a consistent stroke width based on your design ratio
            val strokeWidthPx = size.width * (120f / 638f)

            // 2. Calculate a shared radius that accounts for the stroke width
            // to prevent the edges from being clipped at the Canvas bounds
            val radius = (size.minDimension - strokeWidthPx) / 2f

            // 3. Define the bounding box for the Arc so it matches the Circle's path
            val arcSize = Size(radius * 2, radius * 2)
            val arcTopLeft = Offset(
                x = (size.width / 2f) - radius,
                y = (size.height / 2f) - radius
            )

            // TRACK: Draws the static background circle
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx)
            )

            // PROGRESS: Draws the moving arc on top using the animated values
            if (animatedProgress > 0f) {
                drawArc(
                    color = animatedColor, // Keeps your Green -> White transition
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress, // Keeps your 0.0 -> 1.0 motion
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round // Creates the rounded "pill" look
                    )
                )
            }
        }

        // Show Icon/Text based on the completion phase
        Crossfade(targetState = completionPhase >= 1, label = "ContentFade") { isFinished ->
            if (isFinished && icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animatedColor,
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            } else if (!isFinished && text.isNotEmpty()) {
                Text(
                    text = text,
                    color = Color.White,
                    style = typographyV2.bodyBoldXS.copy(fontSize = 12.sp)
                )
            }
        }
    }
}
