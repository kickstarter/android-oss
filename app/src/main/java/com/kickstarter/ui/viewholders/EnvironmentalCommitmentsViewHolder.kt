package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemEnvironmentalCommitmentsCardBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.viewmodels.EnvironmentalCommitmentsViewHolderViewModel

class EnvironmentalCommitmentsViewHolder(
    val binding: ItemEnvironmentalCommitmentsCardBinding,
) : KSViewHolder(binding.root) {

    private val viewModel: EnvironmentalCommitmentsViewHolderViewModel.ViewModel =
        EnvironmentalCommitmentsViewHolderViewModel.ViewModel(environment())

    init {
        viewModel.outputs.description()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.environmentalCommitmentsCard.setDescription(it) }

        viewModel.outputs.category()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.environmentalCommitmentsCard.setCategoryTitle(it) }
    }

    override fun bindData(data: Any?) {
        this.viewModel.inputs.configureWith(data as EnvironmentalCommitment)
    }
}
