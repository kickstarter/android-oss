package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemFeatureFlagBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.model.FeatureFlagsModel
import com.kickstarter.viewmodels.FeatureFlagViewHolderViewModel

class FeatureFlagViewHolder(val binding: ItemFeatureFlagBinding, val delegate: Delegate) : KSViewHolder(binding.root) {

    private val vm: FeatureFlagViewHolderViewModel.ViewModel = FeatureFlagViewHolderViewModel.ViewModel(environment())

    interface Delegate {
        fun featureOptionToggle(featureName: String, isEnabled: Boolean)
    }

    init {
        this.vm.outputs.key()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { binding.flagKey.text = it }

        this.vm.outputs.value()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { binding.flagValue.isChecked = it }

        this.vm.outputs.isClickable()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    binding.flagValue.isClickable = it
                }

        this.vm.outputs.featureAlpha()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    binding.root.alpha = it
                }

        this.vm.outputs.notifyDelegateFeatureStateChanged()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.delegate.featureOptionToggle(it.first, it.second) }

        binding.flagValue.setOnCheckedChangeListener { _, isChecked ->
            this.vm.inputs.featureFlagCheckedChange(isChecked)
        }
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val flag = data as FeatureFlagsModel

        this.vm.inputs.featureFlag(flag)
    }
}