package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.ConfigFactory.config
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class SignupViewModelTest : KSRobolectricTestCase() {

    private val disposables = CompositeDisposable()
    @Test
    fun testSignupViewModel_FormValidation() {
        val environment = environment()

        environment.currentConfigV2()?.config(config())

        val vm = SignupViewModel.SignupViewModel(environment)
        val formIsValidTest = TestSubscriber<Boolean>()

        vm.outputs.formIsValid().subscribe { formIsValidTest.onNext(it) }.addToDisposable(disposables)

        vm.inputs.name("brandon")

        formIsValidTest.assertNoValues()

        vm.inputs.email("incorrect@kickstarter")

        formIsValidTest.assertNoValues()

        vm.inputs.password("danisawesome")

        formIsValidTest.assertValues(false)

        vm.inputs.email("hello@kickstarter.com")

        formIsValidTest.assertValues(false, true)
    }

    @Test
    fun testSignupViewModel_SuccessfulSignup() {
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun signup(
                name: String,
                email: String,
                password: String,
                passwordConfirmation: String,
                sendNewsletters: Boolean
            ): Observable<AccessTokenEnvelope> {
                return Observable.just(
                    AccessTokenEnvelope.builder()
                        .accessToken("")
                        .user(UserFactory.user())
                        .build()
                )
            }
        }

        val apolloClient = object : MockApolloClientV2() {
            override fun userPrivacy(): Observable<UserPrivacy> {
                return Observable.just(
                    UserPrivacy(UserFactory.user().name(), "hello@kickstarter.com", true, true, true, true, "USD")
                )
            }
        }

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .apolloClientV2(apolloClient)
            .build()

        environment.currentConfigV2()?.config(config())

        val vm = SignupViewModel.SignupViewModel(environment)

        val signupSuccessTest = TestSubscriber<Unit>()
        vm.outputs.signupSuccess().subscribe { signupSuccessTest.onNext(it) }.addToDisposable(disposables)

        val formSubmittingTest = TestSubscriber<Boolean>()
        vm.outputs.formSubmitting().subscribe { formSubmittingTest.onNext(it) }.addToDisposable(disposables)

        vm.inputs.name("brandon")
        vm.inputs.email("hello@kickstarter.com")
        vm.inputs.email("incorrect@kickstarter")
        vm.inputs.password("danisawesome")
        vm.inputs.sendNewslettersClick(true)
        vm.inputs.signupClick()

        formSubmittingTest.assertValues(true, false)
        signupSuccessTest.assertValueCount(1)
        environment.currentUserV2()?.observable()?.subscribe {
            assertEquals("hello@kickstarter.com", it.getValue()?.email())
        }?.addToDisposable(disposables)

        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testSignupViewModel_ApiValidationError() {
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun signup(
                name: String,
                email: String,
                password: String,
                passwordConfirmation: String,
                sendNewsletters: Boolean
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(
                    ApiExceptionFactory.apiError(
                        builder().httpCode(422).errorMessages(listOf("Unprocessable Content")).build()
                    )
                )
            }
        }

        val environment = environment().toBuilder().apiClientV2(apiClient).build()
        val vm = SignupViewModel.SignupViewModel(environment)
        val signupSuccessTest = TestSubscriber<Unit>()
        vm.outputs.signupSuccess().subscribe { signupSuccessTest.onNext(it) }.addToDisposable(disposables)

        val signupErrorTest = TestSubscriber<String>()
        vm.outputs.errorString().subscribe { signupErrorTest.onNext(it) }.addToDisposable(disposables)

        val formSubmittingTest = TestSubscriber<Boolean>()
        vm.outputs.formSubmitting().subscribe { formSubmittingTest.onNext(it) }.addToDisposable(disposables)

        vm.inputs.name("brandon")
        vm.inputs.email("hello@kickstarter.com")
        vm.inputs.email("incorrect@kickstarter")
        vm.inputs.password("danisawesome")
        vm.inputs.sendNewslettersClick(true)
        vm.inputs.signupClick()

        formSubmittingTest.assertValues(true, false)
        signupSuccessTest.assertValueCount(0)
        signupErrorTest.assertValueCount(1)

        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testSignupViewModel_ApiError() {
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun signup(
                name: String,
                email: String,
                password: String,
                passwordConfirmation: String,
                sendNewsletters: Boolean
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val environment = environment().toBuilder().apiClientV2(apiClient).build()
        val vm = SignupViewModel.SignupViewModel(environment)
        val signupSuccessTest = TestSubscriber<Unit>()

        vm.outputs.signupSuccess().subscribe { signupSuccessTest.onNext(it) }.addToDisposable(disposables)

        val signupErrorTest = TestSubscriber<String>()
        vm.outputs.errorString().subscribe { signupErrorTest.onNext(it) }.addToDisposable(disposables)

        val formSubmittingTest = TestSubscriber<Boolean>()
        vm.outputs.formSubmitting().subscribe { formSubmittingTest.onNext(it) }.addToDisposable(disposables)

        vm.inputs.name("brandon")
        vm.inputs.email("hello@kickstarter.com")
        vm.inputs.email("incorrect@kickstarter")
        vm.inputs.password("danisawesome")
        vm.inputs.sendNewslettersClick(true)
        vm.inputs.signupClick()

        formSubmittingTest.assertValues(true, false)
        signupSuccessTest.assertValueCount(0)
        signupErrorTest.assertValueCount(1)

        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
}
