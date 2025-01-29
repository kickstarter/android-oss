package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import com.kickstarter.libs.utils.safeLet
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

// Snackbars
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SnackbarsPreview() {
    KSTheme {
        Column() {
            KSErrorRoundedText(text = "This is some sort of error, better do something about it.  Or don't, im just a text box!")
            Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))
            KSHeadsUpRoundedText(text = "Heads up, something is going on that needs your attention.  Maybe its important, maybe its informational.")
            Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))
            KSSuccessRoundedText(text = "Hey, something went right and all is good!")
        }
    }
}

enum class KSSnackbarTypes {
    KS_ERROR,
    KS_HEADS_UP,
    KS_SUCCESS
}

@Composable
fun KSErrorSnackbar(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.none)
) {
    Snackbar(
        backgroundColor = colors.backgroundDangerBold,
        content = {
            KSErrorRoundedText(text = text, padding = padding)
        }
    )
}

@Composable
fun KSHeadsupSnackbar(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.none)
) {
    Snackbar(
        backgroundColor = colors.backgroundActionPressed,
        content = {
            KSHeadsUpRoundedText(text = text, padding = padding)
        }
    )
}

@Composable
fun KSSuccessSnackbar(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.none)
) {
    Snackbar(
        backgroundColor = colors.backgroundAccentGreenSubtle,
        content = {
            KSSuccessRoundedText(text = text, padding = padding)
        }
    )
}

@Composable
fun KSRoundedPaddedText(
    background: Color,
    textColor: Color,
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.paddingMedium)
) {
    Text(
        modifier = Modifier
            .background(background, shape = shapes.small)
            .fillMaxWidth()
            .padding(padding),
        text = text,
        color = textColor,
    )
}

@Composable
fun KSErrorRoundedText(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.paddingMedium)
) {
    KSRoundedPaddedText(
        background = colors.backgroundDangerBold,
        textColor = colors.textInversePrimary,
        text = text,
        padding = padding
    )
}

@Composable
fun KSHeadsUpRoundedText(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.paddingMedium)
) {
    KSRoundedPaddedText(
        background = colors.backgroundActionPressed,
        textColor = colors.textInversePrimary,
        text = text,
        padding = padding
    )
}

@Composable
fun KSSuccessRoundedText(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.paddingMedium)
) {
    KSRoundedPaddedText(
        background = colors.backgroundAccentGreenSubtle,
        textColor = colors.textPrimary,
        text = text,
        padding = padding
    )
}

// Dialogs
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AlertDialogNoHeadlinePreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_support_400)
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
                .background(color = colors.kds_support_400)
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
                .background(color = colors.kds_support_400)
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
                .background(color = colors.kds_support_400)
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
    rightButtonTextColor: Color? = null,
    rightButtonAction: (() -> Unit)? = null,
    rightButtonModifier: Modifier = Modifier,
    rightButtonColor: Color? = null,
    leftButtonText: String? = null,
    leftButtonTextStyle: TextStyle? = null,
    leftButtonTextColor: Color? = null,
    leftButtonAction: (() -> Unit)? = null,
    leftButtonModifier: Modifier = Modifier,
    leftButtonColor: Color? = null,
    additionalButtonSpacing: Dp? = null,
) {
    Column(
        modifier = modifier
    ) {
        // Headline
        safeLet(headline, headlineStyle, headlineSpacing) { text, style, space ->
            Text(text = text, style = style, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(space))
        }

        // Body
        Text(
            modifier = Modifier.padding(end = dimensions.paddingSmall),
            text = bodyText,
            style = bodyStyle,
            color = colors.textPrimary
        )

        Row(
            Modifier
                .padding(top = dimensions.paddingMedium)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            // Left Button
            safeLet(leftButtonText, leftButtonTextStyle) { text, style ->
                Button(
                    modifier = leftButtonModifier,
                    onClick = {
                        leftButtonAction?.invoke()
                        setShowDialog(false)
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = leftButtonColor ?: colors.backgroundSurfacePrimary
                    ),
                    elevation = ButtonDefaults.elevation(dimensions.none)
                ) {
                    Text(
                        text = text,
                        style = style,
                        color = leftButtonTextColor ?: style.color
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(
                    additionalButtonSpacing ?: dimensions.dialogButtonSpacing
                )
            )

            // Right Button
            safeLet(rightButtonText, rightButtonTextStyle) { text, style ->
                Button(
                    modifier = rightButtonModifier,
                    onClick = {
                        rightButtonAction?.invoke()
                        setShowDialog(false)
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = rightButtonColor ?: colors.backgroundSurfacePrimary
                    ),
                    elevation = ButtonDefaults.elevation(dimensions.none)
                ) {
                    Text(
                        text = text,
                        style = style,
                        color = rightButtonTextColor ?: style.color
                    )
                }
            }
        }
    }
}

@Composable
fun KSAlertDialogNoHeadline(
    setShowDialog: (Boolean) -> Unit,
    bodyText: String,
    leftButtonText: String? = null,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String? = null,
    rightButtonAction: (() -> Unit)? = null
) {
    KSDialog(
        setShowDialog = setShowDialog,
        content = {
            KSDialogVisual(
                modifier = Modifier
                    .width(dimensions.dialogWidth)
                    .background(color = colors.backgroundSurfacePrimary, shape = shapes.small)
                    .padding(
                        start = dimensions.paddingLarge,
                        top = dimensions.paddingLarge,
                        bottom = dimensions.paddingSmall,
                        end = dimensions.paddingSmall
                    ),
                setShowDialog = setShowDialog,
                bodyText = bodyText,
                bodyStyle = typography.callout,
                leftButtonText = leftButtonText,
                leftButtonAction = leftButtonAction,
                leftButtonTextStyle = typography.buttonText,
                rightButtonText = rightButtonText,
                rightButtonAction = rightButtonAction,
                rightButtonTextStyle = typography.buttonText
            )
        }
    )
}

@Composable
fun KSAlertDialog(
    setShowDialog: (Boolean) -> Unit,
    headlineText: String,
    bodyText: String,
    leftButtonText: String?,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String?,
    rightButtonAction: (() -> Unit)? = null
) {
    KSDialog(
        setShowDialog = setShowDialog,
        content = {
            KSDialogVisual(
                modifier = Modifier
                    .width(dimensions.dialogWidth)
                    .background(color = colors.backgroundSurfacePrimary, shape = shapes.small)
                    .padding(
                        start = dimensions.paddingLarge,
                        top = dimensions.paddingLarge,
                        bottom = dimensions.paddingSmall,
                        end = dimensions.paddingSmall
                    ),
                setShowDialog = setShowDialog,
                headline = headlineText,
                headlineStyle = typography.title3Bold,
                headlineSpacing = dimensions.paddingMedium,
                bodyText = bodyText,
                bodyStyle = typography.callout,
                leftButtonText = leftButtonText,
                leftButtonAction = leftButtonAction,
                leftButtonTextStyle = typography.buttonText,
                rightButtonText = rightButtonText,
                rightButtonAction = rightButtonAction,
                rightButtonTextStyle = typography.buttonText
            )
        }
    )
}

@Composable
fun KsTooltip(
    setShowDialog: (Boolean) -> Unit,
    headlineText: String,
    bodyText: String,
    alignment: Alignment? = null
) {
    Popup(
        alignment = alignment ?: Alignment.BottomCenter,
        onDismissRequest = { setShowDialog(false) }
    ) {
        KSDialogVisual(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.kds_white)
                .padding(dimensions.paddingLarge),
            setShowDialog = { },
            bodyText = bodyText,
            bodyStyle = typography.body2,
            headline = headlineText,
            headlineStyle = typography.headline,
            headlineSpacing = dimensions.paddingSmall
        )
    }
}

@Composable
fun KSIntercept(
    setShowDialog: (Boolean) -> Unit,
    bodyText: String,
    leftButtonText: String,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String,
    rightButtonAction: (() -> Unit)? = null,
    alignment: Alignment? = null
) {
    Popup(
        alignment = alignment ?: Alignment.BottomCenter,
        onDismissRequest = { setShowDialog(false) }
    ) {
        KSDialogVisual(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.kds_support_100, shape = shapes.small)
                .padding(
                    start = dimensions.paddingMedium,
                    top = dimensions.paddingLarge,
                    end = dimensions.paddingMedium,
                    bottom = dimensions.paddingMedium
                ),
            setShowDialog = {},
            bodyText = bodyText,
            bodyStyle = typography.calloutMedium,
            leftButtonText = leftButtonText,
            leftButtonAction = leftButtonAction,
            leftButtonTextStyle = typography.buttonText,
            leftButtonTextColor = facebook_blue,
            leftButtonColor = colors.kds_support_100,
            rightButtonText = rightButtonText,
            rightButtonAction = rightButtonAction,
            rightButtonTextStyle = typography.buttonText,
            rightButtonTextColor = colors.kds_white,
            rightButtonColor = colors.kds_trust_500,
            additionalButtonSpacing = dimensions.paddingSmall
        )
    }
}
