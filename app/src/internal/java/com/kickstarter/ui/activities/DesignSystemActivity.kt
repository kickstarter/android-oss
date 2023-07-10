package com.kickstarter.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.compose.designsystem.KSAlertDialog
import com.kickstarter.ui.compose.designsystem.KSAlertDialogNoHeadline
import com.kickstarter.ui.compose.designsystem.KSCheckbox
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSCoralBadge
import com.kickstarter.ui.compose.designsystem.KSFacebookButton
import com.kickstarter.ui.compose.designsystem.KSFullButtonFooter
import com.kickstarter.ui.compose.designsystem.KSGooglePayButton
import com.kickstarter.ui.compose.designsystem.KSGreenBadge
import com.kickstarter.ui.compose.designsystem.KSIntercept
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlueButton
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSRadioButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryGreyButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryRedButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryWhiteButton
import com.kickstarter.ui.compose.designsystem.KSSmallBlueButton
import com.kickstarter.ui.compose.designsystem.KSSmallButtonFooter
import com.kickstarter.ui.compose.designsystem.KSSmallRedButton
import com.kickstarter.ui.compose.designsystem.KSSmallWhiteButton
import com.kickstarter.ui.compose.designsystem.KSSnackbarError
import com.kickstarter.ui.compose.designsystem.KSSnackbarHeadsUp
import com.kickstarter.ui.compose.designsystem.KSSnackbarSuccess
import com.kickstarter.ui.compose.designsystem.KSStepper
import com.kickstarter.ui.compose.designsystem.KSStringDropdown
import com.kickstarter.ui.compose.designsystem.KSSwitch
import com.kickstarter.ui.compose.designsystem.KSTextInput
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.KsTooltip

class DesignSystemActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KSTheme {
                DesignSystemView()
            }
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun DesignSystemViewPreview() {
    KSTheme {
        DesignSystemView()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DesignSystemView() {
    val keyboardController = LocalSoftwareKeyboardController.current
    LazyColumn(
        Modifier
            .background(color = colors.kds_support_100)
            .fillMaxSize()
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null
            ) { keyboardController?.hide() },
        contentPadding = PaddingValues(8.dp)
    ) {
        item {
            AlertsVisuals()

            Spacer(modifier = Modifier.height(16.dp))

            ButtonsVisuals()

            Spacer(modifier = Modifier.height(16.dp))

            BadgesVisuals()

            Spacer(modifier = Modifier.height(12.dp))

            ControlsVisuals()

            Spacer(modifier = Modifier.height(12.dp))

            InputsVisuals()

            Spacer(modifier = Modifier.height(12.dp))

            ProgressIndicatorsVisuals()

            Spacer(modifier = Modifier.height(12.dp))

            FootersVisuals()

            Spacer(modifier = Modifier.height(12.dp))

            TypographyVisuals()

            Spacer(modifier = Modifier.height(24.dp))
        }
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
                setShowDialog = { showNoHeaderDialog = it },
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
            isEnabled = true
        )

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

        KSSecondaryWhiteButton(
            onClickAction = { showTooltip = true },
            text = "Show tooltip",
            isEnabled = true
        )

        if (showTooltip) {
            KsTooltip(
                setShowDialog = { showTooltip = it },
                headlineText = "Tooltip header",
                bodyText = "This is the tooltip!  Cool huh?"
            )
        }

        Spacer(Modifier.height(12.dp))

        var showIntercept by remember { mutableStateOf(false) }

        KSSecondaryGreyButton(
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

@Composable
fun ButtonsVisuals() {
    Column {
        Text(text = "Buttons", style = typography.title1Bold)

        Spacer(modifier = Modifier.height(12.dp))

        KSPrimaryGreenButton(onClickAction = { }, text = "Primary Green Button", isEnabled = true)

        Spacer(modifier = Modifier.height(12.dp))

        KSPrimaryBlueButton(onClickAction = { }, text = "Primary Blue Button", isEnabled = true)

        Spacer(modifier = Modifier.height(12.dp))

        KSPrimaryBlackButton(onClickAction = { }, text = "Primary Black Button", isEnabled = true)

        Spacer(modifier = Modifier.height(12.dp))

        KSSecondaryWhiteButton(
            onClickAction = { },
            text = "Secondary White Button",
            isEnabled = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        KSSecondaryGreyButton(onClickAction = { }, text = "Secondary Grey Button", isEnabled = true)

        Spacer(modifier = Modifier.height(12.dp))

        KSSecondaryRedButton(onClickAction = { }, text = "Secondary Red Button", isEnabled = true)

        Spacer(modifier = Modifier.height(12.dp))

        KSFacebookButton(onClickAction = { }, text = "Facebook Button", isEnabled = true)

        Spacer(modifier = Modifier.height(12.dp))

        KSGooglePayButton(onClickAction = { }, isEnabled = true)

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            KSSmallBlueButton(onClickAction = {}, text = "SMALL", isEnabled = true)

            Spacer(modifier = Modifier.width(12.dp))

            KSSmallRedButton(onClickAction = {}, text = "SMALL", isEnabled = true)

            Spacer(modifier = Modifier.width(12.dp))

            KSSmallWhiteButton(onClickAction = {}, text = "SMALL", isEnabled = true)
        }
    }
}

@Composable
fun BadgesVisuals() {
    Column {
        Text(text = "Badges", style = typography.title1Bold)

        Spacer(modifier = Modifier.height(12.dp))

        KSGreenBadge(text = "Green Badge")

        Spacer(modifier = Modifier.height(12.dp))

        KSCoralBadge(text = "Coral Badge")
    }
}

@Composable
fun ControlsVisuals() {
    Column {
        Text(text = "Controls", style = typography.title1Bold)

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            var switch1Checked by remember { mutableStateOf(true) }
            var switch2Checked by remember { mutableStateOf(false) }
            KSSwitch(
                checked = switch1Checked,
                onCheckChanged = { switch1Checked = it }
            )

            Spacer(modifier = Modifier.width(8.dp))

            KSSwitch(
                checked = switch2Checked,
                onCheckChanged = { switch2Checked = it }
            )

            Spacer(modifier = Modifier.width(8.dp))

            KSSwitch(checked = switch1Checked, onCheckChanged = {}, enabled = false)

            Spacer(modifier = Modifier.width(8.dp))

            KSSwitch(checked = switch2Checked, onCheckChanged = {}, enabled = false)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            var radioButtonSelected by remember { mutableStateOf(1) }

            KSRadioButton(
                selected = radioButtonSelected == 1,
                onClick = { radioButtonSelected = 1 }
            )

            Spacer(modifier = Modifier.width(8.dp))

            KSRadioButton(
                selected = radioButtonSelected == 2,
                onClick = { radioButtonSelected = 2 }
            )

            Spacer(modifier = Modifier.width(8.dp))

            KSRadioButton(
                selected = radioButtonSelected == 3,
                onClick = { radioButtonSelected = 3 }
            )

            Spacer(modifier = Modifier.width(8.dp))

            KSRadioButton(selected = true, onClick = {}, enabled = false)

            Spacer(modifier = Modifier.width(8.dp))

            KSRadioButton(selected = false, onClick = {}, enabled = false)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            var checkBox1 by remember { mutableStateOf(false) }
            var checkBox2 by remember { mutableStateOf(false) }
            var checkBox3 by remember { mutableStateOf(false) }

            KSCheckbox(checked = checkBox1, onCheckChanged = { checkBox1 = it })

            Spacer(modifier = Modifier.width(8.dp))

            KSCheckbox(checked = checkBox2, onCheckChanged = { checkBox2 = it })

            Spacer(modifier = Modifier.width(8.dp))

            KSCheckbox(checked = checkBox3, onCheckChanged = { checkBox3 = it })

            Spacer(modifier = Modifier.width(8.dp))

            KSCheckbox(checked = false, onCheckChanged = {}, enabled = false)

            Spacer(modifier = Modifier.width(8.dp))

            KSCheckbox(checked = true, onCheckChanged = {}, enabled = false)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            var count by remember { mutableStateOf(0) }
            KSStepper(
                onPlusClicked = { count++ },
                isPlusEnabled = count < 10,
                onMinusClicked = { count-- },
                isMinusEnabled = count > 0
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = "$$count", style = typography.body)
        }

        Spacer(modifier = Modifier.height(12.dp))

        KSStringDropdown(
            items = arrayOf("Coffee", "Soda", "Water", "Other"),
            onItemSelected = { _, _ -> })
    }
}

@Composable
fun InputsVisuals() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Inputs", style = typography.title1Bold)

        Spacer(modifier = Modifier.height(12.dp))

        val errorText = "ERROR"
        var errorState by remember { mutableStateOf(false) }
        var currentInput by remember { mutableStateOf("") }
        KSTextInput(
            modifier = Modifier.fillMaxWidth(),
            label = "Input Here",
            onValueChanged = { input ->
                errorState = input == errorText
                currentInput = input
            },
            isError = errorState,
            assistiveText = if (errorState) "This is an error!" else "Input ERROR to see an error",
            showAssistiveText = errorState || currentInput.isEmpty()
        )
    }
}

@Composable
fun ProgressIndicatorsVisuals() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Progress Indicators", style = typography.title1Bold)

        Spacer(modifier = Modifier.height(12.dp))

        KSLinearProgressIndicator(Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(12.dp))

        KSCircularProgressIndicator()

        Spacer(modifier = Modifier.height(12.dp))

        var progress by remember { mutableStateOf(0.0f) }

        KSStepper(
            onPlusClicked = { progress += 0.1f },
            isPlusEnabled = progress < 1f,
            onMinusClicked = { progress -= 0.1f },
            isMinusEnabled = progress > 0f
        )

        Spacer(modifier = Modifier.height(12.dp))

        KSLinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = progress)

        Spacer(modifier = Modifier.height(12.dp))

        KSCircularProgressIndicator(progress = progress)
    }
}

@Composable
fun FootersVisuals() {
    Column {
        Text(text = "Footers", style = typography.title1Bold)

        Spacer(modifier = Modifier.height(12.dp))

        KSFullButtonFooter(buttonText = "Back this project", onClickAction = {})

        Spacer(modifier = Modifier.height(12.dp))

        KSSmallButtonFooter(
            buttonText = "Manage",
            onClickAction = {},
            titleText = "You're a backer",
            subtitleText = "$24 Committed"
        )
    }
}

@Composable
fun TypographyVisuals() {
    Column {
        Text(text = "Typography", style = typography.title1Bold)

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Title 1", style = typography.title1)
        Text(text = "Title 1 Bold", style = typography.title1Bold)

        Text(text = "Title 2", style = typography.title2)
        Text(text = "Title 2 Bold", style = typography.title2Bold)

        Text(text = "Title 3", style = typography.title3)
        Text(text = "Title  Bold", style = typography.title3Bold)

        Text(text = "Headline", style = typography.headline)
        Text(text = "Body", style = typography.body)

        Text(text = "Callout", style = typography.callout)
        Text(text = "Callout Medium", style = typography.calloutMedium)

        Text(text = "Subheadline", style = typography.subheadline)
        Text(text = "Subheadline Medium", style = typography.subheadlineMedium)

        Text(text = "BUTTON TEXT", style = typography.buttonText)

        Text(text = "Body 2", style = typography.body2)
        Text(text = "Body 2 Medium", style = typography.body2Medium)

        Text(text = "Footnote", style = typography.footnote)
        Text(text = "Footnote Medium", style = typography.footnoteMedium)

        Text(text = "Caption 1", style = typography.caption1)
        Text(text = "Caption 1 Medium", style = typography.caption1Medium)

        Text(text = "Caption 2", style = typography.caption2)
        Text(text = "Caption 2 Medium", style = typography.caption2Medium)
    }
}