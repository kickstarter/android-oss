package com.kickstarter.viewmodels

import UpdateUserPasswordMutation
import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.ChangePasswordActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ChangePasswordViewModel {


    interface Inputs {
        /** Call when the user clicks the change password button. */
        fun changePasswordClicked()

        /** Call when the current password field changes.  */
        fun confirmPassword(confirmPassword: String)

        /** Call when the current password field changes.  */
        fun currentPassword(currentPassword: String)

        /** Call when the new password field changes.  */
        fun newPassword(newPassword: String)
    }

    interface Outputs {
        /** Emits when the password update was unsuccessful. */
        fun error(): Observable<String>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the save button should be enabled. */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the password update was unsuccessful. */
        fun success(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ChangePasswordActivity>(environment), Inputs, Outputs {

        private val changePasswordClicked = PublishSubject.create<Void>()
        private val confirmPassword = PublishSubject.create<String>()
        private val currentPassword = PublishSubject.create<String>()
        private val newPassword = PublishSubject.create<String>()

        private val error = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<Void>()

        val inputs: ChangePasswordViewModel.Inputs = this
        val outputs: ChangePasswordViewModel.Outputs = this

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {

            val changePassword = Observable.combineLatest(currentPassword, 
                    confirmPassword,
                    newPassword,
                    { current, new, confirm -> ChangePassword(current, new, confirm) })

            changePassword
                    .map { cp -> cp.isValid() }
                    .compose(bindToLifecycle())
                    .subscribe(this.saveButtonIsEnabled)

            val changePasswordNotification = changePassword
                    .compose(takeWhen<ChangePassword, Void>(this.changePasswordClicked))
                    .switchMap { cp -> submit(cp).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            changePasswordNotification
                    .compose(errors())
                    .subscribe({ this.error.onNext(it.localizedMessage) })

            changePasswordNotification
                    .compose(values())
                    .compose(ignoreValues())
                    .subscribe(this.success)

        }

        private fun submit(changePassword: ChangePasswordViewModel.ViewModel.ChangePassword): Observable<UpdateUserPasswordMutation.Data> {
            return this.apolloClient.updateUserPassword(changePassword.currentPassword, changePassword.newPassword, changePassword.confirmPassword)
                    .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

        override fun changePasswordClicked() {
            this.changePasswordClicked.onNext(null)
        }

        override fun confirmPassword(confirmPassword: String) {
            this.confirmPassword.onNext(confirmPassword)
        }

        override fun currentPassword(currentPassword: String) {
            this.currentPassword.onNext(currentPassword)
        }

        override fun newPassword(newPassword: String) {
            this.newPassword.onNext(newPassword)
        }

        override fun error(): Observable<String> {
            return this.error
        }

        override fun progressBarIsVisible(): Observable<Boolean> {
            return this.progressBarIsVisible
        }

        override fun saveButtonIsEnabled(): Observable<Boolean> {
            return this.saveButtonIsEnabled
        }

        override fun success(): Observable<Void> {
            return this.success
        }

        data class ChangePassword(val currentPassword: String, val newPassword: String, val confirmPassword: String) {
            fun isValid(): Boolean {
                return isNotEmptyAndAtLeast6Chars(currentPassword)
                        && isNotEmptyAndAtLeast6Chars(newPassword)
                        && isNotEmptyAndAtLeast6Chars(confirmPassword)
            }

            private fun isNotEmptyAndAtLeast6Chars(string: String) = !string.isEmpty() && string.length >= 6
        }
    }

}
