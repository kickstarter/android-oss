package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemLightsOnBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ui.data.Editorial
import com.kickstarter.viewmodels.EditorialViewHolderViewModel

class EditorialViewHolder(
    val binding: ItemLightsOnBinding,
    val delegate: Delegate
) : KSViewHolder(binding.root) {

    interface Delegate {
        fun editorialViewHolderClicked(editorial: Editorial)
    }

    private val vm: EditorialViewHolderViewModel.ViewModel = EditorialViewHolderViewModel.ViewModel(environment())

    init {

        this.vm.outputs.ctaTitle()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.title.setText(it) }

        this.vm.outputs.ctaDescription()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.description.setText(it) }

        this.vm.outputs.editorial()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.editorialViewHolderClicked(it) }

        this.vm.outputs.graphic()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.editorialGraphic.setImageResource(it) }

        binding.lightsOnContainer.setOnClickListener { this.vm.inputs.editorialClicked() }
    }

    override fun bindData(data: Any?) {
        this.vm.inputs.configureWith(data as Editorial)
    }
}
