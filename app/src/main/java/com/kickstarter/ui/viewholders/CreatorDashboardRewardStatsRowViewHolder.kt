package com.kickstarter.ui.viewholders

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.databinding.DashboardRewardStatsRowViewBinding
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.RewardStats
import com.kickstarter.viewmodels.DashboardRewardStatsRowHolderViewModel

class CreatorDashboardRewardStatsRowViewHolder(private val binding: DashboardRewardStatsRowViewBinding) :
    KSViewHolder(binding.root) {
    private val viewModel = DashboardRewardStatsRowHolderViewModel.ViewModel(environment())
    private val ksCurrency: KSCurrency = environment().ksCurrency()

    init {
        viewModel.outputs.percentageOfTotalPledged()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.percentagePledgedForTextView.text = it }
        viewModel.outputs.projectAndRewardPledged()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setPledgedColumnValue(it) }
        viewModel.outputs.rewardBackerCount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.rewardBackerCountTextView.text = it }
        viewModel.outputs.projectAndRewardMinimum()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setRewardMinimumText(it) }
    }
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndRewardStats = ObjectUtils.requireNonNull(data as Pair<Project, RewardStats>?)
        viewModel.inputs.projectAndRewardStats(projectAndRewardStats)
    }

    private fun setPledgedColumnValue(projectAndPledgedForReward: Pair<Project, Float>) {
        binding.amountPledgedForRewardTextView.text = ksCurrency.format(projectAndPledgedForReward.second.toDouble(), projectAndPledgedForReward.first)
    }

    private fun setRewardMinimumText(projectAndMinimumForReward: Pair<Project, Int>) {
        binding.rewardMinimumTextView.text = if (IntegerUtils.isZero(projectAndMinimumForReward.second))
            context().getString(R.string.dashboard_graphs_rewards_no_reward)
        else
            ksCurrency.format(projectAndMinimumForReward.second.toDouble(), projectAndMinimumForReward.first)
    }
}
