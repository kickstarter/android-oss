package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

enum class KSVideoFeedBannerTestTag {
    BANNER_CONTAINER,
    BANNER_TITLE,
    BANNER_DESCRIPTION,
    BANNER_BUTTON,
    BANNER_IMAGE
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSVideoFeedBannerPreview() {
    KSTheme {
        Box(
            modifier = Modifier
                .background(KSTheme.colors.kds_black)
                .padding(dimensions.paddingMedium)
        ) {
            KSVideoFeedBanner(onButtonClick = {})
        }
    }
}

/**
 * A banner component designed for discovery Video Feed promotions.
 *
 * @param modifier Modifier for the banner.
 * @param onButtonClick Action to perform when the button or banner is clicked.
 */
@Composable
fun KSVideoFeedBanner(
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.radiusMedium))
            .background(colors.discoveryBanner.background)
            .clickable(
                onClick = onButtonClick,
                role = Role.Button,
                onClickLabel = stringResource(id = R.string.fpo_try_it_now)
            )
            .padding(dimensions.paddingMedium)
            .testTag(KSVideoFeedBannerTestTag.BANNER_CONTAINER.name),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
        ) {
            Text(
                text = stringResource(id = R.string.fpo_try_our_new_discovery_mode),
                style = typographyV2.headingLG,
                color = colors.discoveryBanner.text,
                modifier = Modifier
                    .testTag(KSVideoFeedBannerTestTag.BANNER_TITLE.name)
                    .semantics { heading() }
            )

            Text(
                text = stringResource(id = R.string.fpo_swipe_through_a_video_feed_tuning_your_recommendations_along_the_way),
                style = typographyV2.bodyMD,
                color = colors.discoveryBanner.text,
                modifier = Modifier
                    .testTag(KSVideoFeedBannerTestTag.BANNER_DESCRIPTION.name)
            )

            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

            KSSmallButton(
                onClickAction = onButtonClick,
                isEnabled = true,
                backgroundColor = colors.discoveryBanner.buttonBackground,
                text = stringResource(id = R.string.fpo_try_it_now),
                textColor = colors.discoveryBanner.buttonText,
                radius = dimensions.pillButtonShapeSize,
                modifier = Modifier
                    .testTag(KSVideoFeedBannerTestTag.BANNER_BUTTON.name)
            )
        }

        Spacer(modifier = Modifier.width(dimensions.paddingMedium))

        Image(
            painter = painterResource(id = R.drawable.entrypoint_banner),
            contentDescription = stringResource(id = R.string.fpo_try_our_new_discovery_mode),
            modifier = Modifier
                .size(width = dimensions.discoveryBannerImageWidth, height = dimensions.discoveryBannerImageHeight)
                .testTag(KSVideoFeedBannerTestTag.BANNER_IMAGE.name),
            contentScale = ContentScale.Fit
        )
    }
}
