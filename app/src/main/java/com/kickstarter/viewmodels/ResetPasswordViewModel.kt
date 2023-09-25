package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isEmail
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ResetPasswordScreenState
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ResetPasswordViewModel {

    interface Inputs {

        /** Call when the reset password button is clicked. */
        fun resetPasswordClick()

        fun configureWith(intent: Intent)
    }

    interface Outputs {
        /** Emits a boolean that determines if the form is in the progress of being submitted. */
        fun isFormSubmitting(): Observable<Boolean>

        /** Emits when password reset is completed successfully. */
        fun resetLoginPasswordSuccess(): Observable<Unit>

        /** Emits when password reset is completed successfully. */
        fun resetFacebookLoginPasswordSuccess(): Observable<Unit>

        /** Emits when password reset fails. */
        fun resetError(): Observable<String>

        /** Fill the view's email address when it's supplied from the intent.  */
        fun prefillEmail(): Observable<String>

        /** Fill the view's for forget or reset password state   */
        fun resetPasswordScreenStatus(): Observable<ResetPasswordScreenState>
    }

    class ResetPasswordViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {
        private val client = requireNotNull(environment.apiClientV2())

        private val email = PublishSubject.create<String>()
        private val resetPasswordClick = PublishSubject.create<Unit>()

        private val isFormSubmitting = PublishSubject.create<Boolean>()
        private val isFormValid = PublishSubject.create<Boolean>()
        private val resetLoginPasswordSuccess = PublishSubject.create<Unit>()
        private val resetFacebookLoginPasswordSuccess = PublishSubject.create<Unit>()
        private val resetError = PublishSubject.create<ErrorEnvelope>()
        private val prefillEmail = BehaviorSubject.create<String>()
        private val resetPasswordScreenStatus = BehaviorSubject.create<ResetPasswordScreenState>()
        private val intent = BehaviorSubject.create<Intent>()
        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        // TODO removed with feature flag ANDROID_FACEBOOK_LOGIN_REMOVE
        private var resetPasswordScreenState: ResetPasswordScreenState? = null

        init {

            intent
                .filter { it.hasExtra(IntentKey.EMAIL) }
                .map {
                    it.getStringExtra(IntentKey.EMAIL)
                }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .subscribe {
                    this.prefillEmail.onNext(it)
                    resetPasswordScreenState = ResetPasswordScreenState.ForgetPassword
                    resetPasswordScreenStatus.onNext(ResetPasswordScreenState.ForgetPassword)
                }.addToDisposable(disposables)

            val resetFacebookPasswordFlag = intent
                .filter {
                    it.hasExtra(IntentKey.RESET_PASSWORD_FACEBOOK_LOGIN) && environment.featureFlagClient()?.getBoolean(
                        FlagKey.ANDROID_FACEBOOK_LOGIN_REMOVE
                    ) == true
                }
                .map {
                    it.getBooleanExtra(IntentKey.RESET_PASSWORD_FACEBOOK_LOGIN, false)
                }

            resetFacebookPasswordFlag
                .subscribe {
                    if (it) {
                        resetPasswordScreenState = ResetPasswordScreenState.ResetPassword
                        resetPasswordScreenStatus.onNext(ResetPasswordScreenState.ResetPassword)
                    } else {
                        resetPasswordScreenState = ResetPasswordScreenState.ForgetPassword
                        resetPasswordScreenStatus.onNext(ResetPasswordScreenState.ForgetPassword)
                    }
                }.addToDisposable(disposables)

            this.email
                .map { it.isEmail() }
                .subscribe { this.isFormValid.onNext(it) }
                .addToDisposable(disposables)

            val resetPasswordNotification = this.email
                .compose<String>(Transformers.takeWhenV2(this.resetPasswordClick))
                .switchMap(this::submitEmail)
                .share()

            resetPasswordNotification
                .compose(valuesV2())
                .subscribe {
                    when (resetPasswordScreenState) {
                        ResetPasswordScreenState.ResetPassword -> resetFacebookLoginPasswordSuccess.onNext(
                            Unit
                        )
                        else -> success()
                    }
                }.addToDisposable(disposables)

            resetPasswordNotification
                .compose(errorsV2())
                .map { ErrorEnvelope.fromThrowable(it) }
                .subscribe { this.resetError.onNext(it) }
                .addToDisposable(disposables)
        }

        fun setEmail(email: String) {
            this.email.onNext(email)
        }

        fun resetErrorMessage() {
            this.resetError.onNext(ErrorEnvelope.builder().build())
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        private fun success() {
            this.resetLoginPasswordSuccess.onNext(Unit)
        }

        private fun submitEmail(email: String): Observable<Notification<User>> {
            return this.client.resetPassword(email)
                .doOnSubscribe { this.isFormSubmitting.onNext(true) }
                .doAfterTerminate { this.isFormSubmitting.onNext(false) }
                .materialize()
                .share()
        }

        override fun configureWith(intent: Intent) = this.intent.onNext(intent)

        override fun resetPasswordClick() {
            this.resetPasswordClick.onNext(Unit)
        }

        override fun isFormSubmitting(): Observable<Boolean> {
            return this.isFormSubmitting
        }

        override fun resetLoginPasswordSuccess(): Observable<Unit> {
            return this.resetLoginPasswordSuccess
        }

        override fun resetFacebookLoginPasswordSuccess(): Observable<Unit> {
            return this.resetFacebookLoginPasswordSuccess
        }

        override fun resetError(): Observable<String> {
            return this.resetError
                .takeUntil(this.resetLoginPasswordSuccess)
                .map { it.errorMessage() }
        }

        override fun prefillEmail(): BehaviorSubject<String> = this.prefillEmail

        override fun resetPasswordScreenStatus(): Observable<ResetPasswordScreenState> = this.resetPasswordScreenStatus
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ResetPasswordViewModel(environment) as T
        }
    }
}
