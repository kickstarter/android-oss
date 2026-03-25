package com.kickstarter.features.projectstory.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.SizeResolver
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.grey_04
import org.joda.time.DateTime

enum class ProjectStoryCaptionedImageTestTag {
    CAPTION,
}

private val placeholderPainter = object : Painter() {
    override val intrinsicSize = Size(100f, 50f)
    override fun DrawScope.onDraw() {
        drawRect(grey_04)
    }
}

@Preview(
    device = Devices.PIXEL_3
)
@Composable
private fun CaptionedImageScreenPreview() {
    KSTheme {
        CaptionedImageScreen()
    }
}

@Composable
fun CaptionedImageScreen() {
    Box(
        modifier = Modifier
            .background(Color(0xFFECE4DA))
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        ProjectStoryCaptionedImage(
            image = "https://picsum.photos/1120/630?random=${DateTime.now().millis}",
            caption = "The above image shows the Special Editions of Book 3, Book 2 and Book 1 at the front.",
        )
    }
}

@Preview
@Composable
private fun ProjectStoryCaptionedImagePreview() {
    ProjectStoryCaptionedImage(
        image = "https://picsum.photos/1120/630?random=${DateTime.now().millis}",
        caption = "The above image shows the Special Editions of Book 3, Book 2 and Book 1 at the front."
    )
}

@Composable
fun ProjectStoryCaptionedImage(
    modifier: Modifier = Modifier,
    image: String?,
    caption: String?,
    link: String? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null
) {
    val context = LocalContext.current
    val defaults = context.imageLoader.defaults

    val contentScale = remember {
        ContentScale.FillWidth
    }

    val sizeResolver = remember {
        ConstraintsSizeResolver()
    }

    val imageRequest = remember {
        ImageRequest.Builder(context)
            .data(image)
            .size(sizeResolver)
            .defaults(defaults)
            .crossfade(true)
            .build()
    }

    val inPreview = LocalInspectionMode.current
    val previewPlaceholder = remember {
        if (inPreview) placeholderPainter else null
    }

    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        contentScale = contentScale,
        onSuccess = onSuccess,
        placeholder = previewPlaceholder
    )

    ProjectStoryCaptionedImage(
        modifier,
        painter,
        contentScale,
        sizeResolver,
        caption,
        link,
    )
}

@Composable
fun ProjectStoryCaptionedImage(
    modifier: Modifier = Modifier,
    asyncPainter: AsyncImagePainter,
    contentScale: ContentScale,
    sizeResolver: SizeResolver,
    caption: String?,
    link: String? = null,
) {
    val uriHandler = LocalUriHandler.current

    val clickableModifier =
        if (link.isNullOrBlank())
            Modifier
        else
            Modifier.clickable {
                uriHandler.openUri(link)
            }

    Column(
        modifier = Modifier
            .then(modifier)
            .then(clickableModifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ConstrainedImage(
                modifier = Modifier.fillMaxWidth(),
                painter = asyncPainter,
                contentDescription = caption,
                contentScale = contentScale,
                sizeResolver = sizeResolver,
                wrapContentHeightUnbounded = true
            )
            if (asyncPainter.state is AsyncImagePainter.State.Empty ||
                asyncPainter.state is AsyncImagePainter.State.Loading
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    color = grey_04,
                    trackColor = Color.Transparent
                )
            }
        }
        caption?.let {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(ProjectStoryCaptionedImageTestTag.CAPTION.name),
                text = caption,
                color = if (link.isNullOrBlank()) Color.Unspecified else StoryTheme.InlineStyles.link.color,
                fontStyle = FontStyle.Italic,
                textDecoration = if (link.isNullOrBlank()) null else TextDecoration.Underline,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/* A Modified `Image` component that is mean to be paired with
 * the use of `rememberAsyncImagePainter()` to enable behavior that
 * more closely matches `AsyncImage` in both constrained an unconstrained environments
 * (unconstrainted as in a LazyList or parent with a vertical/horizontalScroll modifier. */
@Composable
private fun ConstrainedImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    sizeResolver: SizeResolver, /* Probably make this nullable */
    wrapContentHeightUnbounded: Boolean = true
) {
    Image(
        modifier = Modifier
            .run { if (wrapContentHeightUnbounded) wrapContentHeight(unbounded = true) else this }
            .then(modifier)
            .then(sizeResolver as? ConstraintsSizeResolver ?: Modifier),
        painter = painter,
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}
