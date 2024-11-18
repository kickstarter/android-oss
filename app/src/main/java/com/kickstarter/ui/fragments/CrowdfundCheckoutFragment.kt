package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.databinding.FragmentCrowdfundCheckoutBinding
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPaymentSheetConfiguration
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.activities.compose.projectpage.CheckoutScreen
import com.kickstarter.ui.activities.compose.projectpage.getRewardListAndPrices
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.fragments.PledgeFragment.PledgeDelegate
import com.kickstarter.viewmodels.projectpage.CheckoutUIState
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel.Factory
import com.stripe.android.paymentsheet.PaymentOptionCallback
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.PaymentSheetResultCallback
import timber.log.Timber

class CrowdfundCheckoutFragment : Fragment() {

    private var binding: FragmentCrowdfundCheckoutBinding? = null

    private lateinit var viewModelFactory: Factory
    private val viewModel: CrowdfundCheckoutViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var flowController: PaymentSheet.FlowController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                    showErrorToast(
                        context,
                        this,
                        message ?: getString(R.string.general_error_something_wrong)
                    )
                }
            }

            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Compose world
            setContent {
                KickstarterApp(
                    useDarkTheme = true
                ) {

                    val checkoutStates =
                        viewModel.crowdfundCheckoutUIState.collectAsStateWithLifecycle(
                            initialValue = CheckoutUIState()
                        ).value

                    val rwList = checkoutStates.selectedRewards
                    val email = checkoutStates.userEmail
                    val storedCards = checkoutStates.storeCards
                    val isLoading = checkoutStates.isLoading
                    val shippingAmount = checkoutStates.shippingAmount
                    val totalAmount = checkoutStates.checkoutTotal
                    val shippingRule = checkoutStates.shippingRule
                    val bonus = checkoutStates.bonusAmount

                    val pledgeData = viewModel.getPledgeData()
                    val pledgeReason = viewModel.getPledgeReason() ?: PledgeReason.PLEDGE
                    val project = pledgeData?.projectData()?.project() ?: Project.builder().build()
                    val selectedRw = pledgeData?.reward() ?: Reward.builder().build()

                    val checkoutSuccess =
                        viewModel.checkoutResultState.collectAsStateWithLifecycle().value
                    val id = checkoutSuccess.first?.id() ?: -1

                    val paymentSheetPresenter =
                        viewModel.presentPaymentSheetStates.collectAsStateWithLifecycle().value
                    val setUpIntent = paymentSheetPresenter.setupClientId

                    configurePaymentSheet(paymentSheetPresenter.setupClientId)
                    LaunchedEffect(key1 = setUpIntent) {
                        if (setUpIntent.isNotEmpty() && email.isNotEmpty()) {
                            flowControllerPresentPaymentOption(setUpIntent, email)
                        }
                    }

                    LaunchedEffect(id) {
                        if (id > 0) {
                            if (pledgeReason == PledgeReason.PLEDGE)
                                (activity as PledgeDelegate?)?.pledgeSuccessfullyCreated(
                                    checkoutSuccess
                                )
                            if (pledgeReason == PledgeReason.UPDATE_PAYMENT)
                                (activity as PledgeDelegate?)?.pledgePaymentSuccessfullyUpdated()
                            if (pledgeReason == PledgeReason.UPDATE_REWARD || pledgeReason == PledgeReason.UPDATE_PLEDGE || pledgeReason == PledgeReason.FIX_PLEDGE)
                                (activity as PledgeDelegate?)?.pledgeSuccessfullyUpdated()
                        }
                    }

                    KSTheme {
                        CheckoutScreen(
                            rewardsList = getRewardListAndPrices(rwList, environment, project),
                            selectedRewardsAndAddOns = rwList,
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
                            rewardsHaveShippables = rwList.any {
                                RewardUtils.isShippable(it)
                            },
                            onPledgeCtaClicked = {
                                viewModel.pledgeOrUpdatePledge()
                            },
                            isLoading = isLoading,
                            newPaymentMethodClicked = {
                                viewModel.getSetupIntent()
                            },
                            onDisclaimerItemClicked = {},
                            onAccountabilityLinkClicked = {},
                            onChangedPaymentMethod = { paymentMethodSelected ->
                                viewModel.userChangedPaymentMethodSelected(paymentMethodSelected)
                            },
                            isPlotEnabled = environment.featureFlagClient()
                                ?.getBoolean(FlagKey.ANDROID_PLEDGE_OVER_TIME) ?: false && pledgeReason == PledgeReason.PLEDGE,
                        )
                    }
                }
            }
        }
        return view
    }

    private fun flowControllerPresentPaymentOption(clientSecret: String, userEmail: String) {
        context?.let {
            flowController.configureWithSetupIntent(
                setupIntentClientSecret = clientSecret,
                configuration = it.getPaymentSheetConfiguration(userEmail),
                callback = ::onConfigured
            )
        }
    }

    private fun onConfigured(success: Boolean, error: Throwable?) {
        if (success) {
            flowController.presentPaymentOptions()
        } else {
            binding?.composeView?.let { view ->
                context?.let {
                    activity?.runOnUiThread {
                        showErrorToast(
                            it,
                            view,
                            error?.message ?: getString(R.string.general_error_something_wrong)
                        )
                    }
                }
            }
        }
        this.viewModel.paymentSheetPresented(success)
    }

    // TODO: explore this piece to be more generic/reusable between crowdfund/late pledges/pledge redemption,
    // TODO: it does require specific VM callbacks
    private fun configurePaymentSheet(setupClientId: String) {

        val paymentOptionCallback = PaymentOptionCallback { paymentOption ->
            paymentOption?.let {
                val storedCard = StoredCard.Builder(
                    lastFourDigits = paymentOption.label.takeLast(4),
                    resourceId = paymentOption.drawableResourceId,
                    clientSetupId = setupClientId
                ).build()
                this.viewModel.newlyAddedPaymentMethod(storedCard)
                Timber.d(" ${this.javaClass.canonicalName} onPaymentOption with ${storedCard.lastFourDigits()} and ${storedCard.clientSetupId()}")
                flowController.confirm()
            }
        }

        val onPaymentSheetResult = PaymentSheetResultCallback { paymentSheetResult ->
            this.viewModel.paymentSheetResult(paymentSheetResult)
            when (paymentSheetResult) {
                is PaymentSheetResult.Canceled -> {
                    binding?.composeView?.let { view ->
                        context?.let {
                            activity?.runOnUiThread {
                                showErrorToast(it, view, getString(R.string.general_error_oops))
                            }
                        }
                    }
                }

                is PaymentSheetResult.Failed -> {
                    binding?.composeView?.let { view ->
                        context?.let {
                            val errorMessage = paymentSheetResult.error.localizedMessage
                                ?: getString(R.string.general_error_something_wrong)
                            activity?.runOnUiThread {
                                showErrorToast(it, view, errorMessage)
                            }
                        }
                    }
                }

                is PaymentSheetResult.Completed -> {
                }
            }
        }

        flowController = PaymentSheet.FlowController.create(
            fragment = this,
            paymentOptionCallback = paymentOptionCallback,
            paymentResultCallback = onPaymentSheetResult
        )
    }
}
