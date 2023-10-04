package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class SearchScreenTest : KSRobolectricTestCase() {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val backButton = composeTestRule.onNodeWithTag(SearchScreenTestTag.BACK_BUTTON.name)
    private val searchTextInput =
        composeTestRule.onNodeWithTag(SearchScreenTestTag.SEARCH_TEXT_INPUT.name)
    private val emptyView = composeTestRule.onNodeWithTag(SearchScreenTestTag.EMPTY_VIEW.name)
    private val loadingView = composeTestRule.onNodeWithTag(SearchScreenTestTag.LOADING_VIEW.name)
    private val inListLoadingView =
        composeTestRule.onNodeWithTag(SearchScreenTestTag.IN_LIST_LOADING_VIEW.name)
    private val listView = composeTestRule.onNodeWithTag(SearchScreenTestTag.LIST_VIEW.name)
    private val popularProjectsTitle =
        composeTestRule.onNodeWithTag(SearchScreenTestTag.POPULAR_PROJECTS_TITLE.name)
    private val featuredProjectView =
        composeTestRule.onNodeWithTag(SearchScreenTestTag.FEATURED_PROJECT_VIEW.name)

    @Test
    fun testEmptyComponentsVisible() {
        composeTestRule.setContent {
            KSTheme {
                SearchScreen(
                    onBackClicked = { },
                    scaffoldState = rememberScaffoldState(),
                    isLoading = false,
                    lazyColumnListState = rememberLazyListState(),
                    showEmptyView = true,
                    onSearchTermChanged = {},
                    onItemClicked = {}
                )
            }
        }

        backButton.assertIsDisplayed()
        searchTextInput.assertIsDisplayed()
        emptyView.assertIsDisplayed()
        loadingView.assertDoesNotExist()
        inListLoadingView.assertDoesNotExist()
        listView.assertDoesNotExist()
        popularProjectsTitle.assertDoesNotExist()
        featuredProjectView.assertDoesNotExist()
    }

    @Test
    fun testPopularListComponentsVisible() {
        composeTestRule.setContent {
            KSTheme {
                SearchScreen(
                    onBackClicked = { },
                    scaffoldState = rememberScaffoldState(),
                    isLoading = false,
                    lazyColumnListState = rememberLazyListState(),
                    showEmptyView = false,
                    isPopularList = true,
                    itemsList = List(20) {
                        Project.builder()
                            .name("This is a test $it")
                            .pledged((it * 2).toDouble())
                            .goal(20.0)
                            .state(if (it in 10..20) Project.STATE_SUBMITTED else Project.STATE_LIVE)
                            .build()
                    },
                    onSearchTermChanged = {},
                    onItemClicked = {}
                )
            }
        }

        backButton.assertIsDisplayed()
        searchTextInput.assertIsDisplayed()
        emptyView.assertDoesNotExist()
        loadingView.assertDoesNotExist()
        inListLoadingView.assertDoesNotExist()
        listView.assertIsDisplayed()

        val popularProjectTitleText = context.getString(R.string.Popular_Projects)
        popularProjectsTitle.assertIsDisplayed()
        popularProjectsTitle.assertTextEquals(popularProjectTitleText)

        featuredProjectView.assertIsDisplayed()

        for (i in 1..19) {
            listView.performScrollToIndex(i)
            val projectView =
                composeTestRule.onNodeWithTag(SearchScreenTestTag.NORMAL_PROJECT_VIEW.name + i)
            projectView.assertIsDisplayed()
        }
    }

    @Test
    fun testSearchedListComponentsVisible() {
        composeTestRule.setContent {
            KSTheme {
                SearchScreen(
                    onBackClicked = { },
                    scaffoldState = rememberScaffoldState(),
                    isLoading = false,
                    lazyColumnListState = rememberLazyListState(),
                    showEmptyView = false,
                    isPopularList = false,
                    itemsList = List(20) {
                        Project.builder()
                            .name("This is a test $it")
                            .pledged((it * 2).toDouble())
                            .goal(20.0)
                            .state(if (it in 10..20) Project.STATE_SUBMITTED else Project.STATE_LIVE)
                            .build()
                    },
                    onSearchTermChanged = {},
                    onItemClicked = {}
                )
            }
        }

        backButton.assertIsDisplayed()
        searchTextInput.assertIsDisplayed()
        emptyView.assertDoesNotExist()
        loadingView.assertDoesNotExist()
        inListLoadingView.assertDoesNotExist()
        listView.assertIsDisplayed()
        popularProjectsTitle.assertDoesNotExist()

        featuredProjectView.assertIsDisplayed()

        for (i in 1..19) {
            listView.performScrollToIndex(i)
            val projectView =
                composeTestRule.onNodeWithTag(SearchScreenTestTag.NORMAL_PROJECT_VIEW.name + i)
            projectView.assertIsDisplayed()
        }
    }

    @Test
    fun testLoadingComponentsEmptyListVisible() {
        composeTestRule.setContent {
            KSTheme {
                SearchScreen(
                    onBackClicked = { },
                    scaffoldState = rememberScaffoldState(),
                    isLoading = true,
                    lazyColumnListState = rememberLazyListState(),
                    showEmptyView = false,
                    onSearchTermChanged = {},
                    onItemClicked = {}
                )
            }
        }

        backButton.assertIsDisplayed()
        searchTextInput.assertIsDisplayed()
        emptyView.assertDoesNotExist()
        loadingView.assertIsDisplayed()
        inListLoadingView.assertDoesNotExist()
        listView.assertExists()
        popularProjectsTitle.assertDoesNotExist()
        featuredProjectView.assertDoesNotExist()
    }

    @Test
    fun testLoadingComponentsWithListVisible() {
        composeTestRule.setContent {
            KSTheme {
                SearchScreen(
                    onBackClicked = { },
                    scaffoldState = rememberScaffoldState(),
                    isLoading = true,
                    lazyColumnListState = rememberLazyListState(),
                    showEmptyView = false,
                    isPopularList = false,
                    itemsList = List(20) {
                        Project.builder()
                            .name("This is a test $it")
                            .pledged((it * 2).toDouble())
                            .goal(20.0)
                            .state(if (it in 10..20) Project.STATE_SUBMITTED else Project.STATE_LIVE)
                            .build()
                    },
                    onSearchTermChanged = {},
                    onItemClicked = {}
                )
            }
        }

        backButton.assertIsDisplayed()
        searchTextInput.assertIsDisplayed()
        emptyView.assertDoesNotExist()
        loadingView.assertDoesNotExist()
        listView.assertIsDisplayed()
        popularProjectsTitle.assertDoesNotExist()

        featuredProjectView.assertIsDisplayed()

        for (i in 1..19) {
            listView.performScrollToIndex(i)
            val projectView =
                composeTestRule.onNodeWithTag(SearchScreenTestTag.NORMAL_PROJECT_VIEW.name + i)
            projectView.assertIsDisplayed()
        }

        inListLoadingView.assertIsDisplayed()
    }

    @Test
    fun testClickActions() {
        var backClickedCount = 0
        var itemClickedCount = 0

        composeTestRule.setContent {
            KSTheme {
                SearchScreen(
                    onBackClicked = { backClickedCount++ },
                    scaffoldState = rememberScaffoldState(),
                    isLoading = false,
                    lazyColumnListState = rememberLazyListState(),
                    showEmptyView = false,
                    isPopularList = false,
                    itemsList = List(20) {
                        Project.builder()
                            .name("This is a test $it")
                            .pledged((it * 2).toDouble())
                            .goal(20.0)
                            .state(if (it in 10..20) Project.STATE_SUBMITTED else Project.STATE_LIVE)
                            .build()
                    },
                    onSearchTermChanged = {},
                    onItemClicked = { itemClickedCount++ }
                )
            }
        }

        backButton.performClick()
        assertEquals(backClickedCount, 1)

        featuredProjectView.performClick()
        assertEquals(itemClickedCount, 1)

        for (i in 1..19) {
            listView.performScrollToIndex(i)
            val projectView =
                composeTestRule.onNodeWithTag(SearchScreenTestTag.NORMAL_PROJECT_VIEW.name + i)
            projectView.performClick()
        }

        assertEquals(itemClickedCount, 20)
    }

    @Test
    fun testSearchTermUpdates() {
        var currentSearchTerm = ""

        composeTestRule.setContent {
            KSTheme {
                SearchScreen(
                    onBackClicked = { },
                    scaffoldState = rememberScaffoldState(),
                    isLoading = false,
                    lazyColumnListState = rememberLazyListState(),
                    showEmptyView = false,
                    isPopularList = false,
                    itemsList = List(20) {
                        Project.builder()
                            .name("This is a test $it")
                            .pledged((it * 2).toDouble())
                            .goal(20.0)
                            .state(if (it in 10..20) Project.STATE_SUBMITTED else Project.STATE_LIVE)
                            .build()
                    },
                    onSearchTermChanged = {
                        currentSearchTerm = it
                    },
                    onItemClicked = { }
                )
            }
        }

        searchTextInput.performTextInput("this is a test")
        assertEquals(currentSearchTerm, "this is a test")
    }
}
