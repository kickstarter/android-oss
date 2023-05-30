package com.kickstarter.viewmodels

import UpdateUserPasswordMutation
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

class ChangePasswordViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ChangePasswordViewModel.ChangePasswordViewModel

    private val error = TestSubscriber<String>()
    private val passwordWarning = TestSubscriber<Int>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()
    private val currentUser = TestSubscriber<User?>()

    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ChangePasswordViewModel.ChangePasswordViewModel(environment)

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
                override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                    return Observable.error(Exception("Oops"))
                }
            }).build()
        )

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
        this.passwordWarning.assertValue(0)
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
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
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

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.success.assertValue("test@email.com")
    }

    @Test
    fun userLoggedIn_whenChangePasswordError_userNotReset() {
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

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.error.assertValue("Oops")

        currentUser.assertValue(user)
    }

    @Test
    fun serLoggedIn_whenChangePasswordSuccess_userReset() {
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
                            UpdateUserPasswordMutation.User("", "test@email.com", false, false)
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

        this.vm.inputs.currentPassword("password")
        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.success.assertValue("test@email.com")

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
