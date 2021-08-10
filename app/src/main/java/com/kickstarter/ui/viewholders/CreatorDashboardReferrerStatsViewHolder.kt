package com.kickstarter.ui.viewholders

import android.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.DashboardReferrerStatsViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.ReferrerStats
import com.kickstarter.ui.adapters.CreatorDashboardReferrerStatsAdapter
import com.kickstarter.viewmodels.CreatorDashboardReferrerStatsHolderViewModel

class CreatorDashboardReferrerStatsViewHolder(private val binding: DashboardReferrerStatsViewBinding) :
    KSViewHolder(binding.root) {

    private val viewModel = CreatorDashboardReferrerStatsHolderViewModel.ViewModel(environment())

    init {
        val referrerStatsAdapter = CreatorDashboardReferrerStatsAdapter()

        binding.dashboardReferrerStatsRecyclerView.apply {
            adapter = referrerStatsAdapter
            layoutManager = LinearLayoutManager(context())
        }
        viewModel.outputs.projectAndReferrerStats()
            .compose(bindToLifecycle())
            .compose<Pair<Project?, List<ReferrerStats?>?>>(Transformers.observeForUI())
            .subscribe { referrerStatsAdapter.takeProjectAndReferrerStats(it) }
        viewModel.outputs.referrerStatsListIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { toggleRecyclerViewAndEmptyStateVisibility(it) }
        viewModel.outputs.referrersTitleIsTopTen()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setTitleCopy(it) }
    }
    private fun setTitleCopy(shouldTitleHaveIsTen: Boolean) {
        binding.dashboardReferrerTitle.setText(if (shouldTitleHaveIsTen) R.string.Top_ten_pledge_sources else R.string.Top_pledge_sources)
    }

    private fun toggleRecyclerViewAndEmptyStateVisibility(gone: Boolean) {
        ViewUtils.setGone(binding.dashboardReferrerStatsRecyclerView, gone)
        ViewUtils.setGone(binding.dashboardReferrerStatsEmptyTextView, !gone)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndReferrerStats = ObjectUtils.requireNonNull(data as Pair<Project, List<ReferrerStats>>?)
        viewModel.inputs.projectAndReferrerStatsInput(projectAndReferrerStats)
    }

    override fun destroy() {
        super.destroy()
        binding.dashboardReferrerStatsRecyclerView.adapter = null
    }
}
