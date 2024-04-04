package com.kickstarter.viewmodels

import android.app.Activity
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookAuthorizationException
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.usecases.LoginUseCase
import com.kickstarter.viewmodels.usecases.RefreshUserUseCase
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface LoginToutViewModel {
    interface Inputs {
        /** Call when the Login to Facebook button is clicked.  */
        fun facebookLoginClick(activity: LoginToutActivity?, facebookPermissions: List<String>)

        /** Call when the disclaimer Item  is clicked.  */
        fun disclaimerItemClicked(disclaimerItem: DisclaimerItems)

        /** call with facebook error dialog reset password button*/
        fun onResetPasswordFacebookErrorDialogClicked()

        /** call with facebook error dialog login button*/
        fun onLoginFacebookErrorDialogClicked()
    }

    interface Outputs {
        /** Emits when a user has successfully logged in; the login flow should finish with a result indicating success.  */
        fun finishWithSuccessfulResult(): Observable<Unit>

        /** Emits the current user exists alongside oauth token when this activity exists  */
        fun finishOauthWithSuccessfulResult(): Observable<Unit>

        /** Emits when a user has failed to authenticate using Facebook.  */
        fun showFacebookAuthorizationErrorDialog(): Observable<String>

        /** Emits when the API was unable to create a new Facebook user.  */
        fun showFacebookInvalidAccessTokenErrorToast(): Observable<String>

        /** Emits when the API could not retrieve an email for the Facebook user.  */
        fun showMissingFacebookEmailErrorToast(): Observable<String>

        /** Emits when a login attempt is unauthorized.  */
        fun showUnauthorizedErrorDialog(): Observable<String>

        /** Emits a Facebook user and an access token string to confirm Facebook signup.  */
        fun startFacebookConfirmationActivity(): Observable<Pair<ErrorEnvelope.FacebookUser, String>>

        /** Emits when the login activity should be started.  */
        fun startLoginActivity(): Observable<Unit>

        /** Emits when click one of disclaimer items  */
        fun showDisclaimerActivity(): Observable<DisclaimerItems>

        /** Emits when the there is error with facebook login  */
        fun showFacebookErrorDialog(): Observable<Unit>

        /** Emits when the resetPassword should be started.  */
        fun startResetPasswordActivity(): Observable<Unit>
    }

    class LoginToutViewmodel(val environment: Environment) :
        ViewModel(),
        Inputs,
        Outputs {

        private var callbackManager: CallbackManager? = null
        private val client: ApiClientTypeV2 = requireNotNull(environment.apiClientV2())
        private val analyticEvents = environment.analytics()
        private val currentUser = requireNotNull(environment.currentUserV2())

        private fun clearFacebookSession(e: FacebookException) {
            LoginManager.getInstance().logOut()
        }

        private fun loginWithFacebookAccessToken(fbAccessToken: String): Observable<Notification<AccessTokenEnvelope?>> {
            return client.loginWithFacebook(fbAccessToken)
                .materialize()
        }

        private fun registerFacebookCallback() {
            callbackManager = create()

            LoginManager.getInstance()
                .registerCallback(
                    callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onSuccess(result: LoginResult) {
                            facebookAccessToken.onNext(result.accessToken.token)
                        }

                        override fun onCancel() {
                            // continue
                        }

                        override fun onError(error: FacebookException) {
                            if (error is FacebookAuthorizationException) {
                                facebookAuthorizationError.onNext(error)
                            }
                        }
                    }
                )
        }

        @VisibleForTesting
        val facebookAccessToken = PublishSubject.create<String>()
        private val facebookLoginClick = PublishSubject.create<List<String>>()
        private val loginClick = PublishSubject.create<Unit>()
        private val onResetPasswordFacebookErrorDialogClicked = PublishSubject.create<Unit>()
        private val onLoginFacebookErrorDialogClicked = PublishSubject.create<Unit>()

        @VisibleForTesting
        val loginError = PublishSubject.create<ErrorEnvelope?>()
        private val loginReason = PublishSubject.create<LoginReason>()
        private val signupClick = PublishSubject.create<Unit>()
        private val disclaimerItemClicked = PublishSubject.create<DisclaimerItems>()

        @VisibleForTesting
        val facebookAuthorizationError = BehaviorSubject.create<FacebookException>()
        private val finishWithSuccessfulResult = BehaviorSubject.create<Unit>()
        private val showFacebookErrorDialog = BehaviorSubject.create<Unit>()
        private val startResetPasswordActivity = BehaviorSubject.create<Unit>()
        private val startFacebookConfirmationActivity: Observable<Pair<ErrorEnvelope.FacebookUser, String>>
        private val startLoginActivity: Observable<Unit>
        private val showDisclaimerActivity: Observable<DisclaimerItems>

        private val finishOauthWithSuccessfulResult = BehaviorSubject.create<Unit>()

        val inputs: Inputs = this
        val outputs: Outputs = this
        private val loginUserCase = LoginUseCase(environment)
        private val refreshUserUseCase = RefreshUserUseCase(environment)

        private val disposables = CompositeDisposable()
        override fun facebookLoginClick(
            activity: LoginToutActivity?,
            facebookPermissions: List<String>
        ) {
            facebookLoginClick.onNext(facebookPermissions)
            if (activity != null) {
                LoginManager.getInstance()
                    .logInWithReadPermissions(activity, facebookPermissions)
            }
        }

        override fun onLoginFacebookErrorDialogClicked() {
            onLoginFacebookErrorDialogClicked.onNext(Unit)
        }

        override fun onResetPasswordFacebookErrorDialogClicked() {
            onResetPasswordFacebookErrorDialogClicked.onNext(Unit)
        }

        override fun disclaimerItemClicked(disclaimerItem: DisclaimerItems) {
            disclaimerItemClicked.onNext(disclaimerItem)
        }

        override fun finishWithSuccessfulResult(): Observable<Unit> {
            return finishWithSuccessfulResult
        }

        override fun finishOauthWithSuccessfulResult(): Observable<Unit> {
            return finishOauthWithSuccessfulResult
        }
        override fun showFacebookAuthorizationErrorDialog(): Observable<String> {
            return facebookAuthorizationError
                .filter {
                    environment.featureFlagClient()
                        ?.getBoolean(FlagKey.ANDROID_FACEBOOK_LOGIN_REMOVE) == false
                }
                .map { it.localizedMessage }
        }

        override fun showFacebookInvalidAccessTokenErrorToast(): Observable<String> {
            return loginError
                .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
                .map { it.errorMessage() }
        }

        override fun showMissingFacebookEmailErrorToast(): Observable<String> {
            return loginError
                .filter(ErrorEnvelope::isMissingFacebookEmailError)
                .map { it.errorMessage() }
        }

        override fun showUnauthorizedErrorDialog(): Observable<String> {
            return loginError
                .filter(ErrorEnvelope::isUnauthorizedError)
                .map { it.errorMessage() }
        }

        override fun startFacebookConfirmationActivity(): Observable<Pair<ErrorEnvelope.FacebookUser, String>> {
            return startFacebookConfirmationActivity
        }

        override fun startLoginActivity(): Observable<Unit> {
            return startLoginActivity
        }

        override fun showDisclaimerActivity(): Observable<DisclaimerItems> {
            return showDisclaimerActivity
        }

        override fun showFacebookErrorDialog(): Observable<Unit> {
            return showFacebookErrorDialog
        }

        override fun startResetPasswordActivity(): Observable<Unit> {
            return startResetPasswordActivity
        }

        init {
            registerFacebookCallback()

            val facebookAccessTokenEnvelope = facebookAccessToken
                .switchMap {
                    loginWithFacebookAccessToken(
                        it
                    )
                }
                .share()

            facebookAuthorizationError
                .subscribe { clearFacebookSession(it) }
                .addToDisposable(disposables)

            facebookAccessTokenEnvelope
                .compose(Transformers.valuesV2())
                .filter { it.isNotNull() }
                .switchMap {
                    this.loginUserCase
                        .loginAndUpdateUserPrivacy(it.user(), it.accessToken())
                }
                .subscribe {
                    refreshUserUseCase.refresh(it)
                    finishWithSuccessfulResult.onNext(Unit)
                }
                .addToDisposable(disposables)

            facebookAccessTokenEnvelope
                .compose(Transformers.errorsV2())
                .map { ErrorEnvelope.fromThrowable(it) }
                .filter { it.isNotNull() }
                .subscribe { loginError.onNext(it) }
                .addToDisposable(disposables)

            startFacebookConfirmationActivity = loginError
                .filter(ErrorEnvelope::isConfirmFacebookSignupError)
                .filter { it.facebookUser() != null }
                .map { it.facebookUser() }
                .compose(Transformers.combineLatestPair(facebookAccessToken))

            facebookAuthorizationError
                .filter {
                    environment.featureFlagClient()
                        ?.getBoolean(FlagKey.ANDROID_FACEBOOK_LOGIN_REMOVE) == true
                }
                .subscribe {
                    showFacebookErrorDialog.onNext(Unit)
                }
                .addToDisposable(disposables)

            startLoginActivity = loginClick
            showDisclaimerActivity = disclaimerItemClicked

            facebookLoginClick
                .compose(Transformers.ignoreValuesV2())
                .subscribe {
                    analyticEvents?.trackLoginOrSignUpCtaClicked(
                        ContextTypeName.FACEBOOK.contextName,
                        ContextPageName.LOGIN_SIGN_UP.contextName
                    )
                }
                .addToDisposable(disposables)

            onResetPasswordFacebookErrorDialogClicked
                .subscribe { startResetPasswordActivity.onNext(Unit) }
                .addToDisposable(disposables)

            onLoginFacebookErrorDialogClicked
                .subscribe {
                    startLoginActivity.onNext(Unit)
                }
                .addToDisposable(disposables)

            currentUser.observable()
                .filter { it.isPresent() }
                .subscribe {
                    if (currentUser.accessToken != null) {
                        finishOauthWithSuccessfulResult.onNext(Unit)
                    }
                }
                .addToDisposable(disposables)
        }

        fun provideLoginReason(loginReason: LoginReason) {
            this.loginReason.onNext(loginReason)
            analyticEvents?.trackLoginOrSignUpPagedViewed()
        }

        fun provideOnActivityResult(activityResult: ActivityResult) {
            callbackManager?.onActivityResult(
                activityResult.requestCode(),
                activityResult.resultCode(),
                activityResult.intent()
            )

            if (activityResult.isRequestCode(ActivityRequestCodes.LOGIN_FLOW) &&
                activityResult.resultCode() == Activity.RESULT_OK
            ) {
                finishWithSuccessfulResult.onNext(Unit)
            }
        }

        override fun onCleared() {
            super.onCleared()
            disposables.clear()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginToutViewmodel(environment) as T
        }
    }
}
