package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.isEmail
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.activities.SignupActivity
import com.kickstarter.viewmodels.usecases.LoginUseCase
import com.kickstarter.viewmodels.usecases.RefreshUserUseCase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface SignupViewModel {
    interface Inputs {
        /** Call when the email field changes.  */
        fun email(email: String)

        /** Call when the name field changes.  */
        fun name(name: String)

        /** Call when the password field changes.  */
        fun password(password: String)

        /** Call when the send newsletter toggle changes.  */
        fun sendNewslettersClick(send: Boolean)

        /** Call when the signup button has been clicked.  */
        fun signupClick()
    }

    interface Outputs {
        /** Emits a string to display when signup fails.  */
        fun errorString(): Observable<String>

        /** Emits a boolean that determines if the sign up button is enabled.  */
        fun formIsValid(): Observable<Boolean>

        /** Emits a boolean that determines if the sign up button is disabled.  */
        fun formSubmitting(): Observable<Boolean>

        /** Emits a boolean that determines if the send newsletter toggle is checked.  */
        fun sendNewslettersIsChecked(): Observable<Boolean>

        /** Finish the activity with a successful result.  */
        fun signupSuccess(): Observable<Void>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<SignupActivity>(environment),
        Inputs,
        Outputs {
        private val client: ApiClientType
        private val currentConfig: CurrentConfigType
        private val loginUserCase = LoginUseCase(environment)
        private val refreshUserUseCase = RefreshUserUseCase(environment)

        private fun submit(data: SignupData): Observable<AccessTokenEnvelope> {
            return client.signup(
                data.name,
                data.email,
                data.password,
                data.password,
                data.sendNewsletters
            )
                .compose(Transformers.pipeApiErrorsTo(signupError))
                .compose(Transformers.neverError())
                .doOnSubscribe { formSubmitting.onNext(true) }
                .doAfterTerminate { formSubmitting.onNext(false) }
        }

        private fun success(user: User) {
            refreshUserUseCase.refresh(user)
            signupSuccess.onNext(null)
        }

        private val email = PublishSubject.create<String>()
        private val name = PublishSubject.create<String>()
        private val password = PublishSubject.create<String>()
        private val sendNewslettersClick = PublishSubject.create<Boolean>()
        private val signupClick = PublishSubject.create<Void>()
        private val errorString: Observable<String>
        private val signupSuccess = PublishSubject.create<Void>()
        private val formSubmitting = BehaviorSubject.create<Boolean>()
        private val formIsValid = BehaviorSubject.create<Boolean>()
        private val sendNewslettersIsChecked = BehaviorSubject.create<Boolean>()
        private val showInterstitial = BehaviorSubject.create<Void>()
        private val signupError = PublishSubject.create<ErrorEnvelope?>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            client = requireNotNull(environment.apiClient())
            currentConfig = requireNotNull(environment.currentConfig())

            val signupData = Observable.combineLatest(
                name,
                email,
                password,
                sendNewslettersIsChecked
            ) { name: String, email: String, password: String, sendNewsletters: Boolean ->
                SignupData(
                    name,
                    email,
                    password,
                    sendNewsletters
                )
            }

            sendNewslettersClick
                .compose(bindToLifecycle())
                .subscribe { sendNewslettersIsChecked.onNext(it) }

            signupData
                .map { it.isValid }
                .compose(bindToLifecycle())
                .subscribe(formIsValid)

            signupData
                .compose(Transformers.takeWhen(signupClick))
                .switchMap { submit(it) }
                .distinctUntilChanged()
                .switchMap {
                    this.loginUserCase
                        .loginAndUpdateUserPrivacy(it.user(), it.accessToken())
                }
                .compose(bindToLifecycle())
                .subscribe { success(it) }

            currentConfig.observable()
                .take(1)
                .map { false }
                .compose(bindToLifecycle())
                .subscribe { sendNewslettersIsChecked.onNext(it) }

            errorString = signupError
                .takeUntil(signupSuccess)
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .map { it.errorMessage() }

            signupClick
                .compose(bindToLifecycle())
                .subscribe { analyticEvents.trackSignUpSubmitCtaClicked() }

            analyticEvents.trackSignUpPageViewed()
        }

        override fun email(email: String) {
            this.email.onNext(email)
        }

        override fun name(name: String) {
            this.name.onNext(name)
        }

        override fun password(password: String) {
            this.password.onNext(password)
        }

        override fun sendNewslettersClick(send: Boolean) {
            sendNewslettersClick.onNext(send)
        }

        override fun signupClick() {
            signupClick.onNext(null)
        }

        override fun errorString(): Observable<String> = errorString
        override fun formIsValid(): BehaviorSubject<Boolean> = formIsValid
        override fun formSubmitting(): BehaviorSubject<Boolean> = formSubmitting
        override fun sendNewslettersIsChecked(): BehaviorSubject<Boolean> = sendNewslettersIsChecked
        override fun signupSuccess(): PublishSubject<Void> = signupSuccess

        internal class SignupData(
            val name: String,
            val email: String,
            val password: String,
            val sendNewsletters: Boolean
        ) {
            val isValid: Boolean
                get() = name.isNotEmpty() && email.isEmail() && password.length >= 6
        }
    }
}
