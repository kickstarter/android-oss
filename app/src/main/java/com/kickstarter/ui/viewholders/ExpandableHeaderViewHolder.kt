package com.kickstarter.ui.viewholders

import android.util.Pair
import com.kickstarter.databinding.ExpandableHeaderItemBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.viewmodels.ExpandableHeaderViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ExpandableHeaderViewHolder(private val binding: ExpandableHeaderItemBinding) : KSViewHolder(binding.root) {
    private var viewModel = ExpandableHeaderViewHolderViewModel.ViewModel(environment())
    private var disposables: CompositeDisposable = CompositeDisposable()

    init {
        this.viewModel.outputs.titleForSummary()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.pledgeHeaderItemTitle.text = it
            }.addToDisposable(disposables)

        this.viewModel.outputs.amountForSummary()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.pledgeHeaderItemAmount.text = it
            }.addToDisposable(disposables)
    }

    override fun destroy() {
        viewModel.inputs.onCleared()
        disposables.clear()
        super.destroy()
    }

    override fun bindData(data: Any?) {
        (data as? Pair<Project, Reward>)?.let {
            this.viewModel.configureWith(it)
        }
    }
}
