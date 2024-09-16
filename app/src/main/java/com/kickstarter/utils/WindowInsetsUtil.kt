package com.kickstarter.utils
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

data class LayoutPaddingConfig(
    val layout: ViewGroup,
    val applyTopPadding: Boolean = true,
    val applyBottomPadding: Boolean = true
)

object WindowInsetsUtil {

    /**
     * Sets up the activity or fragment for edge-to-edge display and adjusts padding
     * for system bars (status bar, navigation bar).
     *
     * @param window The window of the activity or fragment.
     * @param rootView The root view where the window insets will be applied.
     * @param mainLayout The layout that needs padding adjustments to avoid content
     * being hidden by system bars.
     * @param applyTopPadding If true, applies padding to the top (status bar).
     * @param applyBottomPadding If true, applies padding to the bottom (navigation bar).
     */
    fun manageEdgeToEdge(
        window: Window,
        rootView: View,
        mainLayout: ViewGroup,
        applyTopPadding: Boolean = true,
        applyBottomPadding: Boolean = true
    ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply padding based on the flags
            mainLayout.setPadding(
                mainLayout.left,
                if (applyTopPadding) systemBarsInsets.top else mainLayout.top,
                mainLayout.right,
                if (applyBottomPadding) systemBarsInsets.bottom else mainLayout.bottom
            )
            WindowInsetsCompat.CONSUMED

        }
    }

    /**
     * Sets up the activity or fragment for edge-to-edge display and adjusts padding for system bars
     * (status bar, navigation bar) for multiple layouts with configurable padding options.
     *
     * @param window The window of the activity or fragment.
     * @param rootView The root view where the window insets will be applied.
     * @param layoutConfigs A list of LayoutPaddingConfig, which holds the layout and its padding configurations.
     */
    fun manageEdgeToEdgeOnMultipleLayouts(
        window: Window,
        rootView: View,
        layoutConfigs: List<LayoutPaddingConfig>
    ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            layoutConfigs.forEach { config ->
                config.layout.setPadding(
                    config.layout.left,
                    if (config.applyTopPadding) systemBarsInsets.top else config.layout.paddingTop,
                    config.layout.right,
                    if (config.applyBottomPadding) systemBarsInsets.bottom else config.layout.paddingBottom
                )
            }
            WindowInsetsCompat.CONSUMED

        }
    }
}
