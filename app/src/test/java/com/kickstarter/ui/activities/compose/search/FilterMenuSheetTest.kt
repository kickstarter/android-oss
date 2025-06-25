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
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.BottomSheetFooterTestTags
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class FilterMenuSheetTest : KSRobolectricTestCase() {

    @Test
    fun `test FilterMenuSheet renders all pills within ProjectStatusRow`() {

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    FilterMenuSheet()
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
    fun `test FilterMenuSheet renders all available filter Rows with ffOff`() {
        val shouldShowPhase = false
        composeTestRule.setContent {
            KSTheme {
                FilterMenuSheet(
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
    fun `test FilterMenuSheet renders all available filter Rows with ffOn`() {
        composeTestRule.setContent {
            val shouldShowPhase = true
            KSTheme {
                FilterMenuSheet(
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
        composeTestRule.onNodeWithTag(FilterMenuTestTags.AMOUNT_RAISED_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.LOCATION_ROW).assertIsDisplayed()
    }

    @Test
    fun `test selected and unselected status for live pill`() {
        var counter = 0
        composeTestRule.setContent {
            KSTheme {
                FilterMenuSheet(
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
    fun `test FilterMenu _onApplyCallback receives projectState when pressing footer right button`() {
        var selected: DiscoveryParams.State? = null
        var contextFrom: Boolean? = null
        composeTestRule.setContent {
            KSTheme {
                FilterMenuSheet(onApply = { state, from ->
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
    fun `test FilterMenu  _onApplyCallback clears selection when pressing footer left button`() {
        var selected: DiscoveryParams.State? = DiscoveryParams.State.LIVE
        var contextFrom: Boolean? = null

        composeTestRule.setContent {
            KSTheme {
                FilterMenuSheet(
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
    fun `category row, selected category subtext is present`() {
        composeTestRule.setContent {
            KSTheme {
                FilterMenuSheet(
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

    @Test
    fun `percentage raised row, selected percentage bucket, subtext is present`() {
        var textForBucket: String = ""
        composeTestRule.setContent {
            KSTheme {
                textForBucket = textForBucket(DiscoveryParams.RaisedBuckets.BUCKET_2)
                FilterMenuSheet(
                    onApply = { _, _ ->
                    },
                    selectedPercentage = DiscoveryParams.RaisedBuckets.BUCKET_2
                )
            }
        }

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule.onNodeWithTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(textForBucket)
            .assertIsDisplayed()
    }

    @Test
    fun `location row, selected location, subtext is present`() {
        composeTestRule.setContent {
            KSTheme {
                FilterMenuSheet(
                    onApply = { _, _ ->
                    },
                    selectedLocation = LocationFactory.vancouver()
                )
            }
        }

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule
            .onNodeWithText(LocationFactory.vancouver().displayableName())
            .assertIsDisplayed()
    }

    @Test
    fun `amount raised row, selected amount bucket, subtext is present`() {
        var textForBucket: String = ""
        composeTestRule.setContent {
            KSTheme {
                textForBucket = textForBucket(DiscoveryParams.AmountBuckets.BUCKET_4)
                FilterMenuSheet(
                    onApply = { _, _ ->
                    },
                    selectedAmount = DiscoveryParams.AmountBuckets.BUCKET_4
                )
            }
        }

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule
            .onNodeWithText(textForBucket)
            .assertIsDisplayed()
    }
}
