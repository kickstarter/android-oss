package com.kickstarter.ui.compose.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class KSButtonColors(
    // Filled Button Colors
    val filledBackgroundAction: Color = Color.Unspecified,
    val filledBackgroundActionPressed: Color = Color.Unspecified,
    val filledBackgroundActionDisabled: Color = Color.Unspecified,
    val filledTextInverseDisabled: Color = Color.Unspecified,
    val filledLoadingSpinner: Color = Color.Unspecified,

    // Green Button Colors
    val greenBackgroundAccentBold: Color = Color.Unspecified,
    val greenBackgroundAccentBoldPressed: Color = Color.Unspecified,
    val greenBackgroundAccentDisabled: Color = Color.Unspecified,
    val greenTextDisabled: Color = Color.Unspecified,
    val greenLoadingSpinner: Color = Color.Unspecified,

    // Filled Inverted Button Colors
    val filledInvertedBackgroundSurfacePrimary: Color = Color.Unspecified,
    val filledInvertedBackgroundInversePressed: Color = Color.Unspecified,
    val filledInvertedBackgroundInverseDisabled: Color = Color.Unspecified,
    val filledInvertedTextPrimary: Color = Color.Unspecified,
    val filledInvertedTextDisabled: Color = Color.Unspecified,
    val filledInvertedLoadingSpinner: Color = Color.Unspecified,

    // Filled Destructive Button Colors
    val destructiveBackgroundBold: Color = Color.Unspecified,
    val destructiveBackgroundDisabled: Color = Color.Unspecified,
    val destructiveBackgroundPressed: Color = Color.Unspecified,
    val destructiveTextInversePrimary: Color = Color.Unspecified,
    val destructiveTextAccentRedInverseDisabled: Color = Color.Unspecified,
    val destructiveLoadingSpinner: Color = Color.Unspecified,

    // Borderless Button Colors
    val borderlessTextPrimary: Color = Color.Unspecified,
    val borderlessBackgroundInversePressed: Color = Color.Unspecified,
    val borderlessTextDisabled: Color = Color.Unspecified,
    val borderlessLoadingSpinner: Color = Color.Unspecified,

    // Outlined Button Colors
    val outlinedBorderBold: Color = Color.Unspecified,
    val outlinedBackgroundInversePressed: Color = Color.Unspecified,
    val outlinedBorderSubtle: Color = Color.Unspecified,
    val outlinedTextPrimary: Color = Color.Unspecified,
    val outlinedTextDisabled: Color = Color.Unspecified,
    val outlinedLoadingSpinner: Color = Color.Unspecified,

    // Outlined Destructive Button Colors
    val outlinedDestructiveBorderBold: Color = Color.Unspecified,
    val outlinedDestructiveTextAccentRed: Color = Color.Unspecified,
    val outlinedDestructiveBackgroundAccentRedSubtle: Color = Color.Unspecified,
    val outlinedDestructiveBorderPressedBold: Color = Color.Unspecified,
    val outlinedDestructiveTextAccentRedDisabled: Color = Color.Unspecified,
    val outlinedDestructiveLoadingSpinner: Color = Color.Unspecified,

    // Borderless Destructive Button Colors
    val borderlessDestructiveTextAccentRed: Color = Color.Unspecified,
    val borderlessDestructiveTextAccentRedBolder: Color = Color.Unspecified,
    val borderlessDestructiveTextAccentRedDisabled: Color = Color.Unspecified,
    val borderlessDestructiveBackgroundAccentRedSubtle: Color = Color.Unspecified,
    val borderlessDestructiveLoadingSpinner: Color = Color.Unspecified,

    // Facebook Login Button Colors
    val fbLoginButtonBackgroundColor: Color = Color.Unspecified,
    val fbLoginButtonTextColor: Color = Color.Unspecified,
    val fbLoginButtonPressedColor: Color = Color.Unspecified,

    // Icon Colors
    val iconFilled: Color = Color.Unspecified,
    val iconGreen: Color = Color.Unspecified,
    val iconGreenDisabled: Color = Color.Unspecified,
    val iconFilledInverted: Color = Color.Unspecified,
    val iconFilledInvertedDisabled: Color = Color.Unspecified,
    val iconFilledDestructive: Color = Color.Unspecified,
    val iconFilledDestructiveDisabled: Color = Color.Unspecified,
    val iconBorderless: Color = Color.Unspecified,
    val iconBorderlessDisabled: Color = Color.Unspecified,
    val iconOutlined: Color = Color.Unspecified,
    val iconOutlinedDisabled: Color = Color.Unspecified,
    val iconOutlinedDestructive: Color = Color.Unspecified,
    val iconOutlinedDestructiveDisabled: Color = Color.Unspecified,
    val iconBorderlessDestructive: Color = Color.Unspecified,
    val iconBorderlessDestructiveDisabled: Color = Color.Unspecified,
)

val LocalKSButtonColors = staticCompositionLocalOf { KSButtonColors() }

val KSDefaultButtonColors = KSButtonColors(
    // Filled Button Colors – reuse common neutral colors
    filledBackgroundAction = Color(0xFF171717),
    filledBackgroundActionPressed = Color(0xFF3C3C3C),
    filledBackgroundActionDisabled = Color(0xFFB3B3B3),
    filledTextInverseDisabled = Color(0xFFF2F2F2),
    filledLoadingSpinner = Color(0xFF3C3C3C),

    // Green Button Colors
    greenBackgroundAccentBold = Color(0xFF037242),
    greenBackgroundAccentBoldPressed = Color(0xFF024629),
    greenBackgroundAccentDisabled = Color(0xFFEBFEF6),
    greenTextDisabled = Color(0xFFB3B3B3),
    greenLoadingSpinner = Color(0xFF025A34),

    // Filled Inverted Button Colors
    filledInvertedBackgroundSurfacePrimary = Color(0xFFFFFFFF),
    filledInvertedBackgroundInversePressed = Color(0xFFE0E0E0),
    filledInvertedBackgroundInverseDisabled = Color(0xFFF2F2F2),
    filledInvertedTextPrimary = Color(0xFF171717),
    filledInvertedTextDisabled = Color(0xFFB3B3B3),
    filledInvertedLoadingSpinner = Color(0xFF3C3C3C),

    // Filled Destructive Button Colors
    destructiveBackgroundBold = Color(0xFFB81F14),
    destructiveBackgroundPressed = Color(0xFF73140D),
    destructiveBackgroundDisabled = Color(0xFFF7BBB7),
    destructiveTextInversePrimary = Color(0xFFFFFFFF),
    destructiveTextAccentRedInverseDisabled = Color(0xFFFEF2F1),
    destructiveLoadingSpinner = Color(0xFF931910),

    // Borderless Button Colors
    borderlessTextPrimary = Color(0xFF171717),
    borderlessBackgroundInversePressed = Color(0xFFE0E0E0),
    borderlessTextDisabled = Color(0xFFB3B3B3),
    borderlessLoadingSpinner = Color(0xFF3C3C3C),

    // Outlined Button Colors
    outlinedBorderBold = Color(0xFFC9C9C9),
    outlinedBackgroundInversePressed = Color(0xFFE0E0E0),
    outlinedBorderSubtle = Color(0xFFE0E0E0),
    outlinedTextPrimary = Color(0xFF171717),
    outlinedTextDisabled = Color(0xFFB3B3B3),
    outlinedLoadingSpinner = Color(0xFF3C3C3C),

    // Outlined Destructive Button Colors
    outlinedDestructiveBorderBold = Color(0xFFB81F14),
    outlinedDestructiveTextAccentRed = Color(0xFF73140D),
    outlinedDestructiveBackgroundAccentRedSubtle = Color(0xFFFEF2F1),
    outlinedDestructiveBorderPressedBold = Color(0xFF73140D),
    outlinedDestructiveTextAccentRedDisabled = Color(0xFFF7BBB7),
    outlinedDestructiveLoadingSpinner = Color(0xFF931910),

    // Borderless Destructive Button Colors
    borderlessDestructiveTextAccentRed = Color(0xFFB81F14),
    borderlessDestructiveTextAccentRedBolder = Color(0xFF73140D),
    borderlessDestructiveTextAccentRedDisabled = Color(0xFFF7BBB7),
    borderlessDestructiveBackgroundAccentRedSubtle = Color(0xFFFEF2F1),
    borderlessDestructiveLoadingSpinner = Color(0xFF931910),

    // Facebook Login Button Colors
    fbLoginButtonBackgroundColor = Color(0xFF1877F2),
    fbLoginButtonTextColor = Color.White,
    fbLoginButtonPressedColor = Color(0xFF135ABE),

    iconFilled = Color(0xFFE0E0E0),
    iconGreen = Color(0xFFD2FEEB),
    iconGreenDisabled = Color(0xFFB3B3B3),
    iconFilledInverted = Color(0xFF3C3C3C),
    iconFilledInvertedDisabled = Color(0xFFB3B3B3),
    iconFilledDestructive = Color(0xFFFEF2F1),
    iconFilledDestructiveDisabled = Color(0xFFFEF2F1),
    iconBorderless = Color(0xFF3C3C3C),
    iconBorderlessDisabled = Color(0xFFB3B3B3),
    iconOutlined = Color(0xFF3C3C3C),
    iconOutlinedDisabled = Color(0xFFB3B3B3),
    iconOutlinedDestructive = Color(0xFF931910),
    iconOutlinedDestructiveDisabled = Color(0xFFF7BBB7),
    iconBorderlessDestructive = Color(0xFF931910),
    iconBorderlessDestructiveDisabled = Color(0xFFF7BBB7),
)

val KSDarkButtonColors = KSButtonColors(
    // Filled Button Colors – reuse common neutral colors
    filledBackgroundAction = Color(0xFFFAFAFA),
    filledBackgroundActionPressed = Color(0xFFE0E0E0),
    filledBackgroundActionDisabled = Color(0xFF636363),
    filledTextInverseDisabled = Color(0xFF2C2C2C),
    filledLoadingSpinner = Color(0xFFF2F2F2),

    // Green Button Colors
    greenBackgroundAccentBold = Color(0xFF05CE78),
    greenBackgroundAccentBoldPressed = Color(0xFF79FCC3),
    greenBackgroundAccentDisabled = Color(0xFF01321D),
    greenTextDisabled = Color(0xFF636363),
    greenLoadingSpinner = Color(0xFF79FCC3),

    // Filled Inverted Button Colors
    filledInvertedBackgroundSurfacePrimary = Color(0xFF171717),
    filledInvertedBackgroundInversePressed = Color(0xFF3C3C3C),
    filledInvertedBackgroundInverseDisabled = Color(0xFF363636),
    filledInvertedTextDisabled = Color(0xFF636363),
    filledInvertedLoadingSpinner = Color(0xFFF2F2F2),

    // Filled Destructive Button Colors
    destructiveBackgroundBold = Color(0xFFF39C95),
    destructiveBackgroundDisabled = Color(0xFF2E0805),
    destructiveBackgroundPressed = Color(0xFFFBDDDB),
    destructiveTextAccentRedInverseDisabled = Color(0xFF73140D),
    destructiveLoadingSpinner = Color(0xFFF7BBB7),

    // Borderless Button Colors
    borderlessTextPrimary = Color(0xFF171717),
    borderlessBackgroundInversePressed = Color(0xFF3C3C3C),
    borderlessTextDisabled = Color(0xFF636363),
    borderlessLoadingSpinner = Color(0xFFF2F2F2),

    // Outlined Button Colors
    outlinedBorderBold = Color(0xFF858585),
    outlinedBackgroundInversePressed = Color(0xFF3C3C3C),
    outlinedBorderSubtle = Color(0xFF4D4D4D),
    outlinedTextDisabled = Color(0xFF636363),
    outlinedLoadingSpinner = Color(0xFFF2F2F2),

    // Outlined Destructive Button Colors
    outlinedDestructiveBorderBold = Color(0xFFF39C95),
    outlinedDestructiveTextAccentRed = Color(0xFF73140D),
    outlinedDestructiveBackgroundAccentRedSubtle = Color(0xFF530E09),
    outlinedDestructiveBorderPressedBold = Color(0xFFFBDDDB),
    outlinedDestructiveTextAccentRedDisabled = Color(0xFF931910),
    outlinedDestructiveLoadingSpinner = Color(0xFFF7BBB7),

    // Borderless Destructive Button Colors
    borderlessDestructiveTextAccentRed = Color(0xFFB81F14),
    borderlessDestructiveTextAccentRedBolder = Color(0xFF73140D),
    borderlessDestructiveTextAccentRedDisabled = Color(0xFF931910),
    borderlessDestructiveBackgroundAccentRedSubtle = Color(0xFF530E09),
    borderlessDestructiveLoadingSpinner = Color(0xFFF7BBB7),

    // Facebook Login Button Colors
    fbLoginButtonBackgroundColor = Color(0xFF1877F2),
    fbLoginButtonTextColor = Color.White,
    fbLoginButtonPressedColor = Color(0xFF135ABE),

    iconFilled = Color(0xFF3C3C3C),
    iconGreen = Color(0xFF024629),
    iconGreenDisabled = Color(0xFF636363),
    iconFilledInverted = Color(0xFFF2F2F2),
    iconFilledInvertedDisabled = Color(0xFF636363),
    iconFilledDestructive = Color(0xFF73140D),
    iconFilledDestructiveDisabled = Color(0xFF73140D),
    iconBorderless = Color(0xFFF2F2F2),
    iconBorderlessDisabled = Color(0xFF636363),
    iconOutlined = Color(0xFFF2F2F2),
    iconOutlinedDisabled = Color(0xFF3C3C3C),
    iconOutlinedDestructive = Color(0xFFF7BBB7),
    iconOutlinedDestructiveDisabled = Color(0xFF931910),
    iconBorderlessDestructive = Color(0xFFF7BBB7),
    iconBorderlessDestructiveDisabled = Color(0xFF931910),

)
