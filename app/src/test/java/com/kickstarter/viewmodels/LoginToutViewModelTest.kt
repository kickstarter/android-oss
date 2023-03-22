package com.kickstarter.viewmodels

import android.content.Intent
import com.facebook.FacebookAuthorizationException
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.data.LoginReason
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject

class LoginToutViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: LoginToutViewModel.ViewModel
    private val finishWithSuccessfulResult = TestSubscriber<Void>()
    private val loginError = TestSubscriber<ErrorEnvelope>()
    private val startLoginActivity = TestSubscriber<Void>()
    private val startSignupActivity = TestSubscriber<Void>()
    private val currentUser = TestSubscriber<User?>()
    private val showDisclaimerActivity = TestSubscriber<DisclaimerItems>()
    private val startResetPasswordActivity = TestSubscriber<Void>()
    private val showFacebookErrorDialog = TestSubscriber<Void>()

    private fun setUpEnvironment(environment: Environment, loginReason: LoginReason) {
        vm = LoginToutViewModel.ViewModel(environment)
        vm.outputs.finishWithSuccessfulResult().subscribe(finishWithSuccessfulResult)
        vm.loginError.subscribe(loginError)
        vm.outputs.startSignupActivity().subscribe(startSignupActivity)
        vm.outputs.startLoginActivity().subscribe(startLoginActivity)
        vm.outputs.showFacebookErrorDialog().subscribe(showFacebookErrorDialog)
        vm.outputs.startResetPasswordActivity().subscribe(startResetPasswordActivity)
        vm.outputs.showDisclaimerActivity().subscribe(showDisclaimerActivity)
        environment.currentUser()?.observable()?.subscribe(currentUser)
        vm.intent(Intent().putExtra(IntentKey.LOGIN_REASON, loginReason))
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
            .currentUser(currentUser)
            .build()
        val user = BehaviorSubject.create<User>()

        setUpEnvironment(environment, LoginReason.DEFAULT)
        environment.currentUser()?.loggedInUser()?.subscribe(user)

        this.currentUser.assertValuesAndClear(null)

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
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .optimizely(mockExperimentsClientType)
            .build()
        setUpEnvironment(environment, LoginReason.DEFAULT)

        this.currentUser.assertValuesAndClear(null)

        vm.inputs.facebookLoginClick(
            null,
            listOf("public_profile", "user_friends", "email")
        )

        vm.facebookAuthorizationError.onNext(FacebookAuthorizationException())

        this.currentUser.assertNoValues()
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
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .optimizely(mockExperimentsClientType)
            .apiClient(object : MockApiClient() {
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

        this.currentUser.assertValuesAndClear(null)

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
            .apiClient(object : MockApiClient() {
                override fun loginWithFacebook(accessToken: String): Observable<AccessTokenEnvelope> {
                    return Observable.error(Throwable("error"))
                }
            })
            .build()
        setUpEnvironment(environment, LoginReason.DEFAULT)

        this.currentUser.assertValuesAndClear(null)

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
