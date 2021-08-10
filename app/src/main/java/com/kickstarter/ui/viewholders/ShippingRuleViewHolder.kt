package com.kickstarter.ui.viewholders

import android.util.Pair
import com.kickstarter.databinding.ItemShippingRuleBinding
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.viewmodels.ShippingRuleViewHolderViewModel
import rx.android.schedulers.AndroidSchedulers

class ShippingRuleViewHolder(private val binding: ItemShippingRuleBinding, val delegate: Delegate) : KSArrayViewHolder(binding.root) {

    interface Delegate {
        fun ruleSelected(rule: ShippingRule)
    }
    private lateinit var shippingRule: ShippingRule
    private val viewModel = ShippingRuleViewHolderViewModel.ViewModel(environment())

    init {

        this.viewModel.outputs.shippingRuleText()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.binding.shippingRulesItemTextView.text = it }

        this.binding.shippingRuleRoot.setOnClickListener {
            this.delegate.ruleSelected(this.shippingRule)
        }
    }

    override fun bindData(any: Any?) {
        val shippingRuleAndProject = ObjectUtils.requireNonNull(any as Pair<ShippingRule, Project>)
        this.shippingRule = ObjectUtils.requireNonNull(shippingRuleAndProject.first, ShippingRule::class.java)
        val project = ObjectUtils.requireNonNull(shippingRuleAndProject.second, Project::class.java)

        this.viewModel.inputs.configureWith(this.shippingRule, project)
    }
}
