package com.kickstarter.viewmodels

import UpdateUserPasswordMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.*
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ChangePasswordViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ChangePasswordViewModel.ViewModel

    private val error = TestSubscriber<String>()
    private val passwordWarning = TestSubscriber<Int>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ChangePasswordViewModel.ViewModel(environment)

        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.passwordWarning().subscribe(this.passwordWarning)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.saveButtonIsEnabled().subscribe(this.saveButtonIsEnabled)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testError() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                return Observable.error(Exception("Oops"))
            }
        }).build())

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.error.assertValue("Oops")
    }

    @Test
    fun testPasswordWarning() {
        setUpEnvironment(environment())

        this.vm.inputs.newPassword("password")
        this.passwordWarning.assertValue(null)
        this.vm.inputs.newPassword("p")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message)
        this.vm.inputs.newPassword("password")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message, null)
        this.vm.inputs.confirmPassword("p")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message, null, R.string.Passwords_matching_message)
        this.vm.inputs.confirmPassword("passw")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message, null, R.string.Passwords_matching_message)
        this.vm.inputs.confirmPassword("password")
        this.passwordWarning.assertValues(null, R.string.Password_min_length_message, null, R.string.Passwords_matching_message, null)
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.progressBarIsVisible.assertValues(true, false)
    }

    @Test
    fun testSaveButtonIsEnabled() {
        setUpEnvironment(environment())

        this.vm.inputs.currentPassword("password")
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
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                return Observable.just(UpdateUserPasswordMutation.Data(UpdateUserPasswordMutation.UpdateUserAccount("",
                        UpdateUserPasswordMutation.User("", "test@email.com", false))))
            }
        }).build())

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.success.assertValue("test@email.com")
    }

    @Test
    fun userLoggedIn_whenChangePasswordError_userNotReset() {
        val userId = TestSubscriber<Long>()

        // - create MockTracking client with user logged in
        val user = UserFactory.user()
        val trackingClient = MockTrackingClient(MockCurrentUser(user),
                MockCurrentConfig() , TrackingClientType.Type.SEGMENT, MockExperimentsClientType())
        trackingClient.identifiedId.subscribe(userId)

        // - Mock failed response from apollo
        val apolloClient = object : MockApolloClient() {
            override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                return Observable.error(Exception("Oops"))
            }
        }

        // - Create environment with mocked objects
        val environment = environment().toBuilder()
                .apolloClient(apolloClient)
                .analytics(AnalyticEvents(listOf(trackingClient)))
                .build()

        setUpEnvironment(environment)

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.error.assertValue("Oops")

        userId.assertValue(user.id())
    }

    @Test
    fun serLoggedIn_whenChangePasswordSuccess_userReset() {
        val userId = TestSubscriber<Long?>()

        // - create MockTracking client with user logged in
        val user = UserFactory.user()
        val trackingClient = MockTrackingClient(MockCurrentUser(user),
                MockCurrentConfig() , TrackingClientType.Type.SEGMENT, MockExperimentsClientType())
        trackingClient.identifiedId.subscribe(userId)

        // - Mock success response from apollo
        val apolloClient = object : MockApolloClient() {
            override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                return Observable.just(UpdateUserPasswordMutation.Data(UpdateUserPasswordMutation.UpdateUserAccount("",
                        UpdateUserPasswordMutation.User("", "test@email.com", false))))
            }
        }

        // - Create environment with mocked objects
        val environment = environment().toBuilder()
                .apolloClient(apolloClient)
                .analytics(AnalyticEvents(listOf(trackingClient)))
                .build()

        setUpEnvironment(environment)

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.success.assertValue("test@email.com")

        userId.assertValues(user.id(), null)
    }
}
