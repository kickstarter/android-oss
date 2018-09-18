package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.EditProfileActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface EditProfileViewModel {

    interface Inputs {
        /** Call when user toggles the private profile switch.  */
        fun showPublicProfile(checked: Boolean)
    }

    interface Outputs {
        /** Emits the user avatar image to be displayed.  */
        fun avatarImageViewUrl(): Observable<String>

        /** Emits when the user is a creator and we need to hide the private profile row. */
        fun hidePrivateProfileRow(): Observable<Boolean>

        /** Emits user containing settings state.  */
        fun user(): Observable<User>

        /** Emits the user name to be displayed.  */
        fun userNameTextViewText(): Observable<String>
    }

    interface Errors {
        /** Emits when saving preference fails.  */
        fun unableToSavePreferenceError(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<EditProfileActivity>(environment), Inputs, Outputs, Errors {

        private val client: ApiClientType = environment.apiClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        private val userInput = PublishSubject.create<User>()
        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()
        private val updateSuccess = PublishSubject.create<Void>()

        private var hidePrivateProfileRow = BehaviorSubject.create<Boolean>()
        private val userOutput = BehaviorSubject.create<User>()

        private val avatarImageViewUrl: Observable<String>
        private val userNameTextViewText: Observable<String>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.client.fetchCurrentUser()
                    .retry(2)
                    .compose(Transformers.neverError())
                    .compose(bindToLifecycle())
                    .subscribe { this.currentUser.refresh(it) }

            this.currentUser.observable()
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe({ this.userOutput.onNext(it) })

            this.userInput
                    .concatMap<User>({ this.updateSettings(it) })
                    .compose(bindToLifecycle())
                    .subscribe({ this.success(it) })

            this.userInput
                    .compose(bindToLifecycle())
                    .subscribe(this.userOutput)

            this.userOutput
                    .window(2, 1)
                    .flatMap<List<User>>({ it.toList() })
                    .map<User>({ ListUtils.first(it) })
                    .compose<User>(Transformers.takeWhen<User, Throwable>(this.unableToSavePreferenceError))
                    .compose(bindToLifecycle())
                    .subscribe(this.userOutput)

            val currentUser = this.currentUser.observable()

            currentUser
                    .compose(bindToLifecycle())
                    .filter(ObjectUtils::isNotNull)
                    .map { user -> IntegerUtils.isNonZero(user.createdProjectsCount()) }
                    .subscribe(this.hidePrivateProfileRow)

            this.avatarImageViewUrl = this.currentUser.loggedInUser().map { u -> u.avatar().medium() }

            this.userNameTextViewText = this.currentUser.loggedInUser().map({ it.name() })

            this.koala.trackSettingsView()
        }

        override fun avatarImageViewUrl() = this.avatarImageViewUrl

        override fun hidePrivateProfileRow(): Observable<Boolean> = this.hidePrivateProfileRow

        override fun showPublicProfile(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().showPublicProfile(!checked).build())
        }

        override fun unableToSavePreferenceError(): Observable<String> = this.unableToSavePreferenceError
                .takeUntil(this.updateSuccess)
                .map { _ -> null }

        override fun user(): Observable<User> = this.userOutput

        override fun userNameTextViewText() = this.userNameTextViewText

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(null)
        }

        private fun updateSettings(user: User): Observable<User> {
            return this.client.updateUserSettings(user)
                    .compose(Transformers.pipeErrorsTo(this.unableToSavePreferenceError))
        }
    }
}
