package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import android.util.Pair

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.StringUtils
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginActivity

import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.neverError
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen

interface LoginViewModel {

    interface Inputs {

        /** Call when the back or close button has been clicked.  */
        fun backOrCloseButtonClicked()

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

        /** Fill the view's email address and show a dialog indicating the user's password has been reset.  */
        fun prefillEmailFromPasswordReset(): Observable<String>

        /** Emits a boolean to determine whether or not the login button should be enabled.  */
        fun showResetPasswordSuccessDialog(): Observable<Pair<Boolean, String>>

        /** Start two factor activity for result.  */
        fun tfaChallenge(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<LoginActivity>(environment), Inputs, Outputs {

        private val backOrCloseButtonClicked = PublishSubject.create<Void>()
        private val emailEditTextChanged = PublishSubject.create<String>()
        private val logInButtonClicked = PublishSubject.create<Void>()
        private val passwordEditTextChanged = PublishSubject.create<String>()
        private val resetPasswordConfirmationDialogDismissed = PublishSubject.create<Boolean>()

        private val genericLoginError: Observable<String>
        private val invalidloginError: Observable<String>
        private val logInButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val loginSuccess = PublishSubject.create<Void>()
        private val prefillEmailFromPasswordReset = BehaviorSubject.create<String>()
        private val showResetPasswordSuccessDialog = BehaviorSubject.create<Pair<Boolean, String>>()
        private val tfaChallenge: Observable<Void>

        private val loginError = PublishSubject.create<ErrorEnvelope>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val client: ApiClientType = environment.apiClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        init {

            val emailAndPassword = this.emailEditTextChanged
                    .compose<Pair<String, String>>(combineLatestPair(this.passwordEditTextChanged))

            val isValid = emailAndPassword
                    .map<Boolean> { isValid(it.first, it.second) }

            val emailFromIntent = intent()
                    .map { i -> i.getStringExtra(IntentKey.EMAIL) }
                    .ofType(String::class.java)

            emailFromIntent
                    .compose(bindToLifecycle())
                    .subscribe(this.prefillEmailFromPasswordReset)

            emailFromIntent
                    .map { e -> Pair.create(true, e) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showResetPasswordSuccessDialog)

            this.resetPasswordConfirmationDialogDismissed
                    .map<Boolean>({ BooleanUtils.negate(it) })
                    .compose<Pair<Boolean, String>>(combineLatestPair(emailFromIntent))
                    .compose(bindToLifecycle<Pair<Boolean, String>>())
                    .subscribe(this.showResetPasswordSuccessDialog)

            isValid
                    .compose(bindToLifecycle())
                    .subscribe(this.logInButtonIsEnabled)

            emailAndPassword
                    .compose(takeWhen<Pair<String, String>, Void>(this.logInButtonClicked))
                    .switchMap { ep -> submit(ep.first, ep.second) }
                    .compose(bindToLifecycle())
                    .subscribe({ this.success(it) })

            this.loginSuccess
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackLoginSuccess() }

            this.genericLoginError = this.loginError
                    .filter({ it.isGenericLoginError })
                    .map({ it.errorMessage() })

            this.invalidloginError = this.loginError
                    .filter({ it.isInvalidLoginError })
                    .map({ it.errorMessage() })

            this.invalidloginError
                    .mergeWith(this.genericLoginError)
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackLoginError() }

            this.tfaChallenge = this.loginError
                    .filter({ it.isTfaRequiredError })
                    .map { null }
        }

        private fun isValid(email: String, password: String) = StringUtils.isEmail(email) && password.isNotEmpty()

        private fun submit(email: String, password: String) =
                this.client.login(email, password)
                        .compose(Transformers.pipeApiErrorsTo(this.loginError))
                        .compose(neverError())

        private fun success(envelope: AccessTokenEnvelope) {
            this.currentUser.login(envelope.user(), envelope.accessToken())
            this.loginSuccess.onNext(null)
        }

        override fun backOrCloseButtonClicked() = this.backOrCloseButtonClicked.onNext(null)

        override fun email(email: String) = this.emailEditTextChanged.onNext(email)

        override fun loginClick() {
            return this.logInButtonClicked.onNext(null)
        }

        override fun password(password: String) = this.passwordEditTextChanged.onNext(password)

        override fun resetPasswordConfirmationDialogDismissed() = this.resetPasswordConfirmationDialogDismissed.onNext(true)

        override fun genericLoginError() = this.genericLoginError

        override fun invalidLoginError() = this.invalidloginError

        override fun loginButtonIsEnabled() = this.logInButtonIsEnabled

        override fun loginSuccess() = this.loginSuccess

        override fun prefillEmailFromPasswordReset() = this.prefillEmailFromPasswordReset

        override fun showResetPasswordSuccessDialog() = this.showResetPasswordSuccessDialog

        override fun tfaChallenge() = this.tfaChallenge
    }
}
