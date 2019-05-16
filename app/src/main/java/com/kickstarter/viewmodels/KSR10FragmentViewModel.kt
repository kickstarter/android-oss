package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.ui.fragments.KSR10Fragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface KSR10FragmentViewModel {

    interface Inputs {
        /** Call when the user hits the close button. */
        fun closeClicked()

        /** Call when the view has been laid out. */
        fun onGlobalLayout()

    }
    interface Outputs {
        /** Emits when the fragment should be dismissed. */
        fun dismiss() : Observable<Void>

        /** Emits when shapes should start their animations. */
        fun startAnimations(): Observable<Void>

    }

    class ViewModel(val environment: Environment): FragmentViewModel<KSR10Fragment>(environment), Inputs, Outputs {
        private val closeClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()

        private val dismiss = BehaviorSubject.create<Void>()
        private val startAnimations = PublishSubject.create<Void>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val hasSeenKSR10BirthdayModal = this.environment.hasSeenKSR10BirthdayModal()

        init {

            this.closeClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.dismiss)

            this.onGlobalLayout
                    .compose(bindToLifecycle())
                    .subscribe(this.startAnimations)

            this.hasSeenKSR10BirthdayModal.set(true)

            this.koala.trackViewedKSR10BirthdayModal()
        }

        override fun closeClicked() {
            this.closeClicked.onNext(null)
        }

        override fun onGlobalLayout() {
            this.onGlobalLayout.onNext(null)
        }

        @NonNull
        override fun dismiss(): Observable<Void> = this.dismiss

        @NonNull
        override fun startAnimations(): Observable<Void> = this.startAnimations
    }
}
