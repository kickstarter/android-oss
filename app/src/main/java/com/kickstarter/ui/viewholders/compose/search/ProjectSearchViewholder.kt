package com.kickstarter.ui.viewholders.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
fun ProjectSearchViewHolderPreview() {
    KSTheme {
        Column {
            ProjectSearchViewHolder(
                title = "This is a test This is a test This is a test This is a test",
                isLaunched = true,
                fundedAmount = 224,
                timeRemainingString = "24 days to go",
                onClickAction = {}
            )

            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

            ProjectSearchViewHolder(
                title = "This is a test This is a test This is a test This is a test",
                isLaunched = false,
                onClickAction = {}
            )
        }
    }
}

@Composable
fun ProjectSearchViewHolder(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    imageContentDescription: String? = null,
    title: String,
    isLaunched: Boolean,
    fundedAmount: Int = 0,
    timeRemainingString: String = "",
    onClickAction: () -> Unit
) {
    Row(
        modifier = modifier
            .background(color = colors.kds_white)
            .padding(all = dimensions.paddingSmall)
            .fillMaxWidth()
            .clickable { onClickAction.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = imageContentDescription,
            modifier = Modifier
                .width(dimensions.projectSearchImageWidth)
                .height(dimensions.projectSearchImageHeight),
            placeholder = ColorPainter(color = colors.backgroundDisabled),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))

        Column {
            Text(text = title, style = typography.calloutMedium, color = colors.kds_support_700)

            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

            if (isLaunched || fundedAmount > 0) {
                Row {
                    Text(
                        text = "$fundedAmount%",
                        style = typography.body2Medium,
                        color = colors.kds_create_700
                    )
                    Text(
                        text = " " + stringResource(id = R.string.discovery_baseball_card_stats_funded),
                        style = typography.body2,
                        color = colors.kds_support_500
                    )

                    Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))

                    Text(
                        text = timeRemainingString,
                        style = typography.body2Medium,
                        color = colors.kds_support_700
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.Coming_soon),
                    style = typography.body2Medium,
                    color = colors.kds_create_700
                )
            }
        }
    }
}
