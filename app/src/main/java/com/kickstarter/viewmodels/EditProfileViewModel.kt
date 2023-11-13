package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.models.User
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface EditProfileViewModel {

    interface Inputs {
        /** Call when user toggles the private profile switch.  */
        fun privateProfileChecked(checked: Boolean)
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

    class EditProfileViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs, Errors {

        private val apiClient = requireNotNull(environment.apiClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())

        private val userInput = PublishSubject.create<User>()
        private val showPublicProfile = PublishSubject.create<Boolean>()

        private var hidePrivateProfileRow = BehaviorSubject.create<Boolean>()
        private val unableToSavePreferenceError = PublishSubject.create<String>()
        private val updateSuccess = PublishSubject.create<Unit>()
        private val user = BehaviorSubject.create<User>()
        private val userAvatarUrl = BehaviorSubject.create<String>()
        private val userName = BehaviorSubject.create<String>()
        val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        init {

            val currentUser = this.currentUser.observable()

            this.apiClient.fetchCurrentUser()
                .retry(2)
                .compose(Transformers.neverErrorV2())
                .subscribe { this.currentUser.refresh(it) }
                .addToDisposable(disposables)

            currentUser
                .filter { it.isPresent() }
                .map { requireNotNull(it.getValue()) }
                .take(1)
                .subscribe { this.user.onNext(it) }
                .addToDisposable(disposables)

            val updateUserNotification = this.userInput
                .concatMap<Notification<User>> { this.updateSettings(it) }

            updateUserNotification
                .compose(valuesV2())
                .subscribe { this.success(it) }
                .addToDisposable(disposables)

            updateUserNotification
                .compose(errorsV2())
                .subscribe { this.unableToSavePreferenceError.onNext(it?.localizedMessage ?: "") }
                .addToDisposable(disposables)

            this.userInput
                .subscribe { this.user.onNext(it) }
                .addToDisposable(disposables)

            this.user
                .window(2, 1)
                .flatMap<List<User>> { it.toList().toObservable() }
                .map<User> { ListUtils.first(it) }
                .compose<User>(Transformers.takeWhenV2(this.unableToSavePreferenceError))
                .subscribe { this.user.onNext(it) }
                .addToDisposable(disposables)

            currentUser
                .filter { it.isPresent() }
                .map { requireNotNull(it.getValue()) }
                .map { user -> user.createdProjectsCount().isNonZero() }
                .subscribe { this.hidePrivateProfileRow.onNext(it) }
                .addToDisposable(disposables)

            currentUser
                .filter { it.isPresent() }
                .map { requireNotNull(it.getValue()) }
                .map { it.avatar().medium() }
                .subscribe { this.userAvatarUrl.onNext(it) }
                .addToDisposable(disposables)

            currentUser
                .filter { it.isPresent() }
                .map { requireNotNull(it.getValue()) }
                .map { it.name() }
                .subscribe { this.userName.onNext(it) }
                .addToDisposable(disposables)

            this.showPublicProfile
                .withLatestFrom(user) { showProfile, user -> user.toBuilder().showPublicProfile(!showProfile).build() }
                .subscribe { this.userInput.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        override fun userAvatarUrl(): Observable<String> = this.userAvatarUrl

        override fun hidePrivateProfileRow(): Observable<Boolean> = this.hidePrivateProfileRow

        override fun privateProfileChecked(checked: Boolean) {
            this.showPublicProfile.onNext(checked)
        }

        override fun unableToSavePreferenceError(): Observable<String> = this.unableToSavePreferenceError
            .takeUntil(this.updateSuccess)

        override fun user(): Observable<User> = this.user

        override fun userName(): Observable<String> = this.userName

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(Unit)
        }

        private fun updateSettings(user: User): Observable<Notification<User>>? {
            return this.apiClient.updateUserSettings(user)
                .materialize()
                .share()
        }
    }
    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditProfileViewModel(environment) as T
        }
    }
}
