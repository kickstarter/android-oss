package com.kickstarter.ui.activities.compose

import PaymentSchedule
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
import com.kickstarter.models.Amount
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.PaymentIncrement.State
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.joda.time.DateTime
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

    private val samplePaymentIncrements = listOf(
        PaymentIncrement(
            amount = Amount.builder().amount("3400").build(),
            state = State.UNATTEMPTED,
            paymentIncrementableId = "1",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-14T18:12:00Z"), // Mon, 14 Oct 2024 18:12 UTC
            stateReason = ""
        ),
        PaymentIncrement(
            amount = Amount.builder().amount("2500").build(),
            state = State.COLLECTED,
            paymentIncrementableId = "2",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-15T14:00:00Z"), // Tue, 15 Oct 2024 14:00 UTC
            stateReason = ""
        ),
        PaymentIncrement(
            amount = Amount.builder().amount("4500").build(),
            state = State.UNATTEMPTED,
            paymentIncrementableId = "3",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-16T10:00:00Z"), // Wed, 16 Oct 2024 10:00 UTC
            stateReason = ""
        ),
        PaymentIncrement(
            amount = Amount.builder().amount("5200").build(),
            state = State.COLLECTED,
            paymentIncrementableId = "4",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-17T16:30:00Z"), // Thu, 17 Oct 2024 16:30 UTC
            stateReason = ""
        )
    )

    @Test
    fun testCollapsedState() {
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(
                    isExpanded = false,
                    onExpandChange = {},
                    paymentIncrements = samplePaymentIncrements
                )
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
                PaymentSchedule(
                    isExpanded = true,
                    onExpandChange = {},
                    paymentIncrements = samplePaymentIncrements
                )
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
