package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.*
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.LoginHelper
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.StringUtils
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginActivity
import com.kickstarter.ui.data.LoginReason
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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
    }

    interface Outputs {
        /** Emits a string to display when log in fails.  */
        fun genericLoginError(): Observable<String>

        /** Emits a string to display when log in fails, specifically for invalid credentials.  */
        fun invalidLoginError(): Observable<String>

        /** Emits a boolean that determines if the log in button is enabled.  */
        fun loginButtonIsEnabled(): Observable<Boolean>

        /** Finish the activity with a successful result.  */
        fun loginSuccess(): Observable<Void>

        /** Fill the view's email address when it's supplied from the intent.  */
        fun prefillEmail(): Observable<String>

        /** Emits when a user has successfully changed their password and needs to login again.  */
        fun showChangedPasswordSnackbar(): Observable<Void>

        /** Emits when a user has successfully created their password and needs to login again. */
        fun showCreatedPasswordSnackbar(): Observable<Void>

        /** Emits a boolean to determine whether reset password dialog should be shown.  */
        fun showResetPasswordSuccessDialog(): Observable<Pair<Boolean, String>>

        /** Start two factor activity for result.  */
        fun tfaChallenge(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<LoginActivity>(environment), Inputs, Outputs {

        private val emailEditTextChanged = PublishSubject.create<String>()
        private val logInButtonClicked = PublishSubject.create<Void>()
        private val passwordEditTextChanged = PublishSubject.create<String>()
        private val resetPasswordConfirmationDialogDismissed = PublishSubject.create<Boolean>()

        private val genericLoginError: Observable<String>
        private val invalidloginError: Observable<String>
        private val logInButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val loginSuccess = PublishSubject.create<Void>()
        private val prefillEmail = BehaviorSubject.create<String>()
        private val showChangedPasswordSnackbar = BehaviorSubject.create<Void>()
        private val showCreatedPasswordSnackbar = BehaviorSubject.create<Void>()
        private val showResetPasswordSuccessDialog = BehaviorSubject.create<Pair<Boolean, String>>()
        private val tfaChallenge: Observable<Void>

        private val loginError = PublishSubject.create<ErrorEnvelope>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val client: ApiClientType = environment.apiClient()
        private val currentUser: CurrentUserType = environment.currentUser()
        private val currentConfig = environment.currentConfig()

        init {

            val emailAndPassword = this.emailEditTextChanged
                    .compose<Pair<String, String>>(combineLatestPair(this.passwordEditTextChanged))

            val isValid = emailAndPassword
                    .map<Boolean> { isValid(it.first, it.second) }

            val emailAndReason = intent()
                    .filter{ it.hasExtra(IntentKey.EMAIL)}
                    .map {
                        Pair.create(it.getStringExtra(IntentKey.EMAIL),
                                if (it.hasExtra(IntentKey.LOGIN_REASON)) {
                                    it.getSerializableExtra(IntentKey.LOGIN_REASON) as LoginReason
                                } else {
                                    LoginReason.DEFAULT
                                })
                    }

            emailAndReason
                    .map { it.first }
                    .ofType(String::class.java)
                    .compose(bindToLifecycle())
                    .subscribe(this.prefillEmail)

            emailAndReason
                    .filter { it.second == LoginReason.RESET_PASSWORD }
                    .map { it.first }
                    .ofType(String::class.java)
                    .map { e -> Pair.create(true, e) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showResetPasswordSuccessDialog)

            emailAndReason
                    .map { it.second }
                    .ofType(LoginReason::class.java)
                    .filter{ LoginReason.CHANGE_PASSWORD == it }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showChangedPasswordSnackbar)

            emailAndReason
                    .map { it.second }
                    .ofType(LoginReason::class.java)
                    .filter{ LoginReason.CREATE_PASSWORD == it }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showCreatedPasswordSnackbar)

            this.resetPasswordConfirmationDialogDismissed
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .compose<Pair<Boolean, Pair<String, LoginReason>>>(combineLatestPair(emailAndReason))
                    .map { Pair.create(it.first, it.second.first) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showResetPasswordSuccessDialog)

            isValid
                    .compose(bindToLifecycle())
                    .subscribe(this.logInButtonIsEnabled)

            val loginNotification = emailAndPassword
                    .compose(takeWhen<Pair<String, String>, Void>(this.logInButtonClicked))
                    .switchMap { ep -> submit(ep.first, ep.second) }

            loginNotification
                    .compose(values())
                    .compose(bindToLifecycle())
                    .subscribe { this.success(it) }

            val isEmailValidated = LoginHelper.hasCurrentUserVerifiedEmail(
                    this.currentUser.observable().distinctUntilChanged(),
                    this.currentConfig.observable().distinctUntilChanged())

            isEmailValidated
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe { isValidated ->
                        if (isValidated) {
                            this.loginSuccess.onNext(null)
                        } else {
                            // TODO: Present Interstitial https://kickstarter.atlassian.net/browse/NT-1652
                        }
                    }

            loginNotification
                    .compose(errors())
                    .map { ErrorEnvelope.fromThrowable(it) }
                    .filter { ObjectUtils.isNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.loginError)

            this.loginSuccess
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackLoginSuccess() }

            this.genericLoginError = this.loginError
                    .filter { it.isGenericLoginError }
                    .map { it.errorMessage() }

            this.invalidloginError = this.loginError
                    .filter { it.isInvalidLoginError }
                    .map { it.errorMessage() }

            this.invalidloginError
                    .mergeWith(this.genericLoginError)
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackLoginError() }

            this.tfaChallenge = this.loginError
                    .filter { it.isTfaRequiredError }
                    .map { null }

            this.logInButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.lake.trackLogInSubmitButtonClicked() }
        }

        private fun isValid(email: String, password: String) = StringUtils.isEmail(email) && password.isNotEmpty()

        private fun submit(email: String, password: String) =
                this.client.login(email, password)
                        .materialize()
                        .share()

        private fun success(envelope: AccessTokenEnvelope) {
            this.currentUser.login(envelope.user(), envelope.accessToken())
        }

        override fun email(email: String) = this.emailEditTextChanged.onNext(email)

        override fun loginClick() {
            return this.logInButtonClicked.onNext(null)
        }

        override fun password(password: String) = this.passwordEditTextChanged.onNext(password)

        override fun resetPasswordConfirmationDialogDismissed() = this.resetPasswordConfirmationDialogDismissed.onNext(true)

        override fun genericLoginError() = this.genericLoginError

        override fun invalidLoginError() = this.invalidloginError

        override fun loginButtonIsEnabled(): BehaviorSubject<Boolean> = this.logInButtonIsEnabled

        override fun loginSuccess(): PublishSubject<Void> = this.loginSuccess

        override fun prefillEmail(): BehaviorSubject<String> = this.prefillEmail

        override fun showChangedPasswordSnackbar(): Observable<Void> = this.showChangedPasswordSnackbar

        override fun showCreatedPasswordSnackbar(): Observable<Void> = this.showCreatedPasswordSnackbar

        override fun showResetPasswordSuccessDialog(): BehaviorSubject<Pair<Boolean, String>> = this.showResetPasswordSuccessDialog

        override fun tfaChallenge() = this.tfaChallenge
    }
}
