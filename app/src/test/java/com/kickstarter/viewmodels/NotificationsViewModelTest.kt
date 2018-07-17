package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.factories.UserFactory
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.models.User
import org.junit.Test
import rx.observers.TestSubscriber

class NotificationsViewModelTest : KSRobolectricTestCase() {
    private var vm: NotificationsViewModel.ViewModel? = null

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
    fun testUserEmits() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)
    }
}
