package com.kickstarter.features.rewardtracking

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.format
import com.kickstarter.models.Photo
import com.kickstarter.ui.compose.KSAsyncImage
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
            projectName = "This is a project name",
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun RewardTrackingViewYourPledgePreview() {
    KSTheme {
        RewardTrackingViewYourPledge(
            trackingNumber = "123291242342",
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
        )
    }
}

@Composable
fun RewardTrackingActivityFeed(
    modifier: Modifier = Modifier,
    trackingNumber: String,
    photo: Photo? = null,
    projectName: String,
    projectClicked: () -> Unit = {},
    trackingButtonEnabled: Boolean = false,
    trackShipmentClicked: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ProjectInfoHeader(
            photo = photo,
            projectName = projectName,
            projectClicked = projectClicked,
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        Divider(
            color = colors.kds_support_200
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        RewardTrackingModal(
            trackingNumber,
            RewardTrackingPageType.ACTIVITY_FEED,
            trackShipmentClicked,
            trackingButtonEnabled
        )
    }
}

@Composable
fun RewardTrackingViewYourPledge(
    modifier: Modifier = Modifier,
    trackingNumber: String,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(dimensions.radiusMediumSmall), color = colors.kds_support_200)
            .padding(dimensions.paddingMedium)
    ) {

        RewardTrackingModal(
            trackingNumber,
            RewardTrackingPageType.VIEW_YOUR_PLEDGE,
        )
    }
}

@Composable
fun RewardTrackingModal(
    trackingNumber: String,
    pageType: RewardTrackingPageType,
    trackShipmentClicked: () -> Unit = {},
    trackingButtonEnabled: Boolean = false,
) {
    Column {
        Row {
            TextWithStartIcon(
                modifier = Modifier,
                text = stringResource(id = R.string.Your_reward_has_shipped),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_shipping),
                style = when (pageType) {
                    RewardTrackingPageType.VIEW_YOUR_PLEDGE -> typographyV2.headingMD
                    RewardTrackingPageType.ACTIVITY_FEED -> typographyV2.headingLG
                },
                iconHeight = dimensions.imageSizeMedium,
                iconPadding = dimensions.paddingXSmall,
                textColor = colors.textPrimary,
                iconColor = colors.icon
            )
        }

        Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

        Text(
            text = stringResource(R.string.Tracking_number).format(key1 = "number", value1 = trackingNumber),
            style = typographyV2.bodyMD,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

        KSButton(
            modifier = Modifier.testTag(RewardTrackingTestTag.TRACK_SHIPMENT_BUTTON.name),
            backgroundColor = colors.kds_black,
            textColor = colors.kds_white,
            onClickAction = trackShipmentClicked,
            shape = RoundedCornerShape(size = KSTheme.dimensions.radiusExtraSmall),
            text = stringResource(R.string.Track_shipment),
            textStyle = typographyV2.buttonLabel,
            isEnabled = trackingButtonEnabled
        )
    }
}

@Composable
fun ProjectInfoHeader(
    photo: Photo?,
    projectName: String,
    projectClicked: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { projectClicked.invoke() }.testTag(RewardTrackingTestTag.PROJECT_CARD_MODAL.name)
    ) {
        KSAsyncImage(
            image = photo,
            modifier = Modifier
                .height(dimensions.activityFeedProjectImageHeight)
                .width(dimensions.activityFeedProjectImageWidth),
        )

        Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))

        Text(
            text = projectName,
            style = typographyV2.headingSM,
            color = colors.textPrimary
        )
    }
}
enum class RewardTrackingTestTag(name: String) {
    TRACK_SHIPMENT_BUTTON("track_shipment_button"),
    PROJECT_CARD_MODAL("project_card_modal")
}

enum class RewardTrackingPageType(name: String) {
    VIEW_YOUR_PLEDGE("view_your_pledge"),
    ACTIVITY_FEED("activity_feed")
}
