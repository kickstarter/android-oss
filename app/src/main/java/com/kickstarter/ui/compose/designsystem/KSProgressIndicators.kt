package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun IndicatorsPreview() {
    KSTheme {
        Column(Modifier.padding(dimensions.paddingSmall)) {
            KSLinearProgressIndicator()

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSLinearProgressIndicator(progress = 0.5f)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSCircularProgressIndicator()

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSCircularProgressIndicator(progress = 0.5f)
        }
    }
}

@Composable
fun KSLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    if (progress != null) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
            color = colors.kds_create_700
        )
    } else {
        LinearProgressIndicator(modifier = modifier, color = colors.kds_create_700)
    }
}

@Composable
fun KSCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    if (progress != null) {
        CircularProgressIndicator(
            progress = progress,
            modifier = modifier,
            color = colors.kds_create_700
        )
    } else {
        CircularProgressIndicator(modifier = modifier, color = colors.kds_create_700)
    }
}
