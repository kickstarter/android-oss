package com.kickstarter.features.rewardtracking

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.format
import com.kickstarter.ui.compose.TextWithStartIcon
import com.kickstarter.ui.compose.designsystem.KSButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun RewardTrackingActivityPreview() {
    KSTheme {
        RewardTrackingActivityFeed(
            trackingNumber = "123291242342",
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
            projectName = "This is a project name"
        )
    }
}

@Composable
fun RewardTrackingActivityFeed(
    modifier: Modifier = Modifier,
    trackingNumber : String,
    projectPhotoUrl: String? = null,
    projectName: String
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ProjectInfoHeader(
            projectPhotoUrl = projectPhotoUrl,
            projectName = projectName
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        Divider(
            color = colors.kds_support_200
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        Text(
            text = "2 days ago",
            style = typographyV2.bodyBoldXXS,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        Row {
            TextWithStartIcon(
                modifier = Modifier,
                text = stringResource(id = R.string.fpo_your_reward_has_shipped),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_shipping),
                style = typographyV2.headingLG,
                iconHeight = dimensions.imageSizeMedium,
                iconPadding = dimensions.paddingXSmall,
                textColor = colors.textPrimary,
                iconColor = colors.icon
            )
        }
        Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

        TrackingCardFooter(
            trackingNumber
        )

    }
}

@Composable
fun TrackingCardFooter(
    trackingNumber: String
) {
    Column {
        Text(
            text = stringResource(R.string.fpo_tracking_number).format(key1 = "tracking_number", value1 = trackingNumber),
            style = typographyV2.bodyMD,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        KSButton(
            modifier = Modifier,
            backgroundColor = colors.kds_black,
            textColor = colors.kds_white,
            onClickAction = { },
            shape = RoundedCornerShape(size = KSTheme.dimensions.radiusExtraSmall),
            text = stringResource(R.string.fpo_track_shipment),
            textStyle = typographyV2.buttonLabel,
            isEnabled = true
        )
    }

}

@Composable
fun ProjectInfoHeader(
    projectPhotoUrl : String? = null,
    projectName: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(projectPhotoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "project photo",
            modifier = Modifier
                .height(dimensions.activityFeedProjectImageHeight)
                .width(dimensions.activityFeedProjectImageWidth),
            placeholder = ColorPainter(color = colors.backgroundDisabled),
            contentScale = ContentScale.FillWidth
        )
        Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))

        Text(
            text = projectName,
            style = typographyV2.headingSM,
            color = colors.textPrimary
        )

    }
}

@Composable
fun RewardTrackingViewYourPledge(
) {
    Column(
        modifier = Modifier.background(colors.backgroundSurfacePrimary)
    ) {
        Row {
            TextWithStartIcon(
                modifier = Modifier,
                text = stringResource(id = R.string.fpo_your_reward_has_shipped),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_shipping),
                style = typographyV2.headingMD,
                textColor = colors.textPrimary,

                )
        }
    }
}
