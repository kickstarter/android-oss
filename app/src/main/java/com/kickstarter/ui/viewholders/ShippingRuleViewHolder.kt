package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.viewmodels.ShippingRuleViewHolderViewModel
import kotlinx.android.synthetic.main.item_shipping_rule.view.*
import rx.android.schedulers.AndroidSchedulers

class ShippingRuleViewHolder(private val view: View, val delegate: Delegate) : KSArrayViewHolder(view) {


    interface Delegate {
        fun ruleSelected(rule: ShippingRule)
    }

    val viewModel = ShippingRuleViewHolderViewModel.ViewModel(environment())

    init {

        this.viewModel.outputs.shippingRuleText()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    this.view.shipping_rules_item_text_view.text = it
                }

        this.viewModel.outputs.shippingRule()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.delegate.ruleSelected(it) }

        this.view.shipping_rule_root.setOnClickListener {
            this.viewModel.inputs.shippingRuleClicked()
        }
    }

    override fun bindData(any: Any?) {
        val shippingRuleAndProject = ObjectUtils.requireNonNull(any as Pair<ShippingRule, Project>)
        val shippingRule = ObjectUtils.requireNonNull(shippingRuleAndProject.first, ShippingRule::class.java)
        val project = ObjectUtils.requireNonNull(shippingRuleAndProject.second, Project::class.java)

        this.viewModel.inputs.configureWith(shippingRule, project)
    }
}
