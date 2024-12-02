package com.kickstarter.viewmodels

import android.os.Bundle
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValuesV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.math.RoundingMode

interface CancelPledgeViewModel {
    interface Inputs {
        /** Call when user clicks the confirmation button. */
        fun confirmCancellationClicked(note: String)

        /** Call when user clicks the go back button. */
        fun goBackButtonClicked()
    }

    interface Outputs {
        /** Emits when the fragment should be dismissed. */
        fun dismiss(): Observable<Unit>

        /** Emits when the backing has successfully been canceled. */
        fun success(): Observable<Unit>

        /** Emits when the pledged amount and project name. */
        fun pledgeAmountAndProjectName(): Observable<Pair<String, String>>

        /**  Emits a boolean determining if the cancel button should be visible. */
        fun cancelButtonIsVisible(): Observable<Boolean>

        /**  Emits a boolean determining if the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the cancel call fails. */
        fun showCancelError(): Observable<String>

        /** Emits when the cancel call fails because of a generic server error. */
        fun showServerError(): Observable<Unit>
    }

    class CancelPledgeViewModel(private val environment: Environment, private val bundle: Bundle? = null) : ViewModel(), Inputs, Outputs {

        private val confirmCancellationClicked = PublishSubject.create<String>()
        private val goBackButtonClicked = PublishSubject.create<Unit>()

        private val cancelButtonIsVisible = BehaviorSubject.create<Boolean>()
        private val dismiss = BehaviorSubject.create<Unit>()
        private val pledgeAmountAndProjectName = BehaviorSubject.create<Pair<String, String>>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val showCancelError = PublishSubject.create<String>()
        private val showServerError = PublishSubject.create<Unit>()
        private val success = BehaviorSubject.create<Unit>()

        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val ksCurrency = requireNotNull(environment.ksCurrency())

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()

        private fun arguments() = bundle?.let { Observable.just(it) } ?: Observable.empty()

        init {

            val project = arguments()
                .filter { (it.getParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT) as Project?).isNotNull() }
                .map { it.getParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT) as Project? }
                .ofType(Project::class.java)

            val backing = project
                .filter { it.backing().isNotNull() }
                .map { it.backing() }
                .ofType(Backing::class.java)

            project
                .compose<Pair<Project, Backing>>(combineLatestPair(backing))
                .map { Pair(this.ksCurrency.format(it.second.amount(), it.first, RoundingMode.HALF_UP), it.first.name()) }
                .subscribe { this.pledgeAmountAndProjectName.onNext(it) }
                .addToDisposable(disposables)

            val cancelBackingNotification = this.confirmCancellationClicked
                .compose<Pair<String, Backing>>(combineLatestPair(backing))
                .switchMap { cancelBacking(it.first, it.second) }
                .share()

            val cancelBackingResponse = cancelBackingNotification
                .compose(valuesV2())

            cancelBackingNotification
                .compose(errorsV2())
                .compose(ignoreValuesV2())
                .subscribe { this.showServerError.onNext(it) }
                .addToDisposable(disposables)

            cancelBackingResponse
                .filter { it is Boolean && it == false }
                .compose(ignoreValuesV2())
                .subscribe { this.showServerError.onNext(it) }
                .addToDisposable(disposables)

            cancelBackingResponse
                .filter { it is Boolean && it == true }
                .compose(ignoreValuesV2())
                .subscribe { this.success.onNext(it) }
                .addToDisposable(disposables)

            cancelBackingResponse
                .filter { it is String }
                .ofType(String::class.java)
                .subscribe {
                    this.showCancelError.onNext(it)
                }
                .addToDisposable(disposables)

            this.goBackButtonClicked
                .subscribe { this.dismiss.onNext(it) }
                .addToDisposable(disposables)
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

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        override fun confirmCancellationClicked(note: String) {
            this.confirmCancellationClicked.onNext(note)
        }

        override fun goBackButtonClicked() {
            this.goBackButtonClicked.onNext(Unit)
        }

        override fun cancelButtonIsVisible(): Observable<Boolean> = this.cancelButtonIsVisible

        override fun dismiss(): Observable<Unit> = this.dismiss

        override fun success(): Observable<Unit> = this.success

        override fun pledgeAmountAndProjectName(): Observable<Pair<String, String>> = this.pledgeAmountAndProjectName

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun showCancelError(): Observable<String> = this.showCancelError

        override fun showServerError(): Observable<Unit> = this.showServerError
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val environment: Environment, private val bundle: Bundle? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CancelPledgeViewModel(
                environment,
                bundle = bundle
            ) as T
        }
    }
}
