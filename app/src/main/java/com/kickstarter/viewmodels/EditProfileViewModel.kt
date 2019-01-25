package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.errors
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.EditProfileActivity
import rx.Notification
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface EditProfileViewModel {

    interface Inputs {
        /** Call when user toggles the private profile switch.  */
        fun showPublicProfile(checked: Boolean)
    }

    interface Outputs {
        /** Emits when the user is a creator and we need to hide the private profile row. */
        fun hidePrivateProfileRow(): Observable<Boolean>

        /** Emits current user.  */
        fun user(): Observable<User>

        /** Emits the user's avatar URL.  */
        fun userAvatarUrl(): Observable<String>

        /** Emits the user's name.  */
        fun userName(): Observable<String>
    }

    interface Errors {
        /** Emits when saving preference fails.  */
        fun unableToSavePreferenceError(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<EditProfileActivity>(environment), Inputs, Outputs, Errors {

        private val client: ApiClientType = environment.apiClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        private val userInput = PublishSubject.create<User>()

        private var hidePrivateProfileRow = BehaviorSubject.create<Boolean>()
        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()
        private val updateSuccess = PublishSubject.create<Void>()
        private val user = BehaviorSubject.create<User>()
        private val userAvatarUrl = BehaviorSubject.create<String>()
        private val userName = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val currentUser = this.currentUser.observable()

            this.client.fetchCurrentUser()
                    .retry(2)
                    .compose(Transformers.neverError())
                    .compose(bindToLifecycle())
                    .subscribe { this.currentUser.refresh(it) }

            currentUser
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe { this.user.onNext(it) }

            val updateUserNotification = this.userInput
                    .concatMap<Notification<User>> { this.updateSettings(it) }

            updateUserNotification
                    .compose(values())
                    .compose(bindToLifecycle())
                    .subscribe { this.success(it) }

            updateUserNotification
                    .compose(errors())
                    .compose(bindToLifecycle())
                    .subscribe(this.unableToSavePreferenceError)

            this.userInput
                    .compose(bindToLifecycle())
                    .subscribe(this.user)

            this.user
                    .window(2, 1)
                    .flatMap<List<User>> { it.toList() }
                    .map<User> { ListUtils.first(it) }
                    .compose<User>(Transformers.takeWhen<User, Throwable>(this.unableToSavePreferenceError))
                    .compose(bindToLifecycle())
                    .subscribe(this.user)

            currentUser
                    .compose(bindToLifecycle())
                    .filter(ObjectUtils::isNotNull)
                    .map { user -> IntegerUtils.isNonZero(user.createdProjectsCount()) }
                    .subscribe(this.hidePrivateProfileRow)

            currentUser
                    .map { u -> u.avatar().medium() }
                    .subscribe(this.userAvatarUrl)

            currentUser
                    .map { it.name() }
                    .subscribe(this.userName)

        }

        override fun userAvatarUrl(): Observable<String> = this.userAvatarUrl

        override fun hidePrivateProfileRow(): Observable<Boolean> = this.hidePrivateProfileRow

        override fun showPublicProfile(checked: Boolean) {
            this.userInput.onNext(this.user.value.toBuilder().showPublicProfile(!checked).build())
        }

        override fun unableToSavePreferenceError(): Observable<String> = this.unableToSavePreferenceError
                .takeUntil(this.updateSuccess)
                .map { _ -> null }

        override fun user(): Observable<User> = this.user

        override fun userName(): Observable<String> = this.userName

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(null)
        }

        private fun updateSettings(user: User): Observable<Notification<User>>? {
            return this.client.updateUserSettings(user)
                    .materialize()
                    .share()
        }
    }
}
