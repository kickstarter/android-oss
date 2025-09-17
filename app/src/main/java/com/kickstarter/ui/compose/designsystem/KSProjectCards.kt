package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.models.Photo
import com.kickstarter.ui.activities.compose.search.CardProjectState
import com.kickstarter.ui.compose.KSAsyncImage
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSProjectCardsPreview() {
    KSTheme {
        KSProjectCardsColumn()
    }
}

@Composable
fun KSProjectCardsColumn() {
    Column(
        Modifier
            .background(color = colors.kds_white)
            .fillMaxWidth()
            .padding(dimensions.paddingSmall)
    ) {
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck",
            state = CardProjectState.LIVE,
            fundingInfoString = "4 days to go • 50% funded",
            fundedPercentage = 50,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.LATE_PLEDGES_ACTIVE,
            fundingInfoString = "Late pledges active",
            fundedPercentage = 100,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.LAUNCHING_SOON,
            fundingInfoString = "Launching soon",
            fundedPercentage = 0,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.ENDED_SUCCESSFUL,
            fundingInfoString = "Ended",
            fundedPercentage = 100,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.ENDED_UNSUCCESSFUL,
            fundingInfoString = "Ended",
            fundedPercentage = 50,
            onClick = {}
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMedium))

        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck",
            state = CardProjectState.LIVE,
            fundingInfoString = "4 days to go • 50% funded",
            fundedPercentage = 50,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.LATE_PLEDGES_ACTIVE,
            fundingInfoString = "Late pledges active",
            fundedPercentage = 100,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.LAUNCHING_SOON,
            fundingInfoString = "Launching soon",
            fundedPercentage = 0,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.ENDED_SUCCESSFUL,
            fundingInfoString = "Ended",
            fundedPercentage = 100,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.ENDED_UNSUCCESSFUL,
            fundingInfoString = "Ended",
            fundedPercentage = 50,
            onClick = {}
        )
    }
}

@Composable
fun KSProjectCardLarge(
    modifier: Modifier = Modifier,
    photo: Photo? = null,
    title: String,
    titleMinMaxLines: IntRange = 1..3,
    state: CardProjectState,
    fundingInfoString: String = "",
    fundedPercentage: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = colors.backgroundSurfaceRaised
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.grid_2)
        )
    ) {
        Column {
            if (photo != null) {
                KSAsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(dimensions.projectCardImageAspectRatio),
                    image = photo
                )
            }

            Column(
                modifier = Modifier.padding(dimensions.paddingMedium)
            ) {
                Text(
                    text = title,
                    style = typographyV2.headingLG,
                    color = colors.textPrimary,
                    minLines = titleMinMaxLines.first,
                    maxLines = titleMinMaxLines.last,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                KSFundingInfoRow(
                    state = state,
                    fundingInfoString = fundingInfoString,
                    textStyle = typographyV2.bodyMD
                )
            }
            if (state != CardProjectState.LAUNCHING_SOON) {
                KSFundingProgressIndicator(fundedPercentage / 100f, state)
            }
        }
    }
}

@Composable
fun KSProjectCardSmall(
    modifier: Modifier = Modifier,
    photo: Photo? = null,
    title: String,
    state: CardProjectState,
    fundingInfoString: String = "",
    fundedPercentage: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.backgroundSurfaceRaised
        ),
        shape = shapes.small,
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.grid_2)
        ),
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.height(dimensions.smallProjectCardImageHeight)
            ) {
                if (photo != null) {
                    KSAsyncImage(
                        modifier = Modifier
                            .height(dimensions.smallProjectCardImageHeight)
                            .aspectRatio(dimensions.projectCardImageAspectRatio),
                        image = photo
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
                        state = state,
                        fundingInfoString = fundingInfoString,
                        textStyle = typographyV2.bodyXS
                    )
                }
            }
            if (state != CardProjectState.LAUNCHING_SOON) {
                KSFundingProgressIndicator(fundedPercentage / 100f, state)
            }
        }
    }
}

@Composable
fun KSFundingInfoRow(
    state: CardProjectState,
    fundingInfoString: String,
    textStyle: TextStyle
) {
    return when (state) {
        CardProjectState.LATE_PLEDGES_ACTIVE -> StateFundingInfoRow(
            R.drawable.ic_late_pledges,
            "Late pledges active",
            fundingInfoString,
            textStyle
        )
        CardProjectState.LIVE -> StateFundingInfoRow(
            R.drawable.ic_clock,
            "Live",
            fundingInfoString,
            textStyle
        )
        CardProjectState.LAUNCHING_SOON -> StateFundingInfoRow(
            R.drawable.calendar,
            "Launching soon",
            fundingInfoString,
            textStyle
        )
        CardProjectState.ENDED_SUCCESSFUL -> StateFundingInfoRow(
            R.drawable.flag,
            "Ended",
            fundingInfoString,
            textStyle
        )
        CardProjectState.ENDED_UNSUCCESSFUL -> StateFundingInfoRow(
            R.drawable.flag,
            "Ended",
            fundingInfoString,
            textStyle
        )
    }
}

@Composable
fun StateFundingInfoRow(
    iconRes: Int,
    contentDescription: String,
    text: String,
    textStyle: TextStyle
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = colors.iconSubtle,
            modifier = Modifier.size(dimensions.paddingMedium)
        )
        Spacer(modifier = Modifier.width(dimensions.paddingXSmall))
        Text(
            text = text,
            style = textStyle,
            color = colors.textSecondary
        )
    }
}

@Composable
fun KSFundingProgressIndicator(progress: Float, state: CardProjectState) {
    if (state == CardProjectState.ENDED_UNSUCCESSFUL) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.linearProgressBarHeight),
            color = colors.borderBold,
            trackColor = colors.borderDisabled,
            gapSize = 0.dp,
            strokeCap = StrokeCap.Butt
        )
    } else {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.linearProgressBarHeight),
            color = colors.backgroundAccentGreenBrand,
            trackColor = colors.borderSubtle,
            gapSize = 0.dp,
            strokeCap = StrokeCap.Butt
        )
    }
}
