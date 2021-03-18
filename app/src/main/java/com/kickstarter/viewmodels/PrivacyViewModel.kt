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
import com.kickstarter.ui.activities.PrivacyActivity
import rx.Notification
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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
        fun hideConfirmFollowingOptOutPrompt(): Observable<Void>

        /** Emits when the user is a creator and we need to hide the private profile row. */
        fun hidePrivateProfileRow(): Observable<Boolean>

        /** Emits when user should be shown the Following confirmation dialog.  */
        fun showConfirmFollowingOptOutPrompt(): Observable<Void>

        /** Emits user containing settings state.  */
        fun user(): Observable<User>
    }

    interface Errors {
        /** Emits when saving preference fails.  */
        fun unableToSavePreferenceError(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<PrivacyActivity>(environment), Inputs, Outputs, Errors {
        private val optIntoFollowing = PublishSubject.create<Boolean>()
        private val optOutOfFollowing = PublishSubject.create<Boolean>()
        private val userInput = PublishSubject.create<User>()

        private val hideConfirmFollowingOptOutPrompt = BehaviorSubject.create<Void>()
        private var hidePrivateProfileRow = BehaviorSubject.create<Boolean>()
        private val showConfirmFollowingOptOutPrompt = BehaviorSubject.create<Void>()
        private val userOutput = BehaviorSubject.create<User>()
        private val updateSuccess = PublishSubject.create<Void>()

        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()

        val inputs: PrivacyViewModel.Inputs = this
        val outputs: PrivacyViewModel.Outputs = this
        val errors: PrivacyViewModel.Errors = this

        private val client: ApiClientType = environment.apiClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        init {
            this.client.fetchCurrentUser()
                .retry(2)
                .compose(Transformers.neverError())
                .compose(bindToLifecycle())
                .subscribe { this.currentUser.refresh(it) }

            val currentUser = this.currentUser.observable()

            currentUser
                .take(1)
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe { this.userOutput.onNext(it) }

            currentUser
                .compose(bindToLifecycle())
                .filter(ObjectUtils::isNotNull)
                .map { user -> IntegerUtils.isNonZero(user.createdProjectsCount()) }
                .subscribe(this.hidePrivateProfileRow)

            val updateSettingsNotification = this.userInput
                .concatMap { this.updateSettings(it) }

            updateSettingsNotification
                .compose(values())
                .compose(bindToLifecycle())
                .subscribe { this.success(it) }

            updateSettingsNotification
                .compose(errors())
                .compose(bindToLifecycle())
                .subscribe(this.unableToSavePreferenceError)

            this.userInput
                .compose(bindToLifecycle())
                .subscribe(this.userOutput)

            this.userOutput
                .window(2, 1)
                .flatMap<List<User>> { it.toList() }
                .map<User> { ListUtils.first(it) }
                .compose<User>(Transformers.takeWhen<User, Throwable>(this.unableToSavePreferenceError))
                .compose(bindToLifecycle())
                .subscribe(this.userOutput)

            this.optIntoFollowing
                .compose<Boolean>(bindToLifecycle<Boolean>())
                .filter { checked -> checked }
                .subscribe { _ -> this.userInput.onNext(this.userOutput.value.toBuilder().social(true).build()) }

            this.optIntoFollowing
                .compose<Boolean>(bindToLifecycle<Boolean>())
                .filter { checked -> !checked }
                .subscribe { _ -> this.showConfirmFollowingOptOutPrompt.onNext(null) }

            this.optOutOfFollowing
                .compose<Boolean>(bindToLifecycle<Boolean>())
                .filter { optOut -> optOut }
                .subscribe { _ -> this.userInput.onNext(this.userOutput.value.toBuilder().social(false).build()) }

            this.optOutOfFollowing
                .compose<Boolean>(bindToLifecycle<Boolean>())
                .filter { optOut -> !optOut }
                .subscribe { _ -> this.hideConfirmFollowingOptOutPrompt.onNext(null) }
        }

        override fun optIntoFollowing(checked: Boolean) {
            this.optIntoFollowing.onNext(checked)
        }

        override fun optOutOfFollowing(optOut: Boolean) {
            this.optOutOfFollowing.onNext(optOut)
        }

        override fun optedOutOfRecommendations(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().optedOutOfRecommendations(!checked).build())
        }

        override fun showPublicProfile(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().showPublicProfile(!checked).build())
        }

        override fun hideConfirmFollowingOptOutPrompt(): Observable<Void> = this.hideConfirmFollowingOptOutPrompt

        override fun hidePrivateProfileRow(): Observable<Boolean> = this.hidePrivateProfileRow

        override fun showConfirmFollowingOptOutPrompt(): Observable<Void> = this.showConfirmFollowingOptOutPrompt

        override fun user(): Observable<User> = this.userOutput

        override fun unableToSavePreferenceError(): Observable<String> = this.unableToSavePreferenceError
            .takeUntil(this.updateSuccess)
            .map { _ -> null }

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(null)
        }

        private fun updateSettings(user: User): Observable<Notification<User>> {
            return this.client.updateUserSettings(user)
                .materialize()
                .share()
        }
    }
}
