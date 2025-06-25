package com.kickstarter.ui.activities.compose.search

import androidx.compose.material.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CategoryFactory
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
                    availableFilters = if (shouldShowPhase) FilterType.values().asList()
                    else FilterType.values().asList().filter { it != FilterType.LOCATION && it != FilterType.AMOUNT_RAISED },
                    onDismiss = {},
                    onApply = { a, b -> },
                    onNavigate = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(FilterMenuTestTags.SHEET).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.CATEGORY_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.PROJECT_STATUS_ROW).assertIsDisplayed()

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule.onNodeWithTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.LOCATION_ROW).assertDoesNotExist()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.AMOUNT_RAISED_ROW).assertDoesNotExist()
    }

    @Test
    fun `test FilterMenuBottomSheet renders all available filter Rows with ffOn`() {
        composeTestRule.setContent {
            val shouldShowPhase = true
            KSTheme {
                FilterMenuBottomSheet(
                    selectedProjectStatus = DiscoveryParams.State.LIVE,
                    availableFilters = if (shouldShowPhase) FilterType.values().asList()
                    else FilterType.values().asList().filter { it != FilterType.LOCATION && it != FilterType.AMOUNT_RAISED },
                    onDismiss = {},
                    onApply = { a, b -> },
                    onNavigate = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(FilterMenuTestTags.SHEET).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.CATEGORY_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.PROJECT_STATUS_ROW).assertIsDisplayed()

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule.onNodeWithTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.AMOUNT_RAISED_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.LOCATION_ROW).assertIsDisplayed()
    }

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

    @Test
    fun `category row, selected category subtext are present`() {
        composeTestRule.setContent {
            KSTheme {
                FilterMenuBottomSheet(
                    selectedCategory = CategoryFactory.CeramicsCategory(),
                    onApply = { _, _ ->
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithText(CategoryFactory.CeramicsCategory().name())
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(CategoryFactory.CeramicsCategory().name())
            .performScrollTo()
    }
}
