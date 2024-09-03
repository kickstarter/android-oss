package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class PrivacyViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: PrivacyViewModel.PrivacyViewModel

    private val currentUserTest = TestSubscriber<User>()
    private val hideConfirmFollowingOptOutPrompt = TestSubscriber<Unit>()
    private val hidePrivateProfile = TestSubscriber<Boolean>()
    private val showConfirmFollowingOptOutPrompt = TestSubscriber<Unit>()
    private val unableToSavePreferenceError = TestSubscriber<String>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(user: User, environment: Environment = environment()) {
        val currentUser = MockCurrentUserV2(user)

        this.vm = PrivacyViewModel.PrivacyViewModel(
            environment.toBuilder()
                .currentUserV2(currentUser)
                .build()
        )

        currentUser.observable().subscribe { this.currentUserTest.onNext(it.getValue()) }
            .addToDisposable(disposables)
        this.vm.outputs.hideConfirmFollowingOptOutPrompt()
            .subscribe { this.hideConfirmFollowingOptOutPrompt.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.showConfirmFollowingOptOutPrompt()
            .subscribe { this.showConfirmFollowingOptOutPrompt.onNext(it) }
            .addToDisposable(disposables)
        this.vm.errors.unableToSavePreferenceError()
            .subscribe { this.unableToSavePreferenceError.onNext(it) }.addToDisposable(disposables)
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

        val env = environment().toBuilder().apiClientV2(object : MockApiClientV2() {
            override fun updateUserSettings(user: User): Observable<User> {
                return Observable.just(user)
            }
        }).build()

        setUpEnvironment(user, env)

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

        val env = environment().toBuilder().apiClientV2(object : MockApiClientV2() {
            override fun updateUserSettings(user: User): Observable<User> {
                return Observable.just(user)
            }
        }).build()

        setUpEnvironment(user, env)

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

        this.vm.outputs.hidePrivateProfileRow().subscribe { this.hidePrivateProfile.onNext(it) }
            .addToDisposable(disposables)
        this.hidePrivateProfile.assertValue(true)
    }

    @Test
    fun testHidePrivateProfileRow_isTrue() {
        val notCreator = UserFactory.user().toBuilder().createdProjectsCount(0).build()
        setUpEnvironment(notCreator)

        this.vm.outputs.hidePrivateProfileRow().subscribe { this.hidePrivateProfile.onNext(it) }
            .addToDisposable(disposables)
        this.hidePrivateProfile.assertValue(false)
    }

    @Test
    fun testOptedOutOfRecommendations() {
        val user = UserFactory.noRecommendations()
        val env = environment().toBuilder().apiClientV2(object : MockApiClientV2() {
            override fun updateUserSettings(user: User): Observable<User> {
                return Observable.just(user)
            }
        }).build()

        setUpEnvironment(user, env)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.optedOutOfRecommendations(true)
        this.currentUserTest.assertValues(
            user,
            user.toBuilder().optedOutOfRecommendations(false).build()
        )

        this.vm.inputs.optedOutOfRecommendations(false)
        this.currentUserTest.assertValues(
            user,
            user.toBuilder().optedOutOfRecommendations(false).build(),
            user
        )
    }

    @Test
    fun testUnableToSavePreferenceError() {
        setUpEnvironment(
            UserFactory.user(),
            environment().toBuilder().apiClientV2(object : MockApiClientV2() {
                override fun updateUserSettings(user: User): Observable<User> {
                    return Observable.error(Throwable("Error"))
                }
            }).build()
        )

        this.vm.inputs.showPublicProfile(true)
        this.unableToSavePreferenceError.assertValueCount(1)
    }
}
