package com.kickstarter.features.pledgedprojectsoverview.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.TextWithStartIcon
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun BetaMessagingBottomSheetPreview() {
    KSTheme {
        BetaMessagingBottomSheet(
            onSeeAllBackedProjectsClick = {}
        )
    }
}

@Composable
fun BetaMessagingBottomSheet(
    onSeeAllBackedProjectsClick: () -> Unit,
    dismiss: () -> Unit = { }
) {
    Column(
        modifier = Modifier
            .background(color = colors.backgroundSurfacePrimary)
            .padding(start = dimensions.paddingLarge, end = dimensions.paddingLarge, bottom = dimensions.paddingLarge, top = dimensions.alertIconSize)
            .navigationBarsPadding()
            .fillMaxWidth(),
    ) {
        Text(
            text = stringResource(id = R.string.Introducing_the_backings_tab),
            style = typographyV2.headingXL,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingLarge))

        Text(
            text = stringResource(id = R.string.View_and_manage_your_backings_from_our_new_backings_dashboard),
            style = typographyV2.bodyLG,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        Text(
            text = stringResource(id = R.string.Currently_supported),
            style = typographyV2.subHeadlineMedium,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingSmall))

        Column(
            verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
        ) {
            TextWithStartIcon(
                modifier = Modifier,
                text = stringResource(id = R.string.Successfully_funded_backings),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_check_rounded),
                style = typographyV2.body,
                textColor = colors.textPrimary,
            )

            TextWithStartIcon(
                modifier = Modifier,
                text = stringResource(id = R.string.Important_project_alerts),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_check_rounded),
                style = typographyV2.body,
                textColor = colors.textPrimary,
            )
        }

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        Text(
            text = stringResource(id = R.string.Coming_soon),
            style = typographyV2.subHeadlineMedium,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingSmall))

        Column(
            verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
        ) {
            TextWithStartIcon(
                modifier = Modifier,
                text = stringResource(id = R.string.Sorting_and_filtering),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_task_to_do),
                style = typographyV2.body,
                textColor = colors.textPrimary,
            )

            TextWithStartIcon(
                modifier = Modifier,
                text = stringResource(id = R.string.tabbar_search),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_task_to_do),
                style = typographyV2.body,
                textColor = colors.textPrimary,

            )

            TextWithStartIcon(
                modifier = Modifier,
                text = stringResource(id = R.string.Live_backings),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_task_to_do),
                style = typographyV2.body,
                textColor = colors.textPrimary,

            )

            TextWithStartIcon(
                modifier = Modifier,
                text = stringResource(id = R.string.Unsuccessful_and_canceled_backings),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_task_to_do),
                style = typographyV2.body,
                textColor = colors.textPrimary,
            )
        }
        Spacer(modifier = Modifier.height(dimensions.paddingMedium))
        Text(
            text = stringResource(id = R.string.Live_and_unsuccessful_backings_can_currently_be_viewed_in_the_profile_tab),
            style = typographyV2.bodyLG,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingLarge))

        KSPrimaryGreenButton(
            modifier = Modifier.testTag(BetaMessagingBottomSheetTestTag.BACKED_PROJECTS_BUTTON.name),
            onClickAction = {
                onSeeAllBackedProjectsClick.invoke()
                dismiss.invoke()
            },
            text = stringResource(id = R.string.See_all_backed__projects),
            isEnabled = true
        )
    }
}

enum class BetaMessagingBottomSheetTestTag {
    BACKED_PROJECTS_BUTTON
}
