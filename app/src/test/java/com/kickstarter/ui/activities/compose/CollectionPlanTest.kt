package com.kickstarter.ui.activities.compose

import CollectionOptions
import CollectionPlan
import CollectionPlanTestTags
import android.content.Context
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.utils.extensions.format
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.PaymentIncrementFactory
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.joda.time.DateTime
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
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.DESCRIPTION_TEXT.name, useUnmergedTree = true)
    private val badgeText
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.BADGE_TEXT.name, useUnmergedTree = true)
    private val expandedText
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.EXPANDED_DESCRIPTION_TEXT.name, useUnmergedTree = true)
    private val termsText
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.TERMS_OF_USE_TEXT.name, useUnmergedTree = true)
    private val chargeItemsList
        get() = composeTestRule.onAllNodesWithTag(CollectionPlanTestTags.CHARGE_ITEM.name, useUnmergedTree = true)
    private val chargeSchedule
        get() = composeTestRule.onNodeWithTag(CollectionPlanTestTags.CHARGE_SCHEDULE.name, useUnmergedTree = true)
    private val radioButtons
        get() = composeTestRule.onAllNodesWithTag(CollectionPlanTestTags.RADIO_BUTTON.name, useUnmergedTree = true)

    @Test
    fun `test isEligible true, pledge in full option selected`() {
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

        // Assert "Pledge Over Time" option is displayed with correct text and is not selected
        pledgeOverTimeOption.assertIsDisplayed().assert(hasText(pledgeOverTimeText))
            .assertIsNotSelected().assertIsSelectable()

        radioButtons.assertCountEquals(2)
        radioButtons[0].assertHasClickAction()
        radioButtons[1].assertHasClickAction()

        descriptionText
            .assertIsDisplayed()
            .assert(hasText(descriptionTextValue))

        // Assert that other elements are not displayed
        badgeText.assertIsNotDisplayed()
        expandedText.assertIsNotDisplayed()
        termsText.assertIsNotDisplayed()
    }

    @Test
    fun `test isEligible true, pledge over time option selected`() {
        val pledgeInFullText = context.getString(R.string.Pledge_in_full)
        val pledgeOverTimeText = context.getString(R.string.Pledge_Over_Time)
        val descriptionTextValue =
            context.getString(R.string.You_will_be_charged_for_your_pledge_over_four_payments_collapsed_description)
        val extendedTextValue =
            context.getString(R.string.The_first_charge_will_occur_when_the_project_ends_successfully)
        val termsOfUseTextValue = context.getString(R.string.See_our_terms_of_use)
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        composeTestRule.setContent {
            KSTheme {
                CollectionPlan(
                    isEligible = true,
                    initialSelectedOption = CollectionOptions.PLEDGE_OVER_TIME,
                    ksCurrency = KSCurrency(currentConfig),
                    paymentIncrements = listOf(
                        PaymentIncrementFactory.incrementUsdUncollected(dateTime = DateTime.now(), formattedAmount = "$50.00",),
                        PaymentIncrementFactory.incrementUsdUncollected(dateTime = DateTime.now(), formattedAmount = "$50.00",),
                        PaymentIncrementFactory.incrementUsdUncollected(dateTime = DateTime.now(), formattedAmount = "$50.00",),
                        PaymentIncrementFactory.incrementUsdUncollected(dateTime = DateTime.now(), formattedAmount = "$50.00",),
                    )
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert "Pledge in Full" option is displayed with correct text and is not selected
        pledgeInFullOption.assertIsDisplayed().assert(hasText(pledgeInFullText))
            .assertIsNotSelected()

        // Assert "Pledge Over Time" option is displayed with correct text and is selected
        pledgeOverTimeOption.assertIsDisplayed().assert(hasText(pledgeOverTimeText))
            .assertIsSelected()

        descriptionText
            .assertIsDisplayed()
            .assert(hasText(descriptionTextValue))

        expandedText
            .assertIsDisplayed()
            .assert(hasText(extendedTextValue))

        termsText
            .assertIsDisplayed()
            .assert(hasText(termsOfUseTextValue))

        chargeSchedule
            .assertIsDisplayed()

        radioButtons.assertCountEquals(2)
        radioButtons[0].assertHasClickAction()
        radioButtons[1].assertHasClickAction()

        chargeItemsList.assertCountEquals(4)
        chargeItemsList[0].assert(hasText(context.getString(R.string.Charge_number).format(key1 = "number", value1 = "1")))

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

        descriptionText
            .assertIsNotDisplayed()

        expandedText
            .isNotDisplayed()

        termsText
            .isNotDisplayed()

        chargeItemsList[0].isNull()

        radioButtons.assertCountEquals(2)
        radioButtons[0].assertHasClickAction()
        radioButtons[1].assertHasNoClickAction()

        // Assert that other elements are not displayed
        badgeText
            .isDisplayed()
    }
}
