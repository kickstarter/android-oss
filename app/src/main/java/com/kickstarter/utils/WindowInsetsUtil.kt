package com.kickstarter.utils

import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

object WindowInsetsUtil {

    /**
     * Configures the window for edge-to-edge content display and applies system bar insets
     * (status bar and navigation bar) as padding or margins to avoid content overlap.
     *
     * This method makes the content extend to the edges of the screen and adjusts the view's
     * layout parameters to ensure it doesn't overlap with system UI elements such as
     * the status bar or navigation bar.
     *
     * @param window The window of the current activity or fragment.
     * @param rootView The root view where the system bar insets will be applied.
     */
    fun manageEdgeToEdge(
        window: Window,
        rootView: View
    ) {
        // Set the window to allow drawing under the system bars (status, nav bars),
        // making them transparent, and enabling edge-to-edge display.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Listen for window insets (which represent system UI like status and navigation bars)
        // and apply those insets to the view's layout parameters.
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
            // Extract the insets that represent the system bars (status and navigation bars)
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Update the view's layout margins with the insets so the content avoids overlapping
            // with system bars. You can choose to apply top, left, bottom, and right insets as needed.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // Apply the left, right, top, and bottom margins based on the insets
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = insets.top
            }

            // Return CONSUMED to indicate that the window insets have been handled and
            // should not be passed down to child views. If you want child views to handle insets,
            // you can return the windowInsets instead.
            WindowInsetsCompat.CONSUMED
        }
    }
}
