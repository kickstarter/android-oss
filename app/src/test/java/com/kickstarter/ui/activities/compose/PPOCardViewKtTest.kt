package com.kickstarter.ui.activities.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.pledgedprojectsoverview.data.Flag
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
    private val flagsListView =
        composeTestRule.onNodeWithTag(PPOCardViewTestTag.FlAG_LIST_VIEW.name, true)

    @Test
    fun testConfirmAddressView() {
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.CONFIRM_ADDRESS,
                    onCardClick = {},
                    onProjectPledgeSummaryClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    shippingAddress = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                )
            }
        }
        // Shipping address displayed
        shippingAddressView.assertIsDisplayed()
        // CTA
        confirmAddressButtonsView.assertIsDisplayed()
    }

    @Test
    fun testFixPaymentView() {
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.FIX_PAYMENT,
                    onCardClick = {},
                    onProjectPledgeSummaryClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                )
            }
        }

        // Shipping address hidden
        shippingAddressView.assertIsNotDisplayed()
        // CTA
        composeTestRule.onAllNodesWithText("Fix Payment")[0].assertIsDisplayed()
    }

    @Test
    fun testAuthenticateCardView() {
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.AUTHENTICATE_CARD,
                    onCardClick = {},
                    onProjectPledgeSummaryClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$60.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                )
            }
        }

        // Shipping address hidden
        shippingAddressView.assertIsNotDisplayed()
        // CTA
        composeTestRule.onAllNodesWithText("Authenticate Card")[0].assertIsDisplayed()
    }

    @Test
    fun testTakeSurveyView() {
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.OPEN_SURVEY,
                    onCardClick = {},
                    onProjectPledgeSummaryClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                )
            }
        }

        // Shipping address hidden
        shippingAddressView.assertIsNotDisplayed()
        // CTA
        composeTestRule.onAllNodesWithText("Take Survey")[0].assertIsDisplayed()
    }

    @Test
    fun testVisibleFlags() {
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.OPEN_SURVEY,
                    onCardClick = {},
                    onProjectPledgeSummaryClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    flags = listOf(
                        Flag.builder().message("Address locks in 7 days").type("alert").icon("time")
                            .build(),
                        Flag.builder().message("Open Survey").type("warning").icon("time").build()
                    ),
                )
            }
        }

        flagsListView.assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Address locks in 7 days")[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Open Survey")[0].assertIsDisplayed()

    }

    @Test
    fun testInvisibleFlags() {
        composeTestRule.setContent {
            KSTheme {
                PPOCardView(
                    viewType = PPOCardViewType.OPEN_SURVEY,
                    onCardClick = {},
                    onProjectPledgeSummaryClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    flags = listOf(Flag.builder().message("Address locks in 7 days").type(null).icon("time").build(), Flag.builder().message("Open Survey").type("warning").icon("time").build()),
                )
            }
        }

        flagsListView.assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Address locks in 7 days")[0].assertIsNotDisplayed()
        composeTestRule.onAllNodesWithText("Open Survey")[0].assertIsDisplayed()

    }
}
