package com.kickstarter.viewmodels

import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.ErroredBacking
import com.kickstarter.ui.viewholders.ErroredBackingViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

interface ErroredBackingViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ErroredBacking]. */
        fun configureWith(erroredBacking: ErroredBacking)

        /** Call when the manage button is clicked. */
        fun manageButtonClicked()
    }

    interface Outputs {
        /** Emits the final collection date of the [ErroredBacking.Project]. */
        fun projectFinalCollectionDate(): Observable<DateTime>

        /** Emits the name of the [ErroredBacking.Project]. */
        fun projectName(): Observable<String>

        /** Emits the [ErroredBacking.Project] slug when the [ErroredBackingViewHolder.Delegate] should be notified to show the fix pledge flow. */
        fun notifyDelegateToStartFixPaymentMethod(): Observable<String>
    }

    class ViewModel : Inputs, Outputs {

        private val erroredBacking = PublishSubject.create<ErroredBacking>()
        private val manageButtonClicked = PublishSubject.create<Unit>()

        private val notifyDelegateToStartFixPaymentMethod = PublishSubject.create<String>()
        private val projectFinalCollectionDate = BehaviorSubject.create<DateTime>()
        private val projectName = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()

        init {

            val project = this.erroredBacking
                .map { it.project() }

            project
                .map { it.name() }
                .subscribe { this.projectName.onNext(it) }
                .addToDisposable(disposables)

            project
                .map { it.finalCollectionDate() }
                .subscribe { this.projectFinalCollectionDate.onNext(it) }
                .addToDisposable(disposables)

            project
                .map { it.slug() }
                .compose<String>(takeWhenV2(this.manageButtonClicked))
                .subscribe { this.notifyDelegateToStartFixPaymentMethod.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun configureWith(erroredBacking: ErroredBacking) {
            this.erroredBacking.onNext(erroredBacking)
        }

        override fun manageButtonClicked() {
            this.manageButtonClicked.onNext(Unit)
        }

        override fun notifyDelegateToStartFixPaymentMethod(): Observable<String> = this.notifyDelegateToStartFixPaymentMethod

        override fun projectFinalCollectionDate(): Observable<DateTime> = this.projectFinalCollectionDate

        override fun projectName(): Observable<String> = this.projectName

        fun clear() {
            disposables.clear()
        }
    }
}
