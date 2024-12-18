package com.kickstarter.ui.activities.compose

import CollectionOptions
import CollectionPlan
import CollectionPlanTestTags
import android.content.Context
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Before
import org.junit.Test

class CollectionPlanTest : KSRobolectricTestCase() {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    private val pledgeInFullOption
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.OPTION_PLEDGE_IN_FULL.name)
    private val pledgeOverTimeOption
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.OPTION_PLEDGE_OVER_TIME.name)
    private val descriptionText
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.DESCRIPTION_TEXT.name)
    private val badgeText
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.BADGE_TEXT.name)
    private val expandedText
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.EXPANDED_DESCRIPTION_TEXT.name)
    private val termsText
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.TERMS_OF_USE_TEXT.name)

    @Test
    fun testPledgeInFullOptionSelected() {
        val pledgeInFullText = context.getString(R.string.Pledge_in_full)
        val pledgeOverTimeText = context.getString(R.string.Pledge_Over_Time)
        val descriptionTextValue =
            context.getString(R.string.You_will_be_charged_for_your_pledge_over_four_payments_collapsed_description)

        composeTestRule.setContent {
            KSTheme {
                CollectionPlan(isEligible = true, initialSelectedOption = CollectionOptions.PLEDGE_IN_FULL)
            }
        }

        composeTestRule.waitForIdle()

        // Assert "Pledge in Full" option is displayed with correct text and is selected
        pledgeInFullOption.assertIsDisplayed().assert(hasText(pledgeInFullText)).assertIsSelected()

        // Assert "Pledge Over Time" option is not displayed
        // Assert "Pledge Over Time" option is displayed with correct text and is not selected
        pledgeOverTimeOption.assertIsDisplayed().assert(hasText(pledgeOverTimeText))
            .assertIsNotSelected()

        composeTestRule.onNodeWithTag(
            CollectionPlanTestTags.DESCRIPTION_TEXT.name,
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assert(hasText(descriptionTextValue))

        // Assert that other elements are not displayed
        badgeText.assertIsNotDisplayed()
        expandedText.assertIsNotDisplayed()
        termsText.assertIsNotDisplayed()
    }

    @Test
    fun testPledgeOverTimeOptionSelected() {
        val pledgeInFullText = context.getString(R.string.Pledge_in_full)
        val pledgeOverTimeText = context.getString(R.string.Pledge_Over_Time)
        val descriptionTextValue =
            context.getString(R.string.You_will_be_charged_for_your_pledge_over_four_payments_collapsed_description)
        val extendedTextValue =
            context.getString(R.string.You_will_be_charged_for_your_pledge_over_four_payments_expanded_description)
        val termsOfUseTextValue = context.getString(R.string.See_our_terms_of_use)
        composeTestRule.setContent {
            KSTheme {
                CollectionPlan(isEligible = true, initialSelectedOption = CollectionOptions.PLEDGE_OVER_TIME)
            }
        }

        composeTestRule.waitForIdle()

        // Assert "Pledge in Full" option is displayed with correct text and is not selected
        pledgeInFullOption.assertIsDisplayed().assert(hasText(pledgeInFullText))
            .assertIsNotSelected()

        // Assert "Pledge Over Time" option is displayed with correct text and is selected
        pledgeOverTimeOption.assertIsDisplayed().assert(hasText(pledgeOverTimeText))
            .assertIsSelected()

        composeTestRule.onNodeWithTag(
            CollectionPlanTestTags.DESCRIPTION_TEXT.name,
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assert(hasText(descriptionTextValue))

        composeTestRule.onNodeWithTag(
            CollectionPlanTestTags.EXPANDED_DESCRIPTION_TEXT.name,
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assert(hasText(extendedTextValue))

        composeTestRule.onNodeWithTag(
            CollectionPlanTestTags.TERMS_OF_USE_TEXT.name,
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assert(hasText(termsOfUseTextValue))

        // Not eligible badge should not be displayed
        badgeText.assertIsNotDisplayed()
    }

    @Test
    fun testPledgeOverTimeOptionIneligible() {
        val pledgeInFullText = context.getString(R.string.Pledge_in_full)
        val pledgeOverTimeText = context.getString(R.string.Pledge_Over_Time)
        composeTestRule.setContent {
            KSTheme {
                CollectionPlan(isEligible = false, initialSelectedOption = CollectionOptions.PLEDGE_IN_FULL)
            }
        }

        composeTestRule.waitForIdle()

        // Assert "Pledge in Full" option is displayed with correct text and is selected
        pledgeInFullOption.assertIsDisplayed().assert(hasText(pledgeInFullText))
            .assertIsSelected()

        pledgeOverTimeOption.assertIsDisplayed().assert(hasText(pledgeOverTimeText))
            .assertIsNotSelected()

        composeTestRule.onNodeWithTag(
            CollectionPlanTestTags.DESCRIPTION_TEXT.name,
            useUnmergedTree = true
        )
            .assertIsNotDisplayed()

        composeTestRule.onNodeWithTag(
            CollectionPlanTestTags.EXPANDED_DESCRIPTION_TEXT.name,
            useUnmergedTree = true
        )
            .isNotDisplayed()

        composeTestRule.onNodeWithTag(
            CollectionPlanTestTags.TERMS_OF_USE_TEXT.name,
            useUnmergedTree = true
        )
            .isNotDisplayed()

        // Assert that other elements are not displayed
        composeTestRule.onNodeWithTag(
            CollectionPlanTestTags.BADGE_TEXT.name,
            useUnmergedTree = true
        )
            .isDisplayed()
    }
}
