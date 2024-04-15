package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

interface ExpandableHeaderViewHolderViewModel {
    interface Inputs {
        /**
         * Initial configuration data with Project and Reward
         */
        fun configureWith(values: Pair<Project, Reward>)

        /** Clear subscriptions, called from ViewHolder when view is destroyed. */
        fun onCleared()
    }

    interface Outputs {
        /** Emits the String for amount already formatted with the currency of the reward */
        fun amountForSummary(): Observable<String>

        /** Emits the String for the title of the amount */
        fun titleForSummary(): Observable<String>
    }

    class ViewModel(val environment: Environment) : Inputs, Outputs {

        private val projectAndReward = PublishSubject.create<Pair<Project, Reward>>()
        private val titleForSummary = PublishSubject.create<String>()
        private val amountForSummary = PublishSubject.create<String>()
        private val disposables: CompositeDisposable = CompositeDisposable()

        private val ksCurrency = requireNotNull(environment.ksCurrency())

        val inputs = this
        val outputs = this

        init {
            val reward = projectAndReward
                .map { it.second }

            val project = projectAndReward
                .map { it.first }

            reward
                .filter { it.isAddOn() && it.quantity().isNotNull() && it.quantity()?.let { q -> q > 0 } ?: false }
                .map { it.quantity().toString() + " X " + it.title() }
                .subscribe { this.titleForSummary.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { !it.isAddOn() }
                .map { it.title() ?: "" }
                .subscribe { this.titleForSummary.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { this.ksCurrency.format(it.second.minimum() * (it.second.quantity() ?: 1), it.first) }
                .subscribe { this.amountForSummary.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun configureWith(values: Pair<Project, Reward>) = this.projectAndReward.onNext(values)

        override fun onCleared() {
            disposables.clear()
        }

        override fun amountForSummary(): Observable<String> = this.amountForSummary
        override fun titleForSummary(): Observable<String> = this.titleForSummary
    }
}
