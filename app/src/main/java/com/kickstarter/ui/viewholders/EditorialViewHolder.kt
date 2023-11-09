package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemLightsOnBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.data.Editorial
import com.kickstarter.viewmodels.EditorialViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable

class EditorialViewHolder(
    val binding: ItemLightsOnBinding,
    val delegate: Delegate
) : KSViewHolder(binding.root) {

    private val disposables = CompositeDisposable()

    interface Delegate {
        fun editorialViewHolderClicked(editorial: Editorial)
    }

    private val vm: EditorialViewHolderViewModel.ViewModel = EditorialViewHolderViewModel.ViewModel()

    init {

        this.vm.outputs.ctaTitle()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.title.setText(it) }
            .addToDisposable(disposables)

        this.vm.outputs.ctaDescription()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.description.setText(it) }
            .addToDisposable(disposables)

        this.vm.outputs.editorial()
            .compose(Transformers.observeForUIV2())
            .subscribe { this.delegate.editorialViewHolderClicked(it) }
            .addToDisposable(disposables)

        this.vm.outputs.graphic()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.editorialGraphic.setImageResource(it) }
            .addToDisposable(disposables)

        binding.lightsOnContainer.setOnClickListener { this.vm.inputs.editorialClicked() }
    }

    override fun bindData(data: Any?) {
        this.vm.inputs.configureWith(data as Editorial)
    }

    override fun destroy() {
        disposables.clear()
        vm.clear()
        super.destroy()
    }
}
