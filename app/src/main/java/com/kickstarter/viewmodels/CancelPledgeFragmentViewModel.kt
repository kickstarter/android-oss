package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValues
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.fragments.CancelPledgeFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CancelPledgeFragmentViewModel {
    interface Inputs {
        /** Call when user clicks the confirmation button. */
        fun confirmCancellationClicked(note: String)
    }

    interface Outputs {
        fun notifyProjectActivityOfSuccess(): Observable<Void>

        fun pledgeAmountAndProjectName(): Observable<Pair<String, String>>

        fun showError(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<CancelPledgeFragment>(environment), Inputs, Outputs {

        private val confirmCancellationClicked = PublishSubject.create<String>()

        private val notifyProjectActivityOfSuccess = BehaviorSubject.create<Void>()
        private val pledgeAmountAndProjectName = BehaviorSubject.create<android.util.Pair<String, String>>()
        private val showError = BehaviorSubject.create<Void>()

        private val apolloClient = environment.apolloClient()
        private val ksCurrency = environment.ksCurrency()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val project = arguments()
                    .map { it.getParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT) as Project }

            val backing = arguments()
                    .map { it.getParcelable(ArgumentsKey.CANCEL_PLEDGE_BACKING) as Backing }

            project
                    .compose<android.util.Pair<Project, Backing>>(combineLatestPair(backing))
                    .map { Pair(this.ksCurrency.format(it.second.amount(), it.first), it.first.name()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeAmountAndProjectName)

            val cancelBackingNotification = this.confirmCancellationClicked
                    .compose<android.util.Pair<String, Backing>>(combineLatestPair(backing))
                    .switchMap { this.apolloClient.cancelBacking(it.second, it.first).materialize() }
                    .share()

            val checkoutValues = cancelBackingNotification
                    .compose(Transformers.values())

            Observable.merge(cancelBackingNotification.compose(Transformers.errors()), checkoutValues.filter { BooleanUtils.isFalse(it) })
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showError)

            project
                    .compose<Project>(Transformers.takeWhen(checkoutValues.filter { BooleanUtils.isTrue(it) }))
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.notifyProjectActivityOfSuccess)

        }

        override fun confirmCancellationClicked(note: String) {
            this.confirmCancellationClicked.onNext(note)
        }

        override fun notifyProjectActivityOfSuccess(): Observable<Void> = this.notifyProjectActivityOfSuccess

        override fun pledgeAmountAndProjectName(): Observable<android.util.Pair<String, String>> = this.pledgeAmountAndProjectName

        override fun showError(): Observable<Void> = this.showError
    }
}
