package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.ui.fragments.EmailVerificationInterstitialFragment
import rx.Observable
import rx.subjects.BehaviorSubject

class EmailVerificationInterstitialFragmentViewModel {
    interface Inputs {
        /** Invoked when the open inbox button is pressed */
        fun openInboxButtonPressed()
    }

    interface Outputs {
        fun startEmailActivity(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<EmailVerificationInterstitialFragment>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        private val openInboxButtonPressed = BehaviorSubject.create<Void>()

        private val startEmailActivity = BehaviorSubject.create<Void>()


        init {
            openInboxButtonPressed
                    .compose(bindToLifecycle())
                    .subscribe(this.startEmailActivity)
        }

        // - Inputs
        override fun openInboxButtonPressed() = this.openInboxButtonPressed.onNext(null)

        // - Outputs
        override fun startEmailActivity(): Observable<Void> = this.startEmailActivity
    }

}

