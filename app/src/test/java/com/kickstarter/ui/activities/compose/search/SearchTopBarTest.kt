package com.kickstarter.ui.activities.compose.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.views.compose.search.FilterRowPillType
import com.kickstarter.ui.views.compose.search.PillBarTestTags.pillTag
import com.kickstarter.ui.views.compose.search.SearchTopBar
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
}
