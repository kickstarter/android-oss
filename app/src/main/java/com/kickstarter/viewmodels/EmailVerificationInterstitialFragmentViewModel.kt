package com.kickstarter.viewmodels

import android.util.Log
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.EMAIL_VERIFICATION_SKIP
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.fragments.EmailVerificationInterstitialFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class EmailVerificationInterstitialFragmentViewModel {
    interface Inputs {
        /** Invoked when the open inbox button is pressed */
        fun openInboxButtonPressed()

        fun resendEmailButtonPressed()

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

        private val openInboxButtonPressed = BehaviorSubject.create<Void>()
        private val resendEmailButtonPressed = BehaviorSubject.create<Void>()
        private val skipLinkPressed = PublishSubject.create<Void>()

        private val apolloClient = this.environment.apolloClient()
        private val isSkipLinkShown = BehaviorSubject.create<Boolean>()
        private val startEmailActivity = PublishSubject.create<Void>()
        private val dismissInterstitial = PublishSubject.create<Void>()

        private val currentConfig = this.environment.currentConfig().observable()
        private val currentUser = this.environment.currentUser()

        init {

            // - Retrieve data from intent
            val accessTokenEnvelope = arguments()
                    .map { it.getParcelable(ArgumentsKey.ENVELOPE) as AccessTokenEnvelope? }
                    .ofType(AccessTokenEnvelope::class.java)

            // - Log in the user in the current environment
            accessTokenEnvelope
                    .compose(bindToLifecycle())
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

            val sendEmailNotification = this.resendEmailButtonPressed
                    .compose(bindToLifecycle())
                    .switchMap { apolloClient.sendVerificationEmail().materialize() }
                    .share()

            sendEmailNotification
                    .compose(Transformers.errors())
                    .subscribe {
                        Log.d("Resend Email", "Sent: error ")
                    }

            sendEmailNotification
                    .compose(Transformers.values())
                    .subscribe {
                        Log.d("Resend Email", "Sent success: success ")
                    }

            this.skipLinkPressed
                    .compose(bindToLifecycle())
                    .subscribe(this.dismissInterstitial)
        }

        // - Inputs
        override fun openInboxButtonPressed() = this.openInboxButtonPressed.onNext(null)
        override fun skipButtonPressed() = this.skipLinkPressed.onNext(null)

        override fun resendEmailButtonPressed() = this.resendEmailButtonPressed.onNext(null)

        // - Outputs
        override fun isSkipLinkShown(): Observable<Boolean> = this.isSkipLinkShown
        override fun startEmailActivity(): Observable<Void> = this.startEmailActivity
        override fun dismissInterstitial(): Observable<Void> = this.dismissInterstitial
    }

}

