package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.*
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import org.junit.Test
import rx.observers.TestSubscriber

class SettingsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: SettingsViewModel.ViewModel
    private val currentUserTest = TestSubscriber<User>()
    private val logout = TestSubscriber<Void>()
    private val showConfirmLogoutPrompt = TestSubscriber<Boolean>()
    private val userId = TestSubscriber<Long?>()

    private fun setUpEnvironment(user: User) {
        val currentUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .analytics(AnalyticEvents(listOf(getMockClientWithUser(user))))
                .build()

        setUpEnvironment(environment)
        currentUser.observable().subscribe(this.currentUserTest)
    }

    private fun setUpEnvironment(environment: Environment) {
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

    @Test
    fun user_whenPressLogout_userReset() {
        val user = UserFactory.user()

        val environment = environment().toBuilder()
                .analytics(AnalyticEvents(listOf(getMockClientWithUser(user))))
                .build()

        setUpEnvironment(environment)

        this.vm.inputs.logoutClicked()
        this.showConfirmLogoutPrompt.assertValue(true)

        this.userId.assertValues(user.id(), null)
    }

    private fun getMockClientWithUser(user: User) = MockTrackingClient(
            MockCurrentUser(user),
            MockCurrentConfig(),
            TrackingClientType.Type.SEGMENT,
            MockExperimentsClientType()).apply {
        this.identifiedId.subscribe(userId)
    }
}
