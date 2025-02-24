package com.kickstarter.features.pledgedprojectsoverview.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_CATEGORY_NAME
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_LOCATION_NAME
import com.kickstarter.ui.compose.TextCaptionStyleWithStartIcon
import com.kickstarter.ui.compose.TextWithStartIcon
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2


@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun BetaMessagingBottomSheetPreview() {
    KSTheme {
            BetaMessagingBottomSheet()
    }
}

@Composable
fun BetaMessagingBottomSheet() {
    Column(
        modifier = Modifier
            .background(color = colors.backgroundSurfacePrimary)
            .padding(all = dimensions.paddingMedium)
            .fillMaxWidth(),
    ) {
        val blue = colors.backgroundAccentBlueBold

        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                modifier = Modifier.align(Alignment.Center).padding(bottom = dimensions.paddingMediumSmall),
                imageVector = ImageVector.vectorResource(id = R.drawable.drag_handle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = colors.textSecondary)
            )
        }
        Text(
            text = "Introducing the Backings tab",
            style = typographyV2.headingXL
        )

        Spacer(modifier = Modifier.height(dimensions.paddingLarge))

        Text(
            text = "View and manage your backings from our new Backings dashboard! More functionality will become available as our beta evolves.",
            style = typographyV2.bodyLG
        )
        Spacer(modifier = Modifier.height(dimensions.paddingLarge))

        Text(
            text = "Currently supported",
            style = typographyV2.subHeadlineMedium,
            color = colors.textSecondary
        )

        TextWithStartIcon(
            modifier = Modifier
                .testTag(PROJECT_LOCATION_NAME.name),
            text = "Successfully funded backings",
            imageVector = ImageVector.vectorResource(id = R.drawable.icon__check_green),
            style = typographyV2.body,
            textColor = colors.textPrimary,
            iconColor = colors.iconInfo
            
        )
    }
}