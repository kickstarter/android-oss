package com.kickstarter.ui.viewholders

import androidx.core.content.ContextCompat
import com.kickstarter.databinding.ItemFeatureFlagBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.viewmodels.FeatureFlagViewHolderViewModel

class FeatureFlagViewHolder(val binding: ItemFeatureFlagBinding) : KSViewHolder(binding.root) {

    private val vm: FeatureFlagViewHolderViewModel.ViewModel = FeatureFlagViewHolderViewModel.ViewModel(environment())

    init {
        this.vm.outputs.key()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.flagKey.text = it }

        this.vm.outputs.value()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.flagValue.text = it }

        this.vm.outputs.valueTextColor()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.flagValue.setTextColor(ContextCompat.getColor(context(), it)) }
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val flag = data as Pair<String, Boolean>

        this.vm.inputs.featureFlag(flag)
    }
}
