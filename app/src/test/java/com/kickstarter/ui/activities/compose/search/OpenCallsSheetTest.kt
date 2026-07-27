package com.kickstarter.ui.activities.compose.search

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.TagFactory
import com.kickstarter.models.Tag
import com.kickstarter.ui.compose.designsystem.BottomSheetFooterTestTags
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class OpenCallsSheetTest : KSRobolectricTestCase() {

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `title, All chip, and one chip per tag are displayed`() {
        val tags = TagFactory.tags()
        composeTestRule.setContent {
            KSTheme {
                OpenCallsSheet(tags = tags)
            }
        }

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.fpo_Open_calls))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(OpenCallsTestTags.ALL_CHIP)
            .assertIsDisplayed()

        tags.forEach { tag ->
            composeTestRule
                .onNodeWithTag(OpenCallsTestTags.tagChip(tag))
                .assertIsDisplayed()
        }
    }

    @Test
    fun `default selection is All so Reset is disabled and See results enabled`() {
        composeTestRule.setContent {
            KSTheme {
                OpenCallsSheet(tags = TagFactory.tags())
            }
        }

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .assertIsEnabled()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .assertIsNotEnabled()
    }

    @Test
    fun `selecting a tag chip enables Reset`() {
        composeTestRule.setContent {
            KSTheme {
                OpenCallsSheet(tags = TagFactory.tags())
            }
        }

        composeTestRule
            .onNodeWithTag(OpenCallsTestTags.tagChip(TagFactory.zineQuest()))
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .assertIsEnabled()
    }

    @Test
    fun `Reset clears the selection`() {
        var selectedTag: Tag? = TagFactory.make100()

        composeTestRule.setContent {
            KSTheme {
                OpenCallsSheet(
                    tags = TagFactory.tags(),
                    currentTag = TagFactory.make100(),
                    onApply = { tag, _ -> selectedTag = tag }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .performClick()

        assertEquals(null, selectedTag)
    }

    @Test
    fun `See results applies the selected tag and dismisses`() {
        var selectedTag: Tag? = null
        var shouldApply: Boolean? = null

        composeTestRule.setContent {
            KSTheme {
                OpenCallsSheet(
                    tags = TagFactory.tags(),
                    onApply = { tag, apply ->
                        selectedTag = tag
                        shouldApply = apply
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(OpenCallsTestTags.tagChip(TagFactory.witchstarter()))
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .performClick()

        assertEquals(TagFactory.witchstarter(), selectedTag)
        assertEquals(true, shouldApply)
    }
}
