package com.kickstarter.ui.activities.compose.search

import androidx.compose.material.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.BottomSheetFooterTestTags
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class FilterMenuBottomSheetTest : KSRobolectricTestCase() {

    @Test
    fun `test FilterMenuBottomSheet renders all pills within ProjectStatusRow`() {

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    FilterMenuBottomSheet()
                }
            }
        }

        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(null)).assertIsDisplayed() // All
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.PublicState.LIVE)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.PublicState.LATE_PLEDGE)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.PublicState.SUCCESSFUL)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.PublicState.UPCOMING)).assertIsDisplayed()
    }

    @Test
    fun `test FilterMenuBottomSheet renders all available filter Rows`() {
        composeTestRule.setContent {
            KSTheme {
                FilterMenuBottomSheet()
            }
        }

        composeTestRule.onNodeWithTag(FilterMenuTestTags.SHEET).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.CATEGORY_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.PROJECT_STATUS_ROW).assertIsDisplayed()
    }

    @Test
    fun `test selected and unselected status for live pill`() {
        var counter = 0
        composeTestRule.setContent {
            KSTheme {
                FilterMenuBottomSheet(
                    onApply = { publicState: DiscoveryParams.PublicState? ->
                        counter++
                        if (counter == 1)
                            assertEquals(publicState, DiscoveryParams.PublicState.LIVE)

                        if (counter == 2)
                            assertEquals(publicState, null)
                    }
                )
            }
        }

        val livePill = composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.PublicState.LIVE))
        livePill.performClick() // Select
        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name) // apply
            .performClick()

        livePill.performClick() // Unselect (toggle off)
        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name) // apply
            .performClick()
    }

    @Test
    fun filterMenu_onApplyCallbackReceivesSelection() {
        var selected: DiscoveryParams.PublicState? = null

        composeTestRule.setContent {
            KSTheme {
                FilterMenuBottomSheet(onApply = { selected = it })
            }
        }

        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.PublicState.LIVE))
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .performClick()

        assertEquals(DiscoveryParams.PublicState.LIVE, selected)
    }

    @Test
    fun filterMenu_resetClearsSelection() {
        var selected: DiscoveryParams.PublicState? = DiscoveryParams.PublicState.LIVE

        composeTestRule.setContent {
            KSTheme {
                FilterMenuBottomSheet(
                    selectedProjectStatus = selected,
                    onApply = { selected = it }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .performClick()

        assertNull(selected)
    }
}
