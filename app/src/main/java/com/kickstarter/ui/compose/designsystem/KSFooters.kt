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
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
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
                Text(text = titleText, style = typography.calloutMedium)
                Text(text = subtitleText, style = typography.caption1Medium)
            }

            KSSmallBlueButton(
                modifier = Modifier.height(48.dp),
                onClickAction = onClickAction,
                text = buttonText,
                isEnabled = enabled,
                radius = 12.dp
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
        elevation = 8.dp,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .background(
                    color = colors.kds_white,
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}
