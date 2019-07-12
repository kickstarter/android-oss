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
import rx.Notification
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode

interface CancelPledgeViewModel {
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

        /**  */
        fun cancelButtonIsVisible(): Observable<Boolean>

        /**  */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the cancel call fails. */
        fun showCancelError(): Observable<String>

        /** Emits when the cancel call fails because of a generic server error. */
        fun showServerError(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<CancelPledgeFragment>(environment), Inputs, Outputs {

        private val closeButtonClicked = PublishSubject.create<Void>()
        private val confirmCancellationClicked = PublishSubject.create<String>()
        private val goBackButtonClicked = PublishSubject.create<Void>()

        private val cancelButtonIsVisible = BehaviorSubject.create<Boolean>()
        private val dismiss = BehaviorSubject.create<Void>()
        private val pledgeAmountAndProjectName = BehaviorSubject.create<Pair<String, String>>()
        private val progressBarIsVisible = PublishSubject.create<Boolean>()
        private val showCancelError = BehaviorSubject.create<String>()
        private val showServerError = BehaviorSubject.create<Void>()
        private val success = BehaviorSubject.create<Void>()

        private val apolloClient = environment.apolloClient()
        private val ksCurrency = environment.ksCurrency()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val project = arguments()
                    .map { it.getParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT) as Project }

            val backing = project
                    .map { it.backing() }
                    .ofType(Backing::class.java)

            project
                    .compose<Pair<Project, Backing>>(combineLatestPair(backing))
                    .map { Pair(this.ksCurrency.format(it.second.amount(), it.first, RoundingMode.HALF_UP), it.first.name()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeAmountAndProjectName)

            val cancelBackingNotification = this.confirmCancellationClicked
                    .compose<Pair<String, Backing>>(combineLatestPair(backing))
                    .switchMap { cancelBacking(it.first, it.second) }
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

        private fun cancelBacking(note: String, backing: Backing): Observable<Notification<Any>> {
            return this.apolloClient.cancelBacking(backing, note)
                    .doOnSubscribe {
                        this.progressBarIsVisible.onNext(true)
                        this.cancelButtonIsVisible.onNext(false)
                    }
                    .doAfterTerminate {
                        this.progressBarIsVisible.onNext(false)
                        this.cancelButtonIsVisible.onNext(true)
                    }.materialize()
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

        override fun cancelButtonIsVisible(): Observable<Boolean> = this.cancelButtonIsVisible

        override fun dismiss(): Observable<Void> = this.dismiss

        override fun success(): Observable<Void> = this.success

        override fun pledgeAmountAndProjectName(): Observable<Pair<String, String>> = this.pledgeAmountAndProjectName

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun showCancelError(): Observable<String> = this.showCancelError

        override fun showServerError(): Observable<Void> = this.showServerError
    }
}
