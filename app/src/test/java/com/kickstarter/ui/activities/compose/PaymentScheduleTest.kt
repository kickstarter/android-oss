package com.kickstarter.ui.activities.compose

import PaymentRow
import PaymentSchedule
import StatusBadge
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import org.junit.Before
import org.junit.Test

class PaymentScheduleTest : KSRobolectricTestCase() {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    private val paymentScheduleTitle
        get() = composeTestRule.onNodeWithTag("payment_schedule_title")
    private val expandedPaymentRow
        get() = composeTestRule.onNodeWithTag("expanded_payment_row")
    private val collapsedPaymentRow
        get() = composeTestRule.onNodeWithTag("collapsed_payment_row")
    private val badgeText
        get() = composeTestRule.onNodeWithTag("badge_text")

    @Test
    fun testPaymentScheduleCollapsedState() {
        // Arrange
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(isExpanded = false, onExpandChange = {})
            }
        }

        composeTestRule.waitForIdle()

        // Assert collapsed state
        paymentScheduleTitle.assertIsDisplayed()
        collapsedPaymentRow.assertIsDisplayed()
        expandedPaymentRow.assertIsNotDisplayed()

        // Check that "Terms of Use" is not displayed in collapsed state
        composeTestRule.onNodeWithTag("terms_of_use_text").assertIsNotDisplayed()
    }

    @Test
    fun testPaymentScheduleExpandedState() {
        // Arrange
        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(isExpanded = true, onExpandChange = {})
            }
        }

        composeTestRule.waitForIdle()

        // Assert expanded state
        paymentScheduleTitle.assertIsDisplayed()
        expandedPaymentRow.assertIsDisplayed()
        collapsedPaymentRow.assertIsNotDisplayed()

        // Check that "Terms of Use" is displayed in expanded state
        composeTestRule.onNodeWithTag("terms_of_use_text").assertIsDisplayed()
    }

    @Composable
    @Test
    fun testToggleExpandCollapse() {
        // Arrange
        var isExpanded by remember { mutableStateOf(false) }

        composeTestRule.setContent {
            KSTheme {
                PaymentSchedule(isExpanded = isExpanded, onExpandChange = { isExpanded = it })
            }
        }

        composeTestRule.waitForIdle()

        // Assert initially collapsed
        paymentScheduleTitle.assertIsDisplayed()
        collapsedPaymentRow.assertIsDisplayed()
        expandedPaymentRow.assertIsNotDisplayed()

        // Click to expand
        paymentScheduleTitle.performClick()

        // Assert expanded after click
        paymentScheduleTitle.assertIsDisplayed()
        expandedPaymentRow.assertIsDisplayed()
        collapsedPaymentRow.assertIsNotDisplayed()
    }

    @Test
    fun testPaymentRowWithStatusBadge() {
        // Arrange
        composeTestRule.setContent {
            KSTheme {
                PaymentRow(
                    date = "Mar 15, 2024",
                    amount = "$20.00",
                    status = PaymentStatuses.SCHEDULED,
                    statusColor = colors.textSecondary
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert the status badge text
        badgeText.assertIsDisplayed()
        badgeText.assert(hasText(context.getString(R.string.fpo_scheduled)))
    }

    @Test
    fun testStatusBadgeForCollectedStatus() {
        // Arrange
        composeTestRule.setContent {
            KSTheme {
                StatusBadge(PaymentStatuses.COLLECTED)
            }
        }

        composeTestRule.waitForIdle()

        // Assert badge for COLLECTED status
        badgeText.assertIsDisplayed()
        badgeText.assert(hasText(context.getString(R.string.fpo_collected)))
    }

    @Test
    fun testStatusBadgeForAuthenticationRequiredStatus() {
        // Arrange
        composeTestRule.setContent {
            KSTheme {
                StatusBadge(PaymentStatuses.AUTHENTICATION_REQUIRED)
            }
        }

        composeTestRule.waitForIdle()

        // Assert badge for AUTHENTICATION_REQUIRED status
        badgeText.assertIsDisplayed()
        badgeText.assert(hasText(context.getString(R.string.fpo_authentication_required)))
    }

    @Test
    fun testStatusBadgeForScheduledStatus() {
        // Arrange
        composeTestRule.setContent {
            KSTheme {
                StatusBadge(PaymentStatuses.SCHEDULED)
            }
        }

        composeTestRule.waitForIdle()

        // Assert badge for SCHEDULED status
        badgeText.assertIsDisplayed()
        badgeText.assert(hasText(context.getString(R.string.fpo_scheduled)))
    }
}
