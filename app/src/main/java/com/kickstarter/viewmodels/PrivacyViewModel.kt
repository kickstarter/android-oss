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
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.User
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface PrivacyViewModel {
    interface Inputs {
        /** Call when the user toggles the Following switch.  */
        fun optIntoFollowing(checked: Boolean)

        /** Call when the user confirms or cancels opting out of Following.  */
        fun optOutOfFollowing(optOut: Boolean)

        /** Call when the user toggles the Recommendations switch.  */
        fun optedOutOfRecommendations(checked: Boolean)

        /** Call when user toggles the private profile switch.  */
        fun showPublicProfile(checked: Boolean)
    }

    interface Outputs {
        /** Emits when Following switch should be turned back on after user cancels opting out.  */
        fun hideConfirmFollowingOptOutPrompt(): Observable<Unit>

        /** Emits when the user is a creator and we need to hide the private profile row. */
        fun hidePrivateProfileRow(): Observable<Boolean>

        /** Emits when user should be shown the Following confirmation dialog.  */
        fun showConfirmFollowingOptOutPrompt(): Observable<Unit>

        /** Emits user containing settings state.  */
        fun user(): Observable<User>
    }

    interface Errors {
        /** Emits when saving preference fails.  */
        fun unableToSavePreferenceError(): Observable<String>
    }

    class PrivacyViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs, Errors {
        private val optIntoFollowing = PublishSubject.create<Boolean>()
        private val optOutOfFollowing = PublishSubject.create<Boolean>()
        private val userInput = PublishSubject.create<User>()

        private val hideConfirmFollowingOptOutPrompt = BehaviorSubject.create<Unit>()
        private var hidePrivateProfileRow = BehaviorSubject.create<Boolean>()
        private val showConfirmFollowingOptOutPrompt = BehaviorSubject.create<Unit>()
        private val userOutput = BehaviorSubject.create<User>()
        private val updateSuccess = PublishSubject.create<Unit>()

        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        private val client = requireNotNull(environment.apiClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val disposables = CompositeDisposable()

        init {
            this.client.fetchCurrentUser()
                .retry(2)
                .compose(Transformers.neverErrorV2())
                .subscribe { this.currentUser.refresh(it) }
                .addToDisposable(disposables)

            val currentUser = this.currentUser.observable()

            currentUser
                .take(1)
                .filter { it.isNotNull() && it.getValue().isNotNull() }
                .map { requireNotNull(it.getValue()) }
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            currentUser
                .filter { it.isNotNull() && it.getValue().isNotNull() }
                .map { user -> user.getValue()?.createdProjectsCount().isNonZero() }
                .subscribe { this.hidePrivateProfileRow.onNext(it) }
                .addToDisposable(disposables)

            val updateSettingsNotification = this.userInput
                .concatMap { this.updateSettings(it) }

            updateSettingsNotification
                .compose(valuesV2())
                .subscribe { this.success(it) }
                .addToDisposable(disposables)

            updateSettingsNotification
                .compose(errorsV2())
                .filter { it.isNotNull() }
                .map { it }
                .subscribe { this.unableToSavePreferenceError.onNext(it) }
                .addToDisposable(disposables)

            this.userInput
                .filter { it.isNotNull() }
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            this.userOutput
                .window(2, 1)
                .flatMap<List<User>> { it.toList().toObservable() }
                .map<User> { ListUtils.first(it) }
                .compose<User>(Transformers.takeWhenV2<User, Throwable>(this.unableToSavePreferenceError))
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            this.optIntoFollowing
                .filter { checked -> checked }
                .filter { this.userOutput.value.isNotNull() }
                .map { requireNotNull(this.userOutput.value) }
                .subscribe { this.userInput.onNext(it.toBuilder().social(true).build()) }
                .addToDisposable(disposables)

            this.optIntoFollowing
                .filter { checked -> !checked }
                .subscribe { _ -> this.showConfirmFollowingOptOutPrompt.onNext(Unit) }
                .addToDisposable(disposables)

            this.optOutOfFollowing
                .filter { optOut -> optOut }
                .filter { this.userOutput.value.isNotNull() }
                .map { requireNotNull(this.userOutput.value) }
                .subscribe { this.userInput.onNext(it.toBuilder().social(false).build()) }
                .addToDisposable(disposables)

            this.optOutOfFollowing
                .filter { optOut -> !optOut }
                .subscribe { _ -> this.hideConfirmFollowingOptOutPrompt.onNext(Unit) }
                .addToDisposable(disposables)
        }

        override fun optIntoFollowing(checked: Boolean) {
            this.optIntoFollowing.onNext(checked)
        }

        override fun optOutOfFollowing(optOut: Boolean) {
            this.optOutOfFollowing.onNext(optOut)
        }

        override fun optedOutOfRecommendations(checked: Boolean) {
            this.userOutput.value?.let { user ->
                this.userInput.onNext(user.toBuilder().optedOutOfRecommendations(!checked).build())
            }
        }

        override fun showPublicProfile(checked: Boolean) {
            this.userOutput.value?.let { user ->
                this.userInput.onNext(user.toBuilder().showPublicProfile(!checked).build())
            }
        }

        override fun hideConfirmFollowingOptOutPrompt(): Observable<Unit> = this.hideConfirmFollowingOptOutPrompt

        override fun hidePrivateProfileRow(): Observable<Boolean> = this.hidePrivateProfileRow

        override fun showConfirmFollowingOptOutPrompt(): Observable<Unit> = this.showConfirmFollowingOptOutPrompt

        override fun user(): Observable<User> = this.userOutput

        override fun unableToSavePreferenceError(): Observable<String> = this.unableToSavePreferenceError
            .takeUntil(this.updateSuccess)
            .map { it.localizedMessage }

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(Unit)
        }

        private fun updateSettings(user: User): Observable<Notification<User>> {
            return this.client.updateUserSettings(user)
                .materialize()
                .share()
        }

        override fun onCleared() {
            super.onCleared()
            disposables.clear()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PrivacyViewModel(environment) as T
        }
    }
}
