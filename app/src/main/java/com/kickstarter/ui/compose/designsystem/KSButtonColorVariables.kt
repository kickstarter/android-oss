package com.kickstarter.ui.compose.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

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
    val iconEnabled: Color,
    val iconDisabled: Color,
)

@Composable
fun filledButtonColors(
    color: KSButtonColors,
): ButtonColorVariables {

    val buttonColors = ButtonColorVariables(
        backgroundColor = color.filledBackgroundAction,
        pressedColor = color.filledBackgroundActionPressed,
        disabledColor = color.filledBackgroundActionDisabled,
        textColor = colors.textInversePrimary,
        disabledTextColor = color.filledTextInverseDisabled,
        loadingSpinnerColor = color.filledLoadingSpinner,
        borderColor = Color.Transparent,
        borderColorPressed = Color.Transparent,
        borderColorDisabled = Color.Transparent,
        iconEnabled = color.iconFilled,
        iconDisabled = color.iconFilled
    )
    return buttonColors
}

@Composable
fun greenButtonColors(
    color: KSButtonColors,
): ButtonColorVariables {

    val buttonColors = ButtonColorVariables(
        backgroundColor = color.greenBackgroundAccentBold,
        pressedColor = color.greenBackgroundAccentBoldPressed,
        disabledColor = color.greenBackgroundAccentDisabled,
        textColor = colors.textInversePrimary,
        disabledTextColor = color.greenTextDisabled,
        loadingSpinnerColor = color.greenLoadingSpinner,
        borderColor = Color.Transparent,
        borderColorPressed = Color.Transparent,
        borderColorDisabled = Color.Transparent,
        iconEnabled = color.iconGreen,
        iconDisabled = color.iconGreenDisabled
    )
    return buttonColors
}

@Composable
fun filledInvertedButtonColors(
    color: KSButtonColors,
): ButtonColorVariables {

    val buttonColors = ButtonColorVariables(
        backgroundColor = color.filledInvertedBackgroundSurfacePrimary,
        pressedColor = color.filledInvertedBackgroundInversePressed,
        disabledColor = color.filledInvertedBackgroundInverseDisabled,
        textColor = colors.textPrimary,
        disabledTextColor = color.filledInvertedTextDisabled,
        loadingSpinnerColor = color.filledInvertedTextDisabled,
        borderColor = Color.Transparent,
        borderColorPressed = Color.Transparent,
        borderColorDisabled = Color.Transparent,
        iconEnabled = color.iconFilledInverted,
        iconDisabled = color.iconFilledInvertedDisabled
    )
    return buttonColors
}

@Composable
fun filledDestructiveButtonColors(
    color: KSButtonColors,
): ButtonColorVariables {

    val buttonColors = ButtonColorVariables(
        backgroundColor = color.destructiveBackgroundBold,
        pressedColor = color.destructiveBackgroundPressed,
        disabledColor = color.destructiveBackgroundDisabled,
        textColor = colors.textInversePrimary,
        disabledTextColor = color.destructiveTextAccentRedInverseDisabled,
        loadingSpinnerColor = color.destructiveLoadingSpinner,
        borderColor = Color.Transparent,
        borderColorPressed = Color.Transparent,
        borderColorDisabled = Color.Transparent,
        iconEnabled = color.iconFilledDestructive,
        iconDisabled = color.iconFilledDestructiveDisabled,
    )
    return buttonColors
}

@Composable
fun borderlessButtonColors(
    color: KSButtonColors,
): ButtonColorVariables {

    val buttonColors = ButtonColorVariables(
        backgroundColor = Color.Transparent,
        pressedColor = color.borderlessBackgroundInversePressed,
        disabledColor = Color.Transparent,
        textColor = colors.textPrimary,
        disabledTextColor = color.borderlessTextDisabled,
        loadingSpinnerColor = color.borderlessLoadingSpinner,
        borderColor = Color.Transparent,
        borderColorPressed = Color.Transparent,
        borderColorDisabled = Color.Transparent,
        iconEnabled = color.iconBorderless,
        iconDisabled = color.iconBorderlessDisabled,
    )
    return buttonColors
}

@Composable
fun outlinedButtonColors(
    color: KSButtonColors,
): ButtonColorVariables {

    val buttonColors = ButtonColorVariables(
        backgroundColor = Color.Transparent,
        pressedColor = color.outlinedBackgroundInversePressed,
        disabledColor = Color.Transparent,
        textColor = colors.textPrimary,
        disabledTextColor = color.outlinedTextDisabled,
        loadingSpinnerColor = color.outlinedLoadingSpinner,
        borderColor = color.outlinedBorderBold,
        borderColorPressed = color.outlinedBackgroundInversePressed,
        borderColorDisabled = color.outlinedBorderSubtle,
        iconEnabled = color.iconOutlined,
        iconDisabled = color.iconOutlinedDisabled
    )
    return buttonColors
}

@Composable
fun outlinedDestructiveButtonColors(
    color: KSButtonColors,
): ButtonColorVariables {

    val buttonColors = ButtonColorVariables(
        backgroundColor = Color.Transparent,
        pressedColor = color.outlinedDestructiveBackgroundAccentRedSubtle,
        disabledColor = Color.Transparent,
        textColor = colors.textAccentRed,
        disabledTextColor = color.outlinedDestructiveTextAccentRedDisabled,
        loadingSpinnerColor = color.outlinedDestructiveLoadingSpinner,
        borderColor = color.outlinedDestructiveBorderBold,
        borderColorPressed = color.outlinedDestructiveBorderPressedBold,
        borderColorDisabled = color.outlinedDestructiveTextAccentRedDisabled,
        iconEnabled = color.iconOutlinedDestructive,
        iconDisabled = color.iconOutlinedDestructiveDisabled
    )
    return buttonColors
}

@Composable
fun borderLessDestructiveButtonColors(
    color: KSButtonColors,
): ButtonColorVariables {

    val buttonColors = ButtonColorVariables(
        backgroundColor = Color.Transparent,
        pressedColor = color.borderlessDestructiveBackgroundAccentRedSubtle,
        disabledColor = Color.Transparent,
        textColor = colors.textAccentRed,
        disabledTextColor = color.borderlessDestructiveTextAccentRedDisabled,
        loadingSpinnerColor = color.borderlessDestructiveLoadingSpinner,
        borderColor = Color.Transparent,
        borderColorPressed = Color.Transparent,
        borderColorDisabled = Color.Transparent,
        iconEnabled = color.iconBorderlessDestructive,
        iconDisabled = color.iconBorderlessDestructiveDisabled
    )
    return buttonColors
}

@Composable
fun fbLoginButtonColors(
    colors: KSButtonColors,
): ButtonColorVariables {

    val buttonColors = ButtonColorVariables(
        backgroundColor = colors.fbLoginButtonBackgroundColor,
        pressedColor = colors.fbLoginButtonPressedColor,
        disabledColor = colors.fbLoginButtonBackgroundColor,
        textColor = colors.fbLoginButtonTextColor,
        disabledTextColor = colors.fbLoginButtonTextColor,
        loadingSpinnerColor = Color.Transparent,
        borderColor = Color.Transparent,
        borderColorPressed = Color.Transparent,
        borderColorDisabled = Color.Transparent,
        iconEnabled = Color.White,
        iconDisabled = Color.White
    )
    return buttonColors
}
