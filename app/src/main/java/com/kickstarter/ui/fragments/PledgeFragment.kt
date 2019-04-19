package com.kickstarter.ui.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
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
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.activities.NewCardActivity
import com.kickstarter.ui.adapters.RewardCardAdapter
import com.kickstarter.ui.adapters.ShippingRulesAdapter
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.itemdecorations.RewardCardItemDecoration
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

        this.viewModel.outputs.startNewCardActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    startActivityForResult(Intent(this.context, NewCardActivity::class.java),
                            ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD)
                }

        this.viewModel.outputs.shippingRulesAndProject()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .filter { ObjectUtils.isNotNull(context) }
                .subscribe { displayShippingRules(it.first, it.second) }

        this.viewModel.outputs.shippingSelection()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { shipping_rules.setText(it.toString()) }

        this.viewModel.outputs.shippingAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { shipping_amount.text = it }

        this.viewModel.outputs.totalAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { total_amount.text = it }

        shipping_rules.setOnClickListener { shipping_rules.showDropDown() }

    }

    private fun setUpShippingAdapter() {
        //todo: add proper ViewHolder and ViewModel, we're going to need a KSArrayAdapter

        adapter = ShippingRulesAdapter(context!!, R.layout.item_shipping_rule, arrayListOf(), this)
        shipping_rules.setAdapter(adapter)



//        shipping_rules.setAdapter(object : ArrayAdapter<ShippingRule>(this.context,
//                android.R.layout.simple_dropdown_item_1line, arrayListOf()) {
//            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
//                var view = convertView
//                if (view == null) {
//                    view = LayoutInflater.from(this.context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
//                }
//
//                val item = getItem(position)
//
//                val displayableName = item?.location()?.displayableName()
//                //todo: get this amount from KS currency
//                val cost = item?.cost()
//                (view as TextView).text = "$displayableName +($cost)"
//
//                return view
//            }
//        })
//        val itemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
//            this@PledgeFragment.viewModel.inputs.shippingRule(parent?.adapter?.getItem(position) as ShippingRule)
//        }
//        shipping_rules.onItemClickListener = itemClickListener
    }

    private fun setUpCardsAdapter() {
        cards_recycler.layoutManager = FreezeLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        cards_recycler.adapter = RewardCardAdapter(this)
        cards_recycler.addItemDecoration(RewardCardItemDecoration(resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)))
    }

    private fun displayShippingRules(shippingRules: List<ShippingRule>, project: Project) {
        shipping_rules.isEnabled = true
//        val adapter = shipping_rules.adapter as ArrayAdapter<ShippingRule>
//        adapter.clear()
//        adapter.addAll(shippingRules)
        adapter.populateShippingRules(shippingRules, project)
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
    override fun ruleSelected(rule: ShippingRule): AdapterView.OnItemClickListener {
        this.viewModel.inputs.shippingRule(rule)

        shipping_rules.onItemClickListener = ruleSelected(rule)
       return shipping_rules.onItemClickListener
    }


    override fun selectCardButtonClicked(position: Int) {
        this.viewModel.inputs.selectCardButtonClicked(position)
    }

    private fun showPledgeSection(rewardAndLocation: Pair<Reward, ScreenLocation>) {
        val location = rewardAndLocation.second
        val reward = rewardAndLocation.first
        setInitialViewStates(location, reward)
        startPledgeAnimatorSet(true, location)
    }

    private fun positionRewardSnapshot(location: ScreenLocation, reward: Reward) {
        val rewardParams = reward_snapshot.layoutParams as FrameLayout.LayoutParams
        rewardParams.marginStart = location.x.toInt()
        rewardParams.topMargin = location.y.toInt()
        rewardParams.height = location.height.toInt()
        rewardParams.width = location.width.toInt()
        reward_snapshot.layoutParams = rewardParams
        reward_snapshot.pivotX = 0f
        reward_snapshot.pivotY = 0f

//        todo: blocked until rewards work is available
//        val rewardViewHolder = RewardsAdapter.RewardViewHolder(reward_to_copy)
//        rewardViewHolder.bind(reward)

        reward_to_copy.post {
            pledge_root.visibility = View.VISIBLE
//            reward_snapshot.setImageBitmap(ViewUtils.getBitmap(reward_to_copy, location.width, location.width))
            reward_to_copy.visibility = View.GONE
        }
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
            getMarginStartAnimator(initMarginX, finalMarginX) to
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

    private fun getMarginStartAnimator(initialValue: Float, finalValue: Float) =
            ValueAnimator.ofFloat(initialValue, finalValue).apply {
                addUpdateListener {
                    val newParams = reward_snapshot?.layoutParams as FrameLayout.LayoutParams?
                    val newMargin = it.animatedValue as Float
                    newParams?.marginStart = newMargin.toInt()
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

    private fun setInitialViewStates(location: ScreenLocation, reward: Reward) {
        positionRewardSnapshot(location, reward)
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
