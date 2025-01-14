package com.kickstarter.ui.activities.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.views.compose.checkout.ItemizedRewardListContainer
import com.kickstarter.ui.views.compose.checkout.PledgeItemizedDetailsTestTag
import org.junit.Test

class PledgeItemizedDetailsTest : KSRobolectricTestCase() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val pageTitle =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.PAGE_TITLE.name)

    private val deliveryDate =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.DELIVERY_DATE.name)

    private val pledgeAmountTitle =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.PLEDGE_AMOUNT_TITLE.name)

    private val totalAmount =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.TOTAL_AMOUNT.name)

    private val bonusSupportTitle =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.BONUS_SUPPORT_TITLE.name)

    private val bonusSupport =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.BONUS_SUPPORT.name)

    private val disclaimerText =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.DISCLAIMER_TEXT.name)

    private val shippingTitle =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.SHIPPING_TITLE.name)

    private val shippingAmount =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.SHIPPING_AMOUNT.name)

    private val currencyConversion =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.CURRENCY_CONVERSION.name)

    private val plotSelectedBadge =
        composeTestRule.onNodeWithTag(PledgeItemizedDetailsTestTag.PLOT_SELECTED_BADGE.name)

    @Test
    fun `test view init`() {
        val rewardsList = listOf(Pair("T-shirt", "$22"), Pair("Pin", "$10"))

        composeTestRule.setContent {
            KSTheme {
                ItemizedRewardListContainer(
                    ksString = null,
                    rewardsList = rewardsList,
                    shippingAmount = 20.0,
                    shippingAmountString = "",
                    initialShippingLocation = "",
                    totalAmount = "50$",
                    totalAmountCurrencyConverted = "",
                    initialBonusSupport = "0",
                    totalBonusSupport = "0",
                    deliveryDateString = "",
                    rewardsHaveShippables = false,
                    disclaimerText = "",
                    plotSelected = false
                )
            }
        }
        val pageTitleText = context.getString(R.string.Your_pledge)
        val pledgeAmountTitleText = context.getString(R.string.Pledge_amount)

        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(pageTitleText)
        deliveryDate.assertDoesNotExist()

        composeTestRule.onAllNodesWithTag("ITEM_NAME")[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("ITEM_NAME")[1].assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("ITEM_NAME")[0].assertTextEquals("T-shirt")
        composeTestRule.onAllNodesWithTag("ITEM_NAME")[1].assertTextEquals("Pin")

        composeTestRule.onAllNodesWithTag("ITEM_COST")[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("ITEM_COST")[1].assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("ITEM_COST")[0].assertTextEquals("$22")
        composeTestRule.onAllNodesWithTag("ITEM_COST")[1].assertTextEquals("$10")

        pledgeAmountTitle.assertIsDisplayed()
        pledgeAmountTitle.assertTextEquals(pledgeAmountTitleText)

        totalAmount.assertIsDisplayed()
        totalAmount.assertTextEquals("50$")

        bonusSupportTitle.assertDoesNotExist()
        bonusSupport.assertDoesNotExist()
        disclaimerText.assertDoesNotExist()
        shippingAmount.assertDoesNotExist()
        shippingTitle.assertDoesNotExist()
        currencyConversion.assertDoesNotExist()
    }

    @Test
    fun `test bonus support, when no rewards and initial is not the same as total bonus, displays pledge without reward copy`() {
        composeTestRule.setContent {
            KSTheme {
                ItemizedRewardListContainer(
                    ksString = null,
                    rewardsList = emptyList<Pair<String, String>>(),
                    shippingAmount = 20.0,
                    shippingAmountString = "",
                    initialShippingLocation = "",
                    totalAmount = "50$",
                    totalAmountCurrencyConverted = "About CA\$ 1.38",
                    initialBonusSupport = "1",
                    totalBonusSupport = "10",
                    deliveryDateString = "",
                    rewardsHaveShippables = false,
                    disclaimerText = "",
                    plotSelected = false
                )
            }
        }
        val bonusSupportTitleText = context.getString(R.string.Pledge_without_a_reward)

        bonusSupportTitle.assertIsDisplayed()
        bonusSupportTitle.assertTextEquals(bonusSupportTitleText)
        bonusSupport.assertIsDisplayed()
        bonusSupport.assertTextEquals("10")
    }

    @Test
    fun `test bonus support, when rewards exist and initial is not the same as total bonus, displays bonus support copy`() {
        val rewardsList = listOf(Pair("T-shirt", "$22"), Pair("Pin", "$10"))

        composeTestRule.setContent {
            KSTheme {
                ItemizedRewardListContainer(
                    ksString = null,
                    rewardsList = rewardsList,
                    shippingAmount = 20.0,
                    shippingAmountString = "",
                    initialShippingLocation = "",
                    totalAmount = "50$",
                    totalAmountCurrencyConverted = "About CA\$ 1.38",
                    initialBonusSupport = "1",
                    totalBonusSupport = "10",
                    deliveryDateString = "",
                    rewardsHaveShippables = false,
                    disclaimerText = "",
                    plotSelected = false
                )
            }
        }
        val bonusSupportTitleText = context.getString(R.string.Bonus_support)

        bonusSupportTitle.assertIsDisplayed()
        bonusSupportTitle.assertTextEquals(bonusSupportTitleText)
        bonusSupport.assertIsDisplayed()
        bonusSupport.assertTextEquals("10")
    }

    @Test
    fun `test shipping, when rewards are shippable and backer has shipping location, should show shipping location ui`() {
        val rewardsList = listOf(Pair("T-shirt", "$22"), Pair("Pin", "$10"))

        composeTestRule.setContent {
            KSTheme {
                ItemizedRewardListContainer(
                    ksString = null,
                    rewardsList = rewardsList,
                    shippingAmount = 20.0,
                    shippingAmountString = "$20.0",
                    initialShippingLocation = "USA",
                    totalAmount = "50$",
                    totalAmountCurrencyConverted = "About CA\$ 1.38",
                    initialBonusSupport = "1",
                    totalBonusSupport = "10",
                    deliveryDateString = "",
                    rewardsHaveShippables = true,
                    disclaimerText = "",
                    plotSelected = false
                )
            }
        }

        shippingTitle.assertIsDisplayed()
        shippingAmount.assertIsDisplayed()
        shippingAmount.assertTextEquals("$20.0")
    }

    @Test
    fun `test currency conversion, when currency conversion not null, should show currency conversion text`() {
        val rewardsList = listOf(Pair("T-shirt", "$22"), Pair("Pin", "$10"))

        composeTestRule.setContent {
            KSTheme {
                ItemizedRewardListContainer(
                    ksString = null,
                    rewardsList = rewardsList,
                    shippingAmount = 20.0,
                    shippingAmountString = "$20.0",
                    initialShippingLocation = "USA",
                    totalAmount = "50$",
                    totalAmountCurrencyConverted = "About CA\$ 1.38",
                    initialBonusSupport = "1",
                    totalBonusSupport = "10",
                    deliveryDateString = "",
                    rewardsHaveShippables = true,
                    disclaimerText = "",
                    plotSelected = false
                )
            }
        }

        currencyConversion.assertIsDisplayed()
        currencyConversion.assertTextEquals("About CA\$ 1.38")
    }

    @Test
    fun `test disclaimer, when disclaimer not null, should show disclaimer text`() {
        val rewardsList = listOf(Pair("T-shirt", "$22"), Pair("Pin", "$10"))
        val disclaimer =
            context.getString(R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline_and_receive_proof_of_pledge)

        composeTestRule.setContent {
            KSTheme {
                ItemizedRewardListContainer(
                    ksString = null,
                    rewardsList = rewardsList,
                    shippingAmount = 20.0,
                    shippingAmountString = "$20.0",
                    initialShippingLocation = "USA",
                    totalAmount = "50$",
                    totalAmountCurrencyConverted = "About CA\$ 1.38",
                    initialBonusSupport = "1",
                    totalBonusSupport = "10",
                    deliveryDateString = "",
                    rewardsHaveShippables = true,
                    disclaimerText = disclaimer,
                    plotSelected = false
                )
            }
        }
        disclaimerText.assertIsDisplayed()
        disclaimerText.assertTextEquals(disclaimer)
    }

    @Test
    fun `test delivery date, when delivery date not null, should show delivery date`() {
        val rewardsList = listOf(Pair("T-shirt", "$22"), Pair("Pin", "$10"))

        composeTestRule.setContent {
            KSTheme {
                ItemizedRewardListContainer(
                    ksString = null,
                    rewardsList = rewardsList,
                    shippingAmount = 20.0,
                    shippingAmountString = "$20.0",
                    initialShippingLocation = "USA",
                    totalAmount = "50$",
                    totalAmountCurrencyConverted = "About CA\$ 1.38",
                    initialBonusSupport = "1",
                    totalBonusSupport = "10",
                    deliveryDateString = "April 10",
                    rewardsHaveShippables = true,
                    disclaimerText = "",
                    plotSelected = false
                )
            }
        }
        deliveryDate.assertIsDisplayed()
        deliveryDate.assertTextEquals("April 10")
    }

    @Test
    fun `test plot selected, when plot selected should show plot selected badge`() {
        val rewardsList = listOf(Pair("T-shirt", "$22"), Pair("Pin", "$10"))

        composeTestRule.setContent {
            KSTheme {
                ItemizedRewardListContainer(
                    ksString = null,
                    rewardsList = rewardsList,
                    shippingAmount = 20.0,
                    shippingAmountString = "$20.0",
                    initialShippingLocation = "USA",
                    totalAmount = "50$",
                    totalAmountCurrencyConverted = "About CA\$ 1.38",
                    initialBonusSupport = "1",
                    totalBonusSupport = "10",
                    deliveryDateString = "April 10",
                    rewardsHaveShippables = true,
                    disclaimerText = "",
                    plotSelected = true
                )
            }
        }
        plotSelectedBadge.assertIsDisplayed()
        plotSelectedBadge.assertTextEquals(context.getString(R.string.Pledge_Over_Time))
    }
}
