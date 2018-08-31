package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.factories.UserFactory
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.models.User
import org.junit.Test
import rx.observers.TestSubscriber

class ChangeEmailViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ChangeEmailViewModel.ViewModel

    private val userEmail = TestSubscriber<String>()


    private fun setUpEnvironment(user: User) {
        val currentUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()


        this.vm = ChangeEmailViewModel.ViewModel(environment)

        this.vm.outputs.userEmail().subscribe(this.userEmail)
    }

    @Test
    fun testUserEmail() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.userEmail.assertValueCount(1)
    }
}