package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.viewholders.ShippingRuleViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ShippingRuleViewHolderViewModel {

    interface Inputs {
        fun configureWith(shippingRule: ShippingRule, project: Project)
    }

    interface Outputs {
        fun shippingRuleText(): Observable<String>
    }

    class ViewModel(val environment: Environment) : ActivityViewModel<ShippingRuleViewHolder>(environment), Inputs, Outputs {

        private val shippingRule = PublishSubject.create<Pair<ShippingRule, Project>>()

        private val shippingRuleText = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.shippingRule
                    .map { it.first.location().displayableName() + it.first.cost() }
                    .compose(bindToLifecycle())
                    .subscribe{ this.shippingRuleText }
        }

        override fun configureWith(shippingRule: ShippingRule, project: Project) = this.shippingRule.onNext(Pair.create(shippingRule, project))

        override fun shippingRuleText(): Observable<String> = this.shippingRuleText()

        private fun formattedString(shippingRule: ShippingRule): String {
            val displayableName = shippingRule.location()?.displayableName()
            //todo: get this amount from KS currency

//            KSCurrency().formatWithProjectCurrency()
            val cost = shippingRule.cost()

            return "$displayableName +($cost)"
        }

    }


}