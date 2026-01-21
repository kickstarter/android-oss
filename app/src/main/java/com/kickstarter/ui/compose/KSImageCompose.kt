package com.kickstarter.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.models.Photo
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSImagePreview() {
    KSTheme {
        Column {
            val photo = Photo.builder().full("").altText("").build()
            KSRewardAsyncImage(photo)
            KSAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(dimensions.rewardCardImageAspectRatio),
                image = photo
            )
            CircleImageFromURl(
                imageUrl = ("http://goo.gl/gEgYUd"),
                modifier = Modifier.size(dimensionResource(id = R.dimen.profile_avatar_width))
            )
            ProjectImageFromURl(
                imageUrl = ("http://goo.gl/gEgYUd"),
                modifier = Modifier.fillMaxWidth().height(dimensionResource(id = R.dimen.profile_avatar_width))
            )
            KSCircleImage(
                url = ("http://goo.gl/gEgYUd"),
                modifier = Modifier.size(dimensionResource(id = R.dimen.profile_avatar_width))
            )
        }
    }
}

@Composable
fun KSRewardAsyncImage(image: Photo) {
    KSAsyncImage(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(dimensions.rewardCardImageAspectRatio),
        image = image
    )
}

@Composable
fun KSAsyncImage(modifier: Modifier, image: Photo?) {
    AsyncImage(
        model = image?.let {
            ImageRequest.Builder(LocalContext.current)
                .data(image.full())
                .crossfade(true)
                .build()
        },
        contentDescription = image?.altText(),
        modifier = modifier,
        placeholder = ColorPainter(color = colors.backgroundDisabled),
        error = ColorPainter(color = colors.backgroundDisabled),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ProjectImageFromURl(
    imageUrl: String?,
    modifier: Modifier
) {
    Image(
        painter = rememberAsyncImagePainter(
            model = imageUrl
        ),
        contentDescription = "Contact picture",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.kds_support_100,
                        colors.kds_white
                    ),
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(0f, 0f)
                )
            )
    )
}

@Composable
fun CircleImageFromURl(
    imageUrl: String?,
    modifier: Modifier,
    contentDescription: String = "Contact picture",
    colorFilter: ColorFilter? = null
) {
    Image(
        painter = rememberAsyncImagePainter(
            model = imageUrl
        ),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(CircleShape)
            .background(
                colors.kds_support_300,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.grid_1))
            ),
        colorFilter = colorFilter
    )
}

@Composable
fun KSCircleImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholderColor: Color = colors.backgroundDisabled,
    errorColor: Color = colors.backgroundDisabled,
) {
    val context = LocalContext.current

    val model = remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(CircleShape)
            .background(placeholderColor),
        placeholder = ColorPainter(placeholderColor),
        error = ColorPainter(errorColor)
    )
}
