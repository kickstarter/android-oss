package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kickstarter.R
import com.kickstarter.databinding.DashboardReferrerBreakdownLayoutBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope
import com.kickstarter.viewmodels.CreatorDashboardReferrerBreakdownHolderViewModel

class CreatorDashboardReferrerBreakdownViewHolder(private val binding: DashboardReferrerBreakdownLayoutBinding) :
    KSViewHolder(binding.root) {
    private val viewModel = CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel(environment())

    private val ksCurrency = environment().ksCurrency()
    init {
        viewModel.outputs.breakdownViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.referrerBreakdownChartLayout))

        viewModel.outputs.emptyViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.dashboardReferrerBreakdownEmptyTextView))

        viewModel.outputs.customReferrerPercent()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setReferrerWidth(it, binding.pledgedViaCustomBar, binding.pledgedViaCustomIndicator) }

        viewModel.outputs.customReferrerPercentText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.percentViaCustomTextView.text = it }

        viewModel.outputs.externalReferrerPercent()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setReferrerWidth(it, binding.pledgedViaExternalBar, binding.pledgedViaExternalIndicator) }

        viewModel.outputs.externalReferrerPercent()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { flipIndicatorIfStatsOffScreen(binding.pledgedViaExternalIndicator, binding.pledgedViaExternal) }

        viewModel.outputs.externalReferrerPercentText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.percentViaExternalTextView.text = it }

        viewModel.outputs.kickstarterReferrerPercent()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setReferrerWidth(it, binding.pledgedViaKickstarterBar, binding.pledgedViaKickstarterIndicator) }

        viewModel.outputs.kickstarterReferrerPercentText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.percentViaKickstarterTextView.text = it }

        viewModel.outputs.pledgedViaCustomLayoutIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { hideReferrer(it, binding.pledgedViaCustom, binding.pledgedViaCustomBar, binding.pledgedViaCustomIndicator) }

        viewModel.outputs.pledgedViaExternalLayoutIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { hideReferrer(it, binding.pledgedViaExternal, binding.pledgedViaExternalBar, binding.pledgedViaExternalIndicator) }

        viewModel.outputs.pledgedViaKickstarterLayoutIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { hideReferrer(it, binding.pledgedViaKickstarter, binding.pledgedViaKickstarterBar, binding.pledgedViaKickstarterIndicator) }

        viewModel.outputs.projectAndCustomReferrerPledgedAmount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setAmountPledgedTextViewText(it, binding.amountPledgedViaCustomTextView) }

        viewModel.outputs.projectAndExternalReferrerPledgedAmount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setAmountPledgedTextViewText(it, binding.amountPledgedViaExternalTextView) }
        viewModel.outputs.projectAndKickstarterReferrerPledgedAmount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setAmountPledgedTextViewText(it, binding.amountPledgedViaKickstarterTextView) }
    }
    private fun setReferrerWidth(percent: Float, bar: View, indicator: View) {
        val barLayoutParams = bar.layoutParams as ConstraintLayout.LayoutParams
        barLayoutParams.horizontalWeight = percent
        bar.layoutParams = barLayoutParams
        adjustIndicatorMarginForShortBar(bar, indicator)
    }

    private fun adjustIndicatorMarginForShortBar(bar: View, indicator: View) {
        bar.post {
            if (bar.measuredWidth < context().resources.getDimension(R.dimen.grid_3)) {
                val indicatorLayoutParams = indicator.layoutParams as ConstraintLayout.LayoutParams
                indicatorLayoutParams.startToStart = bar.id
                indicatorLayoutParams.endToEnd = bar.id
                indicator.layoutParams = indicatorLayoutParams
            }
        }
    }

    private fun flipIndicatorIfStatsOffScreen(indicator: View, stats: View) {
        stats.post {
            val leftVisibleEdgeOfBreakdownView = binding.referrerBreakdownChartLayout.left + binding.referrerBreakdownChartLayout.paddingLeft
            if (stats.left < leftVisibleEdgeOfBreakdownView) {
                indicator.scaleX = -1f
                val indicatorLayoutParams = indicator.layoutParams as ConstraintLayout.LayoutParams
                indicatorLayoutParams.marginStart = context().resources.getDimension(R.dimen.grid_3).toInt()
                indicator.layoutParams = indicatorLayoutParams
                val statsLayoutParams = stats.layoutParams as ConstraintLayout.LayoutParams
                statsLayoutParams.startToEnd = indicator.id
                statsLayoutParams.marginStart = context().resources.getDimension(R.dimen.grid_1).toInt()
                statsLayoutParams.endToStart = ConstraintLayout.LayoutParams.UNSET
                stats.layoutParams = statsLayoutParams
            }
        }
    }

    private fun hideReferrer(gone: Boolean, layout: View, bar: View, indicator: View) {
        ViewUtils.setGone(layout, gone)
        ViewUtils.setGone(bar, gone)
        ViewUtils.setGone(indicator, gone)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {

        val projectAndStats = ObjectUtils.requireNonNull(data as? Pair<Project, ProjectStatsEnvelope>?)
        viewModel.inputs.projectAndStatsInput(projectAndStats as Pair<Project, ProjectStatsEnvelope>?)
    }

    private fun setAmountPledgedTextViewText(projectAndAmount: Pair<Project, Float>, textview: TextView) {
        val amountString = ksCurrency.format(projectAndAmount.second.toDouble(), projectAndAmount.first)
        textview.text = amountString
    }
}
