package com.kickstarter.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.compose.KSAlertDialog
import com.kickstarter.ui.compose.KSAlertDialogNoHeadline
import com.kickstarter.ui.compose.KSIntercept
import com.kickstarter.ui.compose.KSPrimaryGreenButton
import com.kickstarter.ui.compose.KSSnackbarError
import com.kickstarter.ui.compose.KSSnackbarHeadsUp
import com.kickstarter.ui.compose.KSSnackbarSuccess
import com.kickstarter.ui.compose.KSTheme
import com.kickstarter.ui.compose.KSTheme.colors
import com.kickstarter.ui.compose.KSTheme.typography
import com.kickstarter.ui.compose.KsTooltip

class DesignSystemActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KSTheme {
                DesignSystemView()
            }
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DesignSystemViewPreview() {
    KSTheme {
        DesignSystemView()
    }
}

@Composable
fun DesignSystemView() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.kds_support_100)
            .padding(8.dp)
    ) {
        Column(Modifier.background(color = Color.Transparent)) {
            AlertsVisuals()
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AlertsPreview() {
    KSTheme {
        AlertsVisuals()
    }
}

@Composable
fun AlertsVisuals() {
    Column {
        Text(text = "Alerts", style = typography.title1Bold)

        Spacer(modifier = Modifier.height(12.dp))

        KSSnackbarError(text = "This is some sort of error, better do something about it.  Or don't, im just a text box!")

        Spacer(Modifier.height(12.dp))

        KSSnackbarHeadsUp(text = "Heads up, something is going on that needs your attention.  Maybe its important, maybe its informational.")

        Spacer(Modifier.height(12.dp))

        KSSnackbarSuccess(text = "Hey, something went right and all is good!")

        Spacer(Modifier.height(12.dp))

        var showNoHeaderDialog by remember { mutableStateOf(false) }

        KSPrimaryGreenButton(
            onClickAction = { showNoHeaderDialog = true },
            text = "Show no header dialog",
            isEnabled = true
        )

        if (showNoHeaderDialog) {
            KSAlertDialogNoHeadline(
                setShowDialog =  { showNoHeaderDialog = it },
                bodyText = "This is an example dialog with no header",
                leftButtonText = "Left Button",
                rightButtonText = "Right Button"
            )
        }
        Spacer(Modifier.height(12.dp))

        var showHeaderDialog by remember { mutableStateOf(false) }

        KSPrimaryGreenButton(
            onClickAction = { showHeaderDialog = true },
            text = "Show header dialog",
            isEnabled = true)

        if (showHeaderDialog) {
            KSAlertDialog(
                setShowDialog = { showHeaderDialog = it },
                headlineText = "Headline Here",
                bodyText = "This is an example dialog with a headline",
                leftButtonText = "Left Button",
                rightButtonText = "Right Button"
            )
        }

        Spacer(Modifier.height(12.dp))

        var showTooltip by remember { mutableStateOf(false) }

        KSPrimaryGreenButton(
            onClickAction = { showTooltip = true },
            text = "Show tooltip",
            isEnabled = true
        )

        if (showTooltip) {
            KsTooltip(
                setShowDialog = { showTooltip = it },
                headlineText = "Tooltip header",
                bodyText = "This is the tooltip!  Cool huh?")
        }

        Spacer(Modifier.height(12.dp))

        var showIntercept by remember { mutableStateOf(false) }

        KSPrimaryGreenButton(
            onClickAction = { showIntercept = true },
            text = "Show Intercept",
            isEnabled = true
        )

        if (showIntercept) {
            KSIntercept(
                setShowDialog = { showIntercept = it },
                bodyText = "This is an intercept message",
                leftButtonText = "Left Button",
                rightButtonText = "Right Button"
            )
        }
    }
}