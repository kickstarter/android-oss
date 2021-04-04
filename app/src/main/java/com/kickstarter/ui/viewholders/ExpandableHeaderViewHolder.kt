package com.kickstarter.ui.viewholders

import android.util.Pair
import com.kickstarter.databinding.ExpandableHeaderItemBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.viewmodels.ExpandableHeaderViewHolderViewModel

class ExpandableHeaderViewHolder(private val binding: ExpandableHeaderItemBinding) : KSViewHolder(binding.root) {
    private var viewModel = ExpandableHeaderViewHolderViewModel.ViewModel(environment())

    init {
        this.viewModel.outputs.titleForSummary()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                this.binding.pledgeHeaderItemTitle.text = it
            }

        this.viewModel.outputs.amountForSummary()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                this.binding.pledgeHeaderItemAmount.text = it
            }
    }

    override fun bindData(data: Any?) {
        (data as? Pair<Project, Reward>)?.let {
            this.viewModel.configureWith(it)
        }
    }
}
