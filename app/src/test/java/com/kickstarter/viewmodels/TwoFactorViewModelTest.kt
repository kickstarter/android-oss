package com.kickstarter.viewmodels

import UserPrivacyQuery
import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class TwoFactorViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: TwoFactorViewModel.TwoFactorViewModel

    private val formIsValid = TestSubscriber<Boolean>()
    private val formSubmitting = TestSubscriber<Boolean>()
    private val genericTfaError = TestSubscriber<Unit>()
    private val showResendCodeConfirmation = TestSubscriber<Unit>()
    private val tfaCodeMismatchError = TestSubscriber<Unit>()
    private val tfaSuccess = TestSubscriber<Unit>()
    private val userTest = TestSubscriber<User>()
    private val disposables = CompositeDisposable()

    @Test
    fun testTwoFactorViewModel_FormValidation() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.TwoFactorViewModel(environment(), intent)

        vm.outputs.formIsValid().subscribe { formIsValid.onNext(it) }.addToDisposable(disposables)
        formIsValid.assertNoValues()

        vm.inputs.code("444444")
        formIsValid.assertValue(true)

        vm.inputs.code("")

        formIsValid.assertValues(true, false)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_TfaSuccess() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        val user = UserFactory.user()
        val token = AccessTokenEnvelope.builder()
            .user(user)
            .accessToken("token")
            .build()
        val apiClient = object : MockApiClientV2() {
            override fun login(email: String, password: String, code: String): Observable<AccessTokenEnvelope> {
                return Observable.just(token)
            }
        }
        val apolloClient = object : MockApolloClientV2() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(
                    UserPrivacyQuery.Data(
                        UserPrivacyQuery.Me(
                            "", user.name(),
                            "gina@kickstarter.com", true, true, true, true, "USD"
                        )
                    )
                )
            }
        }

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .apolloClientV2(apolloClient)
            .build()

        vm = TwoFactorViewModel.TwoFactorViewModel(environment, intent)

        vm.outputs.tfaSuccess().subscribe { tfaSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.formSubmitting()
            .subscribe { formSubmitting.onNext(it) }
            .addToDisposable(disposables)
        environment.currentUserV2()?.observable()?.subscribe {
            userTest.onNext(it.getValue())
        }?.addToDisposable(disposables)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertValueCount(1)
        userTest.assertValue {
            it.email() == "gina@kickstarter.com"
        }

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_TfaSuccessFacebook() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, true)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234")

        val user = UserFactory.user()
        val token = AccessTokenEnvelope.builder()
            .user(user)
            .accessToken("token")
            .build()
        val apiClient = object : MockApiClientV2() {
            override fun loginWithFacebook(fbAccessToken: String, code: String): Observable<AccessTokenEnvelope> {
                return Observable.just(token)
            }
        }
        val apolloClient = object : MockApolloClientV2() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(
                    UserPrivacyQuery.Data(
                        UserPrivacyQuery.Me(
                            "", user.name(),
                            "gina@kickstarter.com", true, true, true, true, "USD"
                        )
                    )
                )
            }
        }

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .apolloClientV2(apolloClient)
            .build()

        vm = TwoFactorViewModel.TwoFactorViewModel(environment, intent)

        vm.outputs.tfaSuccess().subscribe { tfaSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.formSubmitting()
            .subscribe { formSubmitting.onNext(it) }
            .addToDisposable(disposables)
        environment.currentUserV2()?.observable()?.subscribe {
            userTest.onNext(it.getValue())
        }?.addToDisposable(disposables)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertValueCount(1)
        userTest.assertValue {
            it.email() == "gina@kickstarter.com"
        }
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_ResendCode() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.TwoFactorViewModel(environment(), intent)

        vm.outputs.showResendCodeConfirmation()
            .subscribe { showResendCodeConfirmation.onNext(it) }
            .addToDisposable(disposables)

        vm.inputs.resendClick()

        showResendCodeConfirmation.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_ResendCodeFacebook() {
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, true)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234")

        vm = TwoFactorViewModel.TwoFactorViewModel(environment(), intent)

        vm.outputs.showResendCodeConfirmation()
            .subscribe { showResendCodeConfirmation .onNext(it) }
            .addToDisposable(disposables)

        vm.inputs.resendClick()

        showResendCodeConfirmation.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_GenericError() {
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun login(
                email: String,
                password: String,
                code: String
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(
                    ApiExceptionFactory.apiError(
                        builder().httpCode(400).errorMessages(listOf("Generic Error")).build()
                    )
                )
            }
        }

        val environment = environment().toBuilder().apiClientV2(apiClient).build()

        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.TwoFactorViewModel(environment, intent)

        vm.outputs.tfaSuccess().subscribe { tfaSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.formSubmitting()
            .subscribe { formSubmitting.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.genericTfaError()
            .subscribe { genericTfaError.onNext(it) }
            .addToDisposable(disposables)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertNoValues()
        genericTfaError.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorViewModel_CodeMismatchError() {
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun login(
                email: String,
                password: String,
                code: String
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(ApiExceptionFactory.tfaFailed())
            }
        }

        val environment = environment().toBuilder().apiClientV2(apiClient).build()
        val intent = Intent()

        intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com")
        intent.putExtra(IntentKey.PASSWORD, "hello")
        intent.putExtra(IntentKey.FACEBOOK_LOGIN, false)
        intent.putExtra(IntentKey.FACEBOOK_TOKEN, "")

        vm = TwoFactorViewModel.TwoFactorViewModel(environment, intent)

        vm.outputs.tfaSuccess().subscribe { tfaSuccess.onNext(it) }.addToDisposable(disposables)
        vm.outputs.formSubmitting()
            .subscribe { formSubmitting.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.tfaCodeMismatchError()
            .subscribe { tfaCodeMismatchError.onNext(it) }
            .addToDisposable(disposables)

        vm.inputs.code("88888")
        vm.inputs.loginClick()

        formSubmitting.assertValues(true, false)
        tfaSuccess.assertNoValues()
        tfaCodeMismatchError.assertValueCount(1)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
}
