package com.kickstarter.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.databinding.FragmentPledgeBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPaymentSheetConfiguration
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.adapters.ExpandableHeaderAdapter
import com.kickstarter.ui.adapters.RewardCardAdapter
import com.kickstarter.ui.adapters.ShippingRulesAdapter
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.itemdecorations.RewardCardItemDecoration
import com.kickstarter.viewmodels.PledgeFragmentViewModel
import com.stripe.android.ApiResultCallback
import com.stripe.android.SetupIntentResult
import com.stripe.android.Stripe
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.model.PaymentOption
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class PledgeFragment :
    Fragment(),
    RewardCardAdapter
    .Delegate,
    ShippingRulesAdapter.Delegate {

    interface PledgeDelegate {
        fun pledgePaymentSuccessfullyUpdated()
        fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>)
        fun pledgeSuccessfullyUpdated()
    }

    private lateinit var adapter: ShippingRulesAdapter
    private var headerAdapter = ExpandableHeaderAdapter()
    private var isExpanded = false
    private var setupClientId: String = ""
    private lateinit var flowController: PaymentSheet.FlowController

    private var binding: FragmentPledgeBinding? = null

    private lateinit var viewModelFactory: PledgeFragmentViewModel.Factory
    private val viewModel: PledgeFragmentViewModel.PledgeFragmentViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    private lateinit var stripe: Stripe
    private lateinit var ksString: KSString

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentPledgeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val env = this.context?.getEnvironment()?.let { env ->
            viewModelFactory = PledgeFragmentViewModel.Factory(env, bundle = arguments)
            env
        }

        stripe = requireNotNull(env?.stripe())
        ksString = requireNotNull(env?.ksString())

        setUpCardsAdapter()
        setUpShippingAdapter()
        setupRewardRecyclerView()

        flowController = PaymentSheet.FlowController.create(
            fragment = this,
            paymentOptionCallback = ::onPaymentOption,
            paymentResultCallback = ::onPaymentSheetResult
        )

        this.viewModel.outputs.headerSectionIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.estimatedDelivery()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderEstimatedDeliveryLabel ?.text = String.format("%1$2s / %2$2s", binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderEstimatedDeliveryLabel ?.text, it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.estimatedDeliveryInfoIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderEstimatedDeliveryLabel ?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.continueButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionFooter?.pledgeFooterContinueButton?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.conversionTextViewIsGone()
            .subscribe {
                binding?.pledgeSectionTotal?.totalAmountConversion?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.conversionText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setConversionTextView(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.paymentContainerIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionPayment?.paymentContainer?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showSelectedCard()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updatePledgeCardState(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeAmountHeader()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderSummaryAmount?.text = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.cardsAndProject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (binding?.pledgeSectionPayment?.cardsRecycler?.adapter as? RewardCardAdapter)?.takeCards(it.first, it.second) }
            .addToDisposable(disposables)

        this.viewModel.outputs.addedCard()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val position = (binding?.pledgeSectionPayment?.cardsRecycler?.adapter as? RewardCardAdapter)?.insertCard(it)
                position?.let { position -> this.viewModel.inputs.addedCardPosition(position) }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.startLoginToutActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startActivity(Intent(this.context, LoginToutActivity::class.java)) }
            .addToDisposable(disposables)

        this.viewModel.outputs.selectedShippingRule()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionShipping?.shippingRulesStatic ?.text = it.toString()
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingAmount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionShipping?. shippingAmountStatic?.let { shippingAmountStatic ->
                    setPlusTextView(
                        shippingAmountStatic, it
                    )
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingSummaryAmount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionSummaryShipping?.shippingSummaryAmount?.let { shippingSummaryAmount ->
                    setPlusTextView(shippingSummaryAmount, it)
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingSummaryLocation()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionSummaryShipping?.shippingLabel?.text = String.format("%s: %s", getString(R.string.Shipping), it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingRulesAndProject()
            .observeOn(AndroidSchedulers.mainThread())
            .filter { context.isNotNull() }
            .subscribe {
                displayShippingRules(it.first, it.second)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingRuleStaticIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionShipping?.staticShippingCl?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingSummaryIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionSummaryShipping?.shippingSummary?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeSummaryAmount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.pledgeSectionSummaryPledge?.pledgeSummaryAmount?.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.bonusSummaryIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionSummaryBonus?.bonusSummary?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.bonusSummaryAmount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.pledgeSectionSummaryBonus?.bonusSummaryAmount?.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeSummaryIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionSummaryPledge?.pledgeSummary?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.totalDividerIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.dividerTotal?.root?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.totalAmount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionTotal?.totalAmountLoadingView?.isGone = true
                binding?.pledgeSectionTotal?.totalAmount ?.text = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.totalAndDeadline()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setDeadlineWarning(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.totalAndDeadlineIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.deadlineWarning?.isInvisible = false
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showPledgeSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (activity as PledgeDelegate?)?.pledgeSuccessfullyCreated(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showSCAFlow()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                stripeNextAction(it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showPledgeError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.applicationContext?.let {
                    binding?.pledgeContent?.let { pledgeContent ->
                        showErrorToast(it, pledgeContent, getString(R.string.general_error_something_wrong))
                    }
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.startChromeTab()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.let { activity ->
                    ChromeTabsHelperActivity.openCustomTab(activity, UrlUtils.baseCustomTabsIntent(activity), Uri.parse(it), null)
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.baseUrlForTerms()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setHtmlStrings(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showUpdatePledgeError()
            .observeOn(AndroidSchedulers.mainThread())
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
            .addToDisposable(disposables)

        this.viewModel.outputs.showUpdatePledgeSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (activity as PledgeDelegate?)?.pledgeSuccessfullyUpdated() }
            .addToDisposable(disposables)

        this.viewModel.outputs.showUpdatePaymentError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.applicationContext?.let {
                    binding?.pledgeContent?.let { pledgeContent ->
                        showErrorToast(it, pledgeContent, getString(R.string.general_error_something_wrong))
                    }
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showUpdatePaymentSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (activity as PledgeDelegate?)?.pledgePaymentSuccessfullyUpdated() }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeButtonCTA()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeButtonIsEnabled()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.isEnabled = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeProgressIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionFooter?.pledgeFooterPledgeButtonProgress?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.continueButtonIsEnabled()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.pledgeSectionFooter?.pledgeFooterContinueButton?.isEnabled = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.headerSelectedItems()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                populateHeaderItems(it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.isNoReward()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer?.isGone = it
                binding?.pledgeSectionRewardSummary?.pledgeHeaderContainerNoReward ?.visibility = View.VISIBLE
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.projectTitle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeSectionRewardSummary?.pledgeHeaderTitleNoReward ?.text = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.localPickUpIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding?.pledgeSectionPickupLocation?.localPickupContainer?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.localPickUpName()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding?.pledgeSectionPickupLocation?.localPickupLocationName?.text = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.presentPaymentSheet()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { clientSecretAndUserEmail: Pair<String, String> ->
                setupClientId = clientSecretAndUserEmail.first
                flowControllerPresentPaymentOption(clientSecretAndUserEmail.first, clientSecretAndUserEmail.second)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeContent?.let { pledgeContent ->
                    context?.let {
                        showErrorToast(it, pledgeContent, getString(R.string.general_error_something_wrong))
                    }
                }
            }
            .addToDisposable(disposables)

        binding?.pledgeSectionHeaderRewardSummary?.pledgeHeaderContainer?.setOnClickListener {
            toggleAnimation(isExpanded)
        }

        binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.setOnClickListener {
            this.viewModel.inputs.pledgeButtonClicked()
        }

        binding?.pledgeSectionFooter?.pledgeFooterContinueButton?.setOnClickListener {
            this.viewModel.inputs.continueButtonClicked()
        }

        this.viewModel.outputs.setState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                (binding?.pledgeSectionPayment?.cardsRecycler?.adapter as? RewardCardAdapter)?.updateState(it)
            }
            .addToDisposable(disposables)
    }

    private fun stripeNextAction(it: String) {
        try {
            // - PaymentIntent format
            if (it.contains("pi_")) {
                stripe.handleNextActionForPayment(this, it)
            } else {
                // - SetupIntent format
                stripe.handleNextActionForSetupIntent(this, it)
            }
        } catch (exception: Exception) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }

    // Update the UI with the returned PaymentOption
    private fun onPaymentOption(paymentOption: PaymentOption?) {
        paymentOption?.let {
            val storedCard = StoredCard.Builder(
                lastFourDigits = paymentOption.label.takeLast(4),
                resourceId = paymentOption.drawableResourceId,
                clientSetupId = setupClientId
            ).build()
            this.viewModel.inputs.cardSaved(storedCard)
            Timber.d(" ${this.javaClass.canonicalName} onPaymentOption with ${storedCard.lastFourDigits()} and ${storedCard.clientSetupId()}")
            flowController.confirm()
        }
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
            binding?.pledgeContent?.let { pledgeContent ->
                context?.let {
                    showErrorToast(it, pledgeContent, getString(R.string.general_error_something_wrong))
                }
            }
        }
        this.viewModel.inputs.paymentSheetPresented(success)
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        this.viewModel.inputs.paymentSheetResult(paymentSheetResult)
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                binding?.pledgeContent?.let { pledgeContent ->
                    context?.let {
                        showErrorToast(it, pledgeContent, getString(R.string.general_error_oops))
                    }
                }
            }
            is PaymentSheetResult.Failed -> {
                binding?.pledgeContent?.let { pledgeContent ->
                    context?.let {
                        val errorMessage = paymentSheetResult.error.localizedMessage ?: getString(R.string.general_error_something_wrong)
                        showErrorToast(it, pledgeContent, errorMessage)
                    }
                }
            }
            is PaymentSheetResult.Completed -> {
            }
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
        stripe.onSetupResult(
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

    override fun onDestroyView() {
        disposables.clear()
        binding?.pledgeSectionPayment?.cardsRecycler?.adapter = null
        binding?.pledgeSectionHeaderRewardSummary?.headerSummaryList?.adapter = null
        super.onDestroyView()
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

        if (binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.isEnabled == false) {
            binding?.pledgeSectionFooter?.pledgeFooterPledgeButton?.isEnabled = true
        }
    }

    private fun displayShippingRules(shippingRules: List<ShippingRule>, project: Project) {
        adapter.populateShippingRules(shippingRules, project)
    }

    private fun relativeTop(view: View, parent: ViewGroup): Int {
        val offsetViewBounds = Rect()
        view.getDrawingRect(offsetViewBounds)
        parent.offsetDescendantRectToMyCoords(view, offsetViewBounds)

        return offsetViewBounds.top - parent.paddingTop
    }

    private fun setDeadlineWarning(totalAndDeadline: Pair<String, String>) {
        val total = totalAndDeadline.first
        val deadline = totalAndDeadline.second
        val warning = ksString.format(
            getString(R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline_and_receive_proof_of_pledge),
            "total", total,
            "project_deadline", deadline
        )

        val spannableWarning = SpannableString(warning)

        ViewUtils.addBoldSpan(spannableWarning, total)
        ViewUtils.addBoldSpan(spannableWarning, deadline)

        binding?.deadlineWarning?.text = spannableWarning
    }

    private fun setPlusTextView(textView: TextView, localizedAmount: CharSequence) {
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
        }
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

    private fun setConversionTextView(amount: String) {
        val currencyConversionString = context?.getString(R.string.About_reward_amount)
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
}
