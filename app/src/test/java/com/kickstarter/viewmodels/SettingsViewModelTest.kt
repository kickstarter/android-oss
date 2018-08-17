package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.factories.UserFactory
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.models.User
import org.junit.Test
import rx.observers.TestSubscriber

class SettingsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: SettingsViewModel.ViewModel
    private val currentUserTest = TestSubscriber<User>()
    private val logout = TestSubscriber<Void>()
    private val showConfirmLogoutPrompt = TestSubscriber<Boolean>()

    private fun setUpEnvironment(user: User) {
        val currentUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()

        currentUser.observable().subscribe(this.currentUserTest)

        this.vm = SettingsViewModel.ViewModel(environment)
        this.vm.outputs.logout().subscribe(this.logout)
        this.vm.outputs.showConfirmLogoutPrompt().subscribe(this.showConfirmLogoutPrompt)
    }

    @Test
    fun testConfirmLogoutClicked() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.confirmLogoutClicked()
        this.logout.assertValueCount(1)
        this.koalaTest.assertValues("Settings View", "Logout")
    }

    @Test
    fun testUserEmits() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)
        this.vm.outputs.showConfirmLogoutPrompt().subscribe(showConfirmLogoutPrompt)
    }

    @Test
    fun testShowConfirmLogoutPrompt() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.logoutClicked()
        this.showConfirmLogoutPrompt.assertValue(true)
    }
}
