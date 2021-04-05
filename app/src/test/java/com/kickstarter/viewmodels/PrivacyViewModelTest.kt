package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.User
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class PrivacyViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: PrivacyViewModel.ViewModel

    private val currentUserTest = TestSubscriber<User>()
    private val hideConfirmFollowingOptOutPrompt = TestSubscriber<Void>()
    private val hidePrivateProfile = TestSubscriber<Boolean>()
    private val showConfirmFollowingOptOutPrompt = TestSubscriber<Void>()
    private val unableToSavePreferenceError = TestSubscriber<String>()

    private fun setUpEnvironment(user: User, environment: Environment = environment()) {
        val currentUser = MockCurrentUser(user)

        this.vm = PrivacyViewModel.ViewModel(
            environment.toBuilder()
                .currentUser(currentUser)
                .build()
        )

        currentUser.observable().subscribe(this.currentUserTest)
        this.vm.outputs.hideConfirmFollowingOptOutPrompt().subscribe(this.hideConfirmFollowingOptOutPrompt)
        this.vm.outputs.showConfirmFollowingOptOutPrompt().subscribe(this.showConfirmFollowingOptOutPrompt)
        this.vm.errors.unableToSavePreferenceError().subscribe(this.unableToSavePreferenceError)
    }

    @Test
    fun tesCurrentUser() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValue(user)
    }

    @Test
    fun testOptIntoFollowing() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.optIntoFollowing(true)
        this.currentUserTest.assertValues(user, user.toBuilder().social(true).build())

        this.showConfirmFollowingOptOutPrompt.assertNoValues()
        this.hideConfirmFollowingOptOutPrompt.assertNoValues()
    }

    @Test
    fun testHideConfirmFollowingOptOutPrompt_userCancelsOptOut() {
        val user = UserFactory.socialUser()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.optIntoFollowing(false)
        this.currentUserTest.assertValues(user)
        this.showConfirmFollowingOptOutPrompt.assertValueCount(1)

        this.vm.inputs.optOutOfFollowing(false)
        this.hideConfirmFollowingOptOutPrompt.assertValueCount(1)
        this.currentUserTest.assertValues(user)
    }

    @Test
    fun testHideConfirmFollowingOptOutPrompt_userConfirmsOptOut() {
        val user = UserFactory.socialUser()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.optIntoFollowing(false)
        this.currentUserTest.assertValues(user)
        this.showConfirmFollowingOptOutPrompt.assertValueCount(1)

        this.vm.inputs.optOutOfFollowing(true)
        this.currentUserTest.assertValues(user, user.toBuilder().social(false).build())

        this.hideConfirmFollowingOptOutPrompt.assertNoValues()
    }

    @Test
    fun testHidePrivateProfileRow_isFalse() {
        val creator = UserFactory.creator()
        setUpEnvironment(creator)

        this.vm.outputs.hidePrivateProfileRow().subscribe(this.hidePrivateProfile)
        this.hidePrivateProfile.assertValue(true)
    }

    @Test
    fun testHidePrivateProfileRow_isTrue() {
        val notCreator = UserFactory.user().toBuilder().createdProjectsCount(0).build()
        setUpEnvironment(notCreator)

        this.vm.outputs.hidePrivateProfileRow().subscribe(this.hidePrivateProfile)
        this.hidePrivateProfile.assertValue(false)
    }

    @Test
    fun testOptedOutOfRecommendations() {
        val user = UserFactory.noRecommendations()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.optedOutOfRecommendations(true)
        this.currentUserTest.assertValues(user, user.toBuilder().optedOutOfRecommendations(false).build())

        this.vm.inputs.optedOutOfRecommendations(false)
        this.currentUserTest.assertValues(user, user.toBuilder().optedOutOfRecommendations(false).build(), user)
    }

    @Test
    fun testUnableToSavePreferenceError() {
        setUpEnvironment(
            UserFactory.user(),
            environment().toBuilder().apiClient(object : MockApiClient() {
                override fun updateUserSettings(user: User): Observable<User> {
                    return Observable.error(Throwable("Error"))
                }
            }).build()
        )

        this.vm.inputs.showPublicProfile(true)
        this.unableToSavePreferenceError.assertValueCount(1)
    }
}
