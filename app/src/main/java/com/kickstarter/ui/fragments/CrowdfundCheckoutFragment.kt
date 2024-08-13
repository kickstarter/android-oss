package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.databinding.FragmentCrowdfundCheckoutBinding
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Checkout
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.activities.compose.projectpage.CheckoutScreen
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.fragments.PledgeFragment.PledgeDelegate
import com.kickstarter.viewmodels.projectpage.CheckoutUIState
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel.Factory


class CrowdfundCheckoutFragment : Fragment() {

    private var binding: FragmentCrowdfundCheckoutBinding? = null

    private lateinit var viewModelFactory: Factory
    private val viewModel: CrowdfundCheckoutViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentCrowdfundCheckoutBinding.inflate(inflater, container, false)

        val view = binding?.root
        binding?.composeView?.apply {
            val environment = this.context.getEnvironment()?.let { env ->
                viewModelFactory = Factory(env, bundle = arguments)
                viewModel.provideBundle(arguments)
                env
            }

            viewModel.provideErrorAction { message ->
                activity?.runOnUiThread {
                    showErrorToast(context, this, message ?: getString(R.string.general_error_something_wrong))
                }
            }

            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Compose world
            setContent {
                KickstarterApp(
                    useDarkTheme = true
                ) {

                    val checkoutStates = viewModel.crowdfundCheckoutUIState.collectAsStateWithLifecycle(
                        initialValue = CheckoutUIState()
                    ).value

                    val rwList = checkoutStates.selectedRewards
                    val email = checkoutStates.userEmail
                    val storedCards = checkoutStates.storeCards
                    val isLoading = checkoutStates.isLoading

                    // TODO: all of this will come from Backing on Manage/Update
                    val pledgeData = viewModel.getPledgeData()
                    val pledgeReason = viewModel.getPledgeReason() ?: PledgeReason.PLEDGE
                    val shippingRule = pledgeData?.shippingRule() ?: ShippingRule.builder().build()
                    val shippingAmount = pledgeData?.shippingCostIfShipping() ?: 0.0
                    val totalAmount = pledgeData?.pledgeAmountTotal() ?: 0.0
                    val bonus = pledgeData?.bonusAmount() ?: 0.0
                    val project = pledgeData?.projectData()?.project() ?: Project.builder().build()
                    val selectedRw = pledgeData?.reward() ?: Reward.builder().build()

                    val resultCheckoutStates = viewModel.checkoutResultState.collectAsStateWithLifecycle(
                        initialValue = Checkout.builder().build()
                    )

                    if(resultCheckoutStates.value.backing().requiresAction()) {
                        (activity as PledgeDelegate?)?.pledgeSuccessfullyUpdated()
                    }

                    KSTheme {
                        CheckoutScreen(
                            rewardsList = rwList.map { Pair(it.title() ?: "", it.pledgeAmount().toString()) },
                            environment = requireNotNull(environment),
                            shippingAmount = shippingAmount,
                            selectedReward = selectedRw,
                            currentShippingRule = shippingRule,
                            totalAmount = totalAmount,
                            totalBonusSupport = bonus,
                            storedCards = storedCards,
                            project = project,
                            email = email,
                            pledgeReason = pledgeReason,
                            rewardsHaveShippables = true, // TODO: pledeData extension for this
                            onPledgeCtaClicked = {
                                viewModel.pledge()
                            },
                            isLoading = isLoading,
                            newPaymentMethodClicked = {},
                            onDisclaimerItemClicked = {},
                            onAccountabilityLinkClicked = {},
                            onSelectedPaymentMethod = { paymentMethodSeelcted ->
                                viewModel.userChangedPaymentMethodSelected(paymentMethodSeelcted)
                            }
                        )
                    }
                }
            }
        }
        return view
    }
}
