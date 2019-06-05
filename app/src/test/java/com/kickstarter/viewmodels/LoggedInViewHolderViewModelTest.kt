package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import org.junit.Test
import rx.observers.TestSubscriber

class LoggedInViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: LoggedInViewHolderViewModel.ViewModel
    private val avatarUrl = TestSubscriber<String>()
    private val dashboardRowIsGone = TestSubscriber<Boolean>()
    private val name = TestSubscriber<String>()
    private val unreadMessagesCount = TestSubscriber<Int>()
    private val unseenActivityCount = TestSubscriber<Int>()
    private val user = TestSubscriber<User>()

    fun setUpEnvironment(environment: Environment) {
        this.vm = LoggedInViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.avatarUrl().subscribe(this.avatarUrl)
        this.vm.outputs.dashboardRowIsGone().subscribe(this.dashboardRowIsGone)
        this.vm.outputs.name().subscribe(this.name)
        this.vm.outputs.unreadMessagesCount().subscribe(this.unreadMessagesCount)
        this.vm.outputs.unseenActivityCount().subscribe(this.unseenActivityCount)
        this.vm.outputs.user().subscribe(this.user)
    }

    @Test
    fun testAvatarUrl() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(UserFactory.user())

        this.avatarUrl.assertValueCount(1)
    }

    @Test
    fun testDashboardRowIsGone() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(UserFactory.user())

        this.dashboardRowIsGone.assertValue(true)

        this.vm.inputs.configureWith(UserFactory.collaborator())

        this.dashboardRowIsGone.assertValues(true, false)
    }

    @Test
    fun testName() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(UserFactory.user().toBuilder().name("Klay Thompson").build())

        this.name.assertValue("Klay Thompson")
    }

    @Test
    fun testUnreadMessagesCount() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(UserFactory.user().toBuilder().unreadMessagesCount(5).build())

        this.unreadMessagesCount.assertValue(5)
    }

    @Test
    fun testUnseenActivityCount() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(UserFactory.user().toBuilder().unseenActivityCount(2).build())

        this.unseenActivityCount.assertValue(2)
    }

    @Test
    fun testUser() {
        setUpEnvironment(environment())

        val user = UserFactory.user()
        this.vm.inputs.configureWith(user)

        this.user.assertValue(user)
    }
}
