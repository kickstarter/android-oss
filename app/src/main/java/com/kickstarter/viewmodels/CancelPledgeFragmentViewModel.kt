package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.fragments.CancelPledgeFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CancelPledgeFragmentViewModel {
    interface Inputs {
        /** Call when user clicks the close button. */
        fun closeButtonClicked()

        /** Call when user clicks the confirmation button. */
        fun confirmCancellationClicked(note: String)

        /** Call when user clicks the go back button. */
        fun goBackButtonClicked()
    }

    interface Outputs {
        /** Emits when the fragment should be dismissed. */
        fun dismiss(): Observable<Void>

        /** Emits when the backing has successfully been canceled. */
        fun success(): Observable<Void>

        /** Emits when the pledged amount and project name. */
        fun pledgeAmountAndProjectName(): Observable<Pair<String, String>>

        /** Emits when the cancel call fails. */
        fun showCancelError(): Observable<String>

        /** Emits when the cancel call fails because of a generic server error. */
        fun showServerError(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<CancelPledgeFragment>(environment), Inputs, Outputs {

        private val closeButtonClicked = PublishSubject.create<Void>()
        private val confirmCancellationClicked = PublishSubject.create<String>()
        private val goBackButtonClicked = PublishSubject.create<Void>()

        private val dismiss = BehaviorSubject.create<Void>()
        private val success = BehaviorSubject.create<Void>()
        private val pledgeAmountAndProjectName = BehaviorSubject.create<Pair<String, String>>()
        private val showCancelError = BehaviorSubject.create<String>()
        private val showServerError = BehaviorSubject.create<Void>()

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
                    .compose<Pair<Project, Backing>>(combineLatestPair(backing))
                    .map { Pair(this.ksCurrency.format(it.second.amount(), it.first), it.first.name()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeAmountAndProjectName)

            val cancelBackingNotification = this.confirmCancellationClicked
                    .compose<Pair<String, Backing>>(combineLatestPair(backing))
                    .switchMap { this.apolloClient.cancelBacking(it.second, it.first).materialize() }
                    .share()

            val cancelBackingResponse = cancelBackingNotification
                    .compose(values())

            cancelBackingNotification
                    .compose(errors())
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showServerError)

            cancelBackingResponse
                    .filter { it is Boolean && it == false }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showServerError)

            cancelBackingResponse
                    .filter { it is Boolean && it == true }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.success)

            cancelBackingResponse
                    .filter { it is String }
                    .ofType(String::class.java)
                    .compose(bindToLifecycle())
                    .subscribe(this.showCancelError)

            Observable.merge(this.closeButtonClicked, this.goBackButtonClicked)
                    .compose(bindToLifecycle())
                    .subscribe(this.dismiss)
        }

        override fun closeButtonClicked() {
            this.closeButtonClicked.onNext(null)
        }

        override fun confirmCancellationClicked(note: String) {
            this.confirmCancellationClicked.onNext(note)
        }

        override fun goBackButtonClicked() {
            this.goBackButtonClicked.onNext(null)
        }

        override fun dismiss(): Observable<Void> = this.dismiss

        override fun success(): Observable<Void> = this.success

        override fun pledgeAmountAndProjectName(): Observable<Pair<String, String>> = this.pledgeAmountAndProjectName

        override fun showCancelError(): Observable<String> = this.showCancelError

        override fun showServerError(): Observable<Void> = this.showServerError
    }
}
