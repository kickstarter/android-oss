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
import org.junit.After
import org.junit.Test

class EditProfileViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: EditProfileViewModel.EditProfileViewModel

    private val hidePrivateProfileRow = TestSubscriber<Boolean>()
    private val user = TestSubscriber<User>()
    private val userAvatarUrl = TestSubscriber<String>()
    private val userName = TestSubscriber<String>()
    private val unableToSavePreferenceError = TestSubscriber<String>()

    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = EditProfileViewModel.EditProfileViewModel(environment)

        this.vm.outputs.hidePrivateProfileRow().subscribe { this.hidePrivateProfileRow.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.user().subscribe { this.user.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.userAvatarUrl().subscribe { this.userAvatarUrl.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.userName().subscribe { this.userName.onNext(it) }.addToDisposable(disposables)
        this.vm.errors.unableToSavePreferenceError().subscribe { this.unableToSavePreferenceError.onNext(it) }.addToDisposable(disposables)
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }

    @Test
    fun testWhenCurrentUserNotPresent_doesNotEmitToUser() {
        setUpEnvironment(
            environment()
                .toBuilder()
                .apiClientV2(MockApiClientV2())
                .currentUserV2(MockCurrentUserV2())
                .build()
        )

        this.user.assertNoValues()
        this.userAvatarUrl.assertNoValues()
        this.userName.assertNoValues()
    }

    @Test
    fun testWhenCurrentUserPresent_emitsToUser() {
        val user = UserFactory.user()
        setUpEnvironment(
            environment()
                .toBuilder()
                .apiClientV2(object : MockApiClientV2() {
                    override fun fetchCurrentUser(): Observable<User> {
                        return Observable.just(user)
                    }
                })
                .build()
        )

        this.user.assertValue(user)
    }

    @Test
    fun testWhenUpdateSettingFails_emitsError() {
        val errorMessage = "failed to update"
        val user = UserFactory.user()
        setUpEnvironment(
            environment()
                .toBuilder()
                .apiClientV2(object : MockApiClientV2() {
                    override fun fetchCurrentUser(): Observable<User> {
                        return Observable.just(user.toBuilder().showPublicProfile(true).build())
                    }

                    override fun updateUserSettings(user: User): Observable<User> {
                        return Observable.error(Throwable(errorMessage))
                    }
                })
                .build()
        )

        this.vm.inputs.privateProfileChecked(true)

        this.user.assertValueCount(2)
        assertEquals(this.user.values().get(0).showPublicProfile(), true)
        assertEquals(this.user.values().get(1).showPublicProfile(), false)
        this.unableToSavePreferenceError.assertValueCount(1)
        this.unableToSavePreferenceError.assertValue(errorMessage)
    }

    @Test
    fun testWhenUpdateSettingSuccess_emitsNewUser() {
        val userFalse = UserFactory.user().toBuilder().showPublicProfile(false).build()
        val userTrue = UserFactory.user().toBuilder().showPublicProfile(true).build()

        setUpEnvironment(
            environment()
                .toBuilder()
                .apiClientV2(object : MockApiClientV2() {
                    override fun fetchCurrentUser(): Observable<User> {
                        return Observable.just(userFalse)
                    }

                    override fun updateUserSettings(user: User): Observable<User> {
                        return Observable.just(userTrue)
                    }
                })
                .build()
        )

        this.vm.inputs.privateProfileChecked(true)
        this.user.assertValueCount(2)
        assertEquals(this.user.values().get(1).showPublicProfile(), false)
    }

    @Test
    fun testPrivateProfileRow_whenUserCreator_hidePrivateProfileRow() {
        val creator = UserFactory.user().toBuilder().createdProjectsCount(3).build()
        setUpEnvironment(
            environment()
                .toBuilder()
                .apiClientV2(object : MockApiClientV2() {
                    override fun fetchCurrentUser(): Observable<User> {
                        return Observable.just(creator)
                    }
                })
                .build()
        )

        assertEquals(this.hidePrivateProfileRow.values().get(0), true)
    }

    @Test
    fun testPrivateProfileRow_whenUserNotCreator_showPrivateProfileRow() {
        val user = UserFactory.user().toBuilder().createdProjectsCount(0).build()
        setUpEnvironment(
            environment()
                .toBuilder()
                .apiClientV2(object : MockApiClientV2() {
                    override fun fetchCurrentUser(): Observable<User> {
                        return Observable.just(user)
                    }
                })
                .build()
        )

        assertEquals(this.hidePrivateProfileRow.values().get(0), false)
    }

    @Test
    fun whenUserPresent_emitsProfilePicture() {
        val user = UserFactory.user().toBuilder().build()
        setUpEnvironment(
            environment()
                .toBuilder()
                .apiClientV2(object : MockApiClientV2() {
                    override fun fetchCurrentUser(): Observable<User> {
                        return Observable.just(user)
                    }
                })
                .build()
        )

        this.userAvatarUrl.assertValueCount(1)
    }

    @Test
    fun whenUserPresent_emitsUsername() {
        val user = UserFactory.user().toBuilder().build()
        setUpEnvironment(
            environment()
                .toBuilder()
                .apiClientV2(object : MockApiClientV2() {
                    override fun fetchCurrentUser(): Observable<User> {
                        return Observable.just(user)
                    }
                })
                .build()
        )

        this.userName.assertValueCount(1)
    }
}
