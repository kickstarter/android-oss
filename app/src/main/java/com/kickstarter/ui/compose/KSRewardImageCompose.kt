package com.kickstarter.ui.compose

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.kickstarter.models.Photo
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
fun KSRewardImageCompose(image: Photo) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(image.full())
            .crossfade(true)
            .build(),
        contentDescription = image.altText(),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(dimensions.rewardCardImageAspectRatio),
        contentScale = ContentScale.Crop
    ) {
        val state = painter.state
        if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
            LinearProgressIndicator(color = KSTheme.colors.backgroundDisabled)
        } else {
            SubcomposeAsyncImageContent()
        }
    }
}