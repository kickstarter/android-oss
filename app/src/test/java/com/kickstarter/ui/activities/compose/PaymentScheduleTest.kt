package com.kickstarter.ui.activities.compose

import PaymentSchedule
import PaymentScheduleTestTags
import android.content.Context
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Before
import org.junit.Test

class PaymentScheduleTest : KSRobolectricTestCase() {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    private val title
        get() = composeTestRule.onNodeWithTag(PaymentScheduleTestTags.PAYMENT_SCHEDULE_TITLE.name)
    private val expandIcon
        get() = composeTestRule.onNodeWithTag(
            PaymentScheduleTestTags.EXPAND_ICON.name,
        )
    private val dateText
        get() = composeTestRule.onAllNodesWithTag(PaymentScheduleTestTags.DATE_TEXT.name)
    private val amountText
        get() = composeTestRule.onAllNodesWithTag(PaymentScheduleTestTags.AMOUNT_TEXT.name)
    private val badgeText
        get() = composeTestRule.onAllNodesWithTag(PaymentScheduleTestTags.BADGE_TEXT.name)
    private val termsOfUseText
        get() = composeTestRule.onNodeWithTag(PaymentScheduleTestTags.TERMS_OF_USE_TEXT.name)

    @Test
    fun testCollapsedState() {
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(isExpanded = false, onExpandChange = {})
            }
        }

        composeTestRule.waitForIdle()

        // Assert title and expand icon are displayed
        title.assertIsDisplayed().assert(hasText(context.getString(R.string.fpo_payment_schedule)))
        expandIcon.assertIsDisplayed()

        // Assert that payment details and terms of use are not displayed
        dateText.assertCountEquals(0)
        amountText.assertCountEquals(0)
        termsOfUseText.assertIsNotDisplayed()
    }

    @Test
    fun testExpandedState() {
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(isExpanded = true, onExpandChange = {})
            }
        }

        composeTestRule.waitForIdle()

        // Assert title and expand icon are displayed
        title.assertIsDisplayed().assert(hasText(context.getString(R.string.fpo_payment_schedule)))
        expandIcon.assertIsDisplayed()

        // Assert that payment details are displayed
        dateText.assertCountEquals(4)
        amountText.assertCountEquals(4)
        badgeText.assertCountEquals(4)

        termsOfUseText
            .assertIsDisplayed().assert(hasText(context.getString(R.string.fpo_terms_of_use)))
    }
}