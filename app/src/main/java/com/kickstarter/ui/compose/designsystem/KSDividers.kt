package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PreviewKSDividers() {
    KSTheme {
        Column(
            modifier = Modifier.background(KSTheme.colors.kds_white)
        ) {
            KSDividerLineGrey()
        }
    }
}

@Composable
fun KSDividerLineGrey(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = KSTheme.colors.kds_support_300
            )
            .height(KSTheme.dimensions.dividerThickness)
    )
}
