package com.kickstarter.ui.viewholders

import android.view.View
import androidx.core.content.ContextCompat
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.viewmodels.FeatureFlagViewHolderViewModel
import kotlinx.android.synthetic.main.item_feature_flag.view.*

class FeatureFlagViewHolder(val view: View) : KSViewHolder(view) {

    private val vm: FeatureFlagViewHolderViewModel.ViewModel = FeatureFlagViewHolderViewModel.ViewModel(environment())

    init {
        this.vm.outputs.key()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.flag_key.text = it }

        this.vm.outputs.value()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.flag_value.text = it }

        this.vm.outputs.valueTextColor()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.flag_value.setTextColor(ContextCompat.getColor(context(), it)) }
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val flag = data as Pair<String, Boolean>

        this.vm.inputs.featureFlag(flag)
    }
}