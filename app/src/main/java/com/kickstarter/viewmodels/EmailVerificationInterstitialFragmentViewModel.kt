package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.R
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

        /** Invoked when the resend email text is pressed */
        fun resendEmailButtonPressed()

        /** Invoked when the open inbox button is pressed */
        fun skipButtonPressed()
    }

    interface Outputs {
        /** Launch Email app  */
        fun startEmailActivity(): Observable<Void>

        /** Emits if the loading indicator should be gon`e */
        fun loadingIndicatorGone(): Observable<Boolean>

        /**Emits when the snackbar should be shown in case of error */
        fun showSnackbarError(): Observable<Int>

        /**Emits when the snackbar should be shown in case of success */
        fun showSnackbarSuccess(): Observable<Int>

        /** Skip link button should be shown/hide */
        fun isSkipLinkShown(): Observable<Boolean>

        /** Dismiss the Interstitial Screen */
        fun dismissInterstitial(): Observable<Void>

        /** On presenting the Fragment a verification email has been sent**/
        fun emailSent(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<EmailVerificationInterstitialFragment>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        private val loadingIndicatorGone = PublishSubject.create<Boolean>()
        private val openInboxButtonPressed = PublishSubject.create<Void>()
        private val resendEmailButtonPressed = PublishSubject.create<Void>()
        private val showSnackbarError = PublishSubject.create<Int>()
        private val showSnackbarSuccess = PublishSubject.create<Int>()
        private val startEmailActivity = PublishSubject.create<Void>()
        private val skipLinkPressed = PublishSubject.create<Void>()
        private val emailSent = PublishSubject.create<Void>()

        private val apolloClient = this.environment.apolloClient()
        private val isSkipLinkShown = BehaviorSubject.create<Boolean>()
        private val dismissInterstitial = PublishSubject.create<Void>()

        private val currentConfig = this.environment.currentConfig().observable()

        init {

            this.apolloClient.sendVerificationEmail()
                    .materialize()
                    .share()
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.emailSent.onNext(null)
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
                    .switchMap {
                        this.apolloClient.sendVerificationEmail()
                                .doOnSubscribe{this.loadingIndicatorGone.onNext(false)}
                                .doAfterTerminate{this.loadingIndicatorGone.onNext(true)}
                                .materialize()
                    }
                    .share()

            sendEmailNotification
                    .compose(Transformers.errors())
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.showSnackbarError.onNext(R.string.we_couldnt_resend_this_email_please_try_again)
                    }

            sendEmailNotification
                    .compose(Transformers.values())
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.showSnackbarSuccess.onNext(R.string.verification_email_sent_inbox)
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
        override fun loadingIndicatorGone(): Observable<Boolean> = this.loadingIndicatorGone
        override fun isSkipLinkShown(): Observable<Boolean> = this.isSkipLinkShown
        override fun startEmailActivity(): Observable<Void> = this.startEmailActivity
        override fun showSnackbarError(): Observable<Int> = this.showSnackbarError
        override fun showSnackbarSuccess(): Observable<Int> = this.showSnackbarSuccess
        override fun dismissInterstitial(): Observable<Void> = this.dismissInterstitial
        override fun emailSent(): Observable<Void> = this.emailSent
    }

}

