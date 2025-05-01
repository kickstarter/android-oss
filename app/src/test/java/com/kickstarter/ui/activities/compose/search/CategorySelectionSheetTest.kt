package com.kickstarter.ui.activities.compose.search

import android.content.Context
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.models.Category
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class CategorySelectionSheetTest : KSRobolectricTestCase() {

    private val dismissButton =
        composeTestRule.onNodeWithTag(CategorySelectionSheetTestTag.DISMISS_BUTTON.name)

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `test tapping dismiss button should register dismiss`() {
        var applyClickCount = 0
        var dismissClickCount = 0

        composeTestRule.setContent {
            KSTheme {
                CategorySelectionSheet(
                    categories = listOf(),
                    onDismiss = { dismissClickCount++ },
                    onApply = { selected, from ->
                        applyClickCount++
                    },
                    isLoading = false,
                    shouldShowPhase2 = false
                )
            }
        }

        dismissButton.performClick()
        assertEquals(0, applyClickCount)
        assertEquals(1, dismissClickCount)
    }

    @Test
    fun `root category expands when clicked, All pill is selected by default`() {
        var selectedCategory: Category? = null
        composeTestRule.setContent {
            KSTheme {
                CategoryItemRow(
                    category = CategoryFactory.artCategory(),
                    selectedCategory = CategoryFactory.artCategory(),
                    onSelectionChange = { newlySelectedCat ->
                        selectedCategory = newlySelectedCat
                    },
                    subcategories = listOf(CategoryFactory.textilesCategory(), CategoryFactory.digitalArtCategory(), CategoryFactory.ceramicsCategory())
                )
            }
        }

        composeTestRule
            .onNodeWithTag(CategoryItemRowTestTags.ROOTCATEGORY_TITLE, useUnmergedTree = true)
            .assertTextEquals(CategoryFactory.artCategory().name())

        composeTestRule
            .onNodeWithText(CategoryFactory.artCategory().name(), useUnmergedTree = true)
            .performClick() // Expand row, performs animation

        composeTestRule
            .onNodeWithTag(CategoryItemRowTestTags.pillTag(CategoryFactory.artCategory()))
            .assertTextEquals(context.resources.getString(R.string.Project_status_all))

        composeTestRule
            .onNodeWithTag(CategoryItemRowTestTags.pillTag(CategoryFactory.textilesCategory()))
            .assertTextEquals(CategoryFactory.textilesCategory().name())

        composeTestRule
            .onNodeWithTag(CategoryItemRowTestTags.pillTag(CategoryFactory.digitalArtCategory()))
            .assertTextEquals(CategoryFactory.digitalArtCategory().name())

        assertEquals(selectedCategory, CategoryFactory.artCategory())
    }

    @Test
    fun `root category expands when clicked, select subcategory `() {

        var selectedCategory: Category? = null
        composeTestRule.setContent {
            KSTheme {
                CategoryItemRow(
                    category = CategoryFactory.artCategory(),
                    selectedCategory = CategoryFactory.artCategory(),
                    onSelectionChange = { newlySelectedCat ->
                        selectedCategory = newlySelectedCat
                    },
                    subcategories = listOf(CategoryFactory.textilesCategory(), CategoryFactory.digitalArtCategory(), CategoryFactory.ceramicsCategory())
                )
            }
        }

        composeTestRule
            .onNodeWithTag(CategoryItemRowTestTags.pillTag(CategoryFactory.digitalArtCategory()), useUnmergedTree = true)
            .performClick()

        assertEquals(selectedCategory, CategoryFactory.digitalArtCategory())
    }

    @Test
    fun `reset button behaviour on CategoryScreen`() {
        var selectedCategory: Category? = null
        composeTestRule.setContent {
            KSTheme {
                CategorySelectionSheet(
                    currentCategory = CategoryFactory.artCategory(),
                    categories = CategoryFactory.rootCategories(),
                    onDismiss = {
                    },
                    onApply = { selected, from ->
                        selectedCategory = selected
                    },
                    isLoading = false,
                    shouldShowPhase2 = false
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Reset_all_filters)) // - This text applies only on SearchScreen
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Reset_filters))
            .assertExists()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.See_results))
            .performClick()

        assertEquals(selectedCategory, CategoryFactory.artCategory())

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Reset_filters))
            .performClick()

        assertNull(selectedCategory)
    }
}
