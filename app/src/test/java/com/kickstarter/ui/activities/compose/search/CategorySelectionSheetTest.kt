package com.kickstarter.ui.activities.compose.search

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.models.Category
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class CategorySelectionSheetTest : KSRobolectricTestCase() {

    private val dismissButton =
        composeTestRule.onNodeWithTag(CategorySelectionSheetTestTag.DISMISS_BUTTON.name)

    private val rootCategory = CategoryFactory.artCategory()
    private val sub1 = CategoryFactory.textilesCategory()
    private val sub2 = CategoryFactory.digitalArtCategory()
    private val sub3 = CategoryFactory.ceramicsCategory()

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
    fun rootCategoryRadio_isSelected_whenMatchingSelectedCategory() {
        composeTestRule.setContent {
            KSTheme {
                CategoryItemRow(
                    category = rootCategory,
                    selectedCategory = rootCategory,
                    onSelectionChange = {},
                    subcategories = listOf(sub1, sub2, sub3)
                )
            }
        }

        composeTestRule
            .onNodeWithTag(CategorySelectionTestTags.RADIO_BUTTON)
            .assertIsSelected()
    }

    @Test
    fun subcategoryPill_isSelected_whenMatchingSelectedCategory() {
        composeTestRule.setContent {
            KSTheme {
                CategoryItemRow(
                    category = rootCategory,
                    selectedCategory = sub3,
                    onSelectionChange = {},
                    subcategories = listOf(sub1, sub2, sub3)
                )
            }
        }

        composeTestRule
            .onNodeWithTag(CategorySelectionTestTags.pillTag(sub3))
            .assertIsSelected()
    }

    @Test
    fun clickingRootCategory_callsSelectionCallback() {
        var clickedCategory: Category? = null

        composeTestRule.setContent {
            KSTheme {
                CategoryItemRow(
                    category = rootCategory,
                    selectedCategory = null,
                    onSelectionChange = { clickedCategory = it },
                    subcategories = listOf(sub1, sub2, sub3)
                )
            }
        }

        composeTestRule
            .onNodeWithTag(CategorySelectionTestTags.ROOTCATEGORY_ROW)
            .performClick()

        assert(clickedCategory == rootCategory)
    }

    @Test
    fun clickingSubcategoryPill_callsSelectionCallback() {
        var clickedCategory: Category? = null

        composeTestRule.setContent {
            KSTheme {
                CategoryItemRow(
                    category = rootCategory,
                    selectedCategory = null,
                    onSelectionChange = { clickedCategory = it },
                    subcategories = listOf(sub1, sub2, sub3)
                )
            }
        }

        composeTestRule
            .onNodeWithTag(CategorySelectionTestTags.pillTag(sub2))
            .performClick()

        assert(clickedCategory == sub2)
    }
}

