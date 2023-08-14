package com.kickstarter.viewmodels

import CreatePasswordMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockTrackingClient
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class CreatePasswordViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CreatePasswordViewModel.CreatePasswordViewModel

    private val error = TestSubscriber<String>()
    private val passwordWarning = TestSubscriber<Int>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()
    private val currentUser = TestSubscriber<User?>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = CreatePasswordViewModel.CreatePasswordViewModel(environment)

        this.vm.outputs.error().subscribe { this.error.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.passwordWarning().subscribe { this.passwordWarning.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.progressBarIsVisible().subscribe { this.progressBarIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.saveButtonIsEnabled().subscribe { this.saveButtonIsEnabled.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.success().subscribe { this.success.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testError() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
                    return Observable.error(Exception("Oops"))
                }
            }).build()
        )

        this.vm.inputs.newPassword("passwo")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.createPasswordClicked()
        this.error.assertValue("Oops")
    }

    @Test
    fun testPasswordWarning() {
        setUpEnvironment(environment())

        this.vm.inputs.newPassword("p")
        this.passwordWarning.assertValues(0, R.string.Password_min_length_message)
        this.vm.inputs.newPassword("password")
        this.passwordWarning.assertValues(0, R.string.Password_min_length_message, 0)
        this.vm.inputs.confirmPassword("p")
        this.passwordWarning.assertValues(0, R.string.Password_min_length_message, 0, R.string.Passwords_matching_message)
        this.vm.inputs.confirmPassword("passw")
        this.passwordWarning.assertValues(0, R.string.Password_min_length_message, 0, R.string.Passwords_matching_message)
        this.vm.inputs.confirmPassword("password")
        this.passwordWarning.assertValues(0, R.string.Password_min_length_message, 0, R.string.Passwords_matching_message, 0)
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.createPasswordClicked()
        this.progressBarIsVisible.assertValues(true, false)
    }

    @Test
    fun testSaveButtonIsEnabled() {
        setUpEnvironment(environment())

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.saveButtonIsEnabled.assertValues(false, true)
        this.vm.inputs.confirmPassword("pass")
        this.saveButtonIsEnabled.assertValues(false, true, false)
        this.vm.inputs.confirmPassword("passwerd")
        this.saveButtonIsEnabled.assertValues(false, true, false)
    }

    @Test
    fun testSuccess() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
                    return Observable.just(
                        CreatePasswordMutation.Data(
                            CreatePasswordMutation.UpdateUserAccount(
                                "",
                                CreatePasswordMutation.User("", "test@emai", true)
                            )
                        )
                    )
                }
            }).build()
        )

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.createPasswordClicked()
        this.success.assertValue("test@emai")
    }

    @Test
    fun userLoggedIn_whenCreatePasswordError_userNotReset() {
        // - create MockTracking client with user logged in
        val user = UserFactory.user()
        val trackingClient = getMockClientWithUser(user)

        // - Mock failed response from apollo
        val apolloClient = object : MockApolloClientV2() {
            override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
                return Observable.error(Exception("Oops"))
            }
        }

        // - Create environment with mocked objects
        val environment = environment().toBuilder()
            .apolloClientV2(apolloClient)
            .analytics(AnalyticEvents(listOf(trackingClient)))
            .build()

        setUpEnvironment(environment)

        this.vm.inputs.newPassword("passwo")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.createPasswordClicked()
        this.error.assertValue("Oops")

        currentUser.assertValue(user)
    }

    @Test
    fun userLoggedIn_whenCreatePasswordSuccess_userReset() {
        // - Create MockTracking client with user logged in
        val user = UserFactory.user()
        val trackingClient = getMockClientWithUser(user)

        // - Mock success response from apollo
        val apolloClient = object : MockApolloClientV2() {
            override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
                return Observable.just(
                    CreatePasswordMutation.Data(
                        CreatePasswordMutation.UpdateUserAccount(
                            "",
                            CreatePasswordMutation.User("", "test@emai", true)
                        )
                    )
                )
            }
        }

        // - Create environment with mocked objects
        val environment = environment().toBuilder()
            .apolloClientV2(apolloClient)
            .analytics(AnalyticEvents(listOf(trackingClient)))
            .build()

        setUpEnvironment(environment)

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.createPasswordClicked()
        this.success.assertValue("test@emai")

        currentUser.assertValues(user, null)
    }

    private fun getMockClientWithUser(user: User) = MockTrackingClient(
        MockCurrentUser(user),
        MockCurrentConfig(),
        TrackingClientType.Type.SEGMENT,
        MockFeatureFlagClient()
    ).apply {
        this.identifiedUser.subscribe { currentUser.onNext(it) }
    }
}
