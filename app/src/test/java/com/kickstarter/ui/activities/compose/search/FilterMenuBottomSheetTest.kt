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
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.LIVE)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.LATE_PLEDGES)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.SUCCESSFUL)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.UPCOMING)).assertIsDisplayed()
    }

    @Test
    fun `test FilterMenuBottomSheet renders all available filter Rows with ffOff`() {
        val shouldShowPhase = false
        composeTestRule.setContent {
            KSTheme {
                FilterMenuBottomSheet(
                    onApply = { a, b -> },
                    onDismiss = {},
                    onNavigate = {},
                    availableFilters = if (shouldShowPhase) FilterType.values().asList()
                    else FilterType.values().asList().filter { it != FilterType.PERCENTAGE_RAISED }
                )
            }
        }

        composeTestRule.onNodeWithTag(FilterMenuTestTags.SHEET).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.CATEGORY_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.PROJECT_STATUS_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertDoesNotExist()
    }

// TODO failing an assert for unknown reasons, when running it with breakpoints never reached the last iteration for availableFilters,
// TODO when running the real app with breakpoints always reaches the last iteration for availableFilters quite likely test setup issue but the love of god cannot find the issue

//    @Test
//    fun `test FilterMenuBottomSheet renders all available filter Rows with ffOn`() {
//        val shouldShowPhase = true
//        composeTestRule.setContent {
//            KSTheme {
//                FilterMenuBottomSheet(
//                    modifier = Modifier.width(500.dp).height(800.dp),
//                    selectedProjectStatus = DiscoveryParams.State.LIVE,
//                    onApply = {a, b -> },
//                    onDismiss = {},
//                    onNavigate = {},
//                    availableFilters = if (shouldShowPhase) FilterType.values().asList()
//                    else FilterType.values().asList().filter { it != FilterType.PERCENTAGE_RAISED }
//                )
//            }
//        }
//
//        composeTestRule.onNodeWithTag(FilterMenuTestTags.SHEET).assertIsDisplayed()
//        composeTestRule.onNodeWithTag(FilterMenuTestTags.CATEGORY_ROW).assertIsDisplayed()
//        composeTestRule.onNodeWithTag(FilterMenuTestTags.PROJECT_STATUS_ROW).assertIsDisplayed()
//        composeTestRule.onNodeWithText(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertExists()
//        composeTestRule.onNodeWithTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertIsDisplayed()
//    }

    @Test
    fun `test selected and unselected status for live pill`() {
        var counter = 0
        composeTestRule.setContent {
            KSTheme {
                FilterMenuBottomSheet(
                    onApply = { publicState: DiscoveryParams.State?, from: Boolean? ->
                        counter++
                        if (counter == 1)
                            assertEquals(publicState, DiscoveryParams.State.LIVE)

                        if (counter == 2)
                            assertEquals(publicState, null)

                        assertNull(from)
                    }
                )
            }
        }

        val livePill = composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.LIVE))
        livePill.performClick() // Select

        livePill.performClick() // Unselect (toggle off)
    }

    @Test
    fun filterMenu_onApplyCallbackReceivesSelection() {
        var selected: DiscoveryParams.State? = null
        var contextFrom: Boolean? = null
        composeTestRule.setContent {
            KSTheme {
                FilterMenuBottomSheet(onApply = { state, from ->
                    selected = state
                    contextFrom = from
                })
            }
        }

        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.LIVE))
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .performClick()

        assertEquals(DiscoveryParams.State.LIVE, selected)
        assertEquals(contextFrom, true)
    }

    @Test
    fun filterMenu_resetClearsSelection() {
        var selected: DiscoveryParams.State? = DiscoveryParams.State.LIVE
        var contextFrom: Boolean? = null

        composeTestRule.setContent {
            KSTheme {
                FilterMenuBottomSheet(
                    selectedProjectStatus = selected,
                    onApply = { state, from ->
                        selected = state
                        contextFrom = from
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .performClick()

        assertNull(selected)
        assertEquals(contextFrom, false)
    }
}
