package com.kickstarter.viewmodels

import android.app.Activity
import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.ConfigFactory.config
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.LoginViewModel.LoginViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class LoginViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: LoginViewModel
    private val genericLoginError = TestSubscriber<String>()
    private val invalidLoginError = TestSubscriber<String>()
    private val logInButtonIsEnabled = TestSubscriber<Boolean>()
    private val loginSuccess = TestSubscriber<Unit>()
    private val preFillEmail = TestSubscriber<String>()
    private val showChangedPasswordSnackbar = TestSubscriber<Unit>()
    private val showResetPasswordSuccessDialog = TestSubscriber<Boolean>()
    private val tfaChallenge = TestSubscriber<Unit>()

    private val disposables = CompositeDisposable()

    @After
    fun cleanUp() {
        disposables.clear()
    }

    fun setUpEnvironment(environment: Environment, intent: Intent) {
        this.vm = LoginViewModel(environment, intent)

        this.vm.outputs.genericLoginError().subscribe { this.genericLoginError.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.invalidLoginError().subscribe { this.invalidLoginError.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.loginButtonIsEnabled().subscribe { this.logInButtonIsEnabled.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.loginSuccess().subscribe { this.loginSuccess.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.prefillEmail().subscribe { this.preFillEmail.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showChangedPasswordSnackbar().subscribe {
            this.showChangedPasswordSnackbar.onNext(it)
        }.addToDisposable(disposables)
        this.vm.outputs.showResetPasswordSuccessDialog()
            .map { showAndEmail -> showAndEmail.first }
            .subscribe { this.showResetPasswordSuccessDialog.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.tfaChallenge().subscribe { this.tfaChallenge.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testLoginButtonEnabled() {
        setUpEnvironment(environment(), Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        // Button should not be enabled until both a valid email and password are entered.
        this.vm.inputs.email("hello")
        this.logInButtonIsEnabled.assertNoValues()

        this.vm.inputs.email("hello@kickstarter.com")
        this.logInButtonIsEnabled.assertNoValues()

        this.vm.inputs.password("")
        this.logInButtonIsEnabled.assertValues(false)

        this.vm.inputs.password("izzyiscool")
        this.logInButtonIsEnabled.assertValues(false, true)
    }

    @Test
    fun testLoginButtonDisabledOnClick() {
        val apiClient = object : MockApiClientV2() {
            override fun login(email: String, password: String): Observable<AccessTokenEnvelope> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val mockConfig = MockCurrentConfig()
        mockConfig.config(config())

        val environment = environment().toBuilder()
            .currentConfig(mockConfig)
            .apiClientV2(apiClient)
            .build()

        setUpEnvironment(environment, Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        this.vm.inputs.email("hello@kickstarter.com")
        this.vm.inputs.password("codeisawesome")

        this.vm.inputs.loginClick()

        this.logInButtonIsEnabled.assertValues(true, true, false)
    }

    @Test
    fun testLoginApiError() {
        val apiClient = object : MockApiClientV2() {
            override fun login(email: String, password: String): Observable<AccessTokenEnvelope> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val mockConfig = MockCurrentConfig()
        mockConfig.config(config())

        val environment = environment().toBuilder()
            .currentConfig(mockConfig)
            .apiClientV2(apiClient)
            .build()

        setUpEnvironment(environment, Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        this.vm.inputs.email("incorrect@kickstarter.com")
        this.vm.inputs.password("lisaiscool")

        this.vm.inputs.loginClick()

        this.loginSuccess.assertNoValues()
        this.genericLoginError.assertValueCount(1)
        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testLoginApiValidationError() {
        val apiClient = object : MockApiClientV2() {
            override fun login(email: String, password: String): Observable<AccessTokenEnvelope> {
                return Observable.error(ApiExceptionFactory.invalidLoginException())
            }
        }

        val mockConfig = MockCurrentConfig()
        mockConfig.config(config())

        val environment = environment().toBuilder()
            .currentConfig(mockConfig)
            .apiClientV2(apiClient)
            .build()

        setUpEnvironment(environment, Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        this.vm.inputs.email("typo@kickstartr.com")
        this.vm.inputs.password("julieiscool")

        this.vm.inputs.loginClick()

        this.loginSuccess.assertNoValues()
        this.invalidLoginError.assertValueCount(1)
        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testLoginTfaChallenge() {
        val apiClient = object : MockApiClientV2() {
            override fun login(email: String, password: String): Observable<AccessTokenEnvelope> {
                return Observable.error(ApiExceptionFactory.tfaRequired())
            }
        }

        val mockConfig = MockCurrentConfigV2()
        mockConfig.config(config())

        val environment = environment().toBuilder()
            .currentConfig2(mockConfig)
            .apiClientV2(apiClient)
            .build()

        setUpEnvironment(environment, Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        this.vm.inputs.email("hello@kickstarter.com")
        this.vm.inputs.password("androidiscool")

        this.vm.inputs.loginClick()

        this.loginSuccess.assertNoValues()
        this.tfaChallenge.assertValueCount(1)
        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testPrefillEmail() {
        setUpEnvironment(environment(), Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        this.preFillEmail.assertValue("hello@kickstarter.com")
        this.showResetPasswordSuccessDialog.assertNoValues()
        this.showChangedPasswordSnackbar.assertNoValues()
    }

    @Test
    fun testPrefillEmailAndDialog() {
        // Start the view model with an email to prefill.
        setUpEnvironment(environment(), Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com").putExtra(IntentKey.LOGIN_REASON, LoginReason.RESET_PASSWORD))

        this.preFillEmail.assertValue("hello@kickstarter.com")
        this.showChangedPasswordSnackbar.assertNoValues()
        this.showResetPasswordSuccessDialog.assertValue(true)

        // Dismiss the confirmation dialog.
        this.vm.inputs.resetPasswordConfirmationDialogDismissed()
        this.showChangedPasswordSnackbar.assertNoValues()
        this.showResetPasswordSuccessDialog.assertValues(true, false)

        // Simulate success result after presenting ResetPassword Activity flow
        this.vm.inputs.activityResult(
            ActivityResult.create(
                requestCode = ActivityRequestCodes.RESET_FLOW,
                resultCode = Activity.RESULT_OK,
                intent = Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com")
            )
        )

        val rotatedPrefillEmail = TestSubscriber<String>()
        this.vm.outputs.prefillEmail().subscribe { rotatedPrefillEmail.onNext(it) }.addToDisposable(disposables)
        val rotatedShowChangedPasswordSnackbar = TestSubscriber<Unit>()
        this.vm.outputs.showChangedPasswordSnackbar().subscribe {
            rotatedShowChangedPasswordSnackbar.onNext(it)
        }.addToDisposable(disposables)

        val rotatedShowResetPasswordSuccessDialog = TestSubscriber<Boolean>()
        this.vm.outputs.showResetPasswordSuccessDialog()
            .map { showAndEmail -> showAndEmail.first }
            .subscribe {
                rotatedShowResetPasswordSuccessDialog.onNext(it)
            }
            .addToDisposable(disposables)

        // Email should still be pre-filled.
        rotatedPrefillEmail.assertValue("hello@kickstarter.com")

        // Dialog should not be shown again – the user has already dismissed it.
        rotatedShowResetPasswordSuccessDialog.assertValue(false)

        // Snackbar should not be shown.
        rotatedShowChangedPasswordSnackbar.assertNoValues()
    }

    @Test
    fun testPrefillEmailAndSnackbar() {
        setUpEnvironment(environment(), Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com").putExtra(IntentKey.LOGIN_REASON, LoginReason.CHANGE_PASSWORD))

        this.preFillEmail.assertValue("hello@kickstarter.com")
        this.showResetPasswordSuccessDialog.assertNoValues()

        this.vm.activityResult(
            ActivityResult.create(
                requestCode = ActivityRequestCodes.RESET_FLOW,
                resultCode = Activity.RESULT_OK,
                intent = Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com").putExtra(IntentKey.LOGIN_REASON, LoginReason.CHANGE_PASSWORD)
            )
        )

        // Create new test subscribers – this emulates a new activity subscribing to the vm's outputs.
        val rotatedPrefillEmail = TestSubscriber<String>()
        this.vm.outputs.prefillEmail().subscribe { rotatedPrefillEmail.onNext(it) }.addToDisposable(disposables)
        val rotatedShowChangedPasswordSnackbar = TestSubscriber<Unit>()
        this.vm.outputs.showChangedPasswordSnackbar().subscribe {
            rotatedShowChangedPasswordSnackbar.onNext(Unit)
        }.addToDisposable(disposables)
        val rotatedShowResetPasswordSuccessDialog = TestSubscriber<Boolean>()
        this.vm.outputs.showResetPasswordSuccessDialog()
            .map { showAndEmail -> showAndEmail.first }
            .subscribe { rotatedShowResetPasswordSuccessDialog.onNext(it) }
            .addToDisposable(disposables)

        // Email should still be pre-filled.
        rotatedPrefillEmail.assertValue("hello@kickstarter.com")
        rotatedShowChangedPasswordSnackbar.assertValueCount(1)

        // Dialog should not be shown.
        rotatedShowResetPasswordSuccessDialog.assertNoValues()
    }

    @Test
    fun testPrefillEmailAndResetPassword() {
        setUpEnvironment(environment(), Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))
        // Start the view model with an email to prefill.
        this.vm.email("test@kickstarter.com")
        this.vm.loginClick()

        // Start the view model with an email to prefill.
        this.vm.inputs.activityResult(
            ActivityResult(
                ActivityRequestCodes.RESET_FLOW,
                Activity.RESULT_OK,
                Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com").putExtra(IntentKey.LOGIN_REASON, LoginReason.RESET_PASSWORD)
            )
        )

        this.preFillEmail.assertValue("hello@kickstarter.com")
        this.showChangedPasswordSnackbar.assertNoValues()
        this.showResetPasswordSuccessDialog.assertValue(true)

        // Dismiss the confirmation dialog.
        this.vm.inputs.resetPasswordConfirmationDialogDismissed()
        this.showChangedPasswordSnackbar.assertNoValues()
        this.showResetPasswordSuccessDialog.assertValues(true, false)

        // Simulate rotating the device, first by sending a new intent (similar to what happens after rotation).
        this.vm.inputs.activityResult(
            ActivityResult(
                ActivityRequestCodes.RESET_FLOW,
                Activity.RESULT_OK,
                Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com")
            )
        )

        // Create new test subscribers – this emulates a new activity subscribing to the vm's outputs.
        val rotatedPrefillEmail = TestSubscriber<String>()
        this.vm.outputs.prefillEmail().subscribe { rotatedPrefillEmail.onNext(it) }.addToDisposable(disposables)
        val rotatedShowChangedPasswordSnackbar = TestSubscriber<Unit>()
        this.vm.outputs.showChangedPasswordSnackbar().subscribe {
            rotatedShowChangedPasswordSnackbar.onNext(Unit)
        }.addToDisposable(disposables)
        val rotatedShowResetPasswordSuccessDialog = TestSubscriber<Boolean>()
        this.vm.outputs.showResetPasswordSuccessDialog()
            .map { showAndEmail -> showAndEmail.first }
            .subscribe { rotatedShowResetPasswordSuccessDialog.onNext(it) }
            .addToDisposable(disposables)

        // Email should still be pre-filled.
        rotatedPrefillEmail.assertValue("hello@kickstarter.com")

        // Dialog should not be shown again – the user has already dismissed it.
        rotatedShowResetPasswordSuccessDialog.assertValue(false)

        // Snackbar should not be shown.
        rotatedShowChangedPasswordSnackbar.assertNoValues()
    }

    @Test
    fun testSuccessfulLogin() {
        val user = UserFactory.user()
        val token = AccessTokenEnvelope.builder()
            .user(user)
            .accessToken("token")
            .build()

        val apiClient = object : MockApiClientV2() {
            override fun login(email: String, password: String): Observable<AccessTokenEnvelope> {
                return Observable.just(token)
            }
        }

        val apolloClient = object : MockApolloClientV2() {
            override fun userPrivacy(): Observable<UserPrivacy> {
                return Observable.just(
                    UserPrivacy(user.name(),"hello@kickstarter.com", true, true, true, true, "USD")
                )
            }
        }

        val environment = environment().toBuilder()
            .apolloClientV2(apolloClient)
            .apiClientV2(apiClient)
            .build()

        setUpEnvironment(environment, Intent().putExtra(IntentKey.EMAIL, "hello@kickstarter.com"))

        this.vm.inputs.email("hello@kickstarter.com")
        this.vm.inputs.password("codeisawesome")

        this.vm.inputs.loginClick()

        this.loginSuccess.assertValueCount(1)
        this.loginSuccess.assertValue(Unit)

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }
}
