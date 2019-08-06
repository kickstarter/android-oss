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
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.extensions.hideKeyboard
import com.kickstarter.extensions.onChange
import com.kickstarter.extensions.showSnackbar
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.FreezeLinearLayoutManager
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
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
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.itemdecorations.RewardCardItemDecoration
import com.kickstarter.ui.viewholders.NativeCheckoutRewardViewHolder
import com.kickstarter.viewmodels.PledgeFragmentViewModel
import kotlinx.android.synthetic.main.fragment_pledge.*
import kotlinx.android.synthetic.main.fragment_pledge_section_delivery.*
import kotlinx.android.synthetic.main.fragment_pledge_section_payment.*
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.*
import kotlinx.android.synthetic.main.fragment_pledge_section_shipping.*
import kotlinx.android.synthetic.main.fragment_pledge_section_total.*

@RequiresFragmentViewModel(PledgeFragmentViewModel.ViewModel::class)
class PledgeFragment : BaseFragment<PledgeFragmentViewModel.ViewModel>(), RewardCardAdapter.Delegate, ShippingRulesAdapter.Delegate {

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
                .subscribe { additional_pledge_amount.text = it }

        this.viewModel.outputs.additionalPledgeAmountIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(additional_pledge_amount_container, it) }

        this.viewModel.outputs.animateRewardCard()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { showPledgeSection(it) }

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

        this.viewModel.outputs.projectCurrencySymbol()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setCurrencySymbols(it) }

        this.viewModel.outputs.pledgeTextColor()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setTextColor(it, pledge_amount, pledge_symbol_start, pledge_symbol_end) }

        this.viewModel.outputs.totalTextColor()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setTextColor(it, total_amount, total_symbol_start, total_symbol_end) }

        this.viewModel.outputs.cards()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { (cards_recycler.adapter as RewardCardAdapter).takeCards(it) }

        this.viewModel.outputs.card()
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
                            ?.add(R.id.secondary_container, NewCardFragment.newInstance(true), NewCardFragment::class.java.simpleName)
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
                    setVisibility(View.VISIBLE, shipping_symbol_start, shipping_symbol_end)
                    shipping_amount.text = it
                }

        this.viewModel.outputs.shippingRulesAndProject()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .filter { ObjectUtils.isNotNull(context) }
                .subscribe { displayShippingRules(it.first, it.second) }

        this.viewModel.outputs.shippingRulesSectionIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    ViewUtils.setGone(shipping_rules_section_text_view, it)
                    ViewUtils.setGone(shipping_rules_row, it)
                }

        this.viewModel.outputs.totalAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    ViewUtils.setGone(total_amount_loading_view, true)
                    setVisibility(View.VISIBLE, total_symbol_start, total_symbol_end)
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
                .subscribe { activity?.showSnackbar(pledge_root, R.string.general_error_something_wrong) }

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

        this.viewModel.outputs.cancelPledgeButtonIsGone()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { ViewUtils.setGone(cancel_pledge_button, it) }

        this.viewModel.outputs.changePaymentMethodButtonIsGone()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { ViewUtils.setGone(change_payment_method_button, it) }

        this.viewModel.outputs.updatePledgeButtonIsGone()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { ViewUtils.setGone(update_pledge_button, it) }

        this.viewModel.outputs.totalContainerIsGone()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { ViewUtils.setGone(total, it) }

        this.viewModel.outputs.showCancelPledge()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe {
                    fragmentManager
                            ?.beginTransaction()
                            ?.setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                            ?.add(R.id.secondary_container, CancelPledgeFragment.newInstance(it), CancelPledgeFragment::class.java.simpleName)
                            ?.addToBackStack(CancelPledgeFragment::class.java.simpleName)
                            ?.commit()
                }

        this.viewModel.outputs.showMinimumWarning()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { showPledgeWarning(it) }

        pledge_amount.setOnTouchListener { _, _ ->
            pledge_amount.post {
                pledge_root.smoothScrollTo(0, relativeTop(pledge_amount_label, pledge_root))
                pledge_amount.requestFocus()
            }
            false
        }

        shipping_rules.setOnTouchListener { _, _ ->
            shipping_rules_section_text_view.post {
                pledge_root.smoothScrollTo(0, relativeTop(shipping_rules_section_text_view, pledge_root))
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

        cancel_pledge_button.setOnClickListener {
            this.viewModel.inputs.cancelPledgeButtonClicked()
        }
    }

    override fun onDetach() {
        super.onDetach()
        cards_recycler?.adapter = null
    }

    override fun addNewCardButtonClicked() {
        this.viewModel.inputs.newCardButtonClicked()
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
            shipping_symbol_start.text = symbol
            total_symbol_start.text = symbol
            pledge_symbol_end.text = null
            shipping_symbol_end.text = null
            total_symbol_end.text = null
        } else {
            pledge_symbol_start.text = null
            shipping_symbol_start.text = null
            total_symbol_start.text = null
            pledge_symbol_end.text = symbol
            shipping_symbol_end.text = symbol
            total_symbol_end.text = symbol
        }
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
        cards_recycler.addItemDecoration(RewardCardItemDecoration(resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)))
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
        val termsOfUseUrl = UrlUtils.buildUrl(baseUrl, HelpActivity.TERMS_OF_USE)
        val cookiePolicyUrl = UrlUtils.buildUrl(baseUrl, HelpActivity.COOKIES)
        val privacyPolicyUrl = UrlUtils.buildUrl(baseUrl, HelpActivity.PRIVACY)

        val ksString = (activity?.applicationContext as KSApplication).component().environment().ksString()
        val byPledgingYouAgree = getString(R.string.By_pledging_you_agree_to_Kickstarters_Terms_of_Use_Privacy_Policy_and_Cookie_Policy)

        val agreementWithUrls = ksString.format(byPledgingYouAgree, "terms_of_use_link", termsOfUseUrl,
                "privacy_policy_link", privacyPolicyUrl, "cookie_policy_link", cookiePolicyUrl)

        setClickableHtml(agreementWithUrls, pledge_agreement)

        val trustUrl = UrlUtils.buildUrl(baseUrl, "trust")

        val kickstarterIsNotAStore = getString(R.string.Kickstarter_is_not_a_store_Its_a_way_to_bring_creative_projects_to_life_Learn_more_about_accountability)
        val accountabilityWithUrls = ksString.format(kickstarterIsNotAStore, "trust_link", trustUrl)

        setClickableHtml(accountabilityWithUrls, accountability)
    }

    private fun showPledgeWarning(rewardMinimum: String) {
        context?.apply {
            val ksString = (this.applicationContext as KSApplication).component().environment().ksString()
            val message = ksString.format(getString(R.string.You_need_to_pledge_at_least_reward_minimum_for_this_reward),
                    "reward_minimum", rewardMinimum)

            val dialog = AlertDialog.Builder(this, R.style.Dialog)
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.general_alert_buttons_ok)) { dialog, _ ->  dialog.dismiss()}
                    .create()

            dialog.show()
        }
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
    private fun showPledgeSection(pledgeData: PledgeData) {
        setInitialViewStates(pledgeData)
        startPledgeAnimatorSet(true, pledgeData.rewardScreenLocation)
    }

    private fun startPledgeAnimatorSet(reveal: Boolean, location: ScreenLocation) {
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

        val margin = this.resources.getDimensionPixelSize(R.dimen.activity_vertical_margin).toFloat()
        val miniRewardWidth = Math.max(pledge_root.width / 3, this.resources.getDimensionPixelSize(R.dimen.mini_reward_width)).toFloat()
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
            val shrinkClickListener = View.OnClickListener { v ->
                if (!width.isRunning) {
                    v?.setOnClickListener(null)
                    this@PledgeFragment.animDuration = this@PledgeFragment.defaultAnimationDuration
                    startPledgeAnimatorSet(false, location)
                }
            }
            reward_snapshot.setOnClickListener(shrinkClickListener)
            expand_icon_container.setOnClickListener(shrinkClickListener)
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
        return Math.min(resources.getDimensionPixelSize(R.dimen.mini_reward_height), scaledHeight).toFloat()
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
                }
            }

    private fun positionRewardSnapshot(pledgeData: PledgeData) {
        val location = pledgeData.rewardScreenLocation
        val reward = pledgeData.reward
        val project = pledgeData.project
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
        positionRewardSnapshot(pledgeData)
        pledge_details.y = pledge_root.height.toFloat()
    }

    companion object {

        fun newInstance(pledgeData: PledgeData): PledgeFragment {
            val fragment = PledgeFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.PLEDGE_REWARD, pledgeData.reward)
            argument.putParcelable(ArgumentsKey.PLEDGE_PROJECT, pledgeData.project)
            argument.putSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION, pledgeData.rewardScreenLocation)
            fragment.arguments = argument
            return fragment
        }
    }
}
