package com.kickstarter.viewmodels

import android.util.Pair

import com.facebook.CallbackManager
import com.facebook.FacebookAuthorizationException
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.data.LoginReason

import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.neverError
import com.kickstarter.libs.rx.transformers.Transformers.pipeApiErrorsTo

interface LoginToutViewModel {

    interface Inputs {
        /** Call when the Login to Facebook button is clicked.  */
        fun facebookLoginClick(activity: LoginToutActivity, facebookPermissions: List<String>)

        /** Call when the login button is clicked.  */
        fun loginClick()

        /** Call when the signup button is clicked.  */
        fun signupClick()
    }

    interface Outputs {
        /** Emits when a user has successfully logged in; the login flow should finish with a result indicating success.  */
        fun finishWithSuccessfulResult(): Observable<Void>

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
        fun startLoginActivity(): Observable<Void>

        /** Emits when the signup activity should be started.  */
        fun startSignupActivity(): Observable<Void>

        /** Emits when a user has successfully logged in using Facebook, but has require two-factor authentication enabled.  */
        fun startTwoFactorChallenge(): Observable<Void>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<LoginToutActivity>(environment), Inputs, Outputs {
        private var callbackManager: CallbackManager? = null
        private val currentUser: CurrentUserType = environment.currentUser()
        private val client: ApiClientType = environment.apiClient()

        private val facebookAccessToken = PublishSubject.create<String>()
        private val loginClick = PublishSubject.create<Void>()
        private val loginError = PublishSubject.create<ErrorEnvelope>()
        private val loginReason = PublishSubject.create<LoginReason>()
        private val signupClick = PublishSubject.create<Void>()

        private val facebookAuthorizationError = BehaviorSubject.create<FacebookException>()
        private val finishWithSuccessfulResult = BehaviorSubject.create<Void>()
        private val startFacebookConfirmationActivity: Observable<Pair<ErrorEnvelope.FacebookUser, String>>
        private val startLoginActivity: Observable<Void>
        private val startSignupActivity: Observable<Void>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            registerFacebookCallback()

            val facebookSuccessTokenEnvelope = this.facebookAccessToken
                    .switchMap<AccessTokenEnvelope>({ this.loginWithFacebookAccessToken(it) })
                    .share()

            intent()
                    .map { i -> i.getSerializableExtra(IntentKey.LOGIN_REASON) }
                    .ofType(LoginReason::class.java)
                    .compose(bindToLifecycle())
                    .subscribe({ this.loginReason.onNext(it) })

            activityResult()
                    .compose(bindToLifecycle())
                    .subscribe { r -> this.callbackManager!!.onActivityResult(r.requestCode(), r.resultCode(), r.intent()) }

            activityResult()
                    .filter { r -> r.isRequestCode(ActivityRequestCodes.LOGIN_FLOW) }
                    .filter({ it.isOk })
                    .compose(bindToLifecycle())
                    .subscribe { this.finishWithSuccessfulResult.onNext(null) }

            this.facebookAuthorizationError
                    .compose(bindToLifecycle())
                    .subscribe({ this.clearFacebookSession() })

            facebookSuccessTokenEnvelope
                    .compose(bindToLifecycle())
                    .subscribe { envelope ->
                        this.currentUser.login(envelope.user(), envelope.accessToken())
                        this.finishWithSuccessfulResult.onNext(null)
                    }

            this.startFacebookConfirmationActivity = this.loginError
                    .filter({ it.isConfirmFacebookSignupError })
                    .map<ErrorEnvelope.FacebookUser>({ it.facebookUser() })
                    .compose<Pair<ErrorEnvelope.FacebookUser, String>>(combineLatestPair<ErrorEnvelope.FacebookUser, String>(this.facebookAccessToken))

            this.startLoginActivity = this.loginClick
            this.startSignupActivity = this.signupClick

            this.loginReason.take(1)
                    .compose(bindToLifecycle())
                    .subscribe({ this.koala.trackLoginRegisterTout(it) })

            this.loginError
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackLoginError() }

            showMissingFacebookEmailErrorToast()
                    .mergeWith(showFacebookInvalidAccessTokenErrorToast())
                    .mergeWith(showFacebookAuthorizationErrorDialog())
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackFacebookLoginError() }
        }

        private fun clearFacebookSession() = LoginManager.getInstance().logOut()

        private fun loginWithFacebookAccessToken(fbAccessToken: String): Observable<AccessTokenEnvelope> {
            return this.client.loginWithFacebook(fbAccessToken)
                    .compose(pipeApiErrorsTo(this.loginError))
                    .compose(neverError())
        }

        private fun registerFacebookCallback() {
            val fbAccessToken = this.facebookAccessToken
            val fbAuthError = this.facebookAuthorizationError

            this.callbackManager = CallbackManager.Factory.create()

            LoginManager.getInstance().registerCallback(this.callbackManager!!, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    fbAccessToken.onNext(result.accessToken.token)
                }

                override fun onCancel() {
                    // continue
                }

                override fun onError(error: FacebookException) {
                    if (error is FacebookAuthorizationException) {
                        fbAuthError.onNext(error)
                    }
                }
            })
        }

        override fun facebookLoginClick(activity: LoginToutActivity, facebookPermissions: List<String>) =
                LoginManager.getInstance().logInWithReadPermissions(activity, facebookPermissions)

        override fun loginClick() = this.loginClick.onNext(null)

        override fun signupClick() = this.signupClick.onNext(null)

        override fun finishWithSuccessfulResult() = this.finishWithSuccessfulResult

        override fun showFacebookAuthorizationErrorDialog(): Observable<String> {
            return this.facebookAuthorizationError
                    .map({ it.localizedMessage })
        }

        override fun showFacebookInvalidAccessTokenErrorToast(): Observable<String> {
            return this.loginError
                    .filter({ it.isFacebookInvalidAccessTokenError })
                    .map({ it.errorMessage() })
        }

        override fun showMissingFacebookEmailErrorToast(): Observable<String> {
            return this.loginError
                    .filter({ it.isMissingFacebookEmailError })
                    .map({ it.errorMessage() })
        }

        override fun showUnauthorizedErrorDialog(): Observable<String> {
            return this.loginError
                    .filter({ it.isUnauthorizedError })
                    .map({ it.errorMessage() })
        }

        override fun startFacebookConfirmationActivity() = this.startFacebookConfirmationActivity

        override fun startLoginActivity() = this.startLoginActivity

        override fun startSignupActivity() = this.startSignupActivity

        override fun startTwoFactorChallenge(): Observable<Void> {
            return this.loginError
                    .filter({ it.isTfaRequiredError })
                    .map { null }
        }
    }
}
