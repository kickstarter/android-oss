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
    val outlinedDestructiveBackgroundBold: Color = Color.Unspecified,
    val outlinedDestructiveTextAccentRed: Color = Color.Unspecified,
    val outlinedDestructiveBackgroundAccentRedSubtle: Color = Color.Unspecified,
    val outlinedDestructiveBorderDangerBold: Color = Color.Unspecified,
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
    outlinedDestructiveBackgroundBold = Color(0xFFB81F14),
    outlinedDestructiveTextAccentRed = Color(0xFF73140D),
    outlinedDestructiveBackgroundAccentRedSubtle = Color(0xFFFEF2F1),
    outlinedDestructiveBorderDangerBold = Color(0xFF73140D),
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
    fbLoginButtonPressedColor = Color(0xFF135ABE)
)
