package com.kickstarter.ui.viewholders

import android.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.DashboardRewardStatsViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.sentenceCase
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.RewardStats
import com.kickstarter.ui.adapters.CreatorDashboardRewardStatsAdapter
import com.kickstarter.viewmodels.CreatorDashboardRewardStatsHolderViewModel

class CreatorDashboardRewardStatsViewHolder(private val binding: DashboardRewardStatsViewBinding) :
    KSViewHolder(binding.root) {
    private val viewModel = CreatorDashboardRewardStatsHolderViewModel.ViewModel(environment())

    init {
        val rewardStatsAdapter = CreatorDashboardRewardStatsAdapter()

        binding.dashboardRewardStatsRecyclerView.apply {
            adapter = rewardStatsAdapter
            layoutManager = LinearLayoutManager(context())
        }
        viewModel.outputs.projectAndRewardStats()
            .compose(bindToLifecycle())
            .compose<Pair<Project?, List<RewardStats?>?>>(Transformers.observeForUI())
            .subscribe { rewardStatsAdapter.takeProjectAndStats(it) }
        viewModel.outputs.rewardsStatsListIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { toggleRecyclerViewAndEmptyStateVisibility(it) }
        viewModel.outputs.rewardsStatsTruncatedTextIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { ViewUtils.setGone(binding.dashboardRewardStatsTruncatedTextView, it) }
        viewModel.outputs.rewardsTitleIsTopTen()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setTitleCopy(it) }
    }

    private fun setTitleCopy(referrersTitleIsTopTen: Boolean) {
        val formattedTopRewards = context().getString(R.string.dashboard_graphs_rewards_top_rewards).sentenceCase()
        binding.dashboardRewardTitle.text = if (referrersTitleIsTopTen) context().getString(R.string.Top_ten_rewards) else formattedTopRewards
    }

    private fun toggleRecyclerViewAndEmptyStateVisibility(gone: Boolean) {
        ViewUtils.setGone(binding.dashboardRewardStatsRecyclerView, gone)
        ViewUtils.setGone(binding.dashboardRewardStatsEmptyTextView, !gone)
    }

    fun pledgedColumnTitleClicked() {
        viewModel.inputs.pledgedColumnTitleClicked()
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndRewardStats = ObjectUtils.requireNonNull(data as Pair<Project, List<RewardStats>>?)
        viewModel.inputs.projectAndRewardStatsInput(projectAndRewardStats)
        binding.dashboardRewardStatsPledgedView.setOnClickListener {
            pledgedColumnTitleClicked()
        }
    }

    override fun destroy() {
        super.destroy()
        binding.dashboardRewardStatsRecyclerView.adapter = null
    }
}
