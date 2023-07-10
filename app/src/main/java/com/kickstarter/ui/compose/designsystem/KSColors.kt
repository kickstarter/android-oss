package com.kickstarter.ui.compose.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class KSCustomColors(
    //Greens
    val kds_create_100: Color = Color.Unspecified,
    val kds_create_300: Color = Color.Unspecified,
    val kds_create_500: Color = Color.Unspecified,
    val kds_create_700: Color = Color.Unspecified,

    //BLUES
    val kds_trust_100: Color = Color.Unspecified,
    val kds_trust_300: Color = Color.Unspecified,
    val kds_trust_500: Color = Color.Unspecified,
    val kds_trust_700: Color = Color.Unspecified,

    //CORALS
    val kds_celebrate_100: Color = Color.Unspecified,
    val kds_celebrate_300: Color = Color.Unspecified,
    val kds_celebrate_500: Color = Color.Unspecified,
    val kds_celebrate_700: Color = Color.Unspecified,

    //GREYS
    val kds_white: Color = Color.Unspecified,
    val kds_support_100: Color = Color.Unspecified,
    val kds_support_200: Color = Color.Unspecified,
    val kds_support_300: Color = Color.Unspecified,
    val kds_support_400: Color = Color.Unspecified,
    val kds_support_500: Color = Color.Unspecified,
    val kds_support_700: Color = Color.Unspecified,
    val kds_black: Color = Color.Unspecified,

    //FUNCTIONAL COLORS
    val kds_alert: Color = Color.Unspecified,
    val kds_warn: Color = Color.Unspecified,
    val kds_inform: Color = Color.Unspecified,
    val facebook_blue: Color = Color.Unspecified,
)

val LocalKSCustomColors = staticCompositionLocalOf {
    KSCustomColors()
}

val KSLightCustomColors = KSCustomColors(
    //Greens
    kds_create_100 = Color(0xFFE6FAF1),
    kds_create_300 = Color(0xFF9BEBC9),
    kds_create_500 = Color(0xFF05CE78),
    kds_create_700 = Color(0xFF028858),

    //BLUES
    kds_trust_100 = Color(0xFFDBE7FF),
    kds_trust_300 = Color(0xFF71A0FF),
    kds_trust_500 = Color(0xFF5555FF),
    kds_trust_700 = Color(0xFF0A007D),

    //CORALS
    kds_celebrate_100 = Color(0xFFFFF2EC),
    kds_celebrate_300 = Color(0xFFFECCB3),
    kds_celebrate_500 = Color(0xFFF97B62),
    kds_celebrate_700 = Color(0xFFD8503D),

    //GREYS
    kds_white = Color(0xFFFFFFFF),
    kds_support_100 = Color(0xFFF3F3F3),
    kds_support_200 = Color(0xFFE6E6E6),
    kds_support_300 = Color(0xFFD1D1D1),
    kds_support_400 = Color(0xFF696969),
    kds_support_500 = Color(0xFF464646),
    kds_support_700 = Color(0xFF222222),
    kds_black = Color(0xff000000),

    //FUNCTIONAL COLORS
    kds_alert = Color(0xFFA12027),
    kds_warn = Color(0xFFF9D66D),
    kds_inform = Color(0xFFB6D9E1),
    facebook_blue = Color(0xFF1877F2),
)

//TODO: Change colors to reflect actual dark theme when available
val KSDarkCustomColors = KSCustomColors(
    //Greens
    kds_create_100 = Color(0xFFE6FAF1),
    kds_create_300 = Color(0xFF9BEBC9),
    kds_create_500 = Color(0xFF05CE78),
    kds_create_700 = Color(0xFF028858),

    //BLUES
    kds_trust_100 = Color(0xFFDBE7FF),
    kds_trust_300 = Color(0xFF71A0FF),
    kds_trust_500 = Color(0xFF5555FF),
    kds_trust_700 = Color(0xFF0A007D),

    //CORALS
    kds_celebrate_100 = Color(0xFFFFF2EC),
    kds_celebrate_300 = Color(0xFFFECCB3),
    kds_celebrate_500 = Color(0xFFF97B62),
    kds_celebrate_700 = Color(0xFFD8503D),

    //GREYS
    kds_white = Color(0xFFFFFFFF),
    kds_support_100 = Color(0xFFF3F3F3),
    kds_support_200 = Color(0xFFE6E6E6),
    kds_support_300 = Color(0xFFD1D1D1),
    kds_support_400 = Color(0xFF696969),
    kds_support_500 = Color(0xFF464646),
    kds_support_700 = Color(0xFF222222),
    kds_black = Color(0xFF000000),

    //FUNCTIONAL COLORS
    kds_alert = Color(0xFFA12027),
    kds_warn = Color(0xFFF9D66D),
    kds_inform = Color(0xFFB6D9E1),
    facebook_blue = Color(0xFF1877F2),
)

//GREENS
val kds_create_100 = Color(0xFFE6FAF1)
val kds_create_300 = Color(0xFF9BEBC9)
val kds_create_500 = Color(0xFF05CE78)
val kds_create_700 = Color(0xFF028858)

//BLUES
val kds_trust_100 = Color(0xFFDBE7FF)
val kds_trust_300 = Color(0xFF71A0FF)
val kds_trust_500 = Color(0xFF5555FF)
val kds_trust_700 = Color(0xFF0A007D)

//CORALS
val kds_celebrate_100 = Color(0xFFFFF2EC)
val kds_celebrate_300 = Color(0xFFFECCB3)
val kds_celebrate_500 = Color(0xFFF97B62)
val kds_celebrate_700 = Color(0xFFD8503D)

//GREYS
val kds_white = Color(0xFFFFFFFF)
val kds_support_100 = Color(0xFFF3F3F3)
val kds_support_200 = Color(0xFFE6E6E6)
val kds_support_300 = Color(0xFFD1D1D1)
val kds_support_400 = Color(0xFF696969)
val kds_support_500 = Color(0xFF464646)
val kds_support_700 = Color(0xFF222222)
val kds_black = Color(0xff000000)

//FUNCTIONAL COLORS
val kds_alert = Color(0xFFA12027)
val kds_warn = Color(0xFFF9D66D)
val kds_inform = Color(0xFFB6D9E1)
val facebook_blue = Color(0xFF1877F2)
