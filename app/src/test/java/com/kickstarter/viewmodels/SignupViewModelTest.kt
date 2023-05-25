package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.ConfigFactory.config
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import io.reactivex.disposables.CompositeDisposable
import org.junit.After
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject

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
        val environment = environment()
        environment.currentConfigV2()?.config(config())

        val vm = SignupViewModel.SignupViewModel(environment)

        val user = BehaviorSubject.create<User>()
        environment().currentUserV2()?.loggedInUser()?.subscribe { user.onNext(it) }?.addToDisposable(disposables)

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

//        formSubmittingTest.assertValues(true, false)
//        signupSuccessTest.assertValueCount(1)
        assertEquals("some@email.com", user.value?.email())

        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testSignupViewModel_ApiValidationError() {
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun signup(
                name: String,
                email: String,
                password: String,
                passwordConfirmation: String,
                sendNewsletters: Boolean
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(
                    ApiExceptionFactory.apiError(
                        builder().httpCode(422).build()
                    )
                )
            }
        }

        val environment = environment().toBuilder().apiClient(apiClient).build()
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
        val apiClient: ApiClientType = object : MockApiClient() {
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

        val environment = environment().toBuilder().apiClient(apiClient).build()
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
