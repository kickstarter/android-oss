package com.kickstarter.ui.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.FreezeLinearLayoutManager
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.activities.NewCardActivity
import com.kickstarter.ui.adapters.RewardCardAdapter
import com.kickstarter.ui.adapters.ShippingRulesAdapter
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.itemdecorations.RewardCardItemDecoration
import com.kickstarter.ui.viewholders.HorizontalRewardViewHolder
import com.kickstarter.ui.viewholders.RewardPledgeCardViewHolder
import com.kickstarter.viewmodels.PledgeFragmentViewModel
import kotlinx.android.synthetic.main.fragment_pledge.*

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

        this.animDuration = when (savedInstanceState) {
            null -> this.defaultAnimationDuration
            else -> 0L
        }

        this.viewModel.outputs.animateRewardCard()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { showPledgeSection(it) }

        this.viewModel.outputs.estimatedDelivery()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { pledge_estimated_delivery.text = it }

        this.viewModel.outputs.continueButtonIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(continue_to_tout, it) }

        this.viewModel.outputs.paymentContainerIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(payment_container, it) }

        this.viewModel.outputs.showPledgeCard()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { updatePledgeCardSelection(it) }

        this.viewModel.outputs.pledgeAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { pledge_amount.text = it }

        this.viewModel.outputs.cards()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { (cards_recycler.adapter as RewardCardAdapter).takeCards(it) }

        this.viewModel.outputs.startLoginToutActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { startActivity(Intent(this.context, LoginToutActivity::class.java)) }

        this.viewModel.outputs.startNewCardActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    startActivityForResult(Intent(this.context, NewCardActivity::class.java),
                            ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD)
                }

        this.viewModel.outputs.selectedShippingRule()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { shipping_rules.setText(it.toString()) }

        this.viewModel.outputs.shippingAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { shipping_amount.text = it }

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
                .subscribe { total_amount.text = it }

        shipping_rules.setOnClickListener { shipping_rules.showDropDown() }

        continue_to_tout.setOnClickListener {
            this.viewModel.inputs.continueButtonClicked()
        }
    }

    override fun onDetach() {
        super.onDetach()
        cards_recycler?.adapter = null
    }

    override fun addNewCardButtonClicked() {
        this.viewModel.inputs.newCardButtonClicked()
    }

    override fun closePledgeButtonClicked(position: Int) {
        this.viewModel.inputs.closeCardButtonClicked(position)
    }

    override fun pledgeButtonClicked(viewHolder: RewardPledgeCardViewHolder) {
        this.viewModel.inputs.pledgeButtonClicked()
    }
    override fun ruleSelected(rule: ShippingRule){
        this.viewModel.inputs.shippingRuleSelected(rule)
        shipping_rules.dismissDropDown()
        shipping_rules.clearFocus()
        shipping_rules?.let {
            val input = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            input.let { it?.hideSoftInputFromWindow(shipping_rules.windowToken, 0) }
        }
    }

    override fun selectCardButtonClicked(position: Int) {
        this.viewModel.inputs.selectCardButtonClicked(position)
    }

    private fun displayShippingRules(shippingRules: List<ShippingRule>, project: Project) {
        shipping_rules.isEnabled = true
        adapter.populateShippingRules(shippingRules, project)
    }

    private fun positionRewardSnapshot(pledgeData: PledgeData) {
        val location = pledgeData.rewardScreenLocation
        val reward = pledgeData.reward
        val project = pledgeData.project
        val rewardParams = reward_snapshot.layoutParams as FrameLayout.LayoutParams
        rewardParams.leftMargin = location.x.toInt()
        rewardParams.topMargin = location.y.toInt()
        rewardParams.height = location.height.toInt()
        rewardParams.width = location.width.toInt()
        reward_snapshot.layoutParams = rewardParams
        reward_snapshot.pivotX = 0f
        reward_snapshot.pivotY = 0f

        val rewardViewHolder = HorizontalRewardViewHolder(reward_to_copy, null)
        rewardViewHolder.bindData(Pair(project, reward))

        reward_to_copy.post {
            pledge_root.visibility = View.VISIBLE
            val bitmap = ViewUtils.getBitmap(reward_to_copy, location.width.toInt(), location.height.toInt())
            reward_snapshot.setImageBitmap(bitmap)
            reward_to_copy.visibility = View.GONE
        }
    }

    private fun setUpCardsAdapter() {
        cards_recycler.layoutManager = FreezeLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        cards_recycler.adapter = RewardCardAdapter(this)
        cards_recycler.addItemDecoration(RewardCardItemDecoration(resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)))
    }

    private fun setUpShippingAdapter() {
        adapter = ShippingRulesAdapter(context!!, R.layout.item_shipping_rule, arrayListOf(), this)
        shipping_rules.setAdapter(adapter)
    }

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
            reward_snapshot.setOnClickListener {
                if (!width.isRunning) {
                    it.setOnClickListener(null)
                    this.animDuration = this.defaultAnimationDuration
                    startPledgeAnimatorSet(false, location)
                }
            }
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
                    val newParams = reward_snapshot?.layoutParams as FrameLayout.LayoutParams?
                    val newHeight = it.animatedValue as Float
                    newParams?.height = newHeight.toInt()
                    reward_snapshot?.layoutParams = newParams
                }
            }

    private fun getMarginLeftAnimator(initialValue: Float, finalValue: Float) =
            ValueAnimator.ofFloat(initialValue, finalValue).apply {
                addUpdateListener {
                    val newParams = reward_snapshot?.layoutParams as FrameLayout.LayoutParams?
                    val newMargin = it.animatedValue as Float
                    newParams?.leftMargin = newMargin.toInt()
                    reward_snapshot?.layoutParams = newParams
                }
            }

    private fun getMarginTopAnimator(initialValue: Float, finalValue: Float): ValueAnimator =
            ValueAnimator.ofFloat(initialValue, finalValue).apply {
                addUpdateListener {
                    val newParams = reward_snapshot?.layoutParams as FrameLayout.LayoutParams?
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
                    val newParams = reward_snapshot?.layoutParams as FrameLayout.LayoutParams?
                    val newWidth = it.animatedValue as Float
                    newParams?.width = newWidth.toInt()
                    reward_snapshot?.layoutParams = newParams
                }
            }

    private fun getYAnimator(initialValue: Float, finalValue: Float) =
            ObjectAnimator.ofFloat(pledge_details, View.Y, initialValue, finalValue).apply {
                addUpdateListener {
                    val animatedFraction = it.animatedFraction
                    pledge_details?.alpha = if (finalValue == 0f) animatedFraction else 1 - animatedFraction
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

    private fun updatePledgeCardSelection(positionAndSelected: Pair<Int, Boolean>) {
        val position = positionAndSelected.first
        val selected = positionAndSelected.second
        val rewardCardAdapter = cards_recycler.adapter as RewardCardAdapter
        if (selected) {
            rewardCardAdapter.setSelectedPosition(position)
            cards_recycler.scrollToPosition(position)
        } else {
            rewardCardAdapter.resetSelectedPosition(position)
        }
        (cards_recycler.layoutManager as FreezeLinearLayoutManager).setFrozen(selected)
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
