package com.kickstarter.viewmodels

import com.kickstarter.CreatePasswordMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.MockTrackingClient
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class CreatePasswordViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CreatePasswordViewModel.CreatePasswordViewModel

    private val error = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()
    private val currentUser = TestSubscriber<User?>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = CreatePasswordViewModel.CreatePasswordViewModel(environment)

        this.vm.outputs.error().subscribe { this.error.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.progressBarIsVisible().subscribe { this.progressBarIsVisible.onNext(it) }.addToDisposable(disposables)
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

        this.vm.updatePasswordData("password")
        this.vm.inputs.createPasswordClicked()
        this.error.assertValue("Oops")
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.updatePasswordData("password")
        this.vm.inputs.createPasswordClicked()
        this.progressBarIsVisible.assertValues(true, false)
    }

    @Test
    fun testSuccess() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
                    return Observable.just(
                        CreatePasswordMutation.Data(
                            CreatePasswordMutation.UpdateUserAccount(
                                CreatePasswordMutation.User("test@emai", true)
                            )
                        )
                    )
                }
            }).build()
        )

        this.vm.updatePasswordData("password")
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

        this.vm.updatePasswordData("password")
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
                            CreatePasswordMutation.User("test@emai", true)
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

        this.vm.updatePasswordData("password")
        this.vm.inputs.createPasswordClicked()
        this.success.assertValue("test@emai")

        // TODO: Force Logout logic after succesful change is living on the UI, should be moved to the VM and tested
    }

    private fun getMockClientWithUser(user: User) = MockTrackingClient(
        MockCurrentUserV2(user),
        MockCurrentConfigV2(),
        TrackingClientType.Type.SEGMENT
    ).apply {
        this.identifiedUser.subscribe { currentUser.onNext(it) }
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
