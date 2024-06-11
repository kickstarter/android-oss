package com.kickstarter.ui.activities.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardView
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewTestTag
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class PPOCardViewKtTest : KSRobolectricTestCase() {
    private val confirmAddressButtonsView =
        composeTestRule.onNodeWithTag(PPOCardViewTestTag.CONFIRM_ADDRESS_BUTTONS_VIEW.name, true)
    private val shippingAddressView =
        composeTestRule.onNodeWithTag(PPOCardViewTestTag.SHIPPING_ADDRESS_VIEW.name, true)
    @Test
    fun testConfirmAddressView() {
        val timeNumberForAction = 5
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.CONFIRM_ADDRESS,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    shippingAddress = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",
                    showBadge = true,
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    timeNumberForAction = timeNumberForAction
                )
            }
        }
        // Alerts
        // TODO: Replace with translated string
        composeTestRule.onAllNodesWithText("Address locks in $timeNumberForAction hours")[0].assertIsDisplayed()
        // Shipping address displayed
        shippingAddressView.assertIsDisplayed()
        // CTA
        confirmAddressButtonsView.assertIsDisplayed()
    }

    @Test
    fun testAddressConfirmedView() {
        val timeNumberForAction = 5
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.ADDRESS_CONFIRMED,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    shippingAddress = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",
                    showBadge = true,
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    timeNumberForAction = timeNumberForAction
                )
            }
        }

        // Alerts
        // TODO: Replace with translated string
        composeTestRule.onAllNodesWithText("Address locks in $timeNumberForAction hours")[0].assertIsDisplayed()
        // Shipping address displayed
        shippingAddressView.assertIsDisplayed()
        // CTA
        composeTestRule.onAllNodesWithText("Address Confirmed")[0].assertIsDisplayed()
    }

    @Test
    fun testFixPaymentView() {
        val timeNumberForAction = 6
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.FIX_PAYMENT,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = true,
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    timeNumberForAction = timeNumberForAction
                )
            }
        }

        // Alerts
        // TODO: Replace with translated strings
        composeTestRule.onAllNodesWithText("Payment failed")[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Pledge will be dropped in $timeNumberForAction days")[0].assertIsDisplayed()
        // Shipping address hidden
        shippingAddressView.assertIsNotDisplayed()
        // CTA
        composeTestRule.onAllNodesWithText("Fix Payment")[0].assertIsDisplayed()
    }

    @Test
    fun testPaymentFixedView() {
        val timeNumberForAction = 6
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.PAYMENT_FIXED,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = false,
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    timeNumberForAction = timeNumberForAction
                )
            }
        }

        // No alerts
        // Shipping address hidden
        shippingAddressView.assertIsNotDisplayed()
        // CTA
        // TODO: Replace with translated strings
        composeTestRule.onAllNodesWithText("Payment Fixed")[0].assertIsDisplayed()
    }

    @Test
    fun testAuthenticateCardView() {
        val timeNumberForAction = 7
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.AUTHENTICATE_CARD,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$60.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = true,
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    timeNumberForAction = timeNumberForAction
                )
            }
        }

        // Alerts
        // TODO: Replace with translated strings
        composeTestRule.onAllNodesWithText("Card needs authentication")[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Pledge will be dropped in $timeNumberForAction days")[0].assertIsDisplayed()
        // Shipping address hidden
        shippingAddressView.assertIsNotDisplayed()
        // CTA
        composeTestRule.onAllNodesWithText("Authenticate Card")[0].assertIsDisplayed()
    }

    @Test
    fun testCardAuthenticatedView() {
        val timeNumberForAction = 7
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.CARD_AUTHENTICATED,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$60.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = false,
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    timeNumberForAction = timeNumberForAction
                )
            }
        }

        // No alerts
        // Shipping address hidden
        shippingAddressView.assertIsNotDisplayed()
        // CTA
        // TODO: Replace with translated strings
        composeTestRule.onAllNodesWithText("Card Authenticated")[0].assertIsDisplayed()
    }

    @Test
    fun testTakeSurveyView() {
        val timeNumberForAction = 8
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.TAKE_SURVEY,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = true,
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    timeNumberForAction = timeNumberForAction
                )
            }
        }

        // Alerts
        // TODO: Replace with translated strings
        composeTestRule.onAllNodesWithText("Address locks in $timeNumberForAction hours")[0].assertIsDisplayed()
        // Shipping address hidden
        shippingAddressView.assertIsNotDisplayed()
        // CTA
        composeTestRule.onAllNodesWithText("Take Survey")[0].assertIsDisplayed()
    }

    @Test
    fun testSurveySubmittedView() {
        val timeNumberForAction = 8
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.SURVEY_SUBMITTED,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = true,
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    timeNumberForAction = timeNumberForAction
                )
            }
        }

        // Alerts
        // TODO: Replace with translated strings
        composeTestRule.onAllNodesWithText("Address locks in $timeNumberForAction hours")[0].assertIsDisplayed()
        // Shipping address displayed
        shippingAddressView.assertIsDisplayed()
        // CTA
        composeTestRule.onAllNodesWithText("Survey Submitted")[0].assertIsDisplayed()
    }
}
