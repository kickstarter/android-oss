package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.models.Photo
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSProjectCardsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_white)
                .fillMaxWidth()
                .padding(dimensions.paddingSmall)
        ) {
            KSProjectCardLarge(
                photo = Photo.builder().altText("").full("").build(),
                title = "Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck",
                isLaunched = true,
                timeRemainingString = "2 days left",
                fundedPercentage = 50,
                onClick = {}
            )
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSProjectCardLarge(
                photo = Photo.builder().altText("").full("").build(),
                title = "Cat Themed Pawker Deck",
                isLaunched = false,
                timeRemainingString = "2 days left",
                fundedPercentage = 0,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSProjectCardSmall(
                photo = Photo.builder().altText("").full("").build(),
                title = "Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck",
                isLaunched = false,
                timeRemainingString = "2 days left",
                fundedPercentage = 0,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSProjectCardSmall(
                photo = Photo.builder().altText("").full("").build(),
                title = "Cat Themed Pawker Deck",
                isLaunched = true,
                timeRemainingString = "2 days left",
                fundedPercentage = 50,
                onClick = {}
            )
        }
    }
}

@Composable
fun KSProjectCardLarge(
    modifier: Modifier = Modifier,
    photo: Photo? = null,
    title: String,
    isLaunched: Boolean,
    timeRemainingString: String = "",
    fundedPercentage: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick.invoke() },
        backgroundColor = colors.backgroundSurfaceRaised,
        shape = shapes.medium,
        elevation = dimensionResource(id = R.dimen.grid_2),
    ) {
        Column {
            if (photo != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.full())
                        .crossfade(true)
                        .build(),
                    contentDescription = photo.altText(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(dimensions.projectCardImageAspectRatio),
                    placeholder = ColorPainter(color = colors.backgroundDisabled),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(dimensions.paddingMedium)
            ) {
                Text(
                    text = title,
                    style = typographyV2.headingLG,
                    color = colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                KSFundingInfoRow(
                    isLaunched = isLaunched,
                    fundedPercentage = fundedPercentage,
                    timeRemainingString = timeRemainingString,
                    textStyle = typographyV2.bodyMD
                )
            }
            if (isLaunched) {
                KSLinearProgressIndicator(fundedPercentage / 100f)
            }
        }
    }
}

@Composable
fun KSProjectCardSmall(
    modifier: Modifier = Modifier,
    photo: Photo? = null,
    title: String,
    isLaunched: Boolean,
    timeRemainingString: String,
    fundedPercentage: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick.invoke() },
        backgroundColor = colors.backgroundSurfaceRaised,
        shape = shapes.small,
        elevation = dimensionResource(id = R.dimen.grid_2)
    ) {
        Column {
            Row(
                modifier = Modifier.height(dimensions.smallProjectCardImageHeight)
            ) {
                if (photo != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photo.full())
                            .crossfade(true)
                            .build(),
                        contentDescription = photo.altText(),
                        modifier = Modifier
                            .height(dimensions.smallProjectCardImageHeight)
                            .aspectRatio(dimensions.projectCardImageAspectRatio),
                        placeholder = ColorPainter(color = colors.backgroundDisabled),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.padding(dimensions.paddingSmall)) {
                    Text(
                        text = title,
                        style = typographyV2.headingMD,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    KSFundingInfoRow(
                        isLaunched = isLaunched,
                        fundedPercentage = fundedPercentage,
                        timeRemainingString = timeRemainingString,
                        textStyle = typographyV2.bodyXS
                    )
                }
            }
            if (isLaunched) {
                KSLinearProgressIndicator(fundedPercentage / 100f)
            }
        }
    }
}

@Composable
fun KSFundingInfoRow(
    isLaunched: Boolean,
    fundedPercentage: Int,
    timeRemainingString: String,
    textStyle: TextStyle
) {
    if (isLaunched || fundedPercentage > 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_clock),
                contentDescription = "Time Left",
                tint = colors.iconSubtle,
                modifier = Modifier.size(dimensions.paddingMedium)
            )
            Spacer(modifier = Modifier.width(dimensions.paddingXSmall))
            Text(
                text = "$timeRemainingString •  $fundedPercentage% " + stringResource(id = R.string.discovery_baseball_card_stats_funded),
                style = textStyle,
                color = colors.textSecondary
            )
        }
    } else {
        Text(
            text = stringResource(id = R.string.Coming_soon),
            style = typographyV2.bodyMD,
            color = colors.kds_create_700
        )
    }
}

@Composable
fun KSLinearProgressIndicator(progress: Float) {
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.linearProgressBarHeight),
        color = colors.textAccentGreen,
        backgroundColor = colors.borderSubtle
    )
}
