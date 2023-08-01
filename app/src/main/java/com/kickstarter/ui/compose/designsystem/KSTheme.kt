package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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

    CompositionLocalProvider(
        LocalKSCustomColors provides colors,
        LocalKSCustomTypography provides typography,
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
}
