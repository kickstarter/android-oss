package com.kickstarter.features.pledgedprojectsoverview.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class PledgedProjectsOverviewScreenTest : KSRobolectricTestCase() {

    private val backButton =
        composeTestRule.onNodeWithTag(PledgedProjectsOverviewScreenTestTag.BACK_BUTTON.name)
    @Test
    fun testBackButtonClick() {
        var backClickedCount = 0
        composeTestRule.setContent {
            KSTheme {
                PledgedProjectsOverviewScreen(
                    modifier = Modifier,
                    onBackPressed = { backClickedCount++ },
                    lazyColumnListState = rememberLazyListState()
                )
            }
        }

        backButton.performClick()
        assertEquals(1, backClickedCount)
    }
}
