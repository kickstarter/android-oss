package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.viewmodels.ExpandableHeaderViewHolderViewModel
import kotlinx.android.synthetic.main.expandable_header_item.view.*

class ExpandableHeaderViewHolder(private val view: View) : KSViewHolder(view) {
    private var viewModel = ExpandableHeaderViewHolderViewModel.ViewModel(environment())

    init {
        this.viewModel.outputs.titleForSummary()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe{
                    this.view.pledge_header_item_title.text = it
                }

        this.viewModel.outputs.amountForSummary()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    this.view.pledge_header_item_amount.text = it
                }
    }

    override fun bindData(data: Any?) {
        (data as? Pair<Project, Reward>)?.let {
            this.viewModel.configureWith(it)
        }
    }
}
