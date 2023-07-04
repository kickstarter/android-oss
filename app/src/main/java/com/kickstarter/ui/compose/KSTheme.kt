package com.kickstarter.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun KSTheme (
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if(!useDarkTheme) {
        KSLightCustomColors
    } else {
        KSDarkCustomColors
    }

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