package com.kickstarter.ui.activities.compose.search

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class SearchEmptyViewTest : KSRobolectricTestCase() {

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `Empty search result state, no filters active, empty query`() {
        val ksString = KSString(application().packageName, application().resources)
        val env = environment()
            .toBuilder()
            .ksString(ksString)
            .build()

        composeTestRule.setContent {
            KSTheme {
                SearchEmptyView(
                    environment = env,
                    currentSearchTerm = "",
                    activeFilters = false
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.No_Results))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Try_rephrasing_your_search))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Try_rephrasing_your_search_or_adjusting_the_filters))
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Remove_all_filters))
            .assertDoesNotExist()
    }

    @Test
    fun `Empty search result state, no filters active, query search "cat"`() {
        val ksString = KSString(application().packageName, application().resources)
        val env = environment()
            .toBuilder()
            .ksString(ksString)
            .build()

        composeTestRule.setContent {
            KSTheme {
                SearchEmptyView(
                    environment = env,
                    currentSearchTerm = "cat",
                    activeFilters = false
                )
            }
        }

        val text = env.ksString()?.format(
            context.resources.getString(R.string.No_results_for),
            "search_term",
            "cat"
        ) ?: ""

        composeTestRule
            .onNodeWithText(text)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Try_rephrasing_your_search))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Try_rephrasing_your_search_or_adjusting_the_filters))
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Remove_all_filters))
            .assertDoesNotExist()
    }

    @Test
    fun `Empty search result state, filters active, query search "cat"`() {
        val ksString = KSString(application().packageName, application().resources)
        val env = environment()
            .toBuilder()
            .ksString(ksString)
            .build()

        composeTestRule.setContent {
            KSTheme {
                SearchEmptyView(
                    environment = env,
                    currentSearchTerm = "cat",
                    activeFilters = true
                )
            }
        }

        val text = env.ksString()?.format(
            context.resources.getString(R.string.No_results_for),
            "search_term",
            "cat"
        ) ?: ""

        composeTestRule
            .onNodeWithText(text)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Try_rephrasing_your_search))
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Try_rephrasing_your_search_or_adjusting_the_filters))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Remove_all_filters))
            .assertIsDisplayed()
    }
}
