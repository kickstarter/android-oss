package com.kickstarter.viewmodels

import com.facebook.FacebookAuthorizationException
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.data.LoginReason
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class LoginToutViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: LoginToutViewModel.LoginToutViewmodel
    private val finishWithSuccessfulResult = TestSubscriber<Unit>()
    private val loginError = TestSubscriber<ErrorEnvelope>()
    private val startLoginActivity = TestSubscriber<Unit>()
    private val startSignupActivity = TestSubscriber<Unit>()
    private val currentUser = TestSubscriber<User?>()
    private val showDisclaimerActivity = TestSubscriber<DisclaimerItems>()
    private val startResetPasswordActivity = TestSubscriber<Unit>()
    private val showFacebookErrorDialog = TestSubscriber<Unit>()

    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment, loginReason: LoginReason) {
        vm = LoginToutViewModel.LoginToutViewmodel(environment)
        vm.outputs.finishWithSuccessfulResult().subscribe { finishWithSuccessfulResult.onNext(it) }
            .addToDisposable(disposables)
        vm.loginError.subscribe { loginError.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startSignupActivity().subscribe { startSignupActivity.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.startLoginActivity().subscribe { startLoginActivity.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.showFacebookErrorDialog().subscribe { showFacebookErrorDialog.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.startResetPasswordActivity().subscribe { startResetPasswordActivity.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.showDisclaimerActivity().subscribe { showDisclaimerActivity.onNext(it) }
            .addToDisposable(disposables)
        environment.currentUser()?.observable()?.subscribe { currentUser.onNext(it) }
        vm.provideLoginReason(loginReason)
    }

    @Test
    fun testLoginButtonClicked() {
        setUpEnvironment(environment(), LoginReason.DEFAULT)

        startLoginActivity.assertNoValues()

        vm.inputs.loginClick()

        startLoginActivity.assertValueCount(1)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testSignupButtonClicked() {
        setUpEnvironment(environment(), LoginReason.DEFAULT)
        startSignupActivity.assertNoValues()

        vm.inputs.signupClick()

        startSignupActivity.assertValueCount(1)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun facebookLogin_success() {
        val currentUser = MockCurrentUser()
        val environment = environment()
            .toBuilder()
            .apiClientV2(MockApiClientV2())
            .apolloClientV2(MockApolloClientV2())
            .currentUser(currentUser)
            .build()
        val user = BehaviorSubject.create<User>()

        setUpEnvironment(environment, LoginReason.DEFAULT)
        environment.currentUser()?.loggedInUser()?.subscribe { user.onNext(it) }

        this.currentUser.values().clear()

        vm.inputs.facebookLoginClick(
            null,
            listOf("public_profile", "user_friends", "email")
        )

        vm.facebookAccessToken.onNext("token")

        this.currentUser.assertValueCount(2)
        finishWithSuccessfulResult.assertValueCount(1)

        assertEquals("some@email.com", user.value?.email())
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun facebookLogin_error_reset_password_WithFeatureFlag_Enabled() {
        val currentUser = MockCurrentUser()
        val mockFeatureFlagClientType: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .featureFlagClient(mockFeatureFlagClientType)
            .build()
        setUpEnvironment(environment, LoginReason.DEFAULT)

        vm.inputs.facebookLoginClick(
            null,
            listOf("public_profile", "user_friends", "email")
        )

        vm.facebookAuthorizationError.onNext(FacebookAuthorizationException())

        finishWithSuccessfulResult.assertNoValues()
        showFacebookErrorDialog.assertValueCount(1)

        vm.inputs.onResetPasswordFacebookErrorDialogClicked()

        startLoginActivity.assertNoValues()
        startResetPasswordActivity.assertValueCount(1)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun facebookLogin_error_login_WithFeatureFlag_Enabled() {
        val currentUser = MockCurrentUser()
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .featureFlagClient(mockFeatureFlagClient)
            .apiClientV2(object : MockApiClientV2() {
                override fun loginWithFacebook(accessToken: String): Observable<AccessTokenEnvelope> {
                    return Observable.error(
                        ApiExceptionFactory.apiError(
                            ErrorEnvelope.builder().httpCode(400).build()
                        )
                    )
                }
            })
            .build()
        setUpEnvironment(environment, LoginReason.DEFAULT)

        this.currentUser.values().clear()

        vm.inputs.facebookLoginClick(
            null,
            listOf("public_profile", "user_friends", "email")
        )

        vm.facebookAccessToken.onNext("token")

        this.currentUser.assertNoValues()
        finishWithSuccessfulResult.assertNoValues()
        showFacebookErrorDialog.assertValueCount(0)
    }

    @Test
    fun facebookLogin_error() {
        val currentUser = MockCurrentUser()
        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .apiClientV2(object : MockApiClientV2() {
                override fun loginWithFacebook(accessToken: String): Observable<AccessTokenEnvelope> {
                    return Observable.error(Throwable("error"))
                }
            })
            .build()
        setUpEnvironment(environment, LoginReason.DEFAULT)

        this.currentUser.values().clear()

        vm.inputs.facebookLoginClick(
            null,
            listOf("public_profile", "user_friends", "email")
        )

        vm.facebookAccessToken.onNext("token")

        this.currentUser.assertNoValues()
        finishWithSuccessfulResult.assertNoValues()
        startResetPasswordActivity.assertNoValues()
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testTermsDisclaimerClicked() {
        setUpEnvironment(environment(), LoginReason.DEFAULT)

        showDisclaimerActivity.assertNoValues()

        vm.inputs.disclaimerItemClicked(DisclaimerItems.TERMS)
        showDisclaimerActivity.assertValue(DisclaimerItems.TERMS)
    }

    @Test
    fun testPrivacyDisclaimerClicked() {
        setUpEnvironment(environment(), LoginReason.DEFAULT)

        showDisclaimerActivity.assertNoValues()

        vm.inputs.disclaimerItemClicked(DisclaimerItems.PRIVACY)
        showDisclaimerActivity.assertValue(DisclaimerItems.PRIVACY)
    }

    @Test
    fun testCookiesDisclaimerClicked() {
        setUpEnvironment(environment(), LoginReason.DEFAULT)

        showDisclaimerActivity.assertNoValues()

        vm.inputs.disclaimerItemClicked(DisclaimerItems.COOKIES)
        showDisclaimerActivity.assertValue(DisclaimerItems.COOKIES)
    }
}
