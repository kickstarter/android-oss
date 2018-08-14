package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.factories.UserFactory
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.models.User
import org.junit.Test
import rx.observers.TestSubscriber

class NotificationsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: NotificationsViewModel.ViewModel

    private val creatorDigestFrequencyIsGone = TestSubscriber<Boolean>()
    private val creatorNotificationsAreGone = TestSubscriber<Boolean>()
    private val currentUserTest = TestSubscriber<User>()

    private fun setUpEnvironment(user: User) {
        val currentUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()

        currentUser.observable().subscribe(this.currentUserTest)

        this.vm = NotificationsViewModel.ViewModel(environment)

        this.vm.outputs.creatorDigestFrequencyIsGone().subscribe(this.creatorDigestFrequencyIsGone)
        this.vm.outputs.creatorNotificationsAreGone().subscribe(this.creatorNotificationsAreGone)
    }

    @Test
    fun testCreatorDigestFrequencyIsGone_IsFalseWhenUserHasBackingsEmails() {
        val user = UserFactory.creator().toBuilder().notifyOfBackings(true).build()

        setUpEnvironment(user)

        this.creatorDigestFrequencyIsGone.assertValue(false)
    }

    @Test
    fun testCreatorDigestFrequencyIsGone_IsTrueWhenUserDoesNotHaveBackingsEmails() {
        val user = UserFactory.creator().toBuilder().notifyOfBackings(false).build()

        setUpEnvironment(user)

        this.creatorDigestFrequencyIsGone.assertValue(true)
    }

    @Test
    fun testCreatorNotificationsAreGone_IsFalseWhenUserACreator() {
        val user = UserFactory.creator()

        setUpEnvironment(user)

        this.creatorNotificationsAreGone.assertValue(false)
    }

    @Test
    fun testCreatorNotificationsAreGone_IsTrueWhenUserIsNotACreator() {
        val user = UserFactory.collaborator()

        setUpEnvironment(user)

        this.creatorNotificationsAreGone.assertValue(true)
    }

    @Test
    fun testCurrentUser() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValue(user)
    }

    @Test
    fun testNotifyMobileOfBackings() {
        val user = UserFactory.user().toBuilder().notifyMobileOfBackings(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyMobileOfBackings(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyMobileOfBackings(true).build())
    }

    @Test
    fun testNotifyMobileOfComments() {
        val user = UserFactory.user().toBuilder().notifyMobileOfComments(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyMobileOfComments(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyMobileOfComments(true).build())
    }

    @Test
    fun testNotifyMobileOfCreatorEdu() {
        val user = UserFactory.user().toBuilder().notifyMobileOfCreatorEdu(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyMobileOfCreatorEdu(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyMobileOfCreatorEdu(true).build())
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
    fun testNotifyMobileOfPostLikes() {
        val user = UserFactory.user().toBuilder().notifyMobileOfPostLikes(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyMobileOfPostLikes(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyMobileOfPostLikes(true).build())
    }

    @Test
    fun testNotifyMobileOfUpdates() {
        val user = UserFactory.user().toBuilder().notifyMobileOfUpdates(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyMobileOfUpdates(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyMobileOfUpdates(true).build())
    } 
    
    @Test
    fun testNotifyOfBackings() {
        val user = UserFactory.user().toBuilder().notifyOfBackings(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfBackings(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfBackings(true).build())


        this.vm.inputs.notifyOfCreatorDigest(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfBackings(true).build(),
                user.toBuilder().notifyOfBackings(true).notifyOfCreatorDigest(true).build())

        //when we turn off backing emails, we also turn off the digest
        this.vm.inputs.notifyOfBackings(false)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfBackings(true).build(),
                user.toBuilder().notifyOfBackings(true).notifyOfCreatorDigest(true).build(),
                user.toBuilder().notifyOfBackings(false).notifyOfCreatorDigest(false).build())
    }

    @Test
    fun testNotifyOfComments() {
        val user = UserFactory.user().toBuilder().notifyOfComments(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfComments(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfComments(true).build())
    }

    @Test
    fun testNotifyOfCreatorDigest() {
        val user = UserFactory.user().toBuilder().notifyOfCreatorDigest(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfCreatorDigest(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfCreatorDigest(true).build())
    }

    @Test
    fun testNotifyOfCreatorEdu() {
        val user = UserFactory.user().toBuilder().notifyOfCreatorEdu(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfCreatorEdu(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfCreatorEdu(true).build())
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
    fun testNotifyOfPostLikes() {
        val user = UserFactory.user().toBuilder().notifyOfPostLikes(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfPostLikes(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfPostLikes(true).build())
    }

    @Test
    fun testNotifyOfUpdates() {
        val user = UserFactory.user().toBuilder().notifyOfUpdates(false).build()

        setUpEnvironment(user)

        this.vm.inputs.notifyOfUpdates(true)
        this.currentUserTest.assertValues(user, user.toBuilder().notifyOfUpdates(true).build())
    }
}
