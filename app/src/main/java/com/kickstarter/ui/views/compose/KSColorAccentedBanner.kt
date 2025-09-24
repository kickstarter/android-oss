package com.kickstarter.ui.views.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSOutlinedButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Preview
@Composable
fun KSColorAccentedBannerPreview() {
    KSColorAccentedBanner(
        imageResToDisplay = R.drawable.ic_alert_diamond,
        titleResToDisplay = R.string.project_project_notices_header,
        textResToDisplay = R.string.project_project_notices_notice_intro,
        buttonTextResToDisplay = R.string.project_project_notices_notice_cta,
        textColor = colors.textPrimary,
        backgroundColor = colors.backgroundDangerSubtle,
        iconColor = colors.iconDanger,
        accentColor = colors.backgroundDangerBoldPressed
    )
}

@Composable
fun KSColorAccentedBanner(
    imageResToDisplay: Int,
    titleResToDisplay: Int,
    textResToDisplay: Int,
    buttonTextResToDisplay: Int,
    textColor: Color,
    backgroundColor: Color,
    iconColor: Color,
    accentColor: Color,
    onClickAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .background(
                color = accentColor,
                shape = RoundedCornerShape(
                    dimensions.radiusSmall
                )
            )
            .padding(
                start = dimensions.paddingXSmall
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(
                    topStart = dimensions.none,
                    topEnd = dimensions.radiusSmall,
                    bottomStart = dimensions.none,
                    bottomEnd = dimensions.radiusSmall
                )
            )
            .padding(
                start = dimensions.paddingMediumSmall, // accounts for accent width
                top = dimensions.paddingMedium,
                bottom = dimensions.paddingMediumSmall, // due to built-in button margins
                end = dimensions.paddingMedium
            )
    ) {
        Image(
            painter = painterResource(id = imageResToDisplay),
            colorFilter = ColorFilter.tint(color = iconColor),
            contentDescription = stringResource(id = titleResToDisplay),
            modifier = Modifier
                .size(dimensions.iconSizeMedium)
        )

        Column(
            modifier = Modifier.padding(start = dimensions.paddingSmall)
        ) {
            Text(
                text = stringResource(id = titleResToDisplay),
                color = textColor,
                style = typographyV2.bodyBoldMD
            )

            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

            Text(
                text = stringResource(id = textResToDisplay),
                color = textColor,
                style = typographyV2.bodyMD
            )

            Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

            KSOutlinedButton(
                backgroundColor = backgroundColor,
                text = stringResource(buttonTextResToDisplay),
                onClickAction = { onClickAction?.invoke() }
            )
        }
    }
}
