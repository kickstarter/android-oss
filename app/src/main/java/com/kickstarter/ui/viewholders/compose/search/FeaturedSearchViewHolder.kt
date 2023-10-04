package com.kickstarter.ui.viewholders.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FeaturedSearchViewHolderPreview() {
    KSTheme {
        Column {
            FeaturedSearchViewHolder(
                title = "This is a Test This is a Test This is a Test This is a Test",
                isLaunched = true,
                fundedAmount = 224,
                timeRemainingString = "24 days to go",
                onClickAction = {}
            )

            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

            FeaturedSearchViewHolder(
                title = "This is a Test This is a Test This is a Test This is a Test",
                isLaunched = false,
                onClickAction = {}
            )
        }
    }
}

@Composable
fun FeaturedSearchViewHolder(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    imageContentDescription: String? = null,
    title: String,
    isLaunched: Boolean,
    fundedAmount: Int = 0,
    timeRemainingString: String = "",
    onClickAction: () -> Unit
) {
    Column(
        modifier = modifier
            .background(color = colors.kds_white)
            .border(width = 1.dp, color = colors.kds_support_300)
            .padding(bottom = dimensions.paddingMediumSmall)
            .clickable { onClickAction.invoke() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = imageContentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.featuredSearchImageHeight),
            placeholder = ColorPainter(color = colors.backgroundDisabled),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        Text(
            text = title,
            modifier = Modifier
                .padding(start = dimensions.paddingSmall, end = dimensions.paddingSmall)
                .fillMaxWidth(),
            style = typography.title3Bold,
            color = colors.kds_black
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        if (isLaunched || fundedAmount > 0) {
            Row {
                Text(
                    text = "$fundedAmount%",
                    modifier = Modifier.padding(start = dimensions.paddingSmall),
                    style = typography.subheadlineMedium,
                    color = colors.kds_create_700
                )
                Text(
                    text = " " + stringResource(id = R.string.discovery_baseball_card_stats_funded),
                    style = typography.subheadline,
                    color = colors.kds_support_500
                )

                Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))

                Text(
                    text = timeRemainingString,
                    modifier = Modifier.padding(start = dimensions.paddingSmall),
                    style = typography.subheadlineMedium,
                    color = colors.kds_support_700
                )
            }
        } else {
            Text(
                text = stringResource(id = R.string.Coming_soon),
                modifier = Modifier.padding(start = dimensions.paddingSmall),
                style = typography.subheadlineMedium,
                color = colors.kds_create_700
            )
        }
    }
}
