package com.kickstarter.ui.activities.compose

import CollectionPlan
import CollectionPlanTestTags
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class CollectionPlanTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val pledgeInFullOption =
        composeTestRule.onNodeWithTag(CollectionPlanTestTags.OPTION_PLEDGE_IN_FULL.name)
    private val pledgeOverTimeOption =
        composeTestRule.onNodeWithTag(CollectionPlanTestTags.OPTION_PLEDGE_OVER_TIME.name)
    private val descriptionText =
        composeTestRule.onNodeWithTag(CollectionPlanTestTags.DESCRIPTION_TEXT.name)
    private val badgeText = composeTestRule.onNodeWithTag(CollectionPlanTestTags.BADGE_TEXT.name)
    private val expandedText =
        composeTestRule.onNodeWithTag(CollectionPlanTestTags.EXPANDED_TEXT.name)
    private val termsText = composeTestRule.onNodeWithTag(CollectionPlanTestTags.TERMS_TEXT.name)

    @Test
    fun `test screen init with eligible pledge in full`() {
        val pledgeInFullText = context.getString(R.string.fpo_pledge_in_full)
        val pledgeOverTimeText = context.getString(R.string.fpo_pledge_over_time)
        val description =
            context.getString(R.string.fpo_you_will_be_charged_for_your_pledge_over_four_payments_at_no_extra_cost)

        composeTestRule.setContent {
            KSTheme {
                CollectionPlan(isEligible = true, initialSelectedOption = "Pledge in full")
            }
        }

        // Assertions for "Pledge in full" option
        pledgeInFullOption.assertIsDisplayed()
        pledgeInFullOption.assertIsSelected()
        pledgeInFullOption.assertTextEquals(pledgeInFullText)

        // Assertions for "Pledge Over Time" option
        pledgeOverTimeOption.assertIsDisplayed()
        pledgeOverTimeOption.assertIsNotSelected()
        pledgeOverTimeOption.assertTextEquals(pledgeOverTimeText)

        // Description and expanded text should be displayed if eligible
        descriptionText.assertIsDisplayed()
        descriptionText.assertTextEquals(description)
        expandedText.assertIsDisplayed()
    }

    @Test
    fun `test selecting pledge over time when eligible`() {
        var selectedOption = "Pledge in full"
        val pledgeOverTimeText = context.getString(R.string.fpo_pledge_over_time)

        composeTestRule.setContent {
            KSTheme {
                CollectionPlan(
                    isEligible = true, initialSelectedOption = selectedOption
                )
            }
        }

        // Perform click to select "Pledge Over Time"
        pledgeOverTimeOption.assertIsDisplayed()
        pledgeOverTimeOption.performClick()

        // Verify the new selection state
        pledgeOverTimeOption.assertIsSelected()
        pledgeOverTimeOption.assertTextEquals(pledgeOverTimeText)
        pledgeInFullOption.assertIsNotSelected()
    }

    @Test
    fun `test badge displayed when not eligible`() {
        val badgeTextValue = context.getString(R.string.fpo_available_for_pledges_over_150)

        composeTestRule.setContent {
            KSTheme {
                CollectionPlan(isEligible = false, initialSelectedOption = "Pledge Over Time")
            }
        }

        // Badge should be displayed when not eligible
        badgeText.assertIsDisplayed()
        badgeText.assertTextEquals(badgeTextValue)

        // Description should not be displayed if not eligible
        descriptionText.assertDoesNotExist()
    }

    @Test
    fun `test terms and expanded text when pledge over time is selected`() {
        val expandedTextValue =
            context.getString(R.string.fpo_the_first_charge_will_be_24_hours_after_the_project_ends_successfully)
        val termsTextValue = context.getString(R.string.fpo_see_our_terms_of_use)

        composeTestRule.setContent {
            KSTheme {
                CollectionPlan(isEligible = true, initialSelectedOption = "Pledge Over Time")
            }
        }

        // Verify expanded text and terms text are displayed
        expandedText.assertIsDisplayed()
        expandedText.assertTextEquals(expandedTextValue)

        termsText.assertIsDisplayed()
        termsText.assertTextEquals(termsTextValue)
    }
}
