package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.viewholders.ExpandableHeaderViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ExpandableHeaderViewHolderViewModel {
    interface Inputs {
        /**
         * Initial configuration data
         * @param Pair(amount, title)
         */
        fun configureWith(titleAndAmount: Pair<String, String>)
    }

    interface Outputs {
        /** Emits the String for amount already formatted with the currency of the reward */
        fun amountForSummary(): Observable<String>

        /** Emits the String for the title of the amount */
        fun titleForSummary(): Observable<String>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<ExpandableHeaderViewHolder>(environment), Inputs, Outputs {

        private val titleAndAmount = PublishSubject.create<Pair<String, String>>()
        private val titleForSummary = BehaviorSubject.create<String>()
        private val amountForSummary = BehaviorSubject.create<String>()

        val inputs = this
        val outputs = this

        init {
            titleAndAmount
                    .map { it.second}
                    .compose(bindToLifecycle())
                    .subscribe(this.titleForSummary)

            titleAndAmount
                    .map { it.first}
                    .compose(bindToLifecycle())
                    .subscribe(this.amountForSummary)
        }

        override fun configureWith(titleAndAmount: Pair<String, String>) = this.titleAndAmount.onNext(titleAndAmount)

        override fun amountForSummary(): Observable<String> = this.titleForSummary
        override fun titleForSummary(): Observable<String> = this.amountForSummary
    }
}