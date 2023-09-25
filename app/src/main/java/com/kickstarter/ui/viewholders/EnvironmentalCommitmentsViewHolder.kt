package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemEnvironmentalCommitmentsCardBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.viewmodels.EnvironmentalCommitmentsViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class EnvironmentalCommitmentsViewHolder(
    val binding: ItemEnvironmentalCommitmentsCardBinding,
) : KSViewHolder(binding.root) {
    private val viewModel = EnvironmentalCommitmentsViewHolderViewModel.EnvironmentalCommitmentsViewHolderViewModel()

    private val disposables = CompositeDisposable()

    init {
        viewModel.outputs.description()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.environmentalCommitmentsCard.setDescription(it) }
            .addToDisposable(disposables)

        viewModel.outputs.category()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.environmentalCommitmentsCard.setCategoryTitle(it) }
            .addToDisposable(disposables)
    }

    override fun bindData(data: Any?) {
        this.viewModel.inputs.configureWith(data as EnvironmentalCommitment)
    }

    override fun destroy() {
        viewModel.inputs.onCleared()
        disposables.clear()
        super.destroy()
    }
}
