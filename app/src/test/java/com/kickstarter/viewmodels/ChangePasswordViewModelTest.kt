package com.kickstarter.viewmodels

import UpdateUserPasswordMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockTrackingClient
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.subscribers.TestSubscriber
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangePasswordViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ChangePasswordViewModel

    private val currentUser = TestSubscriber<User?>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm =
            ChangePasswordViewModelFactory(environment).create(ChangePasswordViewModel::class.java)
    }

    @Test
    fun testError() = runTest {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun updateUserPassword(
                    currentPassword: String,
                    newPassword: String,
                    confirmPassword: String
                ): Observable<UpdateUserPasswordMutation.Data> {
                    return Observable.error(Exception("Oops"))
                }
            }).build()
        )

        val errorStates = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.updatePassword(oldPassword = "password", newPassword = "newpassword")
            vm.error.toList(errorStates)
        }

        // - First empty emission due the initialization in ChangePasswordViewModel:23
        assertEquals(listOf("", "Oops"), errorStates)
    }

    @Test
    fun progressBarIsVisible() = runTest {
        setUpEnvironment(environment())

        val loadingStates = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.updatePassword(oldPassword = "password", newPassword = "newpassword")
            vm.isLoading.toList(loadingStates)
        }

        assertEquals(listOf(false, true, false), loadingStates)
    }

    @Test
    fun testSuccess() = runTest {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun updateUserPassword(
                    currentPassword: String,
                    newPassword: String,
                    confirmPassword: String
                ): Observable<UpdateUserPasswordMutation.Data> {
                    return Observable.just(
                        UpdateUserPasswordMutation.Data(
                            UpdateUserPasswordMutation.UpdateUserAccount(
                                "",
                                UpdateUserPasswordMutation.User("", "test@email.com", false, false)
                            )
                        )
                    )
                }
            }).build()
        )

        val successStates = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.updatePassword(oldPassword = "password", newPassword = "newpassword")
            vm.success.toList(successStates)
        }

        assertEquals(listOf("", "test@email.com"), successStates)
    }

    @Test
    fun userLoggedIn_whenChangePasswordError_userNotReset() = runTest {
        // - create MockTracking client with user logged in
        val user = UserFactory.user()
        val trackingClient = getMockClientWithUser(user)

        // - Mock failed response from apollo
        val apolloClient = object : MockApolloClientV2() {
            override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                return Observable.error(Exception("Oops"))
            }
        }

        // - Create environment with mocked objects
        val environment = environment().toBuilder()
            .apolloClientV2(apolloClient)
            .analytics(AnalyticEvents(listOf(trackingClient)))
            .build()

        setUpEnvironment(environment)

        val errorStates = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.updatePassword(oldPassword = "password", newPassword = "newpassword")
            vm.error.toList(errorStates)
        }

        // - First empty emission due the initialization in ChangePasswordViewModel:23
        assertEquals(listOf("", "Oops"), errorStates)
        currentUser.assertValue(user)
    }

    @Test
    fun serLoggedIn_whenChangePasswordSuccess_userReset() = runTest {
        // - create MockTracking client with user logged in
        val user = UserFactory.user()
        val trackingClient = getMockClientWithUser(user)

        // - Mock success response from apollo
        val apolloClient = object : MockApolloClientV2() {
            override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                return Observable.just(
                    UpdateUserPasswordMutation.Data(
                        UpdateUserPasswordMutation.UpdateUserAccount(
                            "",
                            UpdateUserPasswordMutation.User("", "new22test@email.com", false, false)
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

        val successStates = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.updatePassword(oldPassword = "password", newPassword = "newpassword")
            vm.success.toList(successStates)
        }

        assertEquals(listOf("", "new22test@email.com"), successStates)
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
