package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.MockTrackingClient
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class SettingsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: SettingsViewModel.SettingsViewModel
    private val currentUserTest = TestSubscriber<User>()
    private val logout = TestSubscriber<Unit>()
    private val showConfirmLogoutPrompt = TestSubscriber<Boolean>()
    private val currentUser = TestSubscriber<User?>()

    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(user: User) {
        val currentUser = MockCurrentUserV2(user)
        val environment = environment().toBuilder()
            .currentUserV2(currentUser)
            .analytics(AnalyticEvents(listOf(getMockClientWithUser(user))))
            .build()

        setUpEnvironment(environment)
        currentUser.observable().subscribe {
            it.getValue()?.let { user ->
                this.currentUserTest.onNext(user)
            }
        }.addToDisposable(disposables)
    }

    private fun setUpEnvironment(environment: Environment) {

        this.vm = SettingsViewModel.SettingsViewModel(environment)
        this.vm.outputs.logout().subscribe { this.logout.onNext(Unit) }.addToDisposable(disposables)
        this.vm.outputs.showConfirmLogoutPrompt()
            .subscribe { this.showConfirmLogoutPrompt.onNext(it) }.addToDisposable(disposables)
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
        this.vm.outputs.showConfirmLogoutPrompt()
            .subscribe {
                showConfirmLogoutPrompt.onNext(it)
            }
            .addToDisposable(disposables)
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

        setUpEnvironment(user)

        this.vm.inputs.confirmLogoutClicked()
        this.logout.assertValue(Unit)

        this.currentUser.assertNoValues()
    }

    private fun getMockClientWithUser(user: User) = MockTrackingClient(
        MockCurrentUser(user),
        MockCurrentConfig(),
        TrackingClientType.Type.SEGMENT,
        MockFeatureFlagClient()
    ).apply {
        this.identifiedUser.subscribe { currentUser }
    }
}
