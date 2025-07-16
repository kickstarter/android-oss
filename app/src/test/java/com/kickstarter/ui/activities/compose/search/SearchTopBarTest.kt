package com.kickstarter.ui.activities.compose.search

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.ui.activities.compose.search.PillBarTestTags.pillTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class SearchTopBarTest : KSRobolectricTestCase() {

    @Test
    fun `SearchTopBar when phase 6-7 feature flag is off`() {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    SearchTopBar(
                        onBackPressed = {},
                        onValueChanged = {},
                        selectedFilterCounts = mapOf(
                            FilterRowPillType.SORT.name to 0,
                            FilterRowPillType.CATEGORY.name to 0,
                            FilterRowPillType.PROJECT_STATUS.name to 0,
                            FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                            FilterRowPillType.LOCATION.name to 0,
                            FilterRowPillType.AMOUNT_RAISED.name to 0,
                            FilterRowPillType.RECOMMENDED.name to 0,
                            FilterRowPillType.PROJECTS_LOVED.name to 0,
                            FilterRowPillType.SAVED.name to 0,
                            FilterRowPillType.FOLLOWING.name to 0,
                            FilterRowPillType.GOAL.name to 0,
                        ),
                        onPillPressed = {},
                        shouldShowPhase = false
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PERCENTAGE_RAISED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.LOCATION)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.AMOUNT_RAISED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.RECOMMENDED)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECTS_LOVED)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.GOAL)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.SAVED)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FOLLOWING)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertIsDisplayed()
    }

    @Test
    fun `SearchTopBar when phase 6-7 feature flag is on`() {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    SearchTopBar(
                        onBackPressed = {},
                        onValueChanged = {},
                        selectedFilterCounts = mapOf(
                            FilterRowPillType.SORT.name to 0,
                            FilterRowPillType.CATEGORY.name to 0,
                            FilterRowPillType.PROJECT_STATUS.name to 0,
                            FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                            FilterRowPillType.LOCATION.name to 0,
                            FilterRowPillType.AMOUNT_RAISED.name to 0,
                            FilterRowPillType.RECOMMENDED.name to 0,
                            FilterRowPillType.PROJECTS_LOVED.name to 0,
                            FilterRowPillType.SAVED.name to 0,
                            FilterRowPillType.FOLLOWING.name to 0,
                            FilterRowPillType.GOAL.name to 0,
                        ),
                        onPillPressed = {},
                        shouldShowPhase = true
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PERCENTAGE_RAISED)).assertExists() // exists instead of display to avoid having to scroll
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.LOCATION)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.AMOUNT_RAISED)).assertExists() // exists instead of display to avoid having to scroll
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.RECOMMENDED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECTS_LOVED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.SAVED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FOLLOWING)).assertExists()
    }

    @Test
    fun `SearchTopBar when phase 6-7 feature flag is on User is logged out`() {
        val env = environment()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    SearchTopBar(
                        onBackPressed = {},
                        onValueChanged = {},
                        selectedFilterCounts = mapOf(
                            FilterRowPillType.SORT.name to 0,
                            FilterRowPillType.CATEGORY.name to 0,
                            FilterRowPillType.PROJECT_STATUS.name to 0,
                            FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                            FilterRowPillType.LOCATION.name to 0,
                            FilterRowPillType.AMOUNT_RAISED.name to 0,
                            FilterRowPillType.RECOMMENDED.name to 0,
                            FilterRowPillType.PROJECTS_LOVED.name to 0,
                            FilterRowPillType.SAVED.name to 0,
                            FilterRowPillType.FOLLOWING.name to 0,
                            FilterRowPillType.GOAL.name to 0,
                        ),
                        onPillPressed = {},
                        shouldShowPhase = true
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PERCENTAGE_RAISED)).assertExists() // exists instead of display to avoid having to scroll
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.LOCATION)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.AMOUNT_RAISED)).assertExists() // exists instead of display to avoid having to scroll
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.RECOMMENDED)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECTS_LOVED)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.SAVED)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FOLLOWING)).assertDoesNotExist()
    }

    @Test
    fun `SearchTopBar pillBar all filters active`() {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
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
                            FilterRowPillType.PERCENTAGE_RAISED.name to 1,
                            FilterRowPillType.AMOUNT_RAISED.name to 1,
                            FilterRowPillType.LOCATION.name to 1,
                            FilterRowPillType.GOAL.name to 1,
                            FilterRowPillType.RECOMMENDED.name to 1,
                            FilterRowPillType.PROJECTS_LOVED.name to 1,
                            FilterRowPillType.SAVED.name to 1,
                            FilterRowPillType.FOLLOWING.name to 1,
                        ),
                        onPillPressed = {},
                        shouldShowPhase = true
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PERCENTAGE_RAISED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.LOCATION)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.AMOUNT_RAISED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.RECOMMENDED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECTS_LOVED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.SAVED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FOLLOWING)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.GOAL)).assertExists()

        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER))
            .assertTextEquals("10")
    }

    @Test
    fun `SearchTopBar pillBar Category filter active`() {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
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
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
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
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
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
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertExists()

        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER))
            .assertTextEquals("1")
    }

    @Test
    fun `SearchTopBar pillBar AmountRaised filter active`() {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    SearchTopBar(
                        onBackPressed = {},
                        onValueChanged = {},
                        projectStatusText = "Live",
                        selectedFilterCounts = mapOf(
                            FilterRowPillType.SORT.name to 0,
                            FilterRowPillType.CATEGORY.name to 0,
                            FilterRowPillType.FILTER.name to 1,
                            FilterRowPillType.PROJECT_STATUS.name to 0,
                            FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                            FilterRowPillType.AMOUNT_RAISED.name to 1,
                        ),
                        onPillPressed = {},
                        shouldShowPhase = true
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.AMOUNT_RAISED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertExists()

        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER))
            .assertTextEquals("1")
    }

    @Test
    fun `SearchTopBar pillBar Goal filter active`() {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    SearchTopBar(
                        onBackPressed = {},
                        onValueChanged = {},
                        projectStatusText = "Live",
                        selectedFilterCounts = mapOf(
                            FilterRowPillType.SORT.name to 0,
                            FilterRowPillType.CATEGORY.name to 0,
                            FilterRowPillType.FILTER.name to 1,
                            FilterRowPillType.PROJECT_STATUS.name to 0,
                            FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                            FilterRowPillType.AMOUNT_RAISED.name to 0,
                            FilterRowPillType.GOAL.name to 1,
                        ),
                        onPillPressed = {},
                        shouldShowPhase = true
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.PROJECT_STATUS)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.CATEGORY)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.AMOUNT_RAISED)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.GOAL)).assertExists()
        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER)).assertExists()

        composeTestRule.onNodeWithTag(pillTag(FilterRowPillType.FILTER))
            .assertTextEquals("1")
    }
}
