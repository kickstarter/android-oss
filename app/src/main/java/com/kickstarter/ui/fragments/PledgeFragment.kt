package com.kickstarter.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.databinding.FragmentPledgeBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.setGone
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.adapters.ExpandableHeaderAdapter
import com.kickstarter.ui.adapters.RewardCardAdapter
import com.kickstarter.ui.adapters.ShippingRulesAdapter
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.setTextAndSelection
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.itemdecorations.RewardCardItemDecoration
import com.kickstarter.viewmodels.PledgeFragmentViewModel
import com.stripe.android.ApiResultCallback
import com.stripe.android.SetupIntentResult
import rx.android.schedulers.AndroidSchedulers

@RequiresFragmentViewModel(PledgeFragmentViewModel.ViewModel::class)
class PledgeFragment :
    BaseFragment<PledgeFragmentViewModel.ViewModel>(),
    RewardCardAdapter
    .Delegate,
    ShippingRulesAdapter.Delegate,
    CheckoutRiskMessageFragment.Delegate {

    interface PledgeDelegate {
        fun pledgePaymentSuccessfullyUpdated()
        fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>)
        fun pledgeSuccessfullyUpdated()
    }

    private lateinit var adapter: ShippingRulesAdapter
    private var headerAdapter = ExpandableHeaderAdapter()
    private var isExpanded = false

    private var binding: FragmentPledgeBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentPledgeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCardsAdapter()
        setUpShippingAdapter()
        setupRewardRecyclerView()

        binding?.pledgeSectionPledgeAmount?.pledgeAmount?.onChange { this.viewModel.inputs.pledgeInput(it) }

        binding?.pledgeSectionBonusSupport?.bonusAmount?.onChange { this.viewModel.inputs.bonusInput(it) }

        this.viewModel.outputs.additionalPledgeAmountIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionPledgeAmount?.additionalPledgeAmountContainer?.isGone = it
            }

        this.viewModel.outputs.pledgeSectionIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionPledgeAmount?.pledgeContainer?.isGone = it
            }

        this.viewModel.outputs.decreasePledgeButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionPledgeAmount?.decreasePledge?.isEnabled = it
            }

        this.viewModel.outputs.increasePledgeButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding?.pledgeSectionPledgeAmount?.increasePledge?.isEnabled = it }

        this.viewModel.outputs.headerSectionIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer?.isGone = it
            }

        this.viewModel.outputs.decreaseBonusButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding?.pledgeSectionBonusSupport?.decreaseBonus ?.isEnabled = it }

        this.viewModel.outputs.increaseBonusButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding?.pledgeSectionBonusSupport?.increaseBonus?.isEnabled = it }

        this.viewModel.outputs.estimatedDelivery()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderEstimatedDeliveryLabel ?.text = String.format("%1$2s / %2$2s", binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderEstimatedDeliveryLabel ?.text, it)
            }

        this.viewModel.outputs.estimatedDeliveryInfoIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderEstimatedDeliveryLabel ?.setGone()
            }

        this.viewModel.outputs.continueButtonIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionFooter?.pledgeFooterContinueButton?.isGone = it
            }

        this.viewModel.outputs.conversionTextViewIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionTotal?.totalAmountConversion?.isGone = it
            }

        this.viewModel.outputs.conversionText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setConversionTextView(it) }

        this.viewModel.outputs.paymentContainerIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionPayment?.paymentContainer?.isGone = it
            }

        this.viewModel.outputs.showSelectedCard()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { updatePledgeCardState(it) }

        this.viewModel.outputs.pledgeAmountHeader()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderSummaryAmount?.text = it
            }

        this.viewModel.outputs.pledgeAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionPledgeAmount?.pledgeAmount?.setTextAndSelection(it)
            }

        this.viewModel.outputs.bonusAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionBonusSupport?.bonusAmount?.setTextAndSelection(it)
                binding?.pledgeSectionSummaryBonus?.bonusSummaryAmount?.text = it
            }

        this.viewModel.outputs.pledgeHint()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding?.pledgeSectionPledgeAmount?.pledgeAmount?.hint = it }

        this.viewModel.outputs.bonusHint()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding?.pledgeSectionBonusSupport?.bonusAmount?.hint = it }

        this.viewModel.outputs.pledgeMaximum()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                setPledgeMaximumText(it)
            }

        this.viewModel.outputs.pledgeMaximumIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionPledgeAmount?.pledgeMaximum?.isInvisible = it
                binding?.pledgeSectionBonusSupport?.bonusMaximum?.isInvisible = it
            }

        this.viewModel.outputs.pledgeMinimum()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setPledgeMinimumText(it) }

        this.viewModel.outputs.projectCurrencySymbol()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                setCurrencySymbols(it)
            }

        this.viewModel.outputs.pledgeTextColor()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionPledgeAmount?.pledgeAmount ?.let { pledgeAmount ->
                    setTextColor(it, pledgeAmount)
                }
                binding?.pledgeSectionPledgeAmount?.pledgeSymbolStart ?.let { pledgeAmount ->
                    setTextColor(it, pledgeAmount)
                }
                binding?.pledgeSectionPledgeAmount?.pledgeSymbolEnd ?.let { pledgeAmount ->
                    setTextColor(it, pledgeAmount)
                }
            }

        this.viewModel.outputs.cardsAndProject()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { (binding?.pledgeSectionPayment?.cardsRecycler?.adapter as? RewardCardAdapter)?.takeCards(it.first, it.second) }

        this.viewModel.outputs.addedCard()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                val position = (binding?.pledgeSectionPayment?.cardsRecycler?.adapter as? RewardCardAdapter)?.insertCard(it)
                position?.let { position -> this.viewModel.inputs.addedCardPosition(position) }
            }

        this.viewModel.outputs.startLoginToutActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { startActivity(Intent(this.context, LoginToutActivity::class.java)) }

        this.viewModel.outputs.showNewCardFragment()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                fragmentManager
                    ?.beginTransaction()
                    ?.setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                    ?.add(R.id.secondary_container, NewCardFragment.newInstance(true, it), NewCardFragment::class.java.simpleName)
                    ?.addToBackStack(NewCardFragment::class.java.simpleName)
                    ?.commit()
            }

        this.viewModel.outputs.selectedShippingRule()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionEditableShipping?.shippingRules?.setText(it.toString())
                binding?.pledgeSectionShipping?. shippingRulesStatic ?.text = it.toString()
            }

        this.viewModel.outputs.shippingAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionEditableShipping?.shippingAmountLoadingView?.isGone = true
                binding?.pledgeSectionEditableShipping?.shippingAmount?.let { shippingAmount ->
                    setPlusTextView(
                        shippingAmount, it
                    )
                }
                binding?.pledgeSectionShipping?. shippingAmountStatic?.let { shippingAmountStatic ->
                    setPlusTextView(
                        shippingAmountStatic, it
                    )
                }
            }

        this.viewModel.outputs.shippingSummaryAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionSummaryShipping?.shippingSummaryAmount?.let { shippingSummaryAmount ->
                    setPlusTextView(shippingSummaryAmount, it)
                }
            }

        this.viewModel.outputs.shippingSummaryLocation()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionSummaryShipping?.shippingLabel?.text = String.format("%s: %s", getString(R.string.Shipping), it)
            }

        this.viewModel.outputs.shippingRulesAndProject()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .filter { ObjectUtils.isNotNull(context) }
            .subscribe {
                displayShippingRules(it.first, it.second)
            }

        this.viewModel.outputs.shippingRulesSectionIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionEditableShipping?.editableShippingCl?.isGone = it
            }

        this.viewModel.outputs.shippingRuleStaticIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionShipping?.staticShippingCl?.isGone = it
            }

        this.viewModel.outputs.shippingSummaryIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionSummaryShipping?.shippingSummary?.isGone = it
            }

        this.viewModel.outputs.pledgeSummaryAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding?.pledgeSectionSummaryPledge?.pledgeSummaryAmount?.text = it }

        this.viewModel.outputs.bonusSummaryIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionSummaryBonus?.bonusSummary?.isGone = it
            }

        this.viewModel.outputs.bonusSummaryAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding?.pledgeSectionSummaryBonus?.bonusSummaryAmount?.text = it }

        this.viewModel.outputs.pledgeSummaryIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionSummaryPledge?.pledgeSummary?.isGone = it
            }

        this.viewModel.outputs.totalDividerIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.dividerTotal?.root?.isGone = it
            }

        this.viewModel.outputs.totalAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.pledgeSectionTotal?.totalAmountLoadingView?.isGone = true
                binding?.pledgeSectionTotal?.totalAmount ?.text = it
            }

        this.viewModel.outputs.totalAndDeadline()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setDeadlineWarning(it) }

        this.viewModel.outputs.totalAndDeadlineIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding?.deadlineWarning?.isInvisible = false
            }

        this.viewModel.outputs.showPledgeSuccess()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { (activity as PledgeDelegate?)?.pledgeSuccessfullyCreated(it) }

        this.viewModel.outputs.showSCAFlow()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.viewModel.environment.stripe()?.handleNextActionForSetupIntent(this, it) }

        this.viewModel.outputs.showPledgeError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                activity?.applicationContext?.let {
                    binding?.pledgeContent?.let { pledgeContent ->
                        showErrorToast(it, pledgeContent, getString(R.string.general_error_something_wrong))
                    }
                }
            }

        this.viewModel.outputs.startChromeTab()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                activity?.let { activity ->
                    ChromeTabsHelperActivity.openCustomTab(activity, UrlUtils.baseCustomTabsIntent(activity), Uri.parse(it), null)
                }
            }

        this.viewModel.outputs.baseUrlForTerms()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { setHtmlStrings(it) }

        this.viewModel.outputs.showUpdatePledgeError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                activity?.applicationContext?.let {
                    binding?.pledgeContent?.let { pledgeContent ->
                        showErrorToast(
                            it,
                            pledgeContent,
                            getString(R.string.general_error_something_wrong)
                        )
                    }
                }
            }

        this.viewModel.outputs.showUpdatePledgeSuccess()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { (activity as PledgeDelegate?)?.pledgeSuccessfullyUpdated() }

        this.viewModel.outputs.showUpdatePaymentError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                activity?.applicationContext?.let {
                    binding?.pledgeContent?.let { pledgeContent ->
                        showErrorToast(it, pledgeContent, getString(R.string.general_error_something_wrong))
                    }
                }
            }

        this.viewModel.outputs.showUpdatePaymentSuccess()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { (activity as PledgeDelegate?)?.pledgePaymentSuccessfullyUpdated() }

        this.viewModel.outputs.pledgeButtonCTA()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.setText(it) }

        this.viewModel.outputs.pledgeButtonIsEnabled()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.isEnabled = it }

        this.viewModel.outputs.pledgeButtonIsGone()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.isGone = it
            }

        this.viewModel.outputs.pledgeProgressIsGone()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                binding?.pledgeSectionFooter?.pledgeFooterPledgeButtonProgress?.isGone = it
            }

        this.viewModel.outputs.continueButtonIsEnabled()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { binding?.pledgeSectionFooter?.pledgeFooterContinueButton?.isEnabled = it }

        this.viewModel.outputs.headerSelectedItems()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                populateHeaderItems(it)
            }

        this.viewModel.outputs.isPledgeMinimumSubtitleGone()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                binding?.pledgeSectionPledgeAmount?.pledgeMinimum ?.isGone = it
            }

        this.viewModel.outputs.isBonusSupportSectionGone()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                binding?.pledgeSectionBonusSupport?.bonusContainer?.isGone = it
                binding?.pledgeSectionPledgeAmount?.pledgeContainer?.setPadding(0, resources.getDimension(R.dimen.grid_4).toInt(), 0, 0)
            }

        this.viewModel.outputs.isNoReward()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer?.isGone = it
                binding?.pledgeSectionRewardSummary?.pledgeHeaderContainerNoReward ?.visibility = View.VISIBLE
            }

        this.viewModel.outputs.projectTitle()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                binding?.pledgeSectionRewardSummary?.pledgeHeaderTitleNoReward ?.text = it
            }

        this.viewModel.outputs.localPickUpIsGone()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding?.pledgeSectionPickupLocation?.localPickupContainer?.setGone(it)
            }

        this.viewModel.outputs.localPickUpName()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding?.pledgeSectionPickupLocation?.localPickupLocationName?.text = it
            }

        binding?.pledgeSectionPledgeAmount?. pledgeAmount?.setOnTouchListener { _, _ ->
            binding?.pledgeSectionPledgeAmount?. pledgeAmount?.post {
                binding?.pledgeRoot?.let { pledgeRoot ->
                    binding?.pledgeSectionPledgeAmount?. pledgeAmountLabel?.let { pledgeAmountLabel ->
                        pledgeRoot.smoothScrollTo(0, relativeTop(pledgeAmountLabel, pledgeRoot))
                    }

                    binding?.pledgeSectionPledgeAmount?. pledgeAmount?.requestFocus()
                }
            }
            false
        }

        binding?.pledgeSectionBonusSupport?.bonusAmount ?.setOnTouchListener { _, _ ->
            binding?.pledgeSectionBonusSupport?.bonusAmount ?.post {
                binding?.pledgeRoot?.let { pledgeRoot ->
                    binding?.pledgeSectionBonusSupport?.bonusSupportLabel ?.let {
                        pledgeRoot.smoothScrollTo(0, relativeTop(it, pledgeRoot))
                    }
                    binding?.pledgeSectionBonusSupport?.bonusAmount ?.requestFocus()
                }
            }
            false
        }

        binding?.pledgeSectionEditableShipping?.shippingRules?.setOnTouchListener { _, _ ->
            binding?.pledgeSectionEditableShipping?.shippingRulesLabel?.post {
                binding?.pledgeRoot?.let { pledgeRoot ->
                    binding?.pledgeSectionEditableShipping?.shippingRulesLabel?.let { shippingRulesLabel ->
                        pledgeRoot.smoothScrollTo(0, relativeTop(shippingRulesLabel, pledgeRoot))
                    }
                    binding?.pledgeSectionEditableShipping?.shippingRules?.requestFocus()
                    binding?.pledgeSectionEditableShipping?.shippingRules?.showDropDown()
                }
            }
            false
        }

        binding?.pledgeSectionPledgeAmount?. decreasePledge ?.setOnClickListener {
            this.viewModel.inputs.decreasePledgeButtonClicked()
        }

        binding?.pledgeSectionPledgeAmount?. increasePledge ?.setOnClickListener {
            this.viewModel.inputs.increasePledgeButtonClicked()
        }

        binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer?.setOnClickListener {
            toggleAnimation(isExpanded)
        }
        binding?.pledgeSectionBonusSupport?.decreaseBonus?.setOnClickListener { this.viewModel.inputs.decreaseBonusButtonClicked() }

        binding?.pledgeSectionBonusSupport?.increaseBonus?.setOnClickListener { this.viewModel.inputs.increaseBonusButtonClicked() }

        binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.setOnClickListener {
            this.viewModel.inputs.pledgeButtonClicked()
        }

        binding?.pledgeSectionFooter?.pledgeFooterContinueButton?.let {
            RxView.clicks(it)
                .compose(bindToLifecycle())
                .subscribe { this.viewModel.inputs.continueButtonClicked() }
        }

        this.viewModel.outputs.changeCheckoutRiskMessageBottomSheetStatus()
            .filter {
                it
            }
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                showRiskMessageDialog()
            }

        this.viewModel.outputs.changePledgeSectionAccountabilityFragmentVisiablity()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                binding?.pledgeSectionAccountability?.root?.isGone = it
            }
    }

    private fun showRiskMessageDialog() {
        activity?.supportFragmentManager?.let {
            CheckoutRiskMessageFragment.newInstance(this).show(it.beginTransaction(), "CheckoutRiskMessageFragment")
        }
    }

    private fun populateHeaderItems(selectedItems: List<Pair<Project, Reward>>) {
        headerAdapter.populateData(selectedItems)
    }

    private fun toggleAnimation(isExpanded: Boolean) {
        if (isExpanded)
            collapseAnimation()
        else
            expandAnimation()

        this.isExpanded = !isExpanded
    }

    private fun expandAnimation() {
        binding?.pledgeSectionHeaderRewardSummary?.headerArrowButton?.animate()?.rotation(180f)?.start()

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer)
        constraintSet.clear(R.id.header_summary_list, ConstraintSet.BOTTOM)

        val transition = ChangeBounds()
        transition.duration = 100

        binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer?.let {
            TransitionManager.beginDelayedTransition(it, transition)
            constraintSet.applyTo(it)
        }
    }

    private fun collapseAnimation() {
        binding?.pledgeSectionHeaderRewardSummary?.headerArrowButton?.animate()?.rotation(0f)?.start()

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer)
        constraintSet.connect(R.id.header_summary_list, ConstraintSet.BOTTOM, R.id.header_animation_guideline, ConstraintSet.BOTTOM)

        val transition = ChangeBounds()
        transition.duration = 100

        binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer?.let {
            TransitionManager.beginDelayedTransition(it, transition)
            constraintSet.applyTo(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val stripe = this.viewModel.environment.stripe()
        stripe?.onSetupResult(
            requestCode, data,
            object : ApiResultCallback<SetupIntentResult> {
                override fun onSuccess(result: SetupIntentResult) {
                    this@PledgeFragment.viewModel.inputs.stripeSetupResultSuccessful(result.outcome)
                }

                override fun onError(e: Exception) {
                    this@PledgeFragment.viewModel.inputs.stripeSetupResultUnsuccessful(e)
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        binding?.pledgeSectionPayment?.cardsRecycler?.adapter = null
        binding?.pledgeSectionHeaderRewardSummary?.headerSummaryList?.adapter = null
        this.viewModel = null
    }

    override fun addNewCardButtonClicked() {
        this.viewModel.inputs.newCardButtonClicked()
    }

    fun cardAdded(storedCard: StoredCard) {
        this.viewModel.inputs.cardSaved(storedCard)
    }

    override fun cardSelected(storedCard: StoredCard, position: Int) {
        this.viewModel.inputs.cardSelected(storedCard, position)
    }

    override fun ruleSelected(rule: ShippingRule) {
        this.viewModel.inputs.shippingRuleSelected(rule)
        activity?.hideKeyboard()
        binding?.pledgeSectionEditableShipping?.shippingRules?.clearFocus()

        if (binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.isEnabled == false) {
            binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.isEnabled = true
        }
    }

    private fun displayShippingRules(shippingRules: List<ShippingRule>, project: Project) {
        binding?.pledgeSectionEditableShipping?.shippingRules?.isEnabled = true
        adapter.populateShippingRules(shippingRules, project)
    }

    private fun relativeTop(view: View, parent: ViewGroup): Int {
        val offsetViewBounds = Rect()
        view.getDrawingRect(offsetViewBounds)
        parent.offsetDescendantRectToMyCoords(view, offsetViewBounds)

        return offsetViewBounds.top - parent.paddingTop
    }

    private fun setCurrencySymbols(symbolAndStart: Pair<SpannableString, Boolean>) {
        val symbol = symbolAndStart.first
        val symbolAtStart = symbolAndStart.second
        if (symbolAtStart) {
            binding?.pledgeSectionPledgeAmount?. pledgeSymbolStart ?.text = symbol
            binding?.pledgeSectionPledgeAmount?. pledgeSymbolEnd?.text = null

            binding?.pledgeSectionBonusSupport?.bonusSymbolStart?.text = symbol
            binding?.pledgeSectionBonusSupport?.bonusSymbolEnd?.text = null
        } else {
            binding?.pledgeSectionPledgeAmount?. pledgeSymbolStart ?.text = null
            binding?.pledgeSectionPledgeAmount?. pledgeSymbolEnd?.text = symbol

            binding?.pledgeSectionBonusSupport?.bonusSymbolStart?.text = null
            binding?.pledgeSectionBonusSupport?.bonusSymbolEnd?.text = symbol
        }
    }

    private fun setDeadlineWarning(totalAndDeadline: Pair<String, String>) {
        val total = totalAndDeadline.first
        val deadline = totalAndDeadline.second
        val ksString = requireNotNull(this.viewModel.environment.ksString())
        val warning = ksString.format(
            getString(R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline),
            "total", total,
            "project_deadline", deadline
        )

        val spannableWarning = SpannableString(warning)

        ViewUtils.addBoldSpan(spannableWarning, total)
        ViewUtils.addBoldSpan(spannableWarning, deadline)

        binding?.deadlineWarning?.text = spannableWarning
    }

    private fun setPledgeMaximumText(maximumAmount: String) {
        val ksString = requireNotNull(this.viewModel.environment.ksString())
        binding?.pledgeSectionPledgeAmount?. pledgeMaximum ?.text = ksString.format(getString(R.string.Enter_an_amount_less_than_max_pledge), "max_pledge", maximumAmount)
        binding?.pledgeSectionBonusSupport?.bonusMaximum?.text = ksString.format(getString(R.string.Enter_an_amount_less_than_max_pledge), "max_pledge", maximumAmount)
    }

    private fun setPledgeMinimumText(minimumAmount: String) {
        val ksString = requireNotNull(this.viewModel.environment.ksString())
        binding?.pledgeSectionPledgeAmount?. pledgeMinimum ?.text = ksString.format(getString(R.string.The_minimum_pledge_is_min_pledge), "min_pledge", minimumAmount)
    }

    private fun setPlusTextView(textView: TextView, localizedAmount: CharSequence) {
        val ksString = requireNotNull(this.viewModel.environment.ksString())
        textView.contentDescription = ksString.format(getString(R.string.plus_shipping_cost), "shipping_cost", localizedAmount.toString())
        textView.text = localizedAmount
    }

    private fun setTextColor(colorResId: Int, vararg textViews: TextView) {
        context?.let {
            val color = ContextCompat.getColor(it, colorResId)
            for (textView in textViews) {
                textView.setTextColor(color)
            }
        }
    }

    private fun setUpCardsAdapter() {
        binding?.pledgeSectionPayment?.cardsRecycler?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.pledgeSectionPayment?.cardsRecycler?.adapter = RewardCardAdapter(this)
        binding?.pledgeSectionPayment?.cardsRecycler?.addItemDecoration(RewardCardItemDecoration(resources.getDimensionPixelSize(R.dimen.grid_1)))
    }

    private fun setUpShippingAdapter() {
        context?.let {
            adapter = ShippingRulesAdapter(it, R.layout.item_shipping_rule, arrayListOf(), this)
            binding?.pledgeSectionEditableShipping?.shippingRules?.setAdapter(adapter)
        }

        binding?.pledgeSectionEditableShipping?.shippingRules?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().isNullOrBlank()) {
                    binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun setupRewardRecyclerView() {
        binding?.pledgeSectionHeaderRewardSummary?.headerSummaryList?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.pledgeSectionHeaderRewardSummary?.headerSummaryList?.adapter = headerAdapter
    }

    private fun setClickableHtml(string: String, textView: TextView) {
        val spannableBuilder = SpannableStringBuilder(ViewUtils.html(string))
        // https://stackoverflow.com/a/19989677
        val urlSpans = spannableBuilder.getSpans(0, string.length, URLSpan::class.java)
        for (urlSpan in urlSpans) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    this@PledgeFragment.viewModel.inputs.linkClicked(urlSpan.url)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = ContextCompat.getColor(textView.context, R.color.accent)
                }
            }
            val spanStart = spannableBuilder.getSpanStart(urlSpan)
            val spanEnd = spannableBuilder.getSpanEnd(urlSpan)
            val spanFlags = spannableBuilder.getSpanFlags(urlSpan)
            spannableBuilder.setSpan(clickableSpan, spanStart, spanEnd, spanFlags)
            spannableBuilder.removeSpan(urlSpan)
        }

        textView.text = spannableBuilder
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setConversionTextView(@NonNull amount: String) {
        val currencyConversionString = context?.getString(R.string.About_reward_amount)
        val ksString = requireNotNull(this.viewModel.environment.ksString())
        binding?.pledgeSectionTotal?.totalAmountConversion ?.text = (
            currencyConversionString?.let {
                ksString.format(it, "reward_amount", amount)
            }
            )
    }

    @SuppressLint("SetTextI18n")
    private fun setHtmlStrings(baseUrl: String) {
        val termsOfUseUrl = UrlUtils.appendPath(baseUrl, HelpActivity.TERMS_OF_USE)
        val cookiePolicyUrl = UrlUtils.appendPath(baseUrl, HelpActivity.COOKIES)
        val privacyPolicyUrl = UrlUtils.appendPath(baseUrl, HelpActivity.PRIVACY)

        val ksString = requireNotNull((activity?.applicationContext as? KSApplication)?.component()?.environment()?.ksString())
        val byPledgingYouAgree = getString(R.string.By_pledging_you_agree_to_Kickstarters_Terms_of_Use_Privacy_Policy_and_Cookie_Policy)

        val agreementWithUrls = ksString.format(
            byPledgingYouAgree, "terms_of_use_link", termsOfUseUrl,
            "privacy_policy_link", privacyPolicyUrl, "cookie_policy_link", cookiePolicyUrl
        )

        binding?.pledgeSectionFooter?.pledgeFooterPledgeAgreement?.let {
            setClickableHtml(
                agreementWithUrls,
                it
            )
        }

        val trustUrl = UrlUtils.appendPath(baseUrl, "trust")
        val accountabilityWithUrl = ksString.format(
            getString(R.string.Its_a_way_to_bring_creative_projects_to_life_Learn_more_about_accountability),
            "trust_link",
            trustUrl
        )

        binding?.pledgeSectionAccountability?. accountability?.let {
            setClickableHtml(
                accountabilityWithUrl,
                it
            )
        }
        binding?.pledgeSectionAccountability?. accountabilityContainer?.setOnClickListener {
            this.viewModel.inputs.linkClicked(trustUrl)
        }
    }

    private fun updatePledgeCardState(positionAndCardState: Pair<Int, CardState>) {
        val position = positionAndCardState.first
        val cardState = positionAndCardState.second
        val rewardCardAdapter = binding?.pledgeSectionPayment?.cardsRecycler?.adapter as? RewardCardAdapter

        if (cardState == CardState.SELECTED) {
            rewardCardAdapter?.setSelectedPosition(position)
        } else {
            rewardCardAdapter?.resetSelectedPosition()
        }
    }

    override fun onDialogConfirmButtonClicked() {
        viewModel.inputs.onRiskManagementConfirmed()
    }

    companion object {
        fun newInstance(pledgeData: PledgeData, pledgeReason: PledgeReason): PledgeFragment {
            val fragment = PledgeFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, pledgeData)
            argument.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, pledgeReason)
            fragment.arguments = argument
            return fragment
        }
    }
}
