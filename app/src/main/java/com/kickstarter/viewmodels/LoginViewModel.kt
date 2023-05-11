package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValuesV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isEmail
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.usecases.LoginUseCase
import com.kickstarter.viewmodels.usecases.RefreshUserUseCase
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface LoginViewModel {

    interface Inputs {
        /** Call when the email field changes.  */
        fun email(email: String)

        /** Call when the log in button has been clicked.  */
        fun loginClick()

        /** Call when the password field changes.  */
        fun password(password: String)

        /** Call when the user cancels or dismisses the reset password success confirmation dialog.  */
        fun resetPasswordConfirmationDialogDismissed()

        fun activityResult(result: ActivityResult)
    }

    interface Outputs {
        /** Emits a string to display when log in fails.  */
        fun genericLoginError(): Observable<String>

        /** Emits a string to display when log in fails, specifically for invalid credentials.  */
        fun invalidLoginError(): Observable<String>

        /** Emits a boolean that determines if the log in button is enabled.  */
        fun loginButtonIsEnabled(): Observable<Boolean>

        /** Finish the activity with a successful result.  */
        fun loginSuccess(): Observable<Unit>

        /** Fill the view's email address when it's supplied from the intent.  */
        fun prefillEmail(): Observable<String>

        /** Emits when a user has successfully changed their password and needs to login again.  */
        fun showChangedPasswordSnackbar(): Observable<Unit>

        /** Emits when a user has successfully created their password and needs to login again. */
        fun showCreatedPasswordSnackbar(): Observable<Unit>

        /** Emits a boolean to determine whether reset password dialog should be shown.  */
        fun showResetPasswordSuccessDialog(): Observable<Pair<Boolean, Pair<String, LoginReason>>>

        /** Start two factor activity for result.  */
        fun tfaChallenge(): Observable<Unit>
    }

    class LoginViewModel(
        private val environment: Environment,
        private val intent: Intent
    ) : ViewModel(), Inputs, Outputs {

        private val emailEditTextChanged = BehaviorSubject.create<String>()
        private val logInButtonClicked = BehaviorSubject.create<Unit>()
        private val passwordEditTextChanged = PublishSubject.create<String>()
        private val resetPasswordConfirmationDialogDismissed = PublishSubject.create<Boolean>()
        private val activityResult = PublishSubject.create<ActivityResult>()

        private val genericLoginError: Observable<String>
        private val invalidloginError: Observable<String>
        private val logInButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val loginSuccess = PublishSubject.create<Unit>()
        private val prefillEmail = BehaviorSubject.create<String>()
        private val showChangedPasswordSnackbar = BehaviorSubject.create<Unit>()
        private val showCreatedPasswordSnackbar = BehaviorSubject.create<Unit>()
        private val showResetPasswordSuccessDialog = BehaviorSubject.create<Pair<Boolean, Pair<String, LoginReason>>>()
        private val tfaChallenge: Observable<Unit>

        private val loginError = PublishSubject.create<ErrorEnvelope>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val client = requireNotNull(environment.apiClientV2())
        private val analyticEvents = requireNotNull(environment.analytics())
        private val loginUserCase = LoginUseCase(environment)
        private val refreshUserUseCase = RefreshUserUseCase(environment)

        private val internalIntent = BehaviorSubject.createDefault(this.intent)

        private val disposables = CompositeDisposable()

        init {

            val emailAndPassword = this.emailEditTextChanged
                .compose<Pair<String, String>>(combineLatestPair(this.passwordEditTextChanged))

            val isValid = emailAndPassword
                .map<Boolean> { isValid(it.first, it.second) }

            val emailAndReason = internalIntent
                .filter { it.hasExtra(IntentKey.EMAIL) }
                .map {
                    Pair.create(
                        it.getStringExtra(IntentKey.EMAIL) ?: "",
                        if (it.hasExtra(IntentKey.LOGIN_REASON)) {
                            it.getSerializableExtra(IntentKey.LOGIN_REASON) as LoginReason
                        } else {
                            LoginReason.DEFAULT
                        }
                    )
                }

            // - Contain the errors if any from the login endpoint response
            val errors = PublishSubject.create<Throwable?>()
            // - Contains success data if any from the login endpoint response
            val successResponseData = PublishSubject.create<AccessTokenEnvelope>()

            emailAndPassword
                .compose(takeWhenV2(this.logInButtonClicked))
                .switchMap { ep -> this.client.login(ep.first, ep.second).materialize() }
                .share()
                .subscribe {
                    this.logInButtonIsEnabled.onNext(true)
                    unwrapNotificationEnvelopeError(it)?.let { noti ->
                        errors.onNext(noti)
                    }
                    unwrapNotificationEnvelopeSuccess(it)?.let { noti ->
                        successResponseData.onNext(noti)
                    }
                }.addToDisposable(disposables)

            logInButtonClicked
                .subscribe {
                    this.logInButtonIsEnabled.onNext(false)
                    this.analyticEvents.trackLogInButtonCtaClicked()
                }
                .addToDisposable(disposables)

            emailAndReason
                .map { it.first }
                .ofType(String::class.java)
                .subscribe(this.prefillEmail)

            emailAndReason
                .filter { it.second == LoginReason.RESET_PASSWORD }
                .map { e -> Pair.create(true, e) }
                .subscribe(this.showResetPasswordSuccessDialog)

            emailAndReason
                .filter { it.second == LoginReason.RESET_FACEBOOK_PASSWORD }
                .map { e -> Pair.create(true, e) }
                .subscribe(this.showResetPasswordSuccessDialog)

            emailAndReason
                .map { it.second }
                .ofType(LoginReason::class.java)
                .filter { LoginReason.CHANGE_PASSWORD == it }
                .compose(ignoreValuesV2())
                .subscribe(this.showChangedPasswordSnackbar)

            emailAndReason
                .map { it.second }
                .ofType(LoginReason::class.java)
                .filter { LoginReason.CREATE_PASSWORD == it }
                .compose(ignoreValuesV2())
                .subscribe(this.showCreatedPasswordSnackbar)

            this.resetPasswordConfirmationDialogDismissed
                .map<Boolean> { it.negate() }
                .compose<Pair<Boolean, Pair<String, LoginReason>>>(combineLatestPair(emailAndReason))
                .map { Pair.create(it.first, it.second) }
                .subscribe(this.showResetPasswordSuccessDialog)

            isValid
                .subscribe(this.logInButtonIsEnabled)

            successResponseData
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .distinctUntilChanged()
                .switchMap {
                    this.loginUserCase
                        .loginAndUpdateUserPrivacyV2(it.user(), it.accessToken())
                }
                .subscribe { user ->
                    this.success(user)
                }
                .addToDisposable(disposables)

            errors
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .map { ErrorEnvelope.fromThrowable(it) }
                .filter { ObjectUtils.isNotNull(it) }
                .subscribe(this.loginError)

            this.genericLoginError = this.loginError
                .filter { it.isGenericLoginError }
                .map { it.errorMessage() }

            this.invalidloginError = this.loginError
                .filter { it.isInvalidLoginError }
                .map { it.errorMessage() }

            this.tfaChallenge = this.loginError
                .filter { it.isTfaRequiredError }
                .map { null }

            this.analyticEvents.trackLoginPagedViewed()
        }

        private fun unwrapNotificationEnvelopeError(notification: Notification<AccessTokenEnvelope>) =
            if (notification.isOnError) notification.error else null

        private fun unwrapNotificationEnvelopeSuccess(notification: Notification<AccessTokenEnvelope>) =
            if (!notification.isOnError) notification.value else null

        private fun isValid(email: String, password: String) = email.isEmail() && password.isNotEmpty()

        private fun success(newUser: User) {
            this.refreshUserUseCase.refresh(newUser)
            this.loginSuccess.onNext(Unit)
        }

        // - Inputs
        override fun email(email: String) = this.emailEditTextChanged.onNext(email)

        override fun loginClick() {
            return this.logInButtonClicked.onNext(Unit)
        }

        override fun password(password: String) = this.passwordEditTextChanged.onNext(password)

        override fun resetPasswordConfirmationDialogDismissed() = this.resetPasswordConfirmationDialogDismissed.onNext(true)

        override fun activityResult(result: ActivityResult) = this.activityResult.onNext(result)

        // - Outputs
        override fun genericLoginError() = this.genericLoginError

        override fun invalidLoginError() = this.invalidloginError

        override fun loginButtonIsEnabled(): BehaviorSubject<Boolean> = this.logInButtonIsEnabled

        override fun loginSuccess(): PublishSubject<Unit> = this.loginSuccess

        override fun prefillEmail(): BehaviorSubject<String> = this.prefillEmail

        override fun showChangedPasswordSnackbar(): Observable<Unit> = this.showChangedPasswordSnackbar

        override fun showCreatedPasswordSnackbar(): Observable<Unit> = this.showCreatedPasswordSnackbar

        override fun showResetPasswordSuccessDialog(): BehaviorSubject<Pair<Boolean, Pair<String, LoginReason>>> = this.showResetPasswordSuccessDialog

        override fun tfaChallenge() = this.tfaChallenge
    }

    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(environment, intent) as T
        }
    }
}
