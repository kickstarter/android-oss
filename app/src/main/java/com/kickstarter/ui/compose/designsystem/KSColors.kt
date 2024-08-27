package com.kickstarter.ui.compose.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// OLD COLORS
// GREENS
val kds_create_100 = Color(0xFFE6FAF1)
val kds_create_300 = Color(0xFF9BEBC9)
val kds_create_500 = Color(0xFF05CE78)
val kds_create_700 = Color(0xFF028858)

// BLUES
val kds_trust_100 = Color(0xFFDBE7FF)
val kds_trust_300 = Color(0xFF71A0FF)
val kds_trust_500 = Color(0xFF5555FF)
val kds_trust_700 = Color(0xFF0A007D)

// CORALS
val kds_celebrate_100 = Color(0xFFFFF2EC)
val kds_celebrate_300 = Color(0xFFFECCB3)
val kds_celebrate_500 = Color(0xFFF97B62)
val kds_celebrate_700 = Color(0xFFD8503D)

// GREYS
val kds_white = Color(0xFFFFFFFF)
val kds_support_100 = Color(0xFFF3F3F3)
val kds_support_200 = Color(0xFFE6E6E6)
val kds_support_300 = Color(0xFFD1D1D1)
val kds_support_400 = Color(0xFF696969)
val kds_support_500 = Color(0xFF464646)
val kds_support_700 = Color(0xFF222222)
val kds_black = Color(0xff000000)

// FUNCTIONAL COLORS
val kds_alert = Color(0xFFA12027)
val kds_warn = Color(0xFFF9D66D)
val kds_inform = Color(0xFFB6D9E1)
val facebook_blue = Color(0xFF1877F2)

// NEW COLORS
val black = Color(0xFF000000)
val white = Color(0xFFFFFFFF)

// Greys
val grey_01 = Color(0xFFFAFAFA)
val grey_02 = Color(0xFFF2F2F2)
val grey_03 = Color(0xFFE0E0E0)
val grey_04 = Color(0xFFC9C9C9)
val grey_05 = Color(0xFFB3B3B3)
val grey_06 = Color(0xFF636363)
val grey_07 = Color(0xFF4D4D4D)
val grey_08 = Color(0xFF3C3C3C)
val grey_09 = Color(0xFF2C2C2C)
val grey_10 = Color(0xFF171717)

// Greens
val green_01 = Color(0xFFEBFEF6)
val green_02 = Color(0xFFD2FEEB)
val green_03 = Color(0xFF79FCC3)
val green_04 = Color(0xFF06E584)
val green_05 = Color(0xFF05CE78)
val green_06 = Color(0xFF037242)
val green_07 = Color(0xFF025A34)
val green_08 = Color(0xFF024629)
val green_09 = Color(0xFF01321D)
val green_10 = Color(0xFF011E11)

// Yellows
val yellow_01 = Color(0xFFFEFAF0)
val yellow_02 = Color(0xFFFDF2D3)
val yellow_03 = Color(0xFFF9DD90)
val yellow_04 = Color(0xFFF5C43D)
val yellow_05 = Color(0xFFE4AA0C)
val yellow_06 = Color(0xFF836207)
val yellow_07 = Color(0xFF614805)
val yellow_08 = Color(0xFF4E3A04)
val yellow_09 = Color(0xFF3A2B03)
val yellow_10 = Color(0xFF241B02)

// Oranges
val orange_01 = Color(0xFFFFF9F5)
val orange_02 = Color(0xFFFEEDE2)
val orange_03 = Color(0xFFFCD8C0)
val orange_04 = Color(0xFFF9BD94)
val orange_05 = Color(0xFFF79F64)
val orange_06 = Color(0xFFA54709)
val orange_07 = Color(0xFF7E3607)
val orange_08 = Color(0xFF662C05)
val orange_09 = Color(0xFF441E04)
val orange_10 = Color(0xFF241002)

// Reds
val red_01 = Color(0xFFFFFAFA)
val red_02 = Color(0xFFFEF2F1)
val red_03 = Color(0xFFFBDDDB)
val red_04 = Color(0xFFF7BBB7)
val red_05 = Color(0xFFF39C95)
val red_06 = Color(0xFFB81F14)
val red_07 = Color(0xFF931910)
val red_08 = Color(0xFF73140D)
val red_09 = Color(0xFF530E09)
val red_10 = Color(0xFF2E0805)

// Purples
val purple_01 = Color(0xFFFDFBFE)
val purple_02 = Color(0xFFF8F3FC)
val purple_03 = Color(0xFFEADBF5)
val purple_04 = Color(0xFFDCC3EF)
val purple_05 = Color(0xFFCBA6E7)
val purple_06 = Color(0xFF8936C9)
val purple_07 = Color(0xFF6B2A9D)
val purple_08 = Color(0xFF582281)
val purple_09 = Color(0xFF3F195D)
val purple_10 = Color(0xFF210D30)

// Blues
val blue_01 = Color(0xFFFAFAFF)
val blue_02 = Color(0xFFF1F1FE)
val blue_03 = Color(0xFFDEDEFC)
val blue_04 = Color(0xFFC6C6FA)
val blue_05 = Color(0xFFAFAFF9)
val blue_06 = Color(0xFF4C4CF0)
val blue_07 = Color(0xFF1212E2)
val blue_08 = Color(0xFF0F0FBD)
val blue_09 = Color(0xFF0B0B89)
val blue_10 = Color(0xFF050543)

@Immutable
data class KSCustomColors(
    // NEW COLORS
    // Text Colors
    val textPrimary: Color = Color.Unspecified,
    val textInversePrimary: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
    val textInverseSecondary: Color = Color.Unspecified,
    val textDisabled: Color = Color.Unspecified,
    val textAccentGrey: Color = Color.Unspecified,
    val textAccentRed: Color = Color.Unspecified,
    val textAccentRedBold: Color = Color.Unspecified,
    val textAccentGreen: Color = Color.Unspecified,
    val textAccentGreenBold: Color = Color.Unspecified,
    val textAccentBlue: Color = Color.Unspecified,
    val textAccentBlueBold: Color = Color.Unspecified,
    val textAccentPurple: Color = Color.Unspecified,
    val textAccentPurpleBold: Color = Color.Unspecified,
    val textAccentYellow: Color = Color.Unspecified,
    val textAccentYellowBold: Color = Color.Unspecified,

    // Background Colors
    val backgroundSurfacePrimary: Color = Color.Unspecified,
    val backgroundSurfaceInverse: Color = Color.Unspecified,
    val backgroundDisabled: Color = Color.Unspecified,
    val backgroundInverse: Color = Color.Unspecified,
    val backgroundInverseHover: Color = Color.Unspecified,
    val backgroundInversePressed: Color = Color.Unspecified,
    val backgroundSelected: Color = Color.Unspecified,
    val backgroundAction: Color = Color.Unspecified,
    val backgroundActionHover: Color = Color.Unspecified,
    val backgroundActionDisabled: Color = Color.Unspecified,
    val backgroundActionPressed: Color = Color.Unspecified,
    val backgroundAccentGreenBold: Color = Color.Unspecified,
    val backgroundAccentGreenSubtle: Color = Color.Unspecified,
    val backgroundAccentBlueBold: Color = Color.Unspecified,
    val backgroundAccentBlueSubtle: Color = Color.Unspecified,
    val backgroundAccentOrangeSubtle: Color = Color.Unspecified,
    val backgroundAccentPurpleSubtle: Color = Color.Unspecified,
    val backgroundDangerBold: Color = Color.Unspecified,
    val backgroundDangerSubtle: Color = Color.Unspecified,
    val backgroundDangerBoldPressed: Color = Color.Unspecified,
    val backgroundDangerBoldHovered: Color = Color.Unspecified,
    val backgroundDangerSubtleHovered: Color = Color.Unspecified,
    val backgroundAccentGrayBold: Color = Color.Unspecified,
    val backgroundAccentGraySubtle: Color = Color.Unspecified,
    val backgroundWarningBold: Color = Color.Unspecified,
    val backgroundWarningSubtle: Color = Color.Unspecified,

    // Border Colors
    val borderAccentBlueBold: Color = Color.Unspecified,
    val borderAccentBlueSubtle: Color = Color.Unspecified,
    val borderAccentGreenSubtle: Color = Color.Unspecified,
    val borderWarningBold: Color = Color.Unspecified,
    val borderWarningSubtle: Color = Color.Unspecified,
    val borderDisabled: Color = Color.Unspecified,
    val borderBoldHover: Color = Color.Unspecified,
    val borderSubtleHover: Color = Color.Unspecified,
    val borderActive: Color = Color.Unspecified,
    val borderBold: Color = Color.Unspecified,
    val borderDangerBold: Color = Color.Unspecified,
    val borderDangerSubtle: Color = Color.Unspecified,
    val borderSubtle: Color = Color.Unspecified,
    val borderFocus: Color = Color.Unspecified,

    // Icon Colors
    val icon: Color = Color.Unspecified,
    val iconDanger: Color = Color.Unspecified,
    val iconWarning: Color = Color.Unspecified,
    val iconSubtle: Color = Color.Unspecified,
    val iconInverse: Color = Color.Unspecified,
    val iconInfo: Color = Color.Unspecified,
    val iconDisabled: Color = Color.Unspecified,

    // OLD COLORS
    // Greens
    val kds_create_100: Color = Color.Unspecified,
    val kds_create_300: Color = Color.Unspecified,
    val kds_create_500: Color = Color.Unspecified,
    val kds_create_700: Color = Color.Unspecified,

    // BLUES
    val kds_trust_100: Color = Color.Unspecified,
    val kds_trust_300: Color = Color.Unspecified,
    val kds_trust_500: Color = Color.Unspecified,
    val kds_trust_700: Color = Color.Unspecified,

    // CORALS
    val kds_celebrate_100: Color = Color.Unspecified,
    val kds_celebrate_300: Color = Color.Unspecified,
    val kds_celebrate_500: Color = Color.Unspecified,
    val kds_celebrate_700: Color = Color.Unspecified,

    // GREYS
    val kds_white: Color = Color.Unspecified,
    val kds_support_100: Color = Color.Unspecified,
    val kds_support_200: Color = Color.Unspecified,
    val kds_support_300: Color = Color.Unspecified,
    val kds_support_400: Color = Color.Unspecified,
    val kds_support_500: Color = Color.Unspecified,
    val kds_support_700: Color = Color.Unspecified,
    val kds_black: Color = Color.Unspecified,

    // FUNCTIONAL COLORS
    val kds_alert: Color = Color.Unspecified,
    val kds_warn: Color = Color.Unspecified,
    val kds_inform: Color = Color.Unspecified,
    val facebook_blue: Color = Color.Unspecified,
)

val LocalKSCustomColors = staticCompositionLocalOf {
    KSCustomColors()
}

val KSLightCustomColors = KSCustomColors(
    // NEW COLORS
    // Text
    textPrimary = grey_10,
    textInversePrimary = white,
    textSecondary = grey_07,
    textInverseSecondary = grey_03,
    textDisabled = grey_05,
    textAccentGrey = grey_10,
    textAccentRed = red_06,
    textAccentRedBold = red_08,
    textAccentGreen = green_06,
    textAccentGreenBold = green_08,
    textAccentBlue = blue_06,
    textAccentBlueBold = blue_08,
    textAccentPurple = purple_06,
    textAccentPurpleBold = purple_08,
    textAccentYellow = yellow_06,
    textAccentYellowBold = yellow_08,

    // Background Colors
    backgroundSurfacePrimary = white,
    backgroundSurfaceInverse = grey_10,
    backgroundDisabled = grey_04,
    backgroundInverse = white,
    backgroundInverseHover = grey_02,
    backgroundInversePressed = grey_03,
    backgroundSelected = grey_09,
    backgroundAction = grey_10,
    backgroundActionHover = black,
    backgroundActionDisabled = grey_05,
    backgroundActionPressed = grey_09,
    backgroundAccentGreenBold = green_06,
    backgroundAccentGreenSubtle = green_02,
    backgroundAccentBlueBold = blue_06,
    backgroundAccentBlueSubtle = blue_02,
    backgroundAccentOrangeSubtle = orange_02,
    backgroundAccentPurpleSubtle = purple_02,
    backgroundDangerBold = red_06,
    backgroundDangerSubtle = red_02,
    backgroundDangerBoldPressed = red_08,
    backgroundDangerBoldHovered = red_07,
    backgroundDangerSubtleHovered = red_03,
    backgroundAccentGrayBold = grey_06,
    backgroundAccentGraySubtle = grey_02,
    backgroundWarningBold = yellow_06,
    backgroundWarningSubtle = yellow_02,

    // Border Colors
    borderAccentBlueBold = blue_08,
    borderAccentBlueSubtle = blue_04,
    borderAccentGreenSubtle = green_04,
    borderWarningBold = yellow_08,
    borderWarningSubtle = yellow_04,
    borderDisabled = grey_02,
    borderBoldHover = grey_08,
    borderSubtleHover = grey_05,
    borderActive = grey_08,
    borderBold = grey_04,
    borderDangerBold = red_08,
    borderDangerSubtle = red_04,
    borderSubtle = grey_03,
    borderFocus = blue_05,

    // Icon Colors
    icon = grey_08,
    iconDanger = red_07,
    iconWarning = yellow_07,
    iconSubtle = grey_06,
    iconInverse = grey_03,
    iconInfo = blue_07,
    iconDisabled = grey_05,

    // OLD COLORS
    // Greens
    kds_create_100 = kds_create_100,
    kds_create_300 = kds_create_300,
    kds_create_500 = kds_create_500,
    kds_create_700 = kds_create_700,

    // BLUES
    kds_trust_100 = kds_trust_100,
    kds_trust_300 = kds_trust_300,
    kds_trust_500 = kds_trust_500,
    kds_trust_700 = kds_trust_700,

    // CORALS
    kds_celebrate_100 = kds_celebrate_100,
    kds_celebrate_300 = kds_celebrate_300,
    kds_celebrate_500 = kds_celebrate_500,
    kds_celebrate_700 = kds_celebrate_700,

    // GREYS
    kds_white = kds_white,
    kds_support_100 = kds_support_100,
    kds_support_200 = kds_support_200,
    kds_support_300 = kds_support_300,
    kds_support_400 = kds_support_400,
    kds_support_500 = kds_support_500,
    kds_support_700 = kds_support_700,
    kds_black = kds_black,

    // FUNCTIONAL COLORS
    kds_alert = kds_alert,
    kds_warn = kds_warn,
    kds_inform = kds_inform,
    facebook_blue = facebook_blue,
)

// TODO: Change colors to reflect actual dark theme when available
val KSDarkCustomColors = KSCustomColors(
    // New Colors
    // Text
    textPrimary = grey_01,
    textInversePrimary = grey_10,
    textSecondary = grey_04,
    textInverseSecondary = grey_08,
    textDisabled = grey_06,
    textAccentGrey = grey_02,
    textAccentRed = red_05,
    textAccentRedBold = red_02,
    textAccentGreen = green_05,
    textAccentGreenBold = green_02,
    textAccentBlue = blue_05,
    textAccentBlueBold = blue_02,
    textAccentPurple = purple_05,
    textAccentPurpleBold = purple_02,
    textAccentYellow = yellow_05,
    textAccentYellowBold = yellow_02,

    // Background Colors
    backgroundSurfacePrimary = grey_10,
    backgroundSurfaceInverse = white,
    backgroundDisabled = grey_06,
    backgroundInverse = grey_10,
    backgroundInverseHover = grey_09,
    backgroundInversePressed = grey_08,
    backgroundSelected = grey_01,
    backgroundAction = grey_01,
    backgroundActionHover = white,
    backgroundActionDisabled = grey_06,
    backgroundActionPressed = grey_02,
    backgroundAccentGreenBold = green_05,
    backgroundAccentGreenSubtle = green_09,
    backgroundAccentBlueBold = blue_05,
    backgroundAccentBlueSubtle = blue_09,
    backgroundAccentOrangeSubtle = orange_09,
    backgroundAccentPurpleSubtle = purple_09,
    backgroundDangerBold = red_05,
    backgroundDangerSubtle = red_10,
    backgroundDangerBoldPressed = red_03,
    backgroundDangerBoldHovered = red_04,
    backgroundDangerSubtleHovered = red_08,
    backgroundAccentGrayBold = grey_06,
    backgroundAccentGraySubtle = grey_09,
    backgroundWarningBold = yellow_05,
    backgroundWarningSubtle = yellow_10,

    // Border Colors
    borderAccentBlueBold = blue_03,
    borderAccentBlueSubtle = blue_08,
    borderAccentGreenSubtle = green_08,
    borderWarningBold = yellow_03,
    borderWarningSubtle = yellow_08,
    borderDisabled = grey_09,
    borderBoldHover = grey_03,
    borderSubtleHover = white,
    borderActive = grey_01,
    borderBold = grey_06,
    borderDangerBold = red_03,
    borderDangerSubtle = red_08,
    borderSubtle = grey_08,
    borderFocus = blue_05,

    // Icon Colors
    icon = grey_02,
    iconDanger = red_03,
    iconWarning = yellow_03,
    iconSubtle = grey_05,
    iconInverse = grey_08,
    iconInfo = blue_03,
    iconDisabled = grey_06,

    // OLD COLORS
    // Greens
    kds_create_100 = kds_create_700,
    kds_create_300 = kds_create_500,
    kds_create_500 = kds_create_300,
    kds_create_700 = kds_create_100,

    // BLUES
    kds_trust_100 = kds_trust_700,
    kds_trust_300 = kds_trust_500,
    kds_trust_500 = kds_trust_300,
    kds_trust_700 = kds_trust_100,

    // CORALS
    kds_celebrate_100 = kds_celebrate_700,
    kds_celebrate_300 = kds_celebrate_500,
    kds_celebrate_500 = kds_celebrate_300,
    kds_celebrate_700 = kds_celebrate_100,

    // GREYS
    kds_white = kds_black,
    kds_support_100 = kds_support_700,
    kds_support_200 = kds_support_500,
    kds_support_300 = kds_support_400,
    kds_support_400 = kds_support_300,
    kds_support_500 = kds_support_200,
    kds_support_700 = kds_support_100,
    kds_black = kds_white,

    // FUNCTIONAL COLORS
    kds_alert = kds_celebrate_500,
    kds_warn = kds_warn,
    kds_inform = kds_inform,
    facebook_blue = facebook_blue,
)
