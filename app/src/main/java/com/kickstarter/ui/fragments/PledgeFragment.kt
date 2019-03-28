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
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.activities.NewCardActivity
import com.kickstarter.ui.adapters.RewardCardAdapter
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.itemdecorations.RewardCardItemDecoration
import com.kickstarter.ui.viewholders.RewardPledgeCardViewHolder
import com.kickstarter.viewmodels.PledgeFragmentViewModel
import kotlinx.android.synthetic.main.fragment_pledge.*

@RequiresFragmentViewModel(PledgeFragmentViewModel.ViewModel::class)
class PledgeFragment : BaseFragment<PledgeFragmentViewModel.ViewModel>(), RewardCardAdapter.Delegate {

    private val defaultAnimationDuration = 200L
    private var animDuration = defaultAnimationDuration

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

        cards_recycler.layoutManager = FreezeLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        cards_recycler.adapter = RewardCardAdapter(this)
        cards_recycler.addItemDecoration(RewardCardItemDecoration(resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)))

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
    }

    override fun onDetach() {
        super.onDetach()
        cards_recycler?.adapter = null
    }

    override fun closePledgeButtonClicked(position: Int) {
        this.viewModel.inputs.closeCardButtonClicked(position)
    }

    override fun pledgeButtonClicked(viewHolder: RewardPledgeCardViewHolder) {
        this.viewModel.inputs.pledgeButtonClicked()
    }

    override fun selectCardButtonClicked(position: Int) {
        this.viewModel.inputs.selectCardButtonClicked(position)
    }

    override fun addNewCardButtonClicked() {
        this.viewModel.inputs.newCardButtonClicked()
    }

    private fun updatePledgeCardSelection(positionAndSelected: Pair<Int, Boolean>) {
        val position = positionAndSelected.first
        val selected = positionAndSelected.second
        if (selected) {
            (cards_recycler.adapter as RewardCardAdapter).setSelectedPosition(position)
            cards_recycler.scrollToPosition(position)
        } else {
            (cards_recycler.adapter as RewardCardAdapter).resetSelectedPosition(position)
        }
        (cards_recycler.layoutManager as FreezeLinearLayoutManager).setFrozen(selected)
    }

    private fun showPledgeSection(rewardAndLocation: Pair<Reward, ScreenLocation>) {
        val location = rewardAndLocation.second
        val reward = rewardAndLocation.first
        setInitialViewStates(location, reward)
        revealPledgeSection(location)
    }

    private fun setInitialViewStates(location: ScreenLocation, reward: Reward) {
        positionRewardSnapshot(location, reward)

        setDeliveryHeight(location)

        pledge_details.y = pledge_root.height.toFloat()
    }

    private fun setDeliveryHeight(location: ScreenLocation) {
        val miniRewardWidth = this.resources.getDimensionPixelSize(R.dimen.mini_reward_width)
        val scaleX = miniRewardWidth.toFloat() / location.width
        val miniRewardHeight = location.height * scaleX
        delivery.measure(View.MeasureSpec.makeMeasureSpec(pledge_details.width, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(pledge_details.height, View.MeasureSpec.UNSPECIFIED))
        val targetHeight = delivery.measuredHeight
        val deliveryParams = delivery.layoutParams as LinearLayout.LayoutParams
        deliveryParams.height = Math.max(miniRewardHeight.toInt(), targetHeight)
        delivery.layoutParams = deliveryParams
    }

    private fun positionRewardSnapshot(location: ScreenLocation, reward: Reward) {
        val rewardParams = reward_snapshot.layoutParams as FrameLayout.LayoutParams
        rewardParams.marginStart = location.x.toInt()
        rewardParams.topMargin = location.y.toInt()
        rewardParams.height = location.height
        rewardParams.width = location.width
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

    private fun revealPledgeSection(location: ScreenLocation) {
        val initialMargin = this.resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)

        val slideRewardLeft = getRewardMarginAnimator(location.x.toInt(), initialMargin)

        val miniRewardWidth = this.resources.getDimensionPixelSize(R.dimen.mini_reward_width)
        val shrinkReward = getRewardSizeAnimator(location.width, miniRewardWidth, location)

        val slideDetailsUp = getDetailsYAnimator(pledge_root.height.toFloat(), 0f)

        reward_snapshot.setOnClickListener { view ->
            if (!shrinkReward.isRunning) {
                view.setOnClickListener(null)
                hidePledgeSection(location, initialMargin)
            }
        }

        startPledgeAnimatorSet(shrinkReward, slideRewardLeft, slideDetailsUp)
    }

    private fun hidePledgeSection(location: ScreenLocation, initialMargin: Int) {
        this.animDuration = this.defaultAnimationDuration
        val slideRewardRight = getRewardMarginAnimator(initialMargin, location.x.toInt())

        val expandReward = getRewardSizeAnimator(reward_snapshot.width, location.width, location).apply {
            addUpdateListener {
                if (it.animatedFraction == 1f) {
                    this@PledgeFragment.fragmentManager?.popBackStack()
                }
            }
        }

        val slideDetailsDown = getDetailsYAnimator(0f, pledge_root.height.toFloat())

        startPledgeAnimatorSet(expandReward, slideRewardRight, slideDetailsDown)
    }

    private fun getDetailsYAnimator(initialValue: Float, finalValue: Float): ObjectAnimator? {
        return ObjectAnimator.ofFloat(pledge_details, View.Y, initialValue, finalValue).apply {
            addUpdateListener {
                val animatedFraction = it.animatedFraction
                pledge_details?.alpha = if (finalValue == 0f) animatedFraction else 1 - animatedFraction
            }
        }
    }

    private fun getRewardSizeAnimator(initialValue: Int, finalValue: Int, location: ScreenLocation): ValueAnimator {
        return ValueAnimator.ofInt(initialValue, finalValue).apply {
            addUpdateListener {
                val newParams = reward_snapshot?.layoutParams as FrameLayout.LayoutParams?
                val newWidth = it.animatedValue as Int
                newParams?.width = newWidth
                val scaleX = newWidth / location.width.toFloat()

                newParams?.height = (scaleX * location.height).toInt()
                reward_snapshot?.layoutParams = newParams
            }
        }
    }

    private fun getRewardMarginAnimator(initialValue: Int, finalValue: Int): ValueAnimator {
        return ValueAnimator.ofInt(initialValue, finalValue).apply {
            addUpdateListener {
                val newParams = reward_snapshot?.layoutParams as FrameLayout.LayoutParams?
                val newMargin = it.animatedValue as Int
                newParams?.marginStart = newMargin
                reward_snapshot?.layoutParams = newParams
            }
        }
    }

    private fun startPledgeAnimatorSet(rewardSizeAnimator: ValueAnimator, rewardMarginAnimator: ValueAnimator, detailsYAnimator: ObjectAnimator?) {
        AnimatorSet().apply {
            this.interpolator = FastOutSlowInInterpolator()
            this.duration = animDuration
            playTogether(rewardSizeAnimator, rewardMarginAnimator, detailsYAnimator)
            start()
        }
    }

    companion object {

        fun newInstance(location: ScreenLocation, reward: Reward, project: Project): PledgeFragment {
            val fragment = PledgeFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.PLEDGE_REWARD, reward)
            argument.putParcelable(ArgumentsKey.PLEDGE_PROJECT, project)
            argument.putSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION, location)
            fragment.arguments = argument
            return fragment
        }
    }

}
