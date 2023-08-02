package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FullButtonFooterPreview() {
    KSTheme {
        Column(modifier = Modifier.background(color = colors.kds_support_500)) {
            KSFullButtonFooter(buttonText = "Back this project", onClickAction = {})
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SmallButtonFooterPreview() {
    KSTheme {
        Column(modifier = Modifier.background(color = colors.kds_support_500)) {
            KSSmallButtonFooter(
                buttonText = "Manage",
                onClickAction = {},
                titleText = "You're a backer",
                subtitleText = "$24 Committed"
            )
        }
    }
}

@Composable
fun KSFullButtonFooter(
    buttonText: String,
    onClickAction: () -> Unit,
    enabled: Boolean = true
) {
    KSStandardFooter {
        KSPrimaryGreenButton(onClickAction = onClickAction, text = buttonText, isEnabled = enabled)
    }
}

@Composable
fun KSSmallButtonFooter(
    buttonText: String,
    onClickAction: () -> Unit,
    enabled: Boolean = true,
    titleText: String,
    subtitleText: String
) {
    KSStandardFooter {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = titleText,
                    style = typography.calloutMedium,
                    color = colors.kds_support_700
                )
                Text(
                    text = subtitleText,
                    style = typography.caption1Medium,
                    color = colors.kds_support_700
                )
            }

            KSSmallBlueButton(
                modifier = Modifier.height(dimensions.minButtonHeight),
                onClickAction = onClickAction,
                text = buttonText,
                isEnabled = enabled,
                radius = dimensions.radiusMedium
            )
        }
    }
}

@Composable
fun KSStandardFooter(
    content: @Composable () -> Unit
) {
    Surface(
        color = Color.Transparent,
        elevation = dimensions.elevationMedium,
        shape = RoundedCornerShape(
            topStart = dimensions.radiusLarge,
            topEnd = dimensions.radiusLarge
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.footerHeight)
                .background(
                    color = colors.kds_white,
                    shape = RoundedCornerShape(
                        topStart = dimensions.radiusLarge,
                        topEnd = dimensions.radiusLarge
                    )
                )
                .padding(dimensions.paddingMedium)
        ) {
            content()
        }
    }
}
