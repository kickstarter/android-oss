package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.TwoFactorActivity
import rx.Observable
import rx.subjects.PublishSubject

interface TwoFactorViewModel {
    interface Inputs {
        /** Call when the 2FA code has been submitted.  */
        fun code(code: String)

        /** Call when the log in button has been clicked.  */
        fun loginClick()

        /** Call when the resend button has been clicked.  */
        fun resendClick()
    }

    interface Outputs {
        /** Emits when submitting TFA code errored for an unknown reason.  */
        fun genericTfaError(): Observable<Void>

        /** Emits when TFA code was submitted.  */
        fun formSubmitting(): Observable<Boolean>

        /** Emits when TFA code submission has completed.  */
        fun formIsValid(): Observable<Boolean>

        /** Emits when resend code confirmation should be shown.  */
        fun showResendCodeConfirmation(): Observable<Void>

        /** Emits when a submitted TFA code does not match.  */
        fun tfaCodeMismatchError(): Observable<Void>

        /** Emits when submitting TFA code was successful.  */
        fun tfaSuccess(): Observable<Void>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<TwoFactorActivity>(environment),
        Inputs,
        Outputs {
        private val client: ApiClientType
        private val currentUser: CurrentUserType

        private fun success(envelope: AccessTokenEnvelope) {
            currentUser.login(envelope.user(), envelope.accessToken())
            tfaSuccess.onNext(null)
        }

        private fun login(
            code: String,
            email: String,
            password: String
        ): Observable<AccessTokenEnvelope> {
            return client.login(email, password, code)
                .compose(Transformers.pipeApiErrorsTo(tfaError))
                .compose(Transformers.neverError())
                .doOnSubscribe { formSubmitting.onNext(true) }
                .doAfterTerminate { formSubmitting.onNext(false) }
        }

        private fun loginWithFacebook(
            code: String,
            fbAccessToken: String
        ): Observable<AccessTokenEnvelope> {
            return client.loginWithFacebook(fbAccessToken, code)
                .compose(Transformers.pipeApiErrorsTo(tfaError))
                .compose(Transformers.neverError())
                .doOnSubscribe { formSubmitting.onNext(true) }
                .doAfterTerminate { formSubmitting.onNext(false) }
        }

        private fun resendCode(email: String, password: String): Observable<AccessTokenEnvelope> {
            return client.login(email, password)
                .compose(Transformers.neverError())
                .doOnSubscribe { showResendCodeConfirmation.onNext(null) }
        }

        private fun resendCodeWithFacebook(fbAccessToken: String): Observable<AccessTokenEnvelope> {
            return client.loginWithFacebook(fbAccessToken)
                .compose(Transformers.neverError())
                .doOnSubscribe { showResendCodeConfirmation.onNext(null) }
        }

        private val code = PublishSubject.create<String>()
        private val loginClick = PublishSubject.create<Void>()
        private val resendClick = PublishSubject.create<Void>()
        private val formIsValid = PublishSubject.create<Boolean>()
        private val formSubmitting = PublishSubject.create<Boolean>()
        private val showResendCodeConfirmation = PublishSubject.create<Void>()
        private val tfaError = PublishSubject.create<ErrorEnvelope>()
        private val tfaSuccess = PublishSubject.create<Void>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        override fun formIsValid(): Observable<Boolean> = formIsValid

        override fun formSubmitting(): Observable<Boolean> = formSubmitting

        override fun genericTfaError(): Observable<Void> {
            return tfaError
                .filter { env: ErrorEnvelope -> !env.isTfaFailedError }
                .map { null }
        }

        override fun showResendCodeConfirmation(): Observable<Void> = showResendCodeConfirmation

        override fun tfaCodeMismatchError(): Observable<Void> {
            return tfaError
                .filter(ErrorEnvelope::isTfaFailedError)
                .map { null }
        }

        override fun tfaSuccess(): Observable<Void> {
            return tfaSuccess
        }

        override fun code(s: String) {
            this.code.onNext(s)
        }

        override fun loginClick() {
            loginClick.onNext(null)
        }

        override fun resendClick() {
            resendClick.onNext(null)
        }

        protected inner class TfaData(
            val email: String,
            val isFacebookLogin: Boolean,
            val password: String
        )

        protected inner class TfaDataForFacebook(
            val fbAccessToken: String,
            val isFacebookLogin: Boolean,
        )
        companion object {
            private fun isCodeValid(code: String?): Boolean {
                return code != null && code.isNotEmpty()
            }
        }

        init {
            currentUser = requireNotNull(environment.currentUser())
            client = requireNotNull(environment.apiClient())

            val email = intent()
                .map { it.getStringExtra(IntentKey.EMAIL) }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            val fbAccessToken = intent()
                .map { it.getStringExtra(IntentKey.FACEBOOK_TOKEN) }

            val isFacebookLogin = intent()
                .map { it.getBooleanExtra(IntentKey.FACEBOOK_LOGIN, false) }

            val password = intent()
                .map { it.getStringExtra(IntentKey.PASSWORD) }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            val tfaData = Observable.combineLatest(
                email, isFacebookLogin, password
            ) { email: String, isFacebookLogin: Boolean, password: String ->
                TfaData(
                    email,
                    isFacebookLogin,
                    password
                )
            }

            val tfaFacebookData = Observable.combineLatest(
                fbAccessToken, isFacebookLogin
            ) { fbAccessToken: String?, isFacebookLogin: Boolean ->
                TfaDataForFacebook(
                    fbAccessToken = fbAccessToken ?: "",
                    isFacebookLogin = isFacebookLogin,
                )
            }

            this.code
                .map { code: String? -> isCodeValid(code) }
                .compose(bindToLifecycle())
                .subscribe(formIsValid)

            this.code
                .compose(Transformers.combineLatestPair(tfaData))
                .compose(Transformers.takeWhen(loginClick))
                .filter { !it.second.isFacebookLogin }
                .switchMap {
                    login(
                        it.first, it.second.email, it.second.password
                    )
                }
                .compose(bindToLifecycle())
                .subscribe { success(it) }

            this.code
                .compose(Transformers.combineLatestPair(tfaFacebookData))
                .compose(Transformers.takeWhen(loginClick))
                .filter { it.second.isFacebookLogin }
                .switchMap {
                    loginWithFacebook(
                        it.first, it.second.fbAccessToken
                    )
                }
                .compose(bindToLifecycle())
                .subscribe { success(it) }

            tfaData
                .compose(Transformers.takeWhen(resendClick))
                .filter { !it.isFacebookLogin }
                .flatMap {
                    resendCode(it.email, it.password)
                }
                .compose(bindToLifecycle())
                .subscribe()

            tfaFacebookData
                .compose(Transformers.takeWhen(resendClick))
                .filter { it.isFacebookLogin }
                .flatMap {
                    resendCodeWithFacebook(it.fbAccessToken)
                }
                .compose(bindToLifecycle())
                .subscribe()

            analyticEvents.trackTwoFactorAuthPageViewed()
        }
    }
}
