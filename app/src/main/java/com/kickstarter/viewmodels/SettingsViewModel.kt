package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface SettingsViewModel {

    interface Inputs {
        /** Call when the user dismiss the logout confirmation dialog.  */
        fun closeLogoutConfirmationClicked()

        /** Call when the user has confirmed that they want to log out.  */
        fun confirmLogoutClicked()

        /** Call when the user taps the logout button.  */
        fun logoutClicked()
    }

    interface Outputs {
        /** Emits the user avatar image to be displayed.  */
        fun avatarImageViewUrl(): Observable<String>

        /** Emits when its time to log the user out.  */
        fun logout(): Observable<Unit>

        /** Emits a boolean that determines if the logout confirmation should be displayed.  */
        fun showConfirmLogoutPrompt(): Observable<Boolean>

        /** Emits the user name to be displayed.  */
        fun userNameTextViewText(): Observable<String>
    }

    class SettingsViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {

        private val client = requireNotNull(environment.apiClientV2())
        private val confirmLogoutClicked = PublishSubject.create<Unit>()
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val logout = BehaviorSubject.create<Unit>()
        private val showConfirmLogoutPrompt = BehaviorSubject.create<Boolean>()
        private val userOutput = BehaviorSubject.create<User>()

        val isUserPresent = BehaviorSubject.create<Boolean>()

        private val avatarImageViewUrl: Observable<String>
        private val userNameTextViewText: Observable<String>

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val analytics = this.environment.analytics()

        private val disposables = CompositeDisposable()

        init {

            this.client.fetchCurrentUser()
                .retry(2)
                .compose(Transformers.neverErrorV2())
                .subscribe { this.currentUser.refresh(it) }
                .addToDisposable(disposables)

            this.currentUser.observable()
                .take(1)
                .subscribe { it.getValue()?.let { user -> this.userOutput.onNext(user) } }
                .addToDisposable(disposables)

            this.currentUser.observable().subscribe { isUserPresent.onNext(it.isPresent()) }
                .addToDisposable(disposables)

            this.confirmLogoutClicked
                .subscribe {
                    this.analytics?.reset()
                    this.logout.onNext(Unit)
                }
                .addToDisposable(disposables)

            this.avatarImageViewUrl =
                this.currentUser.loggedInUser().map { u -> u.avatar().medium() }

            this.userNameTextViewText = this.currentUser.loggedInUser().map { it.name() }
        }

        override fun avatarImageViewUrl() = this.avatarImageViewUrl

        override fun closeLogoutConfirmationClicked() = this.showConfirmLogoutPrompt.onNext(false)

        override fun confirmLogoutClicked() = this.confirmLogoutClicked.onNext(Unit)

        override fun logoutClicked() = this.showConfirmLogoutPrompt.onNext(true)

        override fun logout(): Observable<Unit> = this.logout

        override fun showConfirmLogoutPrompt(): Observable<Boolean> = this.showConfirmLogoutPrompt

        override fun userNameTextViewText() = this.userNameTextViewText

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(environment) as T
        }
    }
}
