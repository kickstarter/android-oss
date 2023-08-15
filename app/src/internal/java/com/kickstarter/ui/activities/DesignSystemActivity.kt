package com.kickstarter.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSAlertDialog
import com.kickstarter.ui.compose.designsystem.KSAlertDialogNoHeadline
import com.kickstarter.ui.compose.designsystem.KSCheckbox
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSClickableText
import com.kickstarter.ui.compose.designsystem.KSCoralBadge
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSErrorRoundedText
import com.kickstarter.ui.compose.designsystem.KSFacebookButton
import com.kickstarter.ui.compose.designsystem.KSFullButtonFooter
import com.kickstarter.ui.compose.designsystem.KSGooglePayButton
import com.kickstarter.ui.compose.designsystem.KSGreenBadge
import com.kickstarter.ui.compose.designsystem.KSHeadsUpRoundedText
import com.kickstarter.ui.compose.designsystem.KSHiddenTextInput
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
import com.kickstarter.ui.compose.designsystem.KSStepper
import com.kickstarter.ui.compose.designsystem.KSStringDropdown
import com.kickstarter.ui.compose.designsystem.KSSuccessRoundedText
import com.kickstarter.ui.compose.designsystem.KSSwitch
import com.kickstarter.ui.compose.designsystem.KSTextInput
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.compose.designsystem.KsTooltip
import com.kickstarter.ui.toolbars.compose.TopToolBar

class DesignSystemActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkMode = remember { mutableStateOf(false) }
            KickstarterApp(useDarkTheme = darkMode.value) {
                DesignSystemView(
                    darkMode = darkMode,
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun DesignSystemViewPreview() {
    val currentTheme = isSystemInDarkTheme()
    var darkMode = remember { mutableStateOf(currentTheme) }
    KSTheme(useDarkTheme = darkMode.value) {
        DesignSystemView(darkMode = darkMode, onBackClicked = {})
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DesignSystemView(darkMode: MutableState<Boolean>, onBackClicked: () -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            TopToolBar(
                title = "Design System",
                titleColor = colors.kds_black,
                leftIconColor = colors.kds_black,
                leftOnClickAction = onBackClicked,
                right = {
                    IconButton(
                        onClick = { darkMode.value = !darkMode.value },
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sun),
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.imageSizeLarge),
                            colorFilter = ColorFilter.tint(
                                color = colors.kds_black
                            )
                        )
                    }
                },
                backgroundColor = colors.kds_white
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .background(color = colors.kds_support_100)
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) { keyboardController?.hide() },
            contentPadding = PaddingValues(dimensions.paddingSmall)
        ) {
            item {
                AlertsVisuals()

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                ButtonsVisuals()

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                BadgesVisuals()

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                ControlsVisuals()

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                InputsVisuals()

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                ProgressIndicatorsVisuals()

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                FootersVisuals()

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                TypographyVisuals()

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingLarge))

                ClickableText()

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingLarge))

                Dividers()
            }
        }
    }
}

@Composable
fun AlertsVisuals() {
    Column {
        Text(text = "Alerts", style = typography.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSErrorRoundedText(text = "This is some sort of error, better do something about it.  Or don't, im just a text box!")

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSHeadsUpRoundedText(text = "Heads up, something is going on that needs your attention.  Maybe its important, maybe its informational.")

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSSuccessRoundedText(text = "Hey, something went right and all is good!")

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

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
        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

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

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

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

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

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
        Text(text = "Buttons", style = typography.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSPrimaryGreenButton(onClickAction = { }, text = "Primary Green Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSPrimaryBlueButton(onClickAction = { }, text = "Primary Blue Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSPrimaryBlackButton(onClickAction = { }, text = "Primary Black Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSSecondaryWhiteButton(
            onClickAction = { },
            text = "Secondary White Button",
            isEnabled = true
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSSecondaryGreyButton(onClickAction = { }, text = "Secondary Grey Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSSecondaryRedButton(onClickAction = { }, text = "Secondary Red Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSFacebookButton(onClickAction = { }, text = "Facebook Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSGooglePayButton(onClickAction = { }, isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row {
            KSSmallBlueButton(onClickAction = {}, text = "SMALL", isEnabled = true)

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingMediumSmall))

            KSSmallRedButton(onClickAction = {}, text = "SMALL", isEnabled = true)

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingMediumSmall))

            KSSmallWhiteButton(onClickAction = {}, text = "SMALL", isEnabled = true)
        }
    }
}

@Composable
fun BadgesVisuals() {
    Column {
        Text(text = "Badges", style = typography.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSGreenBadge(text = "Green Badge")

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSCoralBadge(text = "Coral Badge")
    }
}

@Composable
fun ControlsVisuals() {
    Column {
        Text(text = "Controls", style = typography.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row {
            var switch1Checked by remember { mutableStateOf(true) }
            var switch2Checked by remember { mutableStateOf(false) }
            KSSwitch(
                checked = switch1Checked,
                onCheckChanged = { switch1Checked = it }
            )

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSSwitch(
                checked = switch2Checked,
                onCheckChanged = { switch2Checked = it }
            )

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSSwitch(checked = switch1Checked, onCheckChanged = {}, enabled = false)

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSSwitch(checked = switch2Checked, onCheckChanged = {}, enabled = false)
        }

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row {
            var radioButtonSelected by remember { mutableStateOf(1) }

            KSRadioButton(
                selected = radioButtonSelected == 1,
                onClick = { radioButtonSelected = 1 }
            )

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSRadioButton(
                selected = radioButtonSelected == 2,
                onClick = { radioButtonSelected = 2 }
            )

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSRadioButton(
                selected = radioButtonSelected == 3,
                onClick = { radioButtonSelected = 3 }
            )

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSRadioButton(selected = true, onClick = {}, enabled = false)

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSRadioButton(selected = false, onClick = {}, enabled = false)
        }

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row {
            var checkBox1 by remember { mutableStateOf(false) }
            var checkBox2 by remember { mutableStateOf(false) }
            var checkBox3 by remember { mutableStateOf(false) }

            KSCheckbox(checked = checkBox1, onCheckChanged = { checkBox1 = it })

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSCheckbox(checked = checkBox2, onCheckChanged = { checkBox2 = it })

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSCheckbox(checked = checkBox3, onCheckChanged = { checkBox3 = it })

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSCheckbox(checked = false, onCheckChanged = {}, enabled = false)

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            KSCheckbox(checked = true, onCheckChanged = {}, enabled = false)
        }

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row(verticalAlignment = Alignment.CenterVertically) {
            var count by remember { mutableStateOf(0) }
            KSStepper(
                onPlusClicked = { count++ },
                isPlusEnabled = count < 10,
                onMinusClicked = { count-- },
                isMinusEnabled = count > 0
            )

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))

            Text(text = "$$count", style = typography.body)
        }

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSStringDropdown(
            items = arrayOf("Coffee", "Soda", "Water", "Other"),
            onItemSelected = { _, _ -> }
        )
    }
}

@Composable
fun InputsVisuals() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Inputs", style = typography.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

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

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSHiddenTextInput(modifier = Modifier.fillMaxWidth(), label = "Password")
    }
}

@Composable
fun ProgressIndicatorsVisuals() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Progress Indicators",
            style = typography.title1Bold,
            color = colors.kds_support_700
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSLinearProgressIndicator(Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSCircularProgressIndicator()

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        var progress by remember { mutableStateOf(0.0f) }

        KSStepper(
            onPlusClicked = { progress += 0.1f },
            isPlusEnabled = progress < 1f,
            onMinusClicked = { progress -= 0.1f },
            isMinusEnabled = progress > 0f
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSLinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = progress)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSCircularProgressIndicator(progress = progress)
    }
}

@Composable
fun FootersVisuals() {
    Column {
        Text(text = "Footers", style = typography.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSFullButtonFooter(buttonText = "Back this project", onClickAction = {})

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSSmallButtonFooter(
            buttonText = "Manage",
            onClickAction = {},
            titleText = "You're a backer",
            subtitleText = "$24 Committed"
        )
    }
}

@Composable
fun ClickableText() {
    Column {
        Text(text = "Clickable Text", style = typography.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSClickableText(
            resourceId = R.string.Learn_about_AI_policy_on_Kickstarter
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
    }
}

@Composable
fun Dividers() {
    Column {
        Text(text = "Dividers", style = typography.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSDividerLineGrey()

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
    }
}

@Composable
fun TypographyVisuals() {
    Column {
        Text(text = "Typography", style = typography.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Text(text = "Title 1", style = typography.title1, color = colors.kds_support_700)
        Text(text = "Title 1 Bold", style = typography.title1Bold, color = colors.kds_support_700)

        Text(text = "Title 2", style = typography.title2, color = colors.kds_support_700)
        Text(text = "Title 2 Bold", style = typography.title2Bold, color = colors.kds_support_700)

        Text(text = "Title 3", style = typography.title3, color = colors.kds_support_700)
        Text(text = "Title  Bold", style = typography.title3Bold, color = colors.kds_support_700)

        Text(text = "Headline", style = typography.headline, color = colors.kds_support_700)
        Text(text = "Body", style = typography.body, color = colors.kds_support_700)

        Text(text = "Callout", style = typography.callout, color = colors.kds_support_700)
        Text(
            text = "Callout Medium",
            style = typography.calloutMedium,
            color = colors.kds_support_700
        )

        Text(text = "Subheadline", style = typography.subheadline, color = colors.kds_support_700)
        Text(
            text = "Subheadline Medium",
            style = typography.subheadlineMedium,
            color = colors.kds_support_700
        )

        Text(text = "BUTTON TEXT", style = typography.buttonText)

        Text(text = "Body 2", style = typography.body2, color = colors.kds_support_700)
        Text(text = "Body 2 Medium", style = typography.body2Medium, color = colors.kds_support_700)

        Text(text = "Footnote", style = typography.footnote, color = colors.kds_support_700)
        Text(
            text = "Footnote Medium",
            style = typography.footnoteMedium,
            color = colors.kds_support_700
        )

        Text(text = "Caption 1", style = typography.caption1, color = colors.kds_support_700)
        Text(
            text = "Caption 1 Medium",
            style = typography.caption1Medium,
            color = colors.kds_support_700
        )

        Text(text = "Caption 2", style = typography.caption2, color = colors.kds_support_700)
        Text(
            text = "Caption 2 Medium",
            style = typography.caption2Medium,
            color = colors.kds_support_700
        )
    }
}
