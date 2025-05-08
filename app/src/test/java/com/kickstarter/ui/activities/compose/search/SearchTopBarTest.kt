package com.kickstarter.ui.activities.compose.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.activities.compose.search.PillBarTestTags.pillTag
import org.junit.Test

class SearchTopBarTest : KSRobolectricTestCase() {

    @Test
    fun `SearchTopBar when phase 2 feature flag is off`() {
        composeTestRule.setContent {
            KSTheme {
                SearchTopBar(
                    onBackPressed = {},
                    onValueChanged = {},
                    selectedFilterCounts = mapOf(
                        FilterRowPillType.SORT.name to 0,
                        FilterRowPillType.CATEGORY.name to 0
                    ),
                    onPillPressed = {},
                    shouldShowPhase2 = false
                )
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertDoesNotExist()
    }

    @Test
    fun `SearchTopBar when phase 2 feature flag is on`() {
        composeTestRule.setContent {
            KSTheme {
                SearchTopBar(
                    onBackPressed = {},
                    onValueChanged = {},
                    selectedFilterCounts = mapOf(
                        FilterRowPillType.SORT.name to 0,
                        FilterRowPillType.CATEGORY.name to 0
                    ),
                    onPillPressed = {},
                    shouldShowPhase2 = true
                )
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertIsDisplayed()
    }

    @Test
    fun `SearchTopBar pillBar 2 filters active`() {
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
                        FilterRowPillType.PROJECT_STATUS.name to 1
                    ),
                    onPillPressed = {},
                    shouldShowPhase2 = true
                )
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertExists()

        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER))
            .assertTextEquals("2")
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
                        FilterRowPillType.PROJECT_STATUS.name to 0
                    ),
                    onPillPressed = {},
                    shouldShowPhase2 = true
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
                        FilterRowPillType.PROJECT_STATUS.name to 1
                    ),
                    onPillPressed = {},
                    shouldShowPhase2 = true
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
