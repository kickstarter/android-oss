package com.kickstarter.viewmodels

import UpdateUserPasswordMutation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.newPasswordValidationWarnings
import com.kickstarter.libs.utils.extensions.validPassword
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

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

        /** Emits when the password update was successful. */
        fun success(): Observable<String>
    }

    class ChangePasswordViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {

        private val changePasswordClicked = PublishSubject.create<Unit>()
        private val confirmPassword = PublishSubject.create<String>()
        private val currentPassword = PublishSubject.create<String>()
        private val newPassword = PublishSubject.create<String>()

        private val error = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient = requireNotNull(this.environment.apolloClientV2())
        private val analytics = this.environment.analytics()

        private val disposables = CompositeDisposable()

        init {

            val changePassword = Observable.combineLatest(
                this.currentPassword.startWith(""),
                this.newPassword.startWith(""),
                this.confirmPassword.startWith("")
            ) { current, new, confirm -> ChangePassword(current, new, confirm) }

            val changePasswordNotification = changePassword
                .compose(takeWhenV2(this.changePasswordClicked))
                .switchMap { cp -> submit(cp).materialize() }
                .share()

            changePasswordNotification
                .compose(errorsV2())
                .subscribe { error ->
                    error?.localizedMessage?.let { message ->
                        this.error.onNext(message)
                    }
                }
                .addToDisposable(disposables)

            changePasswordNotification
                .compose(valuesV2())
                .map { it.updateUserAccount()?.user()?.email() }
                .subscribe { email ->
                    this.analytics?.reset()
                    email?.let {
                        this.success.onNext(it)
                    }
                }
                .addToDisposable(disposables)
        }

        private fun submit(changePassword: ChangePassword): Observable<UpdateUserPasswordMutation.Data> {
            return this.apolloClient.updateUserPassword(changePassword.currentPassword, changePassword.newPassword, changePassword.confirmPassword)
                .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

        fun updatePasswordData(oldPassword: String, newPassword: String) {
            this.currentPassword.onNext(oldPassword)
            this.newPassword.onNext(newPassword)
            this.confirmPassword.onNext(newPassword)
        }

        fun resetError() {
            this.error.onNext("")
        }

        override fun changePasswordClicked() {
            this.changePasswordClicked.onNext(Unit)
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

        override fun success(): Observable<String> {
            return this.success
        }

        data class ChangePassword(val currentPassword: String, val newPassword: String, val confirmPassword: String) {
            fun isValid(): Boolean {
                return this.currentPassword.validPassword() &&
                    this.newPassword.validPassword() &&
                    this.confirmPassword.validPassword() &&
                    this.confirmPassword == this.newPassword
            }

            fun warning(): Int =
                newPassword.newPasswordValidationWarnings(confirmPassword) ?: 0
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChangePasswordViewModel(environment) as T
        }
    }
}
