package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.factories.UserFactory
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.models.User
import org.junit.Test
import rx.observers.TestSubscriber

class NotificationsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: NotificationsViewModel.ViewModel

    private val currentUserTest = TestSubscriber<User>()

    private fun setUpEnvironment(user: User) {
        val currentUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()

        currentUser.observable().subscribe(this.currentUserTest)

        this.vm = NotificationsViewModel.ViewModel(environment)
    }

    @Test
    fun tesCurrentUser() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValue(user)
    }

    @Test
    fun testNotifyMobileOfFollower() {
        val user = UserFactory.user().toBuilder().notifyMobileOfFollower(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyMobileOfFollower(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyMobileOfFollower(true).build())
    }

    @Test
    fun testNotifyMobileOfFriendActivity() {
        val user = UserFactory.user().toBuilder().notifyMobileOfFriendActivity(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyMobileOfFriendActivity(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyMobileOfFriendActivity(true).build())
    }

    @Test
    fun testNotifyMobileOfMessages() {
        val user = UserFactory.user().toBuilder().notifyMobileOfMessages(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyMobileOfMessages(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyMobileOfMessages(true).build())
    }

    @Test
    fun testNotifyMobileOfUpdates() {
        val user = UserFactory.user().toBuilder().notifyMobileOfUpdates(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyMobileOfUpdates(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyMobileOfUpdates(true).build())
    }
    
    @Test
    fun testNotifyOfFollower() {
        val user = UserFactory.user().toBuilder().notifyOfFollower(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfFollower(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfFollower(true).build())
    }

    @Test
    fun testNotifyOfFriendActivity() {
        val user = UserFactory.user().toBuilder().notifyOfFriendActivity(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfFriendActivity(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfFriendActivity(true).build())
    }

    @Test
    fun testNotifyOfMessages() {
        val user = UserFactory.user().toBuilder().notifyOfMessages(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfMessages(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfMessages(true).build())
    }

    @Test
    fun testNotifyOfUpdates() {
        val user = UserFactory.user().toBuilder().notifyOfUpdates(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfUpdates(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfUpdates(true).build())
    }
}
