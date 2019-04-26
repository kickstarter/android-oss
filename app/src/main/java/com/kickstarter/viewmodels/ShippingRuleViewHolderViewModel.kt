package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.viewholders.ShippingRuleViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode

interface ShippingRuleViewHolderViewModel {

    interface Inputs {
        /** Call with a shipping rule and project when data is bound to the view.  */
        fun configureWith(shippingRule: ShippingRule, project: Project)

        /** Call when the user clicks on a shipping rule. */
        fun shippingRuleClicked()
    }

    interface Outputs {
        /** Emits the selected Shipping rule. */
        fun shippingRule(): Observable<ShippingRule>

        /** Returns the Shipping Rule text. */
        fun shippingRuleText(): Observable<String>
    }

    class ViewModel(val environment: Environment) : ActivityViewModel<ShippingRuleViewHolder>(environment), Inputs, Outputs {

        private val shippingRuleAndProject = PublishSubject.create<Pair<ShippingRule, Project>>()
        private val shippingRuleClicked = PublishSubject.create<Void>()

        private val shippingRule = BehaviorSubject.create<ShippingRule>()
        private val shippingRuleText = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.shippingRuleAndProject
                    .filter { ObjectUtils.isNotNull(it.first) }
                    .map { formattedString(it.first, it.second) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRuleText)

            this.shippingRuleAndProject
                    .map { it.first }
                    .compose<ShippingRule>(takeWhen(this.shippingRuleClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRule)
        }

        override fun configureWith(shippingRule: ShippingRule, project: Project) = this.shippingRuleAndProject.onNext(Pair.create(shippingRule, project))

        override fun shippingRuleClicked() = this.shippingRuleClicked.onNext(null)

        override fun shippingRule(): Observable<ShippingRule> = this.shippingRule

        override fun shippingRuleText(): Observable<String> = this.shippingRuleText

        private fun formattedString(shippingRule: ShippingRule, project: Project): String {
            val displayableName = shippingRule.location().displayableName()
            val cost = shippingRule.cost().toFloat()

            val formattedCost = KSCurrency(this.environment.currentConfig())
                    .formatWithProjectCurrency(cost, project, RoundingMode.UP, 2).toString()

            return "$displayableName $formattedCost"
        }
    }
}
