package com.kickstarter.ui.activities.compose

import PaymentSchedule
import android.content.Context
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.PaymentIncrementFactory
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.PaymentIncrementAmount
import com.kickstarter.type.PaymentIncrementState
import com.kickstarter.type.PaymentIncrementStateReason
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
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.UNATTEMPTED,
            paymentIncrementableId = "1",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-14T18:12:00Z"), // Mon, 14 Oct 2024 18:12 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.COLLECTED,
            paymentIncrementableId = "2",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-15T14:00:00Z"), // Tue, 15 Oct 2024 14:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null

        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.UNATTEMPTED,
            paymentIncrementableId = "3",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-16T10:00:00Z"), // Wed, 16 Oct 2024 10:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.COLLECTED,
            paymentIncrementableId = "4",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-17T16:30:00Z"), // Thu, 17 Oct 2024 16:30 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        )
    )

    private val samplePaymentIncrementsWithCollectedState = listOf(
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.COLLECTED,
            paymentIncrementableId = "1",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-14T18:12:00Z"), // Mon, 14 Oct 2024 18:12 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.COLLECTED,
            paymentIncrementableId = "2",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-15T14:00:00Z"), // Tue, 15 Oct 2024 14:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.COLLECTED,
            paymentIncrementableId = "3",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-16T10:00:00Z"), // Wed, 16 Oct 2024 10:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.COLLECTED,
            paymentIncrementableId = "4",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-17T16:30:00Z"), // Thu, 17 Oct 2024 16:30 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        )
    )

    private val samplePaymentIncrementsWithUnattemptedState = listOf(
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.UNATTEMPTED,
            paymentIncrementableId = "1",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-14T18:12:00Z"), // Mon, 14 Oct 2024 18:12 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.UNATTEMPTED,
            paymentIncrementableId = "2",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-15T14:00:00Z"), // Tue, 15 Oct 2024 14:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.UNATTEMPTED,
            paymentIncrementableId = "3",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-16T10:00:00Z"), // Wed, 16 Oct 2024 10:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.UNATTEMPTED,
            paymentIncrementableId = "4",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-17T16:30:00Z"), // Thu, 17 Oct 2024 16:30 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        )
    )

    private val samplePaymentIncrementsWithCancelledState = listOf(
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.CANCELLED,
            paymentIncrementableId = "1",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-14T18:12:00Z"), // Mon, 14 Oct 2024 18:12 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.CANCELLED,
            paymentIncrementableId = "2",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-15T14:00:00Z"), // Tue, 15 Oct 2024 14:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.CANCELLED,
            paymentIncrementableId = "3",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-16T10:00:00Z"), // Wed, 16 Oct 2024 10:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.CANCELLED,
            paymentIncrementableId = "4",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-17T16:30:00Z"), // Thu, 17 Oct 2024 16:30 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        )
    )

    private val samplePaymentIncrementsWithRefundedState = listOf(
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.REFUNDED,
            paymentIncrementableId = "1",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-14T18:12:00Z"),
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.REFUNDED,
            paymentIncrementableId = "2",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-15T14:00:00Z"),
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.REFUNDED,
            paymentIncrementableId = "3",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-16T10:00:00Z"),
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.REFUNDED,
            paymentIncrementableId = "4",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-17T16:30:00Z"),
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        )
    )

    private val samplePaymentIncrementsWithErroredStateAndRequiresActionStateReason = listOf(
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.ERRORED,
            paymentIncrementableId = "1",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-14T18:12:00Z"), // Mon, 14 Oct 2024 18:12 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.ERRORED,
            paymentIncrementableId = "2",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-15T14:00:00Z"), // Tue, 15 Oct 2024 14:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.ERRORED,
            paymentIncrementableId = "3",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-16T10:00:00Z"), // Wed, 16 Oct 2024 10:00 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.ERRORED,
            paymentIncrementableId = "4",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-17T16:30:00Z"), // Thu, 17 Oct 2024 16:30 UTC
            stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
            refundedAmount = null
        )
    )

    private val samplePaymentIncrementsWithErroredStateAndUnkownStateReason = listOf(
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.ERRORED,
            paymentIncrementableId = "1",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-14T18:12:00Z"), // Mon, 14 Oct 2024 18:12 UTC
            stateReason = PaymentIncrementStateReason.UNKNOWN__,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.ERRORED,
            paymentIncrementableId = "2",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-15T14:00:00Z"), // Tue, 15 Oct 2024 14:00 UTC
            stateReason = PaymentIncrementStateReason.UNKNOWN__,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.ERRORED,
            paymentIncrementableId = "3",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-16T10:00:00Z"), // Wed, 16 Oct 2024 10:00 UTC
            stateReason = PaymentIncrementStateReason.UNKNOWN__,
            refundedAmount = null
        ),
        PaymentIncrement(
            paymentIncrementAmount = PaymentIncrementAmount.builder().formattedAmount("$25")
                .build(),
            state = PaymentIncrementState.ERRORED,
            paymentIncrementableId = "4",
            paymentIncrementableType = "pledge",
            scheduledCollection = DateTime.parse("2024-10-17T16:30:00Z"), // Thu, 17 Oct 2024 16:30 UTC
            stateReason = PaymentIncrementStateReason.UNKNOWN__,
            refundedAmount = null
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
        title.assertIsDisplayed().assert(hasText(context.getString(R.string.Payment_schedule)))
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
        title.assertIsDisplayed().assert(hasText(context.getString(R.string.Payment_schedule)))
        expandIcon.assertIsDisplayed()

        // Assert that payment details are displayed
        dateText.assertCountEquals(samplePaymentIncrements.size)
        amountText.assertCountEquals(samplePaymentIncrements.size)
        badgeText.assertCountEquals(samplePaymentIncrements.size)

        termsOfUseText
            .assertIsDisplayed().assert(hasText(context.getString(R.string.profile_settings_about_terms)))
    }

    @Test
    fun testCollectedBadge() {

        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(
                    isExpanded = true,
                    onExpandChange = {},
                    paymentIncrements = samplePaymentIncrementsWithCollectedState
                )
            }
        }

        composeTestRule.waitForIdle()
        badgeText.assertCountEquals(samplePaymentIncrementsWithCollectedState.size)
        badgeText.assertAll(hasText(context.getString(R.string.project_view_pledge_status_collected)))
    }

    @Test
    fun testScheduledBadge() {
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(
                    isExpanded = true,
                    onExpandChange = {},
                    paymentIncrements = samplePaymentIncrementsWithUnattemptedState
                )
            }
        }

        composeTestRule.waitForIdle()
        badgeText.assertCountEquals(samplePaymentIncrementsWithCollectedState.size)
        badgeText.assertAll(hasText(context.getString(R.string.Scheduled)))
    }

    @Test
    fun testAuthenticationRequiredBadge() {
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(
                    isExpanded = true,
                    onExpandChange = {},
                    paymentIncrements = samplePaymentIncrementsWithErroredStateAndRequiresActionStateReason
                )
            }
        }

        composeTestRule.waitForIdle()
        badgeText.assertCountEquals(samplePaymentIncrementsWithCollectedState.size)
        badgeText.assertAll(hasText(context.getString(R.string.Authentication_required)))
    }

    @Test
    fun testErroredBadge() {
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(
                    isExpanded = true,
                    onExpandChange = {},
                    paymentIncrements = samplePaymentIncrementsWithErroredStateAndUnkownStateReason
                )
            }
        }

        composeTestRule.waitForIdle()
        badgeText.assertCountEquals(samplePaymentIncrementsWithCollectedState.size)
        badgeText.assertAll(hasText(context.getString(R.string.Errored_payment)))
    }

    @Test
    fun testCancelledBadge() {
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(
                    isExpanded = true,
                    onExpandChange = {},
                    paymentIncrements = samplePaymentIncrementsWithCancelledState
                )
            }
        }

        composeTestRule.waitForIdle()
        badgeText.assertCountEquals(samplePaymentIncrementsWithCollectedState.size)
        badgeText.assertAll(hasText(context.getString(R.string.project_view_pledge_status_canceled)))
    }

    @Test
    fun testRefundedBadge() {
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(
                    isExpanded = true,
                    onExpandChange = {},
                    paymentIncrements = samplePaymentIncrementsWithRefundedState
                )
            }
        }

        composeTestRule.waitForIdle()
        badgeText.assertCountEquals(samplePaymentIncrementsWithRefundedState.size)
        badgeText.assertAll(hasText(context.getString(R.string.fpo_refunded)))
    }

    @Test
    fun testPaymentScheduleAmountsText() {
        composeTestRule.setContent {
            KSTheme {
                val config = ConfigFactory.configForUSUser()

                val currentConfig = MockCurrentConfigV2()
                currentConfig.config(config)
                val mockCurrency = KSCurrency(currentConfig)
                PaymentSchedule(
                    isExpanded = true,
                    onExpandChange = {},
                    paymentIncrements = PaymentIncrementFactory.samplePaymentIncrements(),
                    ksCurrency = mockCurrency
                )
            }
        }

        composeTestRule.waitForIdle()
        amountText.assertCountEquals(6)

        // Assert Currency text
        amountText.assertAll(hasText("99.75$", ignoreCase = true))
    }
}
