package com.kickstarter.ui.activities.compose.search

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.toProjectSort
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class SortSelectionBottomSheetKtTest : KSRobolectricTestCase() {

    private val dismissButton =
        composeTestRule.onNodeWithTag(SortSelectionBottomSheetTestTag.DISMISS_BUTTON.name)

    @Test
    fun `test tapping dismiss button should register dismiss`() {
        var dismissClickCount = 0

        composeTestRule.setContent {
            KSTheme {
                SortSelectionBottomSheet(
                    onDismiss = { dismissClickCount++ },
                    currentSelection = DiscoveryParams.Sort.MAGIC,
                    sorts = listOf(),
                    isLoading = false,
                )
            }
        }

        dismissButton.performClick()
        assertEquals(1, dismissClickCount)
    }
}