package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ShippingRuleViewHolderViewModel {

    interface Inputs {
        /** Call with a shipping rule and project when data is bound to the view.  */
        fun configureWith(shippingRule: ShippingRule, project: Project)
    }

    interface Outputs {
        /** Returns the Shipping Rule text. */
        fun shippingRuleText(): Observable<String>
    }

    class ViewModel(val environment: Environment) : Inputs, Outputs {

        private val shippingRuleAndProject = PublishSubject.create<Pair<ShippingRule, Project>>()

        private val shippingRuleText = BehaviorSubject.create<String>()

        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.shippingRuleAndProject
                .filter { it.first.isNotNull() }
                .map { it.first.location()?.displayableName() ?: "" }
                .subscribe {
                    this.shippingRuleText.onNext(it)
                }
                .addToDisposable(disposables)
        }

        override fun configureWith(shippingRule: ShippingRule, project: Project) = this.shippingRuleAndProject.onNext(Pair.create(shippingRule, project))

        override fun shippingRuleText(): Observable<String> = this.shippingRuleText

        fun onCleared() {
            disposables.clear()
        }
    }
}
