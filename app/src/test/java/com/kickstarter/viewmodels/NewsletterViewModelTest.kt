package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import com.kickstarter.ui.data.Newsletter
import org.junit.Test
import rx.observers.TestSubscriber

class NewsletterViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: NewsletterViewModel.ViewModel
    private val currentUserTest = TestSubscriber<User>()
    private val showOptInPromptTest = TestSubscriber<Newsletter>()
    private val subscribeAll = TestSubscriber<Boolean>()

    private fun setUpEnvironment(user: User) {
        val currentUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()

        currentUser.observable().subscribe(this.currentUserTest)

        this.vm = NewsletterViewModel.ViewModel(environment)
        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)
        this.vm.outputs.subscribeAll().subscribe(this.subscribeAll)
    }

    @Test
    fun testUserEmits() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)
        this.koalaTest.assertValue("Viewed Newsletter")
    }

    @Test
    fun testAlumniNewsletter() {
        val user = UserFactory.user().toBuilder().alumniNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendAlumniNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().alumniNewsletter(true).build())

        this.vm.inputs.sendAlumniNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().alumniNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testArtNewsletter() {
        val user = UserFactory.user().toBuilder().artsCultureNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendArtsNewsNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().artsCultureNewsletter(true).build())

        this.vm.inputs.sendArtsNewsNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().artsCultureNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testFilmNewsletter() {
        val user = UserFactory.user().toBuilder().filmNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendFilmsNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().filmNewsletter(true).build())

        this.vm.inputs.sendFilmsNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().filmNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testGamesNewsletter() {
        val user = UserFactory.user().toBuilder().gamesNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendGamesNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().gamesNewsletter(true).build())

        this.vm.inputs.sendGamesNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().gamesNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testHappeningNewsletter() {
        val user = UserFactory.user().toBuilder().happeningNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendHappeningNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build())

        this.vm.inputs.sendHappeningNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testInventNewsletter() {
        val user = UserFactory.user().toBuilder().inventNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendInventNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter","Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().inventNewsletter(true).build())

        this.vm.inputs.sendInventNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().inventNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testPromoNewsletter() {
        val user = UserFactory.user().toBuilder().promoNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendPromoNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build())

        this.vm.inputs.sendPromoNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testPublishingNewsletter() {
        val user = UserFactory.user().toBuilder().publishingNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendReadsNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().publishingNewsletter(true).build())

        this.vm.inputs.sendReadsNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().publishingNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testSubscribeAll() {
        val user = UserFactory.user().toBuilder().alumniNewsletter(true).artsCultureNewsletter(true)
                .filmNewsletter(true).gamesNewsletter(true).inventNewsletter(true)
                .happeningNewsletter(true).promoNewsletter(true).publishingNewsletter(true)
                .weeklyNewsletter(false).build()

        setUpEnvironment(user)

        this.subscribeAll.assertValue(false)

        this.vm.inputs.sendWeeklyNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build())
        this.subscribeAll.assertValues(false, true)

        this.vm.inputs.sendWeeklyNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build(), user)
        this.subscribeAll.assertValues(false, true, false)
    }

    @Test
    fun testWeeklyNewsletter() {
        val user = UserFactory.user().toBuilder().weeklyNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendWeeklyNewsletter(true)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build())

        this.vm.inputs.sendWeeklyNewsletter(false)
        this.koalaTest.assertValues("Viewed Newsletter", "Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }
}
