package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

data class ButtonColorVariables(
    val backgroundColor: Color,
    val pressedColor: Color,
    val disabledColor: Color,
    val textColor: Color,
    val disabledTextColor: Color,
    val loadingSpinnerColor: Color,
    val borderColor: Color,
    val borderColorPressed: Color,
    val borderColorDisabled: Color,
)

val FilledButtonColors = ButtonColorVariables(
    backgroundColor = KSDefaultButtonColors.filledBackgroundAction,
    pressedColor = KSDefaultButtonColors.filledBackgroundActionPressed,
    disabledColor = KSDefaultButtonColors.filledBackgroundActionDisabled,
    textColor = Color.White,
    disabledTextColor = KSDefaultButtonColors.filledTextInverseDisabled,
    loadingSpinnerColor = KSDefaultButtonColors.filledLoadingSpinner,
    borderColor = Color.Transparent,
    borderColorPressed = Color.Transparent,
    borderColorDisabled = Color.Transparent
)

// For Green buttons, enabled text is also white.
val GreenButtonColors = ButtonColorVariables(
    backgroundColor = KSDefaultButtonColors.greenBackgroundAccentBold,
    pressedColor = KSDefaultButtonColors.greenBackgroundAccentBoldPressed,
    disabledColor = KSDefaultButtonColors.greenBackgroundAccentDisabled,
    textColor = Color.White,
    disabledTextColor = KSDefaultButtonColors.greenTextDisabled,
    loadingSpinnerColor = KSDefaultButtonColors.greenLoadingSpinner,
    borderColor = Color.Transparent,
    borderColorPressed = Color.Transparent,
    borderColorDisabled = Color.Transparent
)

// For Filled Inverted buttons, we use the inverted color palette.
val FilledInvertedButtonColors = ButtonColorVariables(
    backgroundColor = KSDefaultButtonColors.filledInvertedBackgroundSurfacePrimary,
    pressedColor = KSDefaultButtonColors.filledInvertedBackgroundInversePressed,
    disabledColor = KSDefaultButtonColors.filledInvertedBackgroundInverseDisabled,
    textColor = KSDefaultButtonColors.filledInvertedTextPrimary,
    disabledTextColor = KSDefaultButtonColors.filledInvertedTextDisabled,
    loadingSpinnerColor = KSDefaultButtonColors.filledInvertedLoadingSpinner,
    borderColor = Color.Transparent,
    borderColorPressed = Color.Transparent,
    borderColorDisabled = Color.Transparent
)

// For Filled Destructive buttons.
val FilledDestructiveButtonColors = ButtonColorVariables(
    backgroundColor = KSDefaultButtonColors.destructiveBackgroundBold,
    pressedColor = KSDefaultButtonColors.destructiveBackgroundBold, // Using the same as background for pressed.
    disabledColor = KSDefaultButtonColors.destructiveBackgroundDisabled,
    textColor = KSDefaultButtonColors.destructiveTextInversePrimary,
    disabledTextColor = KSDefaultButtonColors.destructiveTextAccentRedInverseDisabled,
    loadingSpinnerColor = KSDefaultButtonColors.destructiveLoadingSpinner,
    borderColor = Color.Transparent,
    borderColorPressed = Color.Transparent,
    borderColorDisabled = Color.Transparent
)

// For Borderless buttons, we use a transparent background.
val BorderlessButtonColors = ButtonColorVariables(
    backgroundColor = Color.Transparent,
    pressedColor = KSDefaultButtonColors.borderlessBackgroundInversePressed,
    disabledColor = Color.Transparent,
    textColor = KSDefaultButtonColors.borderlessTextPrimary,
    disabledTextColor = KSDefaultButtonColors.borderlessTextDisabled,
    loadingSpinnerColor = KSDefaultButtonColors.borderlessLoadingSpinner,
    borderColor = Color.Transparent,
    borderColorPressed = Color.Transparent,
    borderColorDisabled = Color.Transparent
)

// For Outlined buttons.
val OutlinedButtonColors = ButtonColorVariables(
    backgroundColor = Color.Transparent,
    pressedColor = KSDefaultButtonColors.outlinedBackgroundInversePressed,
    disabledColor = Color.Transparent,
    textColor = KSDefaultButtonColors.outlinedTextPrimary,
    disabledTextColor = KSDefaultButtonColors.outlinedTextDisabled,
    loadingSpinnerColor = KSDefaultButtonColors.outlinedLoadingSpinner,
    borderColor = KSDefaultButtonColors.outlinedBorderBold,
    borderColorPressed = KSDefaultButtonColors.outlinedBackgroundInversePressed,
    borderColorDisabled = KSDefaultButtonColors.outlinedBorderSubtle
)

// For Outlined Destructive buttons.
val OutlinedDestructiveButtonColors = ButtonColorVariables(
    backgroundColor = Color.Transparent,
    pressedColor = KSDefaultButtonColors.outlinedDestructiveBackgroundAccentRedSubtle,
    disabledColor = Color.Transparent,
    textColor = KSDefaultButtonColors.outlinedDestructiveTextAccentRed,
    disabledTextColor = KSDefaultButtonColors.outlinedDestructiveTextAccentRedDisabled,
    loadingSpinnerColor = KSDefaultButtonColors.outlinedDestructiveLoadingSpinner,
    borderColor = KSDefaultButtonColors.outlinedDestructiveBackgroundBold,
    borderColorPressed = KSDefaultButtonColors.outlinedDestructiveTextAccentRed,
    borderColorDisabled = KSDefaultButtonColors.outlinedDestructiveTextAccentRedDisabled
)

// For Borderless Destructive buttons.
val BorderlessDestructiveButtonColors = ButtonColorVariables(
    backgroundColor = Color.Transparent,
    pressedColor = KSDefaultButtonColors.borderlessDestructiveBackgroundAccentRedSubtle,
    disabledColor = Color.Transparent,
    textColor = KSDefaultButtonColors.borderlessDestructiveTextAccentRed,
    disabledTextColor = KSDefaultButtonColors.borderlessDestructiveTextAccentRedDisabled,
    loadingSpinnerColor = KSDefaultButtonColors.borderlessDestructiveLoadingSpinner,
    borderColor = Color.Transparent,
    borderColorPressed = Color.Transparent,
    borderColorDisabled = Color.Transparent
)

// For Facebook Login buttons.
val FBLoginButtonColors = ButtonColorVariables(
    backgroundColor = KSDefaultButtonColors.fbLoginButtonBackgroundColor,
    pressedColor = KSDefaultButtonColors.fbLoginButtonPressedColor,
    disabledColor = KSDefaultButtonColors.fbLoginButtonBackgroundColor,
    textColor = KSDefaultButtonColors.fbLoginButtonTextColor,
    disabledTextColor = KSDefaultButtonColors.fbLoginButtonTextColor,
    loadingSpinnerColor = Color.Transparent,
    borderColor = Color.Transparent,
    borderColorPressed = Color.Transparent,
    borderColorDisabled = Color.Transparent
)

// Define a sealed class for different button types
sealed class KSButtonType(val colors: ButtonColorVariables) {
    object Filled : KSButtonType(FilledButtonColors)
    object Green : KSButtonType(GreenButtonColors)
    object FilledInverted : KSButtonType(FilledInvertedButtonColors)
    object FilledDestructive : KSButtonType(FilledDestructiveButtonColors)
    object Borderless : KSButtonType(BorderlessButtonColors)
    object Outlined : KSButtonType(OutlinedButtonColors)
    object OutlinedDestructive : KSButtonType(OutlinedDestructiveButtonColors)
    object BorderlessDestructive : KSButtonType(BorderlessDestructiveButtonColors)
    object Facebook : KSButtonType(FBLoginButtonColors)
}

@Composable
fun KSButton(
    modifier: Modifier = Modifier,
    type: KSButtonType,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null,
) {
    CreateButton(
        modifier = if (type is KSButtonType.Outlined || type is KSButtonType.OutlinedDestructive) {
            modifier.border(
                width = dimensions.dividerThickness,
                color = when {
                    isPressed -> type.colors.borderColorPressed
                    !isEnabled -> type.colors.borderColorDisabled
                    else -> type.colors.borderColor
                },
                shape = RoundedCornerShape(size = dimensions.radiusExtraSmall)
            )
        } else modifier,
        onClickAction = onClickAction,
        text = text,
        colors = type.colors,
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed,
        isBorderless = type is KSButtonType.Borderless || type is KSButtonType.BorderlessDestructive
    )
}

@Composable
fun CreateButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    colors: ButtonColorVariables,
    imageId: Int? = null,
    imageContentDescription: String? = null,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    isBorderless: Boolean = false,
) {
    BaseButton(
        modifier = modifier,
        onClickAction = onClickAction,
        text = text,
        backgroundColor = colors.backgroundColor,
        pressedColor = colors.pressedColor,
        disabledColor = colors.disabledColor,
        textColor = colors.textColor,
        disabledTextColor = colors.disabledTextColor,
        loadingSpinnerColor = colors.loadingSpinnerColor,
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed,
        isBorderless = isBorderless
    )
}

@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    backgroundColor: Color,
    pressedColor: Color,
    disabledColor: Color,
    textColor: Color,
    disabledTextColor: Color,
    loadingSpinnerColor: Color,
    imageId: Int? = null,
    imageContentDescription: String? = null,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    isBorderless: Boolean = false,
) {
    val currentBackgroundColor = when {
        !isEnabled -> disabledColor
        isPressed -> pressedColor
        else -> backgroundColor
    }

    val currentTextColor = if (isEnabled) textColor else disabledTextColor

    Button(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = currentBackgroundColor,
            disabledBackgroundColor = disabledColor
        ),
        onClick = { onClickAction.invoke() },
        enabled = isEnabled && !isLoading,
        shape = RoundedCornerShape(size = dimensions.radiusExtraSmall),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (imageId != null) {
                    Image(
                        modifier = Modifier.size(dimensions.paddingMedium),
                        painter = painterResource(id = imageId),
                        contentDescription = imageContentDescription
                    )
                }
                Text(
                    text = text,
                    color = currentTextColor,
                    style = typographyV2.buttonLabel,
                    modifier = Modifier.padding(start = if (imageId != null) dimensions.paddingSmall else dimensions.none)
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensions.loadingSpinnerSize),
                    color = loadingSpinnerColor,
                    strokeWidth = dimensions.strokeWith
                )
            }
        }
    }
}

@Composable
fun FBLoginButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isPressed: Boolean = false,
) {
    val currentBackgroundColor =
        if (isPressed) FBLoginButtonColors.pressedColor else FBLoginButtonColors.backgroundColor

    Button(
        onClick = { onClickAction.invoke() },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(backgroundColor = currentBackgroundColor),
        shape = RoundedCornerShape(size = dimensions.radiusExtraSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.com_facebook_button_icon),
                contentDescription = "Facebook Logo",
                modifier = Modifier
                    .size(dimensions.imageSizeMedium)
            )
            Text(
                text = text,
                color = FBLoginButtonColors.textColor,
                style = typographyV2.buttonLabel,
                modifier = Modifier.padding(start = dimensions.paddingSmall)
            )
        }
    }
}

@Preview
@Composable
fun KSFilledButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(colors.kds_white),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.Filled,
                text = "Filled",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Filled,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Filled,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Filled,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Preview
@Composable
fun KSGreenButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(colors.kds_white),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.Green,
                text = "Green",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Green,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Green,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Green,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Preview
@Composable
fun KSFilledInvertedButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(colors.kds_white),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.FilledInverted,
                text = "Inverted",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FilledInverted,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FilledInverted,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FilledInverted,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Preview
@Composable
fun KSFilledDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(colors.kds_white),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.FilledDestructive,
                text = "Destructive",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FilledDestructive,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FilledDestructive,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FilledDestructive,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Preview
@Composable
fun KSBorderlessButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.Borderless,
                text = "Borderless",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Borderless,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Borderless,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Borderless,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Preview
@Composable
fun KSOutlinedButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.Outlined,
                text = "Outlined",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Outlined,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Outlined,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.Outlined,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Preview
@Composable
fun KSOutlinedDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.OutlinedDestructive,
                text = "Outlined Destructive",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OutlinedDestructive,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OutlinedDestructive,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OutlinedDestructive,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Preview
@Composable
fun KSBorderlessDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.BorderlessDestructive,
                text = "Borderless Destructive",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BorderlessDestructive,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BorderlessDestructive,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BorderlessDestructive,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Preview
@Composable
fun FBLoginButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.kds_white)
                .padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook")
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook", isPressed = true)
        }
    }
}
