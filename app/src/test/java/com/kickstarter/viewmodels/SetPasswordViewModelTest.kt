package com.kickstarter.viewmodels

import UpdateUserPasswordMutation
import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.usecases.LoginUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class SetPasswordViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: SetPasswordViewModel.SetPasswordViewModel
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val error = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()
    private val isFormSubmitting = TestSubscriber<Boolean>()
    private val setUserEmail = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = SetPasswordViewModel.SetPasswordViewModel(environment)

        this.vm.outputs.error().subscribe { this.error.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.progressBarIsVisible().subscribe { this.progressBarIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.success().subscribe { this.success.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.isFormSubmitting().subscribe { this.isFormSubmitting.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.setUserEmail().subscribe { this.setUserEmail.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testApiError() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun updateUserPassword(
                    currentPassword: String,
                    newPassword: String,
                    confirmPassword: String
                ): Observable<UpdateUserPasswordMutation.Data> {
                    return Observable.error(ApiExceptionFactory.badRequestException())
                }
            }).build()
        )

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.savePasswordClicked()

        this.error.assertValue("bad request")
    }

    @Test
    fun testError() {
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

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.savePasswordClicked()

        this.error.assertValue("Oops")
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.savePasswordClicked()
        this.progressBarIsVisible.assertValues(true, false)
        this.isFormSubmitting.assertValues(true, false)
    }

    @Test
    fun testSuccess() {
        val user = UserFactory.userNeedPassword()
        val mockUser = MockCurrentUser(user)
        val currentUserV2 = MockCurrentUserV2()

        val environment = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun updateUserPassword(
                currentPassword: String,
                newPassword: String,
                confirmPassword: String
            ): Observable<UpdateUserPasswordMutation.Data> {
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
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val loginUserCase = LoginUseCase(environment)

        loginUserCase.login(user, "token")

        this.vm.inputs.newPassword("password")
        this.vm.inputs.confirmPassword("password")
        this.vm.inputs.savePasswordClicked()

        this.success.assertValue("test@email.com")

        assertEquals(false, mockUser.user?.needsPassword())
    }

    @Test
    fun testSetUserEmail() {
        setUpEnvironment(environment())
        this.vm.configureWith(Intent().putExtra(IntentKey.EMAIL, "test@email.com"))

        this.setUserEmail.assertValue("****@email.com")
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
