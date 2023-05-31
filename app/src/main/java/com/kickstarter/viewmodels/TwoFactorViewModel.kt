package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.usecases.LoginUseCase
import com.kickstarter.viewmodels.usecases.RefreshUserUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

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
        fun genericTfaError(): Observable<Unit>

        /** Emits when TFA code was submitted.  */
        fun formSubmitting(): Observable<Boolean>

        /** Emits when TFA code submission has completed.  */
        fun formIsValid(): Observable<Boolean>

        /** Emits when resend code confirmation should be shown.  */
        fun showResendCodeConfirmation(): Observable<Unit>

        /** Emits when a submitted TFA code does not match.  */
        fun tfaCodeMismatchError(): Observable<Unit>

        /** Emits when submitting TFA code was successful.  */
        fun tfaSuccess(): Observable<Unit>
    }

    class TwoFactorViewModel(
        environment: Environment,
        intent: Intent? = null
    ) : ViewModel(), Inputs, Outputs {
        private val client: ApiClientTypeV2 = requireNotNull(environment.apiClientV2())
        private val analytics = requireNotNull(environment.analytics())
        private val loginUserCase = LoginUseCase(environment)
        private val refreshUserUseCase = RefreshUserUseCase(environment)
        private val internalIntent = BehaviorSubject.createDefault(intent)
        private val disposables = CompositeDisposable()

        private fun success(user: User) {
            refreshUserUseCase.refresh(user)
            tfaSuccess.onNext(Unit)
        }

        private fun login(
            code: String,
            email: String,
            password: String
        ): Observable<AccessTokenEnvelope> {
            return client.login(email, password, code)
                .compose(Transformers.pipeApiErrorsToV2(tfaError))
                .compose(Transformers.neverErrorV2())
                .doOnSubscribe { formSubmitting.onNext(true) }
                .doAfterTerminate { formSubmitting.onNext(false) }
        }

        private fun loginWithFacebook(
            code: String,
            fbAccessToken: String
        ): Observable<AccessTokenEnvelope> {
            return client.loginWithFacebook(fbAccessToken, code)
                .compose(Transformers.pipeApiErrorsToV2(tfaError))
                .compose(Transformers.neverErrorV2())
                .doOnSubscribe { formSubmitting.onNext(true) }
                .doAfterTerminate { formSubmitting.onNext(false) }
        }

        private fun resendCode(email: String, password: String): Observable<AccessTokenEnvelope> {
            return client.login(email, password)
                .compose(Transformers.neverErrorV2())
                .doOnSubscribe { showResendCodeConfirmation.onNext(Unit) }
        }

        private fun resendCodeWithFacebook(fbAccessToken: String): Observable<AccessTokenEnvelope> {
            return client.loginWithFacebook(fbAccessToken)
                .compose(Transformers.neverErrorV2())
                .doOnSubscribe { showResendCodeConfirmation.onNext(Unit) }
        }

        private val code = PublishSubject.create<String>()
        private val loginClick = PublishSubject.create<Unit>()
        private val resendClick = PublishSubject.create<Unit>()
        private val formIsValid = PublishSubject.create<Boolean>()
        private val formSubmitting = PublishSubject.create<Boolean>()
        private val showResendCodeConfirmation = PublishSubject.create<Unit>()
        private val tfaError = PublishSubject.create<ErrorEnvelope>()
        private val tfaSuccess = PublishSubject.create<Unit>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        override fun formIsValid(): Observable<Boolean> = formIsValid

        override fun formSubmitting(): Observable<Boolean> = formSubmitting

        override fun genericTfaError(): Observable<Unit> {
            return tfaError
                .filter { env: ErrorEnvelope -> !env.isTfaFailedError }
                .map { Unit }
        }

        override fun showResendCodeConfirmation(): Observable<Unit> = showResendCodeConfirmation

        override fun tfaCodeMismatchError(): Observable<Unit> {
            return tfaError
                .filter(ErrorEnvelope::isTfaFailedError)
                .map { Unit }
        }

        override fun tfaSuccess(): Observable<Unit> {
            return tfaSuccess
        }

        override fun code(s: String) {
            this.code.onNext(s)
        }

        override fun loginClick() {
            loginClick.onNext(Unit)
        }

        override fun resendClick() {
            resendClick.onNext(Unit)
        }

        protected inner class TfaData(
            val email: String,
            val isFacebookLogin: Boolean,
            val password: String
        )

        protected inner class TfaDataForFacebook(
            val fbAccessToken: String,
            val isFacebookLogin: Boolean
        )
        companion object {
            private fun isCodeValid(code: String?): Boolean {
                return code != null && code.isNotEmpty()
            }
        }

        init {

            val email = internalIntent
                .map { it.getStringExtra(IntentKey.EMAIL) }
                .filter { ObjectUtils.isNotNull(it) }

            val fbAccessToken = internalIntent
                .map { it.getStringExtra(IntentKey.FACEBOOK_TOKEN) }

            val isFacebookLogin = internalIntent
                .map { it.getBooleanExtra(IntentKey.FACEBOOK_LOGIN, false) }

            val password = internalIntent
                .map { it.getStringExtra(IntentKey.PASSWORD) }
                .filter { ObjectUtils.isNotNull(it) }

            val tfaData = Observable.combineLatest(
                email,
                isFacebookLogin,
                password
            ) { email: String, isFacebookLogin: Boolean, password: String ->
                TfaData(
                    email,
                    isFacebookLogin,
                    password
                )
            }

            val tfaFacebookData = Observable.combineLatest(
                fbAccessToken,
                isFacebookLogin
            ) { fbAccessToken: String?, isFacebookLogin: Boolean ->
                TfaDataForFacebook(
                    fbAccessToken = fbAccessToken ?: "",
                    isFacebookLogin = isFacebookLogin
                )
            }

            this.code
                .map { code: String? -> isCodeValid(code) }
                .subscribe { formIsValid.onNext(it) }
                .addToDisposable(disposables)


            this.code
                .compose(Transformers.combineLatestPair(tfaData))
                .compose(Transformers.takeWhenV2(loginClick))
                .filter { !it.second.isFacebookLogin }
                .switchMap {
                    login(
                        it.first,
                        it.second.email,
                        it.second.password
                    )
                }
                .switchMap {
                    this.loginUserCase
                        .loginAndUpdateUserPrivacyV2(it.user(), it.accessToken())
                }
                .subscribe { success(it) }
                .addToDisposable(disposables)

            this.code
                .compose(Transformers.combineLatestPair(tfaFacebookData))
                .compose(Transformers.takeWhenV2(loginClick))
                .filter { it.second.isFacebookLogin }
                .switchMap {
                    loginWithFacebook(
                        it.first,
                        it.second.fbAccessToken
                    )
                }
                .switchMap {
                    this.loginUserCase
                        .loginAndUpdateUserPrivacyV2(it.user(), it.accessToken())
                }
                .subscribe { success(it) }
                .addToDisposable(disposables)

            tfaData
                .compose(Transformers.takeWhenV2(resendClick))
                .filter { !it.isFacebookLogin }
                .flatMap {
                    resendCode(it.email, it.password)
                }
                .subscribe()
                .addToDisposable(disposables)

            tfaFacebookData
                .compose(Transformers.takeWhenV2(resendClick))
                .filter { it.isFacebookLogin }
                .flatMap {
                    resendCodeWithFacebook(it.fbAccessToken)
                }
                .subscribe()
                .addToDisposable(disposables)

            analytics.trackTwoFactorAuthPageViewed()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TwoFactorViewModel(environment) as T
        }
    }
}
