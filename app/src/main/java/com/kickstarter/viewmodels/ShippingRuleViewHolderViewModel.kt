package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.viewholders.ShippingRuleViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ShippingRuleViewHolderViewModel {

    interface Inputs {
        /** Call with a shipping rule and project when data is bound to the view.  */
        fun configureWith(shippingRule: ShippingRule, project: Project)
    }

    interface Outputs {
        /** Returns the Shipping Rule text. */
        fun shippingRuleText(): Observable<String>
    }

    class ViewModel(val environment: Environment) : ActivityViewModel<ShippingRuleViewHolder>(environment), Inputs, Outputs {

        private val shippingRuleAndProject = PublishSubject.create<Pair<ShippingRule, Project>>()

        private val shippingRuleText = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.shippingRuleAndProject
                .filter { ObjectUtils.isNotNull(it.first) }
                .map { it.first.location().displayableName() }
                .compose(bindToLifecycle())
                .subscribe(this.shippingRuleText)
        }

        override fun configureWith(shippingRule: ShippingRule, project: Project) = this.shippingRuleAndProject.onNext(Pair.create(shippingRule, project))

        override fun shippingRuleText(): Observable<String> = this.shippingRuleText
    }
}
