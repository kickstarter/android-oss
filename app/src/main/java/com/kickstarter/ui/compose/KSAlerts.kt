package com.kickstarter.ui.compose

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
@Preview
@Composable
fun SnackbarsPreview () {
    Column() {
        KSSnackbarError(text = "This is some sort of error, better do something about it.  Or don't, im just a text box!")
        Spacer(Modifier.height(12.dp))
        KSSnackbarHeadsUp(text = "Heads up, something is going on that needs your attention.  Maybe its important, maybe its informational.")
        Spacer(Modifier.height(12.dp))
        KSSnackbarSuccess(text = "Hey, something went right and all is good!")
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
    KSSnackbar(background = kds_alert, textColor = kds_white, text = text)
}

@Composable
fun KSSnackbarHeadsUp(text: String) {
    KSSnackbar(background = kds_support_700, textColor = kds_white, text = text)
}

@Composable
fun KSSnackbarSuccess(text: String) {
    KSSnackbar(background = kds_create_300, textColor = kds_support_700, text = text)
}

//Dialogs
@Preview
@Composable
fun AlertDialogNoHeadlinePreview() {
    Column(
        Modifier
            .background(color = kds_support_400)
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

@Preview
@Composable
fun AlertDialogPreview() {
    Column(
        Modifier
            .background(color = kds_support_400)
            .fillMaxWidth()
    ) {
        val showDialog = remember { mutableStateOf(true) }
        KSAlertDialog(
            setShowDialog = { showDialog.value },
            headline = "Headline",
            bodyText = "Apparently we had reached a great height in the atmosphere for the...",
            leftButtonText = "BUTTON",
            rightButtonText = "BUTTON"
        )
    }
}

@Composable
fun KSDialog(
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
    leftButtonText: String? = null,
    leftButtonTextStyle: TextStyle? = null,
    leftButtonOverrideColor: Color? = null,
    leftButtonAction: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
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
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                leftButtonAction?.invoke()
                                setShowDialog(false)
                            },
                        text = text,
                        style = style,
                        color = leftButtonOverrideColor ?: style.color)
                }

                //Right Button
                safeLet(rightButtonText, rightButtonTextStyle) { text, style ->
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                rightButtonAction?.invoke()
                                setShowDialog(false)
                            },
                        text = text,
                        style = style,
                        color = rightButtonOverrideColor ?: style.color)
                }
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
        modifier = Modifier
            .width(280.dp)
            .background(color = kds_white, shape = shapes.small)
            .padding(16.dp),
        bodyText = bodyText,
        bodyStyle = callout,
        leftButtonText = leftButtonText,
        leftButtonAction = leftButtonAction,
        leftButtonTextStyle = buttonText,
        rightButtonText = rightButtonText,
        rightButtonAction = rightButtonAction,
        rightButtonTextStyle = buttonText)
}

@Composable
fun KSAlertDialog(
    setShowDialog: (Boolean) -> Unit,
    headline: String,
    bodyText: String,
    leftButtonText: String,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String,
    rightButtonAction: (() -> Unit)? = null
) {
    KSDialog(
        setShowDialog = setShowDialog,
        modifier = Modifier
            .width(280.dp)
            .background(color = kds_white, shape = shapes.small)
            .padding(16.dp),
        headline = headline,
        headlineStyle = title3Bold,
        headlineSpacing = 16.dp,
        bodyText = bodyText,
        bodyStyle = callout,
        leftButtonText = leftButtonText,
        leftButtonAction = leftButtonAction,
        leftButtonTextStyle = buttonText,
        rightButtonText = rightButtonText,
        rightButtonAction = rightButtonAction,
        rightButtonTextStyle = buttonText)
}

