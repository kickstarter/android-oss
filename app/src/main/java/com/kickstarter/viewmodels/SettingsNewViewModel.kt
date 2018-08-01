package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.SettingsNewActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface SettingsNewViewModel {

    interface Inputs {
        /** Call when the user dismiss the logout confirmation dialog.  */
        fun closeLogoutConfirmationClicked()

        /** Call when the user has confirmed that they want to log out.  */
        fun confirmLogoutClicked()

        /** Call when the user taps the logout button.  */
        fun logoutClicked()
    }

    interface Outputs {
        /** Emits the user's avatar photo url for display.  */
        fun avatarPhotoUrl(): Observable<String>

        /** Emits when its time to log the user out.  */
        fun logout(): Observable<Void>

        /** Emits a boolean that determines if the logout confirmation should be displayed.  */
        fun showConfirmLogoutPrompt(): Observable<Boolean>

        /** Emits the user's name. */
        fun userName(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<SettingsNewActivity>(environment), Inputs, Outputs {

        private var avatarPhotoUrl: Observable<String>
        private val client: ApiClientType = environment.apiClient()
        private val confirmLogoutClicked = PublishSubject.create<Void>()
        private val currentUser: CurrentUserType = environment.currentUser()
        private val logout = BehaviorSubject.create<Void>()
        private val showConfirmLogoutPrompt = BehaviorSubject.create<Boolean>()
        private var userName: Observable<String>
        private val userOutput = BehaviorSubject.create<User>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.client.fetchCurrentUser()
                    .retry(2)
                    .compose(Transformers.neverError())
                    .compose(bindToLifecycle())
                    .subscribe { this.currentUser.refresh(it) }

            this.confirmLogoutClicked
                    .compose<Void>(bindToLifecycle<Void>())
                    .subscribe{
                        this.koala.trackLogout()
                        this.logout.onNext(null)
                    }

            this.currentUser.observable()
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe({ this.userOutput.onNext(it) })

            this.avatarPhotoUrl = userOutput.map { user -> user.avatar().thumb() }

            this.userName = userOutput.map { user -> user.name() }
        }

        override fun avatarPhotoUrl(): Observable<String> = this.avatarPhotoUrl

        override fun closeLogoutConfirmationClicked() = this.showConfirmLogoutPrompt.onNext(false)

        override fun confirmLogoutClicked() = this.confirmLogoutClicked.onNext(null)

        override fun logoutClicked() = this.showConfirmLogoutPrompt.onNext(true)

        override fun logout(): Observable<Void> = this.logout

        override fun showConfirmLogoutPrompt(): Observable<Boolean> = this.showConfirmLogoutPrompt

        override fun userName(): Observable<String> = this.userName
    }
}
