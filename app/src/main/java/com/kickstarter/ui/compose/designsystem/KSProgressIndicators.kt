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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
    progress: Float, // Expected 0.0 to 1.0 from the Card
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String = "",
    contentDescription: String = "",
    baseColor: Color = Color.White,
    completeColor: Color = Color(0xFF8CE71A), // JSON Green
    trackColor: Color = Color.White.copy(alpha = 0.2f)
) {
    // 1. Technical Constants from JSON
    val lottieEasing = CubicBezierEasing(0.15f, 0f, 0.27f, 1f)
    val strokeRatio = 120f / 638f

    // 2. Animation State Management
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val pulseScale = remember { Animatable(1f) }
    var isAnimationComplete by remember { mutableStateOf(false) }

    LaunchedEffect(progress) {
        targetProgress = progress
    }

    // The "Running" Sweep Animation
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = lottieEasing),
        label = "ProgressSweep"
    )

    // 3. Phase Trigger: Run Pulse and Color shift once sweep hits 100%
    LaunchedEffect(animatedProgress >= 1f) {
        if (animatedProgress >= 1f) {
            isAnimationComplete = true
            // Pulse Up (Frames 125-150 in JSON)
            pulseScale.animateTo(1.06f, tween(300, easing = lottieEasing))
            // Settle Down (Frames 216-252 in JSON)
            pulseScale.animateTo(1f, tween(300, easing = lottieEasing))
        } else {
            isAnimationComplete = false
            pulseScale.snapTo(1f)
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isAnimationComplete) completeColor else baseColor,
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
            val strokeWidthPx = size.width * strokeRatio
            val radius = (size.width - strokeWidthPx) / 2

            // Background Track (Layer 3)
            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = strokeWidthPx)
            )

            // Progress Arc (Layer 2)
            if (animatedProgress > 0f) {
                drawArc(
                    color = animatedColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Layer 1: Icon/Text Reveal
        Crossfade(targetState = isAnimationComplete, label = "ContentFade") { completed ->
            if (completed && icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animatedColor,
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            } else if (!completed && text.isNotEmpty()) {
                Text(
                    text = text,
                    color = Color.White,
                    style = typographyV2.bodyBoldXS.copy(fontSize = 12.sp)
                )
            }
        }
    }
}
