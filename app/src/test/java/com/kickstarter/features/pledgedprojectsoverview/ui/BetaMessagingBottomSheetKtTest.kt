package com.kickstarter.features.pledgedprojectsoverview.ui

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class
BetaMessagingBottomSheetKtTest : KSRobolectricTestCase() {

    private val backedProjectsButton =
        composeTestRule.onNodeWithTag(BetaMessagingBottomSheetTestTag.BACKED_PROJECTS_BUTTON.name, useUnmergedTree = true)

    @Test
    fun `test tapping see all backed projects button, should register click and dismiss`() {
        var backedProjectsClickedCount = 0
        var dismissBottomSheet = 0
        composeTestRule.setContent {
            KSTheme {
                BetaMessagingBottomSheet(
                    onSeeAllBackedProjectsClick = { backedProjectsClickedCount++ },
                    dismiss = { dismissBottomSheet++ }
                )
            }
        }

        backedProjectsButton.performClick()
        assertEquals(1, backedProjectsClickedCount)
        assertEquals(1, dismissBottomSheet)
    }
}
