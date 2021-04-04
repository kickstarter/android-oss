package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.viewholders.ExpandableHeaderViewHolder
import rx.Observable
import rx.subjects.PublishSubject

interface ExpandableHeaderViewHolderViewModel {
    interface Inputs {
        /**
         * Initial configuration data with Project and Reward
         */
        fun configureWith(values: Pair<Project, Reward>)
    }

    interface Outputs {
        /** Emits the String for amount already formatted with the currency of the reward */
        fun amountForSummary(): Observable<String>

        /** Emits the String for the title of the amount */
        fun titleForSummary(): Observable<String>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<ExpandableHeaderViewHolder>(environment), Inputs, Outputs {

        private val projectAndReward = PublishSubject.create<Pair<Project, Reward>>()
        private val titleForSummary = PublishSubject.create<String>()
        private val amountForSummary = PublishSubject.create<String>()

        private val ksCurrency = environment.ksCurrency()

        val inputs = this
        val outputs = this

        init {
            val reward = projectAndReward
                .map { it.second }

            val project = projectAndReward
                .map { it.first }

            reward
                .filter { it.isAddOn && ObjectUtils.isNotNull(it.quantity()) && it.quantity()?.let { q -> q > 0 } ?: false }
                .map { it.quantity().toString() + " X " + it.title() }
                .compose(bindToLifecycle())
                .subscribe(this.titleForSummary)

            reward
                .filter { !it.isAddOn }
                .map { it.title() }
                .compose(bindToLifecycle())
                .subscribe(this.titleForSummary)

            projectAndReward
                .map { this.ksCurrency.format(it.second.minimum(), it.first) }
                .compose(bindToLifecycle())
                .subscribe(this.amountForSummary)
        }

        override fun configureWith(values: Pair<Project, Reward>) = this.projectAndReward.onNext(values)

        override fun amountForSummary(): Observable<String> = this.amountForSummary
        override fun titleForSummary(): Observable<String> = this.titleForSummary
    }
}
