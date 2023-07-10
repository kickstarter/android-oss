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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

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

            Spacer(modifier = Modifier.height(8.dp))

            KSStepper(
                onPlusClicked = {},
                isPlusEnabled = false,
                onMinusClicked = {},
                isMinusEnabled = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            KSStepper(
                onPlusClicked = {},
                isPlusEnabled = true,
                onMinusClicked = {},
                isMinusEnabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

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
    isMinusEnabled: Boolean
) {

    Row(
        modifier = Modifier
            .height(36.dp)
            .width(108.dp)
    ) {
        Button(
            modifier = Modifier.width(54.dp),
            shape = RoundedCornerShape(
                topStart = 9.dp,
                bottomStart = 9.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colors.kds_white,
                disabledBackgroundColor = colors.kds_support_300
            ),
            onClick = onMinusClicked,
            enabled = isMinusEnabled,
            elevation = ButtonDefaults.elevation(0.dp)
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
            modifier = Modifier.width(54.dp),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                bottomStart = 0.dp,
                topEnd = 9.dp,
                bottomEnd = 9.dp
            ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colors.kds_white,
                disabledBackgroundColor = colors.kds_support_300
            ),
            onClick = onPlusClicked,
            enabled = isPlusEnabled,
            elevation = ButtonDefaults.elevation(0.dp)
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
