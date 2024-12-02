package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSBadgesPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_white)
                .fillMaxWidth()
                .padding(dimensions.paddingSmall)
        ) {

            KSGreenBadge(text = "Add-ons available")

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSCoralBadge(text = "3 days left")

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSAlertBadge(
                icon = ImageVector.vectorResource(id = R.drawable.ic_alert),
                message = "Payment Failed"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSWarningBadge(
                icon = ImageVector.vectorResource(id = R.drawable.ic_clock),
                message = "Address locks in 7 days"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSBetaBadge()
        }
    }
}

@Composable
fun KSGreenBadge(
    leadingIcon: @Composable () -> Unit = {},
    text: String,
    textColor: Color = colors.textAccentGreen
) {
    Row(
        modifier = Modifier
            .background(
                color = colors.backgroundAccentGreenSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingMediumSmall,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                end = dimensions.paddingMediumSmall
            )
    ) {
        leadingIcon()

        Text(
            text = text,
            color = textColor,
            style = typography.footnoteMedium
        )
    }
}

@Composable
fun KSCoralBadge(
    leadingIcon: @Composable () -> Unit = {},
    text: String,
    textColor: Color = colors.textSecondary
) {
    Row(
        modifier = Modifier
            .background(
                color = colors.backgroundDangerSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingMediumSmall,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                end = dimensions.paddingMediumSmall
            )
    ) {
        leadingIcon()

        Text(
            text = text,
            color = textColor,
            style = typography.footnoteMedium
        )
    }
}

@Composable
fun KSAlertBadge(
    icon: ImageVector?,
    message: String?
) {
    if (!message.isNullOrEmpty()) {
        Row(
            modifier = Modifier
                .background(
                    color = colors.backgroundDangerSubtle,
                    shape = shapes.small
                )
                .padding(
                    start = dimensions.paddingMediumSmall,
                    top = dimensions.paddingSmall,
                    bottom = dimensions.paddingSmall,
                    end = dimensions.paddingMediumSmall
                )
        ) {
            if (icon != null) {
                Image(
                    modifier = Modifier
                        .padding(end = dimensions.paddingXSmall)
                        .size(dimensions.alertIconSize),
                    imageVector = icon,
                    contentDescription = message,
                    colorFilter = ColorFilter.tint(colors.textAccentRedBold)
                )
            }

            Text(
                text = message,
                color = colors.textAccentRedBold,
                style = typography.footnoteMedium
            )
        }
    }
}

@Composable
fun KSWarningBadge(
    icon: ImageVector?,
    message: String?
) {
    if (!message.isNullOrEmpty()) {
        Row(
            modifier = Modifier
                .background(
                    color = colors.backgroundAccentOrangeSubtle,
                    shape = shapes.small
                )
                .padding(
                    start = dimensions.paddingMediumSmall,
                    top = dimensions.paddingSmall,
                    bottom = dimensions.paddingSmall,
                    end = dimensions.paddingMediumSmall
                )
        ) {
            if (icon != null) {
                Image(
                    modifier = Modifier
                        .padding(end = dimensions.paddingXSmall)
                        .size(dimensions.alertIconSize),
                    imageVector = icon,
                    contentDescription = message,
                    colorFilter = ColorFilter.tint(colors.textSecondary)
                )
            }
            Text(
                text = message,
                color = colors.textSecondary,
                style = typography.footnoteMedium
            )
        }
    }
}

@Composable
fun KSBetaBadge() {
    Text(
        modifier = Modifier
            .background(
                color = colors.backgroundAccentGreenSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingXSmall,
                top = dimensions.paddingXSmall,
                bottom = dimensions.paddingXSmall,
                end = dimensions.paddingXSmall
            ),
        text = stringResource(R.string.Beta).uppercase(),
        color = colors.textAccentGreen,
        style = typography.caption2Medium
    )
}
