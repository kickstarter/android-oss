package com.kickstarter.features.socialshare.ui.components

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.ui.icons.KSLogo
import com.kickstarter.models.Photo
import com.kickstarter.ui.compose.KSAsyncImage
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SocialShareProjectCardPreview() {
    KSTheme {
        SocialShareProjectCard(
            shareData = SocialShareData(
                projectName = "Ringo Move - The Ultimate Workout Bottle",
                projectUrl = "https://kickstarter.com",
                imageUrl = "",
                creatorName = "Ringo"
            )
        )
    }
}

enum class SocialShareProjectCardTestTag {
    PROJECT_NAME,
    CREATOR_NAME,
    KS_LOGO
}

/**
 * @param heroBitmap when non-null, the card renders this already-downloaded, already-decoded bitmap
 *   **synchronously** via [Image] instead of loading [SocialShareData.imageUrl] asynchronously. The
 *   share flow downloads the hero image first and only then passes it here, so the card is drawn
 *   complete on the next frame with no async loader/placeholder gap. Left null in previews/tests,
 *   where the async [KSAsyncImage] path is used.
 * @param onCaptured opt-in capture hook. When provided (together with [heroBitmap]), the card
 *   renderizes itself into a [Bitmap] via a [rememberGraphicsLayer] and invokes this callback once,
 *   as soon as it has actually been drawn with the hero image. This keeps the branded-image capture
 *   mechanics local to the card; the caller only supplies where the bitmap should go (e.g. the
 *   ViewModel). Null in previews/tests, where no capture occurs and no graphics layer is recorded.
 */
@Composable
fun SocialShareProjectCard(
    shareData: SocialShareData,
    heroBitmap: Bitmap? = null,
    onCaptured: ((Bitmap) -> Unit)? = null
) {
    val graphicsLayer = rememberGraphicsLayer()
    var rendered by remember { mutableStateOf(false) }
    var captured by remember { mutableStateOf(false) }

    // - The card's own surface color, used to fill the transparent rounded-corner cut-outs
    val cardBackgroundArgb = colors.backgroundSurfaceRaised.toArgb()

    val captureModifier = if (onCaptured != null) {
        Modifier.drawWithContent {
            // Only record into the graphics layer until the one-shot capture is done; after that,
            // draw the card normally so we don't re-record/re-draw the layer on every frame while
            // the sheet stays visible.
            if (!captured) {
                graphicsLayer.record { this@drawWithContent.drawContent() }
                drawLayer(graphicsLayer)
                if (!rendered && size.minDimension > 0f) rendered = true
            } else {
                drawContent()
            }
        }
    } else {
        Modifier
    }

    LaunchedEffect(rendered, heroBitmap) {
        if (onCaptured != null && heroBitmap != null && rendered && !captured) {
            withFrameNanos { }
            captured = true
            val cardBitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
            onCaptured(cardBitmap.flattenOnOpaqueBackground(cardBackgroundArgb))
        }
    }

    Column(
        modifier = Modifier
            .then(captureModifier)
            .width(dimensions.socialShareCardWidth)
            .heightIn(max = dimensions.socialShareCardHeight)
            .clip(RoundedCornerShape(dimensions.socialShareCardRadius))
            .background(colors.backgroundSurfaceRaised)
            .padding(dimensions.socialShareCardContentPadding),
    ) {
        val imageModifier = Modifier.weight(1f).clip(RoundedCornerShape(dimensions.socialShareImageRadius))
        if (heroBitmap != null) {
            Image(
                bitmap = heroBitmap.asImageBitmap(),
                contentDescription = shareData.projectName,
                contentScale = ContentScale.Crop,
                modifier = imageModifier.fillMaxWidth()
            )
        } else {
            KSAsyncImage(
                image = Photo.builder().full(shareData.imageUrl).altText(shareData.projectName).build(),
                modifier = imageModifier
            )
        }
        Spacer(modifier = Modifier.height(dimensions.paddingSmall))
        Text(
            text = shareData.projectName,
            style = KSTheme.typographyV2.headingLG.copy(
                fontSize = 17.sp,
                lineHeight = 22.sp,
                letterSpacing = (-0.41).sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = colors.textPrimary,
            maxLines = 2,
            modifier = Modifier.testTag(SocialShareProjectCardTestTag.PROJECT_NAME.name)
        )
        Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
        Text(
            text = shareData.creatorName,
            style = KSTheme.typographyV2.bodyMD.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = (-0.08).sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = colors.textSecondary,
            modifier = Modifier.testTag(SocialShareProjectCardTestTag.CREATOR_NAME.name)
        )
        Spacer(modifier = Modifier.height(dimensions.paddingXLarge))
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            Icon(
                imageVector = KSLogo,
                contentDescription = "Kickstarter",
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SocialShareProjectCardTestTag.KS_LOGO.name)
            )
        }
    }
}

/**
 * Composites the captured card — which has transparent rounded-corner cut-outs — onto an opaque
 * [backgroundArgb] fill. The result is a clean rectangle with no transparent/black corner artifacts
 * on the receiving surface; using the card's own surface colour makes the fill blend seamlessly with
 * the card body.
 */
private fun Bitmap.flattenOnOpaqueBackground(backgroundArgb: Int): Bitmap {
    val source = if (config == Bitmap.Config.HARDWARE) {
        copy(Bitmap.Config.ARGB_8888, false)
    } else {
        this
    }
    val result = createBitmap(source.width, source.height)
    Canvas(result).apply {
        drawColor(backgroundArgb)
        drawBitmap(source, 0f, 0f, null)
    }
    if (source !== this) source.recycle()
    return result
}
