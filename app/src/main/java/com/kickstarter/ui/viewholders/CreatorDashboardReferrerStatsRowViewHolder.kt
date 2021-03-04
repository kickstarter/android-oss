package com.kickstarter.ui.viewholders

import android.util.Pair
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.kickstarter.databinding.DashboardReferrerStatsRowViewBinding
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.ReferrerStats
import com.kickstarter.viewmodels.CreatorDashboardReferrerStatsRowHolderViewModel

class CreatorDashboardReferrerStatsRowViewHolder(
    private val binding: DashboardReferrerStatsRowViewBinding
) : KSViewHolder(binding.root) {
    private val viewModel = CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel(environment())
    private val ksCurrency: KSCurrency = environment().ksCurrency()

    init {
        viewModel.outputs.projectAndPledgedForReferrer()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setPledgedColumnValue(it) }
        viewModel.outputs.referrerBackerCount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.referrerBackerCountTextView.text = it }
        viewModel.outputs.referrerSourceColorId()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setReferrerCircleColor(it) }
        viewModel.outputs.referrerSourceName()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.referrerSourceTextView.text = it }
    }
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndReferrerStats = ObjectUtils.requireNonNull(data as Pair<Project, ReferrerStats>?)
        viewModel.inputs.projectAndReferrerStatsInput(projectAndReferrerStats)
    }

    private fun setPledgedColumnValue(projectAndPledgedForReferrer: Pair<Project, Float>) {
        val goalString = ksCurrency.format(projectAndPledgedForReferrer.second.toDouble(), projectAndPledgedForReferrer.first)
        binding.amountPledgedForReferrerTextView.text = goalString
    }

    private fun setReferrerCircleColor(referrerCircleColorResourceId: Int) {
        val color = ContextCompat.getColor(context(), referrerCircleColorResourceId)
        DrawableCompat.setTint(binding.referrerSourceCircle.drawable, color)
    }
}
