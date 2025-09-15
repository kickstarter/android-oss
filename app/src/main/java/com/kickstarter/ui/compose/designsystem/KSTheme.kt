package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun KickstarterApp(
    backgroundColor: Color = KSTheme.colors.kds_support_100,
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    KSTheme(useDarkTheme = useDarkTheme) {
        Surface(color = backgroundColor) {
            content()
        }
    }
}

@Composable
fun KSTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!useDarkTheme) {
        KSLightCustomColors
    } else {
        KSDarkCustomColors
    }

    val systemUiController = rememberSystemUiController()

    systemUiController.setSystemBarsColor(color = colors.kds_support_100)

    val typography = KSCustomTypography

    val typographyV2 = KSCustomTypographyV2

    val dimensions = KSStandardDimensions

    CompositionLocalProvider(
        LocalKSCustomColors provides colors,
        LocalKSCustomTypography provides typography,
        LocalKSCustomTypographyV2
            provides typographyV2,
        LocalKSCustomDimensions provides dimensions,
        content = content
    )
}

object KSTheme {
    val colors: KSCustomColors
        @Composable
        get() = LocalKSCustomColors.current

    val typography: KSTypography
        @Composable
        get() = LocalKSCustomTypography.current

    val dimensions: KSDimensions
        @Composable
        get() = LocalKSCustomDimensions.current

    val typographyV2: KSTypographyV2
        @Composable
        get() = LocalKSCustomTypographyV2.current
}
