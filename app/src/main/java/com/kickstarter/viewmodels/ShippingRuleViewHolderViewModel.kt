package com.kickstarter.viewmodels

import android.database.Observable
import android.widget.TextView
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.viewholders.ShippingRuleViewHolder
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ShippingRuleViewHolderViewModel {

    interface Inputs {
        fun configureWith(shippingRule: ShippingRule)
    }

    interface Outputs {
        fun shippingRuleText(): Observable<String>
    }

    class ViewModel(val environment: Environment) : ActivityViewModel<ShippingRuleViewHolder>(environment), Inputs, Outputs {

        private val shippingRule = PublishSubject.create<ShippingRule>()

        private val shippingRuleText = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.shippingRule
                    .map { it.location().displayableName() + it.cost() }
                    .subscribe{ this.shippingRuleText }
        }

        override fun configureWith(shippingRule: ShippingRule) = this.shippingRule.onNext(shippingRule)

        override fun shippingRuleText(): Observable<String> = this.shippingRuleText()

        private fun formattedString(shippingRule: ShippingRule): String {
            val displayableName = shippingRule.location()?.displayableName()
            //todo: get this amount from KS currency
            val cost = shippingRule.cost()

            return "$displayableName +($cost)"
        }

    }


}