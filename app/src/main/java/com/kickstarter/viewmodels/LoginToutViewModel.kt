package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.VisibleForTesting
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookAuthorizationException
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.usecases.LoginUseCase
import rx.Notification
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface LoginToutViewModel {
    interface Inputs {
        /** Call when the Login to Facebook button is clicked.  */
        fun facebookLoginClick(activity: LoginToutActivity?, facebookPermissions: List<String>)

        /** Call when the login button is clicked.  */
        fun loginClick()

        /** Call when the signup button is clicked.  */
        fun signupClick()

        /** Call when the disclaimer Item  is clicked.  */
        fun disclaimerItemClicked(disclaimerItem: DisclaimerItems)

        /** call with facebook error dialog reset password button*/
        fun onResetPasswordFacebookErrorDialogClicked()

        /** call with facebook error dialog login button*/
        fun onLoginFacebookErrorDialogClicked()
    }

    interface Outputs {
        /** Emits when a user has successfully logged in; the login flow should finish with a result indicating success.  */
        fun finishWithSuccessfulResult(): Observable<Void>

        /** Emits when a user has failed to authenticate using Facebook.  */
        fun showFacebookAuthorizationErrorDialog(): Observable<String>

        /** Emits when the API was unable to create a new Facebook user.  */
        fun showFacebookInvalidAccessTokenErrorToast(): Observable<String?>

        /** Emits when the API could not retrieve an email for the Facebook user.  */
        fun showMissingFacebookEmailErrorToast(): Observable<String?>

        /** Emits when a login attempt is unauthorized.  */
        fun showUnauthorizedErrorDialog(): Observable<String>

        /** Emits a Facebook user and an access token string to confirm Facebook signup.  */
        fun startFacebookConfirmationActivity(): Observable<Pair<ErrorEnvelope.FacebookUser, String>>

        /** Emits when the login activity should be started.  */
        fun startLoginActivity(): Observable<Void>

        /** Emits when the signup activity should be started.  */
        fun startSignupActivity(): Observable<Void>

        /** Emits when a user has successfully logged in using Facebook, but has require two-factor authentication enabled.  */
        fun startTwoFactorChallenge(): Observable<Void>

        /** Emits when click one of disclaimer items  */
        fun showDisclaimerActivity(): Observable<DisclaimerItems>

        /** Emits when the there is error with facebook login  */
        fun showFacebookErrorDialog(): Observable<Void>

        /** Emits when the resetPassword should be started.  */
        fun startResetPasswordActivity(): Observable<Void>
    }

    class ViewModel(val environment: Environment) :
        ActivityViewModel<LoginToutActivity>(environment),
        Inputs,
        Outputs {

        private var callbackManager: CallbackManager? = null
        private val client: ApiClientType = requireNotNull(environment.apiClient())

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

        @VisibleForTesting val facebookAccessToken = PublishSubject.create<String>()
        private val facebookLoginClick = PublishSubject.create<List<String>>()
        private val loginClick = PublishSubject.create<Void>()
        private val onResetPasswordFacebookErrorDialogClicked = PublishSubject.create<Void>()
        private val onLoginFacebookErrorDialogClicked = PublishSubject.create<Void>()

        @VisibleForTesting val loginError = PublishSubject.create<ErrorEnvelope?>()
        private val loginReason = PublishSubject.create<LoginReason>()
        private val signupClick = PublishSubject.create<Void>()
        private val disclaimerItemClicked = PublishSubject.create<DisclaimerItems>()

        @VisibleForTesting val facebookAuthorizationError = BehaviorSubject.create<FacebookException>()
        private val finishWithSuccessfulResult = BehaviorSubject.create<Void>()
        private val showFacebookErrorDialog = BehaviorSubject.create<Void>()
        private val startResetPasswordActivity = BehaviorSubject.create<Void>()
        private val startFacebookConfirmationActivity: Observable<Pair<ErrorEnvelope.FacebookUser, String>>
        private val startLoginActivity: Observable<Void>
        private val startSignupActivity: Observable<Void>
        private val showDisclaimerActivity: Observable<DisclaimerItems>

        val inputs: Inputs = this
        val outputs: Outputs = this
        private val loginUserCase = LoginUseCase(environment)

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
            onLoginFacebookErrorDialogClicked.onNext(null)
        }

        override fun onResetPasswordFacebookErrorDialogClicked() {
            onResetPasswordFacebookErrorDialogClicked.onNext(null)
        }

        override fun loginClick() {
            loginClick.onNext(null)
        }

        override fun signupClick() {
            signupClick.onNext(null)
        }

        override fun disclaimerItemClicked(disclaimerItem: DisclaimerItems) {
            disclaimerItemClicked.onNext(disclaimerItem)
        }

        override fun finishWithSuccessfulResult(): Observable<Void> {
            return finishWithSuccessfulResult
        }

        override fun showFacebookAuthorizationErrorDialog(): Observable<String> {
            return facebookAuthorizationError
                .filter { environment.optimizely()?.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_FACEBOOK_LOGIN_REMOVE) == false }
                .map { it.localizedMessage }
        }

        override fun showFacebookInvalidAccessTokenErrorToast(): Observable<String?> {
            return loginError
                .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
                .map { it.errorMessage() }
        }

        override fun showMissingFacebookEmailErrorToast(): Observable<String?> {
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

        override fun startLoginActivity(): Observable<Void> {
            return startLoginActivity
        }

        override fun startSignupActivity(): Observable<Void> {
            return startSignupActivity
        }

        override fun startTwoFactorChallenge(): Observable<Void> {
            return loginError
                .filter(ErrorEnvelope::isTfaRequiredError)
                .map { null }
        }

        override fun showDisclaimerActivity(): Observable<DisclaimerItems> {
            return showDisclaimerActivity
        }

        override fun showFacebookErrorDialog(): Observable<Void> {
            return showFacebookErrorDialog
        }

        override fun startResetPasswordActivity(): Observable<Void> {
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

            intent()
                .map { it.getSerializableExtra(IntentKey.LOGIN_REASON) }
                .ofType(LoginReason::class.java)
                .compose(bindToLifecycle())
                .subscribe { it: LoginReason ->
                    loginReason.onNext(it)
                    analyticEvents.trackLoginOrSignUpPagedViewed()
                }

            activityResult()
                .compose(bindToLifecycle())
                .subscribe {
                    callbackManager?.onActivityResult(
                        it.requestCode(),
                        it.resultCode(),
                        it.intent()
                    )
                }

            activityResult()
                .filter { it.isRequestCode(ActivityRequestCodes.LOGIN_FLOW) }
                .filter(ActivityResult::isOk)
                .compose(bindToLifecycle())
                .subscribe { finishWithSuccessfulResult.onNext(null) }

            facebookAuthorizationError
                .compose(bindToLifecycle())
                .subscribe { clearFacebookSession(it) }

            facebookAccessTokenEnvelope
                .compose(Transformers.values())
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    loginUserCase.login(it.user(), it.accessToken())
                    finishWithSuccessfulResult.onNext(null)
                }

            facebookAccessTokenEnvelope
                .compose(Transformers.errors())
                .map { ErrorEnvelope.fromThrowable(it) }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe { loginError.onNext(it) }

            startFacebookConfirmationActivity = loginError
                .filter(ErrorEnvelope::isConfirmFacebookSignupError)
                .map { it.facebookUser() }
                .compose(Transformers.combineLatestPair(facebookAccessToken))

            facebookAuthorizationError
                .filter {
                    environment.optimizely()?.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_FACEBOOK_LOGIN_REMOVE) == true
                }
                .compose(bindToLifecycle())
                .subscribe {
                    showFacebookErrorDialog.onNext(null)
                }

            startLoginActivity = loginClick
            startSignupActivity = signupClick
            showDisclaimerActivity = disclaimerItemClicked

            facebookLoginClick
                .compose(Transformers.ignoreValues())
                .compose(bindToLifecycle())
                .subscribe {
                    analyticEvents.trackLoginOrSignUpCtaClicked(
                        ContextTypeName.FACEBOOK.contextName,
                        ContextPageName.LOGIN_SIGN_UP.contextName
                    )
                }

            loginClick
                .compose(bindToLifecycle())
                .subscribe { analyticEvents.trackLogInInitiateCtaClicked() }

            signupClick
                .compose(bindToLifecycle())
                .subscribe { analyticEvents.trackSignUpInitiateCtaClicked() }

            onResetPasswordFacebookErrorDialogClicked
                .compose(bindToLifecycle())
                .subscribe { startResetPasswordActivity.onNext(null) }

            onLoginFacebookErrorDialogClicked
                .compose(bindToLifecycle())
                .subscribe { startLoginActivity.onNext(null) }
        }
    }
}
