package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.factories.UserFactory
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.models.User
import org.junit.Test
import rx.observers.TestSubscriber

class NewsletterViewModelTest : KSRobolectricTestCase() {

    private var vm: NewsletterViewModel.ViewModel? = null
    private val currentUserTest = TestSubscriber<User>()
//    private val showOptInPromptTest = TestSubscriber<Newsletter>()

    private fun setUpEnvironment( user : User) {
        val currentUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()

        currentUser.observable().subscribe(this.currentUserTest)

        this.vm = NewsletterViewModel.ViewModel(environment)
//        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)
    }

    @Test
    fun testUserEmits() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)
    }

//    @Test
//    fun testSettingsViewModel_sendHappeningNewsletter() {
//        val user = UserFactory.user().toBuilder().happeningNewsletter(false).build()
//
//        setUpEnvironment(user)
//
//        vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)
//
//        currentUserTest.assertValues(user)
//        koalaTest.assertValues("Newsletter View")
//
//        vm.inputs.sendHappeningNewsletter(true)
//        koalaTest.assertValues("Newsletter View", "Newsletter Subscribe")
//        currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build())
//
//        vm.inputs.sendHappeningNewsletter(false)
//        koalaTest.assertValues("Newsletter View", "Newsletter Subscribe", "Newsletter Unsubscribe")
//        currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build(), user)
//
//        showOptInPromptTest.assertNoValues();
//        showOptInPromptTest.assertNoValues()
//    }
//
//    @Test
//    fun testSettingsViewModel_sendPromoNewsletter() {
//        val user = UserFactory.user().toBuilder().promoNewsletter(false).build()
//
//        setUpEnvironment(user)
//
//        vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)
//
//        currentUserTest.assertValues(user)
//        koalaTest.assertValues("Newsletter View")
//
//        vm.inputs.sendPromoNewsletter(true)
//        koalaTest.assertValues("Newsletter View", "Newsletter Subscribe")
//        currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build())
//
//        vm.inputs.sendPromoNewsletter(false)
//        koalaTest.assertValues("Newsletter View", "Newsletter Subscribe", "Newsletter Unsubscribe")
//        currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build(), user)
//
//        showOptInPromptTest.assertNoValues()
//    }
//
//    @Test
//    fun testSettingsViewModel_sendWeeklyNewsletter() {
//        val user = UserFactory.user().toBuilder().weeklyNewsletter(false).build()
//
//        setUpEnvironment(user)
//
//        vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)
//
//        currentUserTest.assertValues(user)
//        koalaTest.assertValues("Newsletter View")
//
//        vm.inputs.sendWeeklyNewsletter(true)
//        koalaTest.assertValues("Newsletter View", "Newsletter Subscribe")
//        currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build())
//
//        vm.inputs.sendWeeklyNewsletter(false)
//        koalaTest.assertValues("Newsletter View", "Newsletter Subscribe", "Newsletter Unsubscribe")
//        currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build(), user)
//
//        showOptInPromptTest.assertNoValues()
//    }
}