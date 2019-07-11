package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.RewardDecoration
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.NativeCheckoutRewardsAdapter
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.viewmodels.RewardFragmentViewModel
import kotlinx.android.synthetic.main.fragment_rewards.*

@RequiresFragmentViewModel(RewardFragmentViewModel.ViewModel::class)
class RewardsFragment : BaseFragment<RewardFragmentViewModel.ViewModel>(), NativeCheckoutRewardsAdapter.Delegate {

    private var rewardsAdapter = NativeCheckoutRewardsAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_rewards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        this.viewModel.outputs.project()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { rewardsAdapter.populateRewards(it) }

        this.viewModel.outputs.backedRewardPosition()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { scrollToReward(it) }

        this.viewModel.outputs.showPledgeFragment()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { showPledgeFragment(it) }

    }

    private fun scrollToReward(position: Int) {
        if (position != 0) {
            val recyclerWidth = (rewards_recycler?.width ?: 0)
            val linearLayoutManager = rewards_recycler?.layoutManager as LinearLayoutManager
            val rewardWidth = resources.getDimensionPixelSize(R.dimen.item_reward_width)
            val rewardMargin = resources.getDimensionPixelSize(R.dimen.reward_margin)
            val center = (recyclerWidth - rewardWidth - rewardMargin) / 2
            linearLayoutManager.scrollToPositionWithOffset(position, center)
        }
    }

    override fun onDetach() {
        super.onDetach()
        rewards_recycler?.adapter = null
    }

    override fun rewardClicked(screenLocation: ScreenLocation, reward: Reward) {
        this.viewModel.inputs.rewardClicked(screenLocation, reward)
    }

    fun takeProject(project: Project) {
        this.viewModel.inputs.project(project)
    }

    private fun addItemDecorator() {
        val radius = resources.getDimensionPixelSize(R.dimen.circle_radius).toFloat()
        val inactiveColor = ContextCompat.getColor(rewards_recycler.context, R.color.ksr_dark_grey_400)
        val activeColor = ContextCompat.getColor(rewards_recycler.context, R.color.ksr_soft_black)
        val margin = resources.getDimension(R.dimen.reward_margin).toInt()
        val padding = radius * 2
        rewards_recycler.addItemDecoration(RewardDecoration(margin, activeColor, inactiveColor, radius, padding))
    }

    private fun setupRecyclerView() {
        rewards_recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rewards_recycler.adapter = rewardsAdapter
        addItemDecorator()
    }

    private fun showPledgeFragment(pledgeData: PledgeData) {
        if (this.fragmentManager?.findFragmentByTag(PledgeFragment::class.java.simpleName) == null) {
            val pledgeFragment = PledgeFragment.newInstance(pledgeData)
            this.fragmentManager?.beginTransaction()
                    ?.add(R.id.fragment_container,
                            pledgeFragment,
                            PledgeFragment::class.java.simpleName)
                    ?.addToBackStack(PledgeFragment::class.java.simpleName)
                    ?.commit()
        }
    }
}
