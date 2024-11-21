package com.kickstarter.features.pledgeredemption.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.kickstarter.features.pledgeredemption.viewmodels.PledgeRedemptionViewModel
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.activities.compose.projectpage.CheckoutScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.PledgeReason

class PledgeRedemptionActivity : ComponentActivity() {
    private lateinit var viewModelFactory: PledgeRedemptionViewModel.Factory
    private val viewModel: PledgeRedemptionViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val env = this.getEnvironment()?.let { env ->
            viewModelFactory = PledgeRedemptionViewModel.Factory(env, bundle = intent.extras)
            env
        }
        viewModel.start()

        setContent {
            setContent {

                val backing = viewModel.backing
                val project = viewModel.project
                val lists = mutableListOf<Reward>()
                val shippingAmount = backing.shippingAmount().toDouble()
                val totalAmount = backing.amount()
                val bonus = backing.bonusAmount()
                val pledgeReason = PledgeReason.PLEDGE

                backing.reward()?.let { lists.add(it) }
                backing.addOns()?.map { lists.add(it) }

                val darModeEnabled = this.isDarkModeEnabled(env = requireNotNull(env))
                KickstarterApp(useDarkTheme = darModeEnabled) {
                    CheckoutScreen(
                        rewardsList = lists.map {
                            Pair(
                                it.title() ?: "",
                                it.pledgeAmount().toString()
                            )
                        },
                        environment = env,
                        shippingAmount = shippingAmount,
                        selectedReward = RewardFactory.rewardWithShipping(),
                        currentShippingRule = ShippingRule.builder().build(),
                        totalAmount = totalAmount,
                        totalBonusSupport = bonus,
                        storedCards = emptyList(),
                        project = project,
                        email = "example@example.com",
                        pledgeReason = pledgeReason,
                        rewardsHaveShippables = true,
                        onPledgeCtaClicked = { },
                        newPaymentMethodClicked = { },
                        onDisclaimerItemClicked = {},
                        onAccountabilityLinkClicked = {},
                        isPlotEnabled = env.featureFlagClient()
                            ?.getBoolean(FlagKey.ANDROID_PLEDGE_OVER_TIME) ?: false,
                    )
                }
            }
        }
    }
}
