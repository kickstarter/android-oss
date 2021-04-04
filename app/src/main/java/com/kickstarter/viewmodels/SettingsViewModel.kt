package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.SettingsActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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
        fun logout(): Observable<Void>

        /** Emits a boolean that determines if the logout confirmation should be displayed.  */
        fun showConfirmLogoutPrompt(): Observable<Boolean>

        /** Emits the user name to be displayed.  */
        fun userNameTextViewText(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<SettingsActivity>(environment), Inputs, Outputs {

        private val client: ApiClientType = environment.apiClient()
        private val confirmLogoutClicked = PublishSubject.create<Void>()
        private val currentUser: CurrentUserType = environment.currentUser()
        private val logout = BehaviorSubject.create<Void>()
        private val showConfirmLogoutPrompt = BehaviorSubject.create<Boolean>()
        private val userOutput = BehaviorSubject.create<User>()

        private val avatarImageViewUrl: Observable<String>
        private val userNameTextViewText: Observable<String>

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val analytics: AnalyticEvents = this.environment.analytics()
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

            this.confirmLogoutClicked
                .compose(bindToLifecycle())
                .subscribe {
                    this.analytics.reset()
                    this.logout.onNext(null)
                }

            this.avatarImageViewUrl = this.currentUser.loggedInUser().map { u -> u.avatar().medium() }

            this.userNameTextViewText = this.currentUser.loggedInUser().map({ it.name() })
        }

        override fun avatarImageViewUrl() = this.avatarImageViewUrl

        override fun closeLogoutConfirmationClicked() = this.showConfirmLogoutPrompt.onNext(false)

        override fun confirmLogoutClicked() = this.confirmLogoutClicked.onNext(null)

        override fun logoutClicked() = this.showConfirmLogoutPrompt.onNext(true)

        override fun logout(): Observable<Void> = this.logout

        override fun showConfirmLogoutPrompt(): Observable<Boolean> = this.showConfirmLogoutPrompt

        override fun userNameTextViewText() = this.userNameTextViewText
    }
}
