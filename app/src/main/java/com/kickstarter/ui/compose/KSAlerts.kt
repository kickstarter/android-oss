package com.kickstarter.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kickstarter.ui.extensions.safeLet

//Snackbars
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SnackbarsPreview () {
    KSTheme {
        Column() {
            KSSnackbarError(text = "This is some sort of error, better do something about it.  Or don't, im just a text box!")
            Spacer(Modifier.height(12.dp))
            KSSnackbarHeadsUp(text = "Heads up, something is going on that needs your attention.  Maybe its important, maybe its informational.")
            Spacer(Modifier.height(12.dp))
            KSSnackbarSuccess(text = "Hey, something went right and all is good!")
        }
    }
}

@Composable
fun KSSnackbar(
    background: Color,
    textColor: Color,
    text: String
) {
    Text(
        modifier = Modifier
            .background(background, shape = shapes.small)
            .fillMaxWidth()
            .padding(16.dp),
        text = text,
        color = textColor,
    )
}

@Composable
fun KSSnackbarError(text: String) {
    KSSnackbar(
        background = KSTheme.colors.kds_alert,
        textColor = KSTheme.colors.kds_white,
        text = text)
}

@Composable
fun KSSnackbarHeadsUp(text: String) {
    KSSnackbar(
        background = KSTheme.colors.kds_support_700,
        textColor = KSTheme.colors.kds_white,
        text = text)
}

@Composable
fun KSSnackbarSuccess(text: String) {
    KSSnackbar(
        background = KSTheme.colors.kds_create_300,
        textColor = KSTheme.colors.kds_support_700,
        text = text)
}

//Dialogs
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AlertDialogNoHeadlinePreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = KSTheme.colors.kds_support_400)
                .fillMaxWidth()
        ) {
            val showDialog = remember { mutableStateOf(true) }
            KSAlertDialogNoHeadline(
                setShowDialog = { showDialog.value },
                bodyText = "Alert dialog prompt",
                leftButtonText = "BUTTON",
                rightButtonText = "BUTTON"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AlertDialogPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = KSTheme.colors.kds_support_400)
                .fillMaxWidth()
        ) {
            val showDialog = remember { mutableStateOf(true) }
            KSAlertDialog(
                setShowDialog = { showDialog.value },
                headlineText = "Headline",
                bodyText = "Apparently we had reached a great height in the atmosphere for the...",
                leftButtonText = "BUTTON",
                rightButtonText = "BUTTON"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TooltipPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = KSTheme.colors.kds_support_400)
                .fillMaxWidth()
        ) {
            val showDialog = remember { mutableStateOf(true) }
            KsTooltip(
                setShowDialog = { showDialog.value },
                headlineText = "Short title",
                bodyText = "This text should be informative copy only. No actions should live in this tooltip.",
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun InterceptPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = KSTheme.colors.kds_support_400)
                .fillMaxWidth()
        ) {
            val showDialog = remember { mutableStateOf(true) }
            KSIntercept(
                setShowDialog = { showDialog.value },
                bodyText = "Take our survey to help us make a better app for you.",
                leftButtonText = "BUTTON",
                rightButtonText = "ACTION"
            )
        }
    }
}

@Composable
fun KSDialog(
    setShowDialog: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
        content()
    }
}

@Composable
fun KSDialogVisual(
    modifier: Modifier,
    setShowDialog: (Boolean) -> Unit,
    headline: String? = null,
    headlineStyle: TextStyle? = null,
    headlineSpacing: Dp? = null,
    bodyText: String,
    bodyStyle: TextStyle,
    rightButtonText: String? = null,
    rightButtonTextStyle: TextStyle? = null,
    rightButtonOverrideColor: Color? = null,
    rightButtonAction: (() -> Unit)? = null,
    rightButtonModifier: Modifier? = null,
    leftButtonText: String? = null,
    leftButtonTextStyle: TextStyle? = null,
    leftButtonOverrideColor: Color? = null,
    leftButtonAction: (() -> Unit)? = null,
    leftButtonModifier: Modifier? = null,
    additionalButtonSpacing: Dp? = null,
) {
    val rButtonMod = rightButtonModifier ?: Modifier
        .padding(8.dp)
        .clickable {
            rightButtonAction?.invoke()
            setShowDialog(false)
        }

    val lButtonMod = leftButtonModifier ?: Modifier
        .padding(8.dp)
        .clickable {
            leftButtonAction?.invoke()
            setShowDialog(false)
        }

    Column(
        modifier = modifier
    ) {
        //Headline
        safeLet(headline, headlineStyle, headlineSpacing) { text, style, space ->
            Text(text = text, style = style)
            Spacer(modifier = Modifier.height(space))
        }

        //Body
        Text(text = bodyText, style = bodyStyle)

        Row (
            Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            //Left Button
            safeLet(leftButtonText, leftButtonTextStyle) { text, style ->
                Text(
                    modifier = lButtonMod,
                    text = text,
                    style = style,
                    color = leftButtonOverrideColor ?: style.color)
            }

            additionalButtonSpacing?.let { space ->
                Spacer(modifier = Modifier.width(space))
            }

            //Right Button
            safeLet(rightButtonText, rightButtonTextStyle) { text, style ->
                Text(
                    modifier = rButtonMod,
                    text = text,
                    style = style,
                    color = rightButtonOverrideColor ?: style.color)
            }
        }
    }
}

@Composable
fun KSAlertDialogNoHeadline(
    setShowDialog: (Boolean) -> Unit,
    bodyText: String,
    leftButtonText: String,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String,
    rightButtonAction: (() -> Unit)? = null
) {
    KSDialog(
        setShowDialog = setShowDialog,
        content = {
            KSDialogVisual(
                modifier = Modifier
                    .width(280.dp)
                    .background(color = KSTheme.colors.kds_white, shape = shapes.small)
                    .padding(start = 24.dp, top = 24.dp, bottom = 8.dp, end = 8.dp),
                setShowDialog = setShowDialog,
                bodyText = bodyText,
                bodyStyle = KSTheme.typography.callout,
                leftButtonText = leftButtonText,
                leftButtonAction = leftButtonAction,
                leftButtonTextStyle = KSTheme.typography.buttonText,
                rightButtonText = rightButtonText,
                rightButtonAction = rightButtonAction,
                rightButtonTextStyle = KSTheme.typography.buttonText
            )
        }
    )
}

@Composable
fun KSAlertDialog(
    setShowDialog: (Boolean) -> Unit,
    headlineText: String,
    bodyText: String,
    leftButtonText: String,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String,
    rightButtonAction: (() -> Unit)? = null
) {
    KSDialog(
        setShowDialog = setShowDialog,
        content = {
            KSDialogVisual(
                modifier = Modifier
                    .width(280.dp)
                    .background(color = KSTheme.colors.kds_white, shape = shapes.small)
                    .padding(start = 24.dp, top = 24.dp, bottom = 8.dp, end = 8.dp),
                setShowDialog = setShowDialog,
                headline = headlineText,
                headlineStyle = KSTheme.typography.title3Bold,
                headlineSpacing = 16.dp,
                bodyText = bodyText,
                bodyStyle = KSTheme.typography.callout,
                leftButtonText = leftButtonText,
                leftButtonAction = leftButtonAction,
                leftButtonTextStyle = KSTheme.typography.buttonText,
                rightButtonText = rightButtonText,
                rightButtonAction = rightButtonAction,
                rightButtonTextStyle = KSTheme.typography.buttonText
            )
        }
    )
}

@Composable
fun KsTooltip(
    setShowDialog: (Boolean) -> Unit,
    headlineText: String,
    bodyText: String,
) {
    KSDialogVisual(modifier = Modifier
        .fillMaxWidth()
        .background(color = KSTheme.colors.kds_white)
        .padding(24.dp),
        setShowDialog = setShowDialog,
        bodyText = bodyText,
        bodyStyle = KSTheme.typography.body2,
        headline = headlineText,
        headlineStyle = KSTheme.typography.headline,
        headlineSpacing = 8.dp
    )
}

@Composable
fun KSIntercept(
    setShowDialog: (Boolean) -> Unit,
    bodyText: String,
    leftButtonText: String,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String,
    rightButtonAction: (() -> Unit)? = null
) {
    KSDialogVisual(modifier = Modifier
        .fillMaxWidth()
        .background(color = KSTheme.colors.kds_support_100, shape = shapes.small)
        .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 16.dp),
        setShowDialog = setShowDialog,
        bodyText = bodyText,
        bodyStyle = KSTheme.typography.calloutMedium,
        leftButtonText = leftButtonText,
        leftButtonAction = leftButtonAction,
        leftButtonTextStyle = KSTheme.typography.buttonText,
        leftButtonOverrideColor = facebook_blue,
        rightButtonText = rightButtonText,
        rightButtonAction = rightButtonAction,
        rightButtonTextStyle = KSTheme.typography.buttonText,
        rightButtonOverrideColor = KSTheme.colors.kds_white,
        rightButtonModifier = Modifier
            .background(color = KSTheme.colors.kds_trust_500, shape = shapes.small)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
            .clickable {
                rightButtonAction?.invoke()
                setShowDialog(false)
            },
        additionalButtonSpacing = 8.dp
    )
}

