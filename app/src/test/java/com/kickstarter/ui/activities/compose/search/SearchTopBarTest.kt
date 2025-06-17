package com.kickstarter.ui.activities.compose.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ui.activities.compose.search.PillBarTestTags.pillTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class SearchTopBarTest : KSRobolectricTestCase() {

    @Test
    fun `SearchTopBar when phase 3 feature flag is off`() {
        composeTestRule.setContent {
            KSTheme {
                SearchTopBar(
                    onBackPressed = {},
                    onValueChanged = {},
                    selectedFilterCounts = mapOf(
                        FilterRowPillType.SORT.name to 0,
                        FilterRowPillType.CATEGORY.name to 0,
                        FilterRowPillType.PROJECT_STATUS.name to 0,
                        FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                        FilterRowPillType.LOCATION.name to 0
                    ),
                    onPillPressed = {},
                    shouldShowPhase = false
                )
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PERCENTAGE_RAISED)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.LOCATION)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertIsDisplayed()
    }

    @Test
    fun `SearchTopBar when phase 3 feature flag is on`() {
        composeTestRule.setContent {
            KSTheme {
                SearchTopBar(
                    onBackPressed = {},
                    onValueChanged = {},
                    selectedFilterCounts = mapOf(
                        FilterRowPillType.SORT.name to 0,
                        FilterRowPillType.CATEGORY.name to 0,
                        FilterRowPillType.PROJECT_STATUS.name to 0,
                        FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                        FilterRowPillType.LOCATION.name to 0
                    ),
                    onPillPressed = {},
                    shouldShowPhase = true
                )
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PERCENTAGE_RAISED)).assertExists() // Requires scroll is wanna check isDisplayed
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.LOCATION)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertIsDisplayed()
    }

    @Test
    fun `SearchTopBar pillBar 3 filters active`() {
        composeTestRule.setContent {
            KSTheme {
                SearchTopBar(
                    onBackPressed = {},
                    onValueChanged = {},
                    categoryPillText = "Art",
                    projectStatusText = "Live",
                    selectedFilterCounts = mapOf(
                        FilterRowPillType.SORT.name to 0,
                        FilterRowPillType.CATEGORY.name to 1,
                        FilterRowPillType.FILTER.name to 1,
                        FilterRowPillType.PROJECT_STATUS.name to 1,
                        FilterRowPillType.PERCENTAGE_RAISED.name to 1
                    ),
                    onPillPressed = {},
                    shouldShowPhase = true
                )
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PERCENTAGE_RAISED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertExists()

        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER))
            .assertTextEquals("3")
    }

    @Test
    fun `SearchTopBar pillBar Category filter active`() {
        composeTestRule.setContent {
            KSTheme {
                SearchTopBar(
                    onBackPressed = {},
                    onValueChanged = {},
                    categoryPillText = "Art",
                    projectStatusText = "Project Status",
                    selectedFilterCounts = mapOf(
                        FilterRowPillType.SORT.name to 0,
                        FilterRowPillType.CATEGORY.name to 1,
                        FilterRowPillType.FILTER.name to 1,
                        FilterRowPillType.PROJECT_STATUS.name to 0,
                        FilterRowPillType.PERCENTAGE_RAISED.name to 0
                    ),
                    onPillPressed = {},
                    shouldShowPhase = true
                )
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertExists()

        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER))
            .assertTextEquals("1")
    }

    @Test
    fun `SearchTopBar pillBar ProjectStatus filter active`() {
        composeTestRule.setContent {
            KSTheme {
                SearchTopBar(
                    onBackPressed = {},
                    onValueChanged = {},
                    projectStatusText = "Live",
                    selectedFilterCounts = mapOf(
                        FilterRowPillType.SORT.name to 0,
                        FilterRowPillType.CATEGORY.name to 0,
                        FilterRowPillType.FILTER.name to 1,
                        FilterRowPillType.PROJECT_STATUS.name to 1,
                        FilterRowPillType.PERCENTAGE_RAISED.name to 0
                    ),
                    onPillPressed = {},
                    shouldShowPhase = true
                )
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertExists()

        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER))
            .assertTextEquals("1")
    }

    @Test
    fun `SearchTopBar pillBar PercentageRaised filter active`() {
        composeTestRule.setContent {
            KSTheme {
                SearchTopBar(
                    onBackPressed = {},
                    onValueChanged = {},
                    projectStatusText = "Live",
                    selectedFilterCounts = mapOf(
                        FilterRowPillType.SORT.name to 0,
                        FilterRowPillType.CATEGORY.name to 0,
                        FilterRowPillType.FILTER.name to 1,
                        FilterRowPillType.PROJECT_STATUS.name to 0,
                        FilterRowPillType.PERCENTAGE_RAISED.name to 1
                    ),
                    onPillPressed = {},
                    shouldShowPhase = true
                )
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertExists()

        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER))
            .assertTextEquals("1")
    }
}
