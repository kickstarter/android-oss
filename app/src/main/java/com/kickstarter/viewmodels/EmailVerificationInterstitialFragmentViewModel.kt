package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.utils.extensions.EMAIL_VERIFICATION_SKIP
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.ui.fragments.EmailVerificationInterstitialFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class EmailVerificationInterstitialFragmentViewModel {
    interface Inputs {
        /** Input to trigger the logic contained within the ViewModel */
        fun configureWith(accessTokenEnvelope: AccessTokenEnvelope)

        /** Invoked when the open inbox button is pressed */
        fun openInboxButtonPressed()

        /** Invoked when the open inbox button is pressed */
        fun skipButtonPressed()
    }

    interface Outputs {
        /** Launch Email app  */
        fun startEmailActivity(): Observable<Void>

        /** Skip link button should be shown/hide */
        fun isSkipLinkShown(): Observable<Boolean>

        /** Dismiss the Interstitial Screen */
        fun dismissInterstitial(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<EmailVerificationInterstitialFragment>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        private val accessTokenEnvelope = BehaviorSubject.create<AccessTokenEnvelope>()
        private val openInboxButtonPressed = PublishSubject.create<Void>()
        private val skipLinkPressed = PublishSubject.create<Void>()

        private val isSkipLinkShown = PublishSubject.create<Boolean>()
        private val startEmailActivity = PublishSubject.create<Void>()
        private val dismissInterstitial = PublishSubject.create<Void>()

        private val currentConfig = this.environment.currentConfig().observable()
        private val currentUser = this.environment.currentUser()

        init {
            // - Log in the user in the app
            this.accessTokenEnvelope
                    .subscribe {
                        this.currentUser.login(it.user(), it.accessToken())
                    }

            this.currentConfig
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.isSkipLinkShown.onNext(it.isFeatureFlagEnabled(EMAIL_VERIFICATION_SKIP))
                    }

            this.openInboxButtonPressed
                    .compose(bindToLifecycle())
                    .subscribe(this.startEmailActivity)

            this.dismissInterstitial
                    .compose(bindToLifecycle())
                    .subscribe(this.dismissInterstitial)
        }

        // - Inputs
        override fun configureWith(accessTokenEnvelope: AccessTokenEnvelope) = this.accessTokenEnvelope.onNext(accessTokenEnvelope)
        override fun openInboxButtonPressed() = this.openInboxButtonPressed.onNext(null)
        override fun skipButtonPressed() = this.skipLinkPressed.onNext(null)

        // - Outputs
        override fun isSkipLinkShown(): Observable<Boolean> = this.isSkipLinkShown
        override fun startEmailActivity(): Observable<Void> = this.startEmailActivity
        override fun dismissInterstitial(): Observable<Void> = this.dismissInterstitial
    }

}

