package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValuesV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.usecases.LoginUseCase
import com.kickstarter.viewmodels.usecases.RefreshUserUseCase
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
        fun isLoading(): Observable<Boolean>

        /** Finish the activity with a successful result.  */
        fun loginSuccess(): Observable<Unit>

        /** Fill the view's email address when it's supplied from the intent.  */
        fun prefillEmail(): Observable<String>

        /** Emits when a user has successfully changed their password and needs to login again.  */
        fun showChangedPasswordSnackbar(): Observable<Boolean>

        /** Emits when a user has successfully created their password and needs to login again. */
        fun showCreatedPasswordSnackbar(): Observable<Boolean>

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

        private val emailAndReason = BehaviorSubject.create<Pair<String, LoginReason>>()
        private val genericLoginError: Observable<String>
        private val invalidloginError: Observable<String>
        private val isLoading = BehaviorSubject.create<Boolean>()
        private val loginSuccess = PublishSubject.create<Unit>()
        private val prefillEmail = BehaviorSubject.create<String>()
        private val showChangedPasswordSnackbar = BehaviorSubject.create<Boolean>()
        private val showCreatedPasswordSnackbar = BehaviorSubject.create<Boolean>()
        private val showResetPasswordSuccessDialog =
            BehaviorSubject.create<Pair<Boolean, Pair<String, LoginReason>>>()
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

            internalIntent
                .filter { it.hasExtra(IntentKey.EMAIL) }
                .map {
                    extractFromIntent(it)
                }
                .subscribe {
                    emailAndReason.onNext(it)
                }
                .addToDisposable(disposables)

            // - Contain the errors if any from the login endpoint response
            val errors = PublishSubject.create<Throwable>()
            // - Contains success data if any from the login endpoint response
            val successResponseData = PublishSubject.create<AccessTokenEnvelope>()

            emailAndPassword
                .compose(takeWhenV2(this.logInButtonClicked))
                .switchMap { ep -> this.client.login(ep.first, ep.second).materialize() }
                .share()
                .subscribe {
                    this.isLoading.onNext(false)
                    if (it.isOnError) {
                        it.error?.let { e -> errors.onNext(e) }
                    } else {
                        it.value?.let { v -> successResponseData.onNext(v) }
                    }
                }.addToDisposable(disposables)

            logInButtonClicked
                .subscribe {
                    this.isLoading.onNext(true)
                    this.analyticEvents.trackLogInButtonCtaClicked()
                }
                .addToDisposable(disposables)

            emailAndReason
                .map { it.first }
                .distinctUntilChanged()
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
                .subscribe {
                    this.showChangedPasswordSnackbar.onNext(true)
                }.addToDisposable(disposables)

            emailAndReason
                .map { it.second }
                .ofType(LoginReason::class.java)
                .filter { LoginReason.CREATE_PASSWORD == it }
                .compose(ignoreValuesV2())
                .subscribe {
                    this.showCreatedPasswordSnackbar.onNext(true)
                }.addToDisposable(disposables)

            this.resetPasswordConfirmationDialogDismissed
                .map<Boolean> { it.negate() }
                .compose<Pair<Boolean, Pair<String, LoginReason>>>(combineLatestPair(emailAndReason))
                .map { Pair.create(it.first, it.second) }
                .subscribe(this.showResetPasswordSuccessDialog)

            successResponseData
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
                .map { }

            this.analyticEvents.trackLoginPagedViewed()

            this.activityResult
                .filter { it.isRequestCode(ActivityRequestCodes.RESET_FLOW) }
                .filter(ActivityResult::isOk)
                .subscribe {
                    it.intent?.let { intent ->
                        this.emailAndReason.onNext(extractFromIntent(intent))
                    }
                }
                .addToDisposable(disposables)
        }

        private fun extractFromIntent(it: Intent): Pair<String, LoginReason> =
            Pair.create(
                it.getStringExtra(IntentKey.EMAIL) ?: "",
                if (it.hasExtra(IntentKey.LOGIN_REASON)) {
                    (it.getSerializableExtra(IntentKey.LOGIN_REASON) as LoginReason?)
                        ?: LoginReason.DEFAULT
                } else {
                    LoginReason.DEFAULT
                }
            )

        private fun success(newUser: User) {
            this.refreshUserUseCase.refresh(newUser)
            this.loginSuccess.onNext(Unit)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        fun hideChangePasswordSnackbar() {
            showChangedPasswordSnackbar.onNext(false)
        }

        fun hideCreatedPasswordSnackbar() {
            showCreatedPasswordSnackbar.onNext(false)
        }

        // - Inputs
        override fun email(email: String) = this.emailEditTextChanged.onNext(email)

        override fun loginClick() {
            return this.logInButtonClicked.onNext(Unit)
        }

        override fun password(password: String) = this.passwordEditTextChanged.onNext(password)

        override fun resetPasswordConfirmationDialogDismissed() =
            this.resetPasswordConfirmationDialogDismissed.onNext(true)

        override fun activityResult(result: ActivityResult) = this.activityResult.onNext(result)

        // - Outputs
        override fun genericLoginError() = this.genericLoginError

        override fun invalidLoginError() = this.invalidloginError

        override fun isLoading(): BehaviorSubject<Boolean> = this.isLoading

        override fun loginSuccess(): PublishSubject<Unit> = this.loginSuccess

        override fun prefillEmail(): BehaviorSubject<String> = this.prefillEmail

        override fun showChangedPasswordSnackbar(): Observable<Boolean> =
            this.showChangedPasswordSnackbar

        override fun showCreatedPasswordSnackbar(): Observable<Boolean> =
            this.showCreatedPasswordSnackbar

        override fun showResetPasswordSuccessDialog(): BehaviorSubject<Pair<Boolean, Pair<String, LoginReason>>> =
            this.showResetPasswordSuccessDialog

        override fun tfaChallenge() = this.tfaChallenge
    }

    class Factory(private val environment: Environment, private val intent: Intent) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(environment, intent) as T
        }
    }
}
