package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSStepperPreview() {
    KSTheme {
        Column {
            KSStepper(
                onPlusClicked = {},
                isPlusEnabled = true,
                onMinusClicked = {},
                isMinusEnabled = true
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSStepper(
                onPlusClicked = {},
                isPlusEnabled = false,
                onMinusClicked = {},
                isMinusEnabled = true
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSStepper(
                onPlusClicked = {},
                isPlusEnabled = true,
                onMinusClicked = {},
                isMinusEnabled = false
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSStepper(
                onPlusClicked = {},
                isPlusEnabled = false,
                onMinusClicked = {},
                isMinusEnabled = false
            )
        }
    }
}

@Composable
fun KSStepper(
    onPlusClicked: () -> Unit,
    isPlusEnabled: Boolean,
    onMinusClicked: () -> Unit,
    isMinusEnabled: Boolean,
    enabledButtonBackgroundColor: Color = colors.backgroundAccentGraySubtle
) {

    Row(
        modifier = Modifier
            .height(dimensions.stepperHeight)
            .width(dimensions.stepperWidth)
    ) {
        Button(
            modifier = Modifier.width(dimensions.stepperButtonWidth),
            shape = RoundedCornerShape(
                topStart = dimensions.radiusMediumSmall,
                bottomStart = dimensions.radiusMediumSmall,
                topEnd = dimensions.none,
                bottomEnd = dimensions.none
            ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = enabledButtonBackgroundColor,
                disabledBackgroundColor = colors.backgroundActionDisabled
            ),
            onClick = onMinusClicked,
            enabled = isMinusEnabled,
            elevation = ButtonDefaults.elevation(dimensions.none)
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.ic_minus
                ),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color =
                    if (isMinusEnabled) colors.kds_create_700
                    else colors.kds_support_400
                )
            )
        }

        Button(
            modifier = Modifier.width(dimensions.stepperButtonWidth),
            shape = RoundedCornerShape(
                topStart = dimensions.none,
                bottomStart = dimensions.none,
                topEnd = dimensions.radiusMediumSmall,
                bottomEnd = dimensions.radiusMediumSmall
            ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = enabledButtonBackgroundColor,
                disabledBackgroundColor = colors.backgroundActionDisabled
            ),
            onClick = onPlusClicked,
            enabled = isPlusEnabled,
            elevation = ButtonDefaults.elevation(dimensions.none)
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.ic_plus
                ),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color =
                    if (isPlusEnabled) colors.kds_create_700
                    else colors.kds_support_400
                )
            )
        }
    }
}
