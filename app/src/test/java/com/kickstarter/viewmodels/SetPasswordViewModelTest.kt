package com.kickstarter.viewmodels

import UpdateUserPasswordMutation
import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class SetPasswordViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: SetPasswordViewModel.ViewModel
    private val error = TestSubscriber<String>()
    private val passwordWarning = TestSubscriber<Int>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()
    private val isFormSubmitting = TestSubscriber<Boolean>()
    private val setUserEmail = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = SetPasswordViewModel.ViewModel(environment)

        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.passwordWarning().subscribe(this.passwordWarning)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.saveButtonIsEnabled().subscribe(this.saveButtonIsEnabled)
        this.vm.outputs.success().subscribe(this.success)
        this.vm.outputs.isFormSubmitting().subscribe(this.isFormSubmitting)
        this.vm.outputs.setUserEmail().subscribe(this.setUserEmail)
    }

    @Test
    fun testApiError() {
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun setUserPassword(
                    newPassword: String,
                    confirmPassword: String
                ): Observable<UpdateUserPasswordMutation.Data> {
                    return Observable.error(ApiExceptionFactory.badRequestException())
                }
            }).build()
        )

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()

        this.error.assertValue("bad request")
    }

    @Test
    fun testError() {
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun setUserPassword(
                    newPassword: String,
                    confirmPassword: String
                ): Observable<UpdateUserPasswordMutation.Data> {
                    return Observable.error(Exception("Oops"))
                }
            }).build()
        )

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

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()
        this.progressBarIsVisible.assertValues(true, false)
        this.isFormSubmitting.assertValues(true, false)
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
        val user = UserFactory.userNeedPassword()
        val mockUser = MockCurrentUser(user)
        mockUser.login(user, "token")

        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun setUserPassword(newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
                    return Observable.just(
                        UpdateUserPasswordMutation.Data(
                            UpdateUserPasswordMutation.UpdateUserAccount(
                                "",
                                UpdateUserPasswordMutation.User("", "test@email.com", false, true)
                            )
                        )
                    )
                }
            })
                .currentUser(mockUser)
                .build()
        )

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.changePasswordClicked()

        this.success.assertValue("test@email.com")

        assertEquals(false, mockUser.user?.needsPassword())
    }

    @Test
    fun testSetUserEmail() {
        setUpEnvironment(environment())
        vm.intent(Intent().putExtra(IntentKey.EMAIL, "test@email.com"))

        this.setUserEmail.assertValue("****@email.com")
    }
}
