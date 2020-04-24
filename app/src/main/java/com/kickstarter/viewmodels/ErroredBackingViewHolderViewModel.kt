package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.models.ErroredBacking
import com.kickstarter.ui.viewholders.ErroredBackingViewHolder
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<ErroredBackingViewHolder>(environment), Inputs, Outputs {

        private val erroredBacking = PublishSubject.create<ErroredBacking>()
        private val manageButtonClicked = PublishSubject.create<Void>()

        private val notifyDelegateToStartFixPaymentMethod = PublishSubject.create<String>()
        private val projectFinalCollectionDate = BehaviorSubject.create<DateTime>()
        private val projectName = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val project = this.erroredBacking
                    .map { it.project() }

            project
                    .map { it.name() }
                    .compose(bindToLifecycle())
                    .subscribe { this.projectName.onNext(it) }

            project
                    .map { it.finalCollectionDate() }
                    .compose(bindToLifecycle())
                    .subscribe { this.projectFinalCollectionDate.onNext(it) }

            project
                    .map { it.slug() }
                    .compose<String>(takeWhen(this.manageButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe { this.notifyDelegateToStartFixPaymentMethod.onNext(it) }
        }

        override fun configureWith(erroredBacking: ErroredBacking) {
            this.erroredBacking.onNext(erroredBacking)
        }

        override fun manageButtonClicked() {
            this.manageButtonClicked.onNext(null)
        }

        override fun notifyDelegateToStartFixPaymentMethod(): Observable<String> = this.notifyDelegateToStartFixPaymentMethod

        override fun projectFinalCollectionDate(): Observable<DateTime> = this.projectFinalCollectionDate

        override fun projectName(): Observable<String> = this.projectName

    }
}
