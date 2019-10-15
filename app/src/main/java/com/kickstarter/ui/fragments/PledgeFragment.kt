package com.kickstarter.ui.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.extensions.hideKeyboard
import com.kickstarter.extensions.onChange
import com.kickstarter.extensions.snackbar
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.FreezeLinearLayoutManager
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.activities.ThanksActivity
import com.kickstarter.ui.adapters.RewardCardAdapter
import com.kickstarter.ui.adapters.ShippingRulesAdapter
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.itemdecorations.RewardCardItemDecoration
import com.kickstarter.ui.viewholders.NativeCheckoutRewardViewHolder
import com.kickstarter.viewmodels.PledgeFragmentViewModel
import kotlinx.android.synthetic.main.fragment_pledge.*
import kotlinx.android.synthetic.main.fragment_pledge_section_delivery.*
import kotlinx.android.synthetic.main.fragment_pledge_section_payment.*
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.*
import kotlinx.android.synthetic.main.fragment_pledge_section_shipping.*
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_pledge.*
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_shipping.*
import kotlinx.android.synthetic.main.fragment_pledge_section_total.*
import kotlin.math.max
import kotlin.math.min

@RequiresFragmentViewModel(PledgeFragmentViewModel.ViewModel::class)
class PledgeFragment : BaseFragment<PledgeFragmentViewModel.ViewModel>(), RewardCardAdapter.Delegate, ShippingRulesAdapter.Delegate {

    interface PledgeDelegate {
        fun pledgePaymentSuccessfullyUpdated()
        fun pledgeSuccessfullyUpdated()
    }

    private val defaultAnimationDuration = 200L
    private var animDuration = defaultAnimationDuration

    private lateinit var adapter: ShippingRulesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_pledge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewTreeObserver = pledge_root.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    this@PledgeFragment.viewModel.inputs.onGlobalLayout()
                    pledge_root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        setUpCardsAdapter()
        setUpShippingAdapter()

        pledge_amount.onChange { this.viewModel.inputs.pledgeInput(it) }

        this.animDuration = when (savedInstanceState) {
            null -> this.defaultAnimationDuration
            else -> 0L
        }

        this.viewModel.outputs.additionalPledgeAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setPlusTextView(additional_pledge_amount, it) }

        this.viewModel.outputs.additionalPledgeAmountIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(additional_pledge_amount_container, it) }

        this.viewModel.outputs.startRewardShrinkAnimation()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { revealPledgeSection(it) }

        this.viewModel.outputs.startRewardExpandAnimation()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { hidePledgeSection() }

        this.viewModel.outputs.snapshotIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { configureUIBySnapshotVisibility(it) }

        this.viewModel.outputs.pledgeSectionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(pledge_container, it) }

        this.viewModel.outputs.decreasePledgeButtonIsEnabled()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { decrease_pledge.isEnabled = it }

        this.viewModel.outputs.increasePledgeButtonIsEnabled()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { increase_pledge.isEnabled = it }

        this.viewModel.outputs.estimatedDelivery()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { pledge_estimated_delivery.text = it }

        this.viewModel.outputs.estimatedDeliveryInfoIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(pledge_estimated_delivery_container, it) }

        this.viewModel.outputs.deliverySectionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(delivery, it) }

        this.viewModel.outputs.deliveryDividerIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(divider_delivery, it) }

        this.viewModel.outputs.continueButtonIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(continue_to_tout, it) }

        this.viewModel.outputs.conversionTextViewIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(total_amount_conversion))

        this.viewModel.outputs.conversionText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setConversionTextView(it) }

        this.viewModel.outputs.paymentContainerIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(payment_container, it) }

        this.viewModel.outputs.showPledgeCard()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { updatePledgeCardState(it) }

        this.viewModel.outputs.pledgeAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    pledge_amount.setText(it)
                    pledge_amount.setSelection(it.length)
                }

        this.viewModel.outputs.pledgeHint()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { pledge_amount.hint = it }

        this.viewModel.outputs.pledgeMinimum()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setPledgeMinimumText(it) }

        this.viewModel.outputs.projectCurrencySymbol()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setCurrencySymbols(it) }

        this.viewModel.outputs.pledgeTextColor()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setTextColor(it, pledge_amount, pledge_symbol_start, pledge_symbol_end) }

        this.viewModel.outputs.cardsAndProject()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { (cards_recycler.adapter as RewardCardAdapter).takeCards(it.first, it.second) }

        this.viewModel.outputs.addedCard()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    val position = (cards_recycler.adapter as RewardCardAdapter).insertCard(it)
                    this.viewModel.inputs.addedCardPosition(position)
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
                .subscribe { shipping_rules.setText(it.toString()) }

        this.viewModel.outputs.shippingAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    ViewUtils.setGone(shipping_amount_loading_view, true)
                    setPlusTextView(shipping_amount, it)
                }

        this.viewModel.outputs.shippingSummaryAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setPlusTextView(shipping_summary_amount, it) }

        this.viewModel.outputs.shippingSummaryLocation()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { shipping_label.text = String.format("%s: %s", getString(R.string.Shipping), it) }

        this.viewModel.outputs.shippingRulesAndProject()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .filter { ObjectUtils.isNotNull(context) }
                .subscribe { displayShippingRules(it.first, it.second) }

        this.viewModel.outputs.shippingRulesSectionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(shipping_rules_row, it) }

        this.viewModel.outputs.shippingSummaryIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(shipping_summary, it) }

        this.viewModel.outputs.pledgeSummaryAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { pledge_summary_amount.text = it }

        this.viewModel.outputs.pledgeSummaryIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(pledge_summary, it) }

        this.viewModel.outputs.totalDividerIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(divider_total, it) }

        this.viewModel.outputs.totalAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    ViewUtils.setGone(total_amount_loading_view, true)
                    total_amount.text = it
                }

        this.viewModel.outputs.startThanksActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { project ->
                    activity?.let {
                        startActivity(Intent(it, ThanksActivity::class.java)
                                .putExtra(IntentKey.PROJECT, project))
                    }
                }

        this.viewModel.outputs.showPledgeError()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { snackbar(pledge_content, getString(R.string.general_error_something_wrong)).show() }

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

        this.viewModel.outputs.updatePledgeButtonIsGone()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { ViewUtils.setGone(update_pledge_button, it) }

        this.viewModel.outputs.updatePledgeButtonIsEnabled()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { update_pledge_button.isEnabled = it }

        this.viewModel.outputs.updatePledgeProgressIsGone()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { ViewUtils.setGone(update_pledge_button_progress, it) }

        this.viewModel.outputs.showUpdatePledgeError()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { snackbar(pledge_content, getString(R.string.general_error_something_wrong)).show() }

        this.viewModel.outputs.showUpdatePledgeSuccess()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { (activity as PledgeDelegate?)?.pledgeSuccessfullyUpdated() }

        this.viewModel.outputs.showUpdatePaymentError()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { snackbar(pledge_content, getString(R.string.general_error_something_wrong)).show() }

        this.viewModel.outputs.showUpdatePaymentSuccess()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { (activity as PledgeDelegate?)?.pledgePaymentSuccessfullyUpdated() }

        this.viewModel.outputs.pledgeButtonIsEnabled()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { enablePledgeButton(it) }

        this.viewModel.outputs.continueButtonIsEnabled()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { continue_to_tout.isEnabled = it }

        pledge_amount.setOnTouchListener { _, _ ->
            pledge_amount.post {
                pledge_root.smoothScrollTo(0, relativeTop(pledge_amount_label, pledge_root))
                pledge_amount.requestFocus()
            }
            false
        }

        shipping_rules.setOnTouchListener { _, _ ->
            shipping_rules_label.post {
                pledge_root.smoothScrollTo(0, relativeTop(shipping_rules_label, pledge_root))
                shipping_rules.requestFocus()
                shipping_rules.showDropDown()
            }
            false
        }

        continue_to_tout.setOnClickListener {
            this.viewModel.inputs.continueButtonClicked()
        }

        decrease_pledge.setOnClickListener {
            this.viewModel.inputs.decreasePledgeButtonClicked()
        }

        increase_pledge.setOnClickListener {
            this.viewModel.inputs.increasePledgeButtonClicked()
        }

        RxView.clicks(update_pledge_button)
                .compose(bindToLifecycle())
                .subscribe { this.viewModel.inputs.updatePledgeButtonClicked() }
    }

    private fun enablePledgeButton(enabled: Boolean) {
        val rewardCardAdapter = cards_recycler.adapter as RewardCardAdapter
        rewardCardAdapter.setPledgeEnabled(enabled)
    }

    override fun onDetach() {
        super.onDetach()
        cards_recycler?.adapter = null
    }

    override fun addNewCardButtonClicked() {
        this.viewModel.inputs.newCardButtonClicked()
    }

    fun backPressed() {
        this.viewModel.inputs.backPressed()
    }

    fun cardAdded(storedCard: StoredCard) {
        this.viewModel.inputs.cardSaved(storedCard)
    }

    override fun closePledgeButtonClicked(position: Int) {
        this.viewModel.inputs.closeCardButtonClicked(position)
    }

    override fun pledgeButtonClicked(id: String) {
        this.viewModel.inputs.pledgeButtonClicked(id)
    }

    override fun ruleSelected(rule: ShippingRule) {
        this.viewModel.inputs.shippingRuleSelected(rule)
        activity?.hideKeyboard()
        shipping_rules.clearFocus()
    }

    override fun selectCardButtonClicked(position: Int) {
        this.viewModel.inputs.selectCardButtonClicked(position)
    }

    private fun configureUIBySnapshotVisibility(gone: Boolean) {
        val visibility = if (gone) View.GONE else View.VISIBLE
        setVisibility(visibility, reward_snapshot, reward_to_copy, expand_icon_container)
        ViewUtils.setInvisible(pledge_root, !gone)
        pledge_background.alpha = if (gone) 1f else 0f
    }

    private fun displayShippingRules(shippingRules: List<ShippingRule>, project: Project) {
        shipping_rules.isEnabled = true
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
            pledge_symbol_start.text = symbol
            pledge_symbol_end.text = null
        } else {
            pledge_symbol_start.text = null
            pledge_symbol_end.text = symbol
        }
    }

    private fun setPledgeMinimumText(minimumAmount: String) {
        val ksString = this.viewModel.environment.ksString()
        pledge_minimum.text = ksString.format(getString(R.string.The_minimum_pledge_is_min_pledge), "min_pledge", minimumAmount)
    }

    private fun setPlusTextView(textView: TextView, localizedAmount: CharSequence) {
        val ksString = this.viewModel.environment.ksString()
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
        cards_recycler.layoutManager = FreezeLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        cards_recycler.adapter = RewardCardAdapter(this)
        cards_recycler.addItemDecoration(RewardCardItemDecoration(resources.getDimensionPixelSize(R.dimen.grid_3_half)))
    }

    private fun setUpShippingAdapter() {
        context?.let {
            adapter = ShippingRulesAdapter(it, R.layout.item_shipping_rule, arrayListOf(), this)
            shipping_rules.setAdapter(adapter)
        }
    }

    private fun setVisibility(visibility: Int, vararg views: View) {
        context?.let {
            for (view in views) {
                view.visibility = visibility
            }
        }
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
        total_amount_conversion.text = (currencyConversionString?.let {
            this.viewModel.environment.ksString().format(it, "reward_amount", amount)
        })
    }

    private fun setHtmlStrings(baseUrl: String) {
        val termsOfUseUrl = UrlUtils.appendPath(baseUrl, HelpActivity.TERMS_OF_USE)
        val cookiePolicyUrl = UrlUtils.appendPath(baseUrl, HelpActivity.COOKIES)
        val privacyPolicyUrl = UrlUtils.appendPath(baseUrl, HelpActivity.PRIVACY)

        val ksString = (activity?.applicationContext as KSApplication).component().environment().ksString()
        val byPledgingYouAgree = getString(R.string.By_pledging_you_agree_to_Kickstarters_Terms_of_Use_Privacy_Policy_and_Cookie_Policy)

        val agreementWithUrls = ksString.format(byPledgingYouAgree, "terms_of_use_link", termsOfUseUrl,
                "privacy_policy_link", privacyPolicyUrl, "cookie_policy_link", cookiePolicyUrl)

        setClickableHtml(agreementWithUrls, pledge_agreement)

        val trustUrl = UrlUtils.appendPath(baseUrl, "trust")

        val kickstarterIsNotAStore = getString(R.string.Kickstarter_is_not_a_store_Its_a_way_to_bring_creative_projects_to_life_Learn_more_about_accountability)
        val accountabilityWithUrls = ksString.format(kickstarterIsNotAStore, "trust_link", trustUrl)

        setClickableHtml(accountabilityWithUrls, accountability)
    }

    private fun updatePledgeCardState(positionAndCardState: Pair<Int, CardState>) {
        val position = positionAndCardState.first
        val cardState = positionAndCardState.second
        val rewardCardAdapter = cards_recycler.adapter as RewardCardAdapter

        val freezeLinearLayoutManager = cards_recycler.layoutManager as FreezeLinearLayoutManager
        if (cardState == CardState.SELECT) {
            rewardCardAdapter.resetPledgePosition(position)
            freezeLinearLayoutManager.setFrozen(false)
        } else {
            if (cardState == CardState.PLEDGE) {
                rewardCardAdapter.setPledgePosition(position)
            } else {
                rewardCardAdapter.setLoadingPosition(position)
            }
            cards_recycler.scrollToPosition(position)
            freezeLinearLayoutManager.setFrozen(true)
        }
    }

    //Reward card animation helper methods
    private fun revealPledgeSection(pledgeData: PledgeData) {
        pledgeData.rewardScreenLocation?.let {
            setInitialViewStates(pledgeData)
            startPledgeAnimatorSet(true)
        }
    }

    private fun hidePledgeSection() {
        this@PledgeFragment.animDuration = this@PledgeFragment.defaultAnimationDuration
        startPledgeAnimatorSet(false)
    }

    private fun startPledgeAnimatorSet(reveal: Boolean) {
        val initMarginX: Float
        val initMarginY: Float
        val finalMarginX: Float
        val finalMarginY: Float
        val initHeight: Float
        val initWidth: Float
        val finalHeight: Float
        val finalWidth: Float
        val initY: Float
        val finalY: Float

        val location = arguments?.getSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION) as ScreenLocation
        val margin = this.resources.getDimensionPixelSize(R.dimen.activity_vertical_margin).toFloat()
        val miniRewardWidth = max(pledge_root.width / 3, this.resources.getDimensionPixelSize(R.dimen.mini_reward_width)).toFloat()
        val miniRewardHeight = getMiniRewardHeight(miniRewardWidth, location)

        if (reveal) {
            initMarginX = location.x
            initMarginY = location.y
            finalMarginX = margin
            finalMarginY = margin
            initWidth = location.width
            initHeight = location.height
            finalWidth = miniRewardWidth
            finalHeight = miniRewardHeight
            initY = pledge_root.height.toFloat()
            finalY = 0f
            setDeliveryParams(miniRewardWidth, margin)
        } else {
            initMarginX = margin
            initMarginY = margin
            finalMarginX = location.x
            finalMarginY = location.y
            initWidth = miniRewardWidth
            initHeight = miniRewardHeight
            finalWidth = location.width
            finalHeight = location.height
            initY = 0f
            finalY = pledge_root.height.toFloat()
        }

        val (startMargin, topMargin) = margin.let {
            getMarginLeftAnimator(initMarginX, finalMarginX) to
                    getMarginTopAnimator(initMarginY, finalMarginY)
        }

        val (width, height) = location.let {
            getWidthAnimator(initWidth, finalWidth) to
                    getHeightAnimator(initHeight, finalHeight)
        }

        val detailsY = getYAnimator(initY, finalY)

        if (reveal) {
            val expandRewardClickListener = View.OnClickListener { v ->
                if (!width.isRunning) {
                    v?.setOnClickListener(null)
                    this.viewModel.inputs.miniRewardClicked()
                }
            }
            reward_snapshot.setOnClickListener(expandRewardClickListener)
            expand_icon_container.setOnClickListener(expandRewardClickListener)
        } else {
            width.addUpdateListener {
                if (it.animatedFraction == 1f) {
                    this@PledgeFragment.fragmentManager?.popBackStack()
                }
            }
        }

        AnimatorSet().apply {
            playTogether(width, height, startMargin, topMargin, detailsY)
            this.interpolator = FastOutSlowInInterpolator()
            this.duration = animDuration
            start()
        }

    }

    private fun getHeightAnimator(initialValue: Float, finalValue: Float) =
            ValueAnimator.ofFloat(initialValue, finalValue).apply {
                addUpdateListener {
                    val newParams = reward_snapshot?.layoutParams as CoordinatorLayout.LayoutParams?
                    val newHeight = it.animatedValue as Float
                    newParams?.height = newHeight.toInt()
                    reward_snapshot?.layoutParams = newParams
                }
            }

    private fun getMarginLeftAnimator(initialValue: Float, finalValue: Float) =
            ValueAnimator.ofFloat(initialValue, finalValue).apply {
                addUpdateListener {
                    val newParams = reward_snapshot?.layoutParams as CoordinatorLayout.LayoutParams?
                    val newMargin = it.animatedValue as Float
                    newParams?.leftMargin = newMargin.toInt()
                    reward_snapshot?.layoutParams = newParams
                }
            }

    private fun getMarginTopAnimator(initialValue: Float, finalValue: Float): ValueAnimator =
            ValueAnimator.ofFloat(initialValue, finalValue).apply {
                addUpdateListener {
                    val newParams = reward_snapshot?.layoutParams as CoordinatorLayout.LayoutParams?
                    val newMargin = it.animatedValue as Float
                    newParams?.topMargin = newMargin.toInt()
                    reward_snapshot?.layoutParams = newParams
                }
            }

    private fun getMiniRewardHeight(miniRewardWidth: Float, location: ScreenLocation): Float {
        val scale = miniRewardWidth / location.width
        val scaledHeight = (location.height * scale).toInt()
        return min(resources.getDimensionPixelSize(R.dimen.mini_reward_height), scaledHeight).toFloat()
    }

    private fun getWidthAnimator(initialValue: Float, finalValue: Float) =
            ValueAnimator.ofFloat(initialValue, finalValue).apply {
                addUpdateListener {
                    val newParams = reward_snapshot?.layoutParams as CoordinatorLayout.LayoutParams?
                    val newWidth = it.animatedValue as Float
                    newParams?.width = newWidth.toInt()
                    reward_snapshot?.layoutParams = newParams
                    expand_icon_container?.alpha = if (finalValue < initialValue) animatedFraction else 1 - animatedFraction
                }
            }

    private fun getYAnimator(initialValue: Float, finalValue: Float) =
            ObjectAnimator.ofFloat(pledge_details, View.Y, initialValue, finalValue).apply {
                addUpdateListener {
                    val animatedFraction = it.animatedFraction
                    pledge_details?.alpha = if (finalValue == 0f) animatedFraction else 1 - animatedFraction
                    pledge_background?.alpha = if (finalValue == 0f) animatedFraction else 1 - animatedFraction
                }
            }

    private fun positionRewardSnapshot(location: ScreenLocation, reward: Reward, project: Project) {
        val rewardParams = reward_snapshot.layoutParams as CoordinatorLayout.LayoutParams
        rewardParams.leftMargin = location.x.toInt()
        rewardParams.topMargin = location.y.toInt()
        rewardParams.height = location.height.toInt()
        rewardParams.width = location.width.toInt()
        reward_snapshot.layoutParams = rewardParams
        reward_snapshot.pivotX = 0f
        reward_snapshot.pivotY = 0f

        val rewardViewHolder = NativeCheckoutRewardViewHolder(reward_to_copy, null)
        rewardViewHolder.bindData(Pair(project, reward))

        reward_to_copy.post {
            pledge_root.visibility = View.VISIBLE
            val bitmap = ViewUtils.getBitmap(reward_to_copy, location.width.toInt(), location.height.toInt())
            reward_snapshot.setImageBitmap(bitmap)
            reward_snapshot.requestFocus()
            reward_to_copy.visibility = View.GONE
        }
    }

    private fun setDeliveryParams(miniRewardWidth: Float, margin: Float) {
        val deliveryParams = (delivery.layoutParams as LinearLayout.LayoutParams).apply {
            marginStart = (miniRewardWidth + margin).toInt()
        }
        delivery.layoutParams = deliveryParams
    }

    private fun setInitialViewStates(pledgeData: PledgeData) {
        pledgeData.rewardScreenLocation?.let { positionRewardSnapshot(it, pledgeData.reward, pledgeData.project) }
        pledge_details.y = pledge_root.height.toFloat()
    }

    companion object {

        fun newInstance(pledgeData: PledgeData, pledgeReason: PledgeReason): PledgeFragment {
            val fragment = PledgeFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.PLEDGE_REWARD, pledgeData.reward)
            argument.putParcelable(ArgumentsKey.PLEDGE_PROJECT, pledgeData.project)
            argument.putSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION, pledgeData.rewardScreenLocation)
            argument.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, pledgeReason)
            fragment.arguments = argument
            return fragment
        }
    }
}
