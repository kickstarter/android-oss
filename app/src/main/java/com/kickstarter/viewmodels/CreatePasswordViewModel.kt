package com.kickstarter.viewmodels

import CreatePasswordMutation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.MINIMUM_PASSWORD_LENGTH
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.newPasswordValidationWarnings
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface CreatePasswordViewModel {

    interface Inputs {

        /** Call when the confirm password field changes.  */
        fun confirmPassword(confirmPassword: String)

        /** Call when the new password field changes.  */
        fun newPassword(newPassword: String)

        /** Call when the user clicks the submit password button. */
        fun createPasswordClicked()
    }

    interface Outputs {
        /** Emits when the password update was unsuccessful. */
        fun error(): Observable<String>

        /** Emits a string resource to display when the user's new password entries are invalid. */
        fun passwordWarning(): Observable<Int>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the save button should be enabled. */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the password update was successful. */
        fun success(): Observable<String>
    }

    class CreatePasswordViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {

        private val confirmPassword = PublishSubject.create<String>()
        private val newPassword = PublishSubject.create<String>()
        private val submitPasswordClicked = PublishSubject.create<Unit>()

        private val error = BehaviorSubject.create<String>()
        private val passwordWarning = BehaviorSubject.create<Int>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClientV2 = requireNotNull(this.environment.apolloClientV2())
        private val analytics = requireNotNull(this.environment.analytics())

        private val disposables = CompositeDisposable()

        init {

            val password = Observable.combineLatest(
                this.newPassword.startWith(""),
                this.confirmPassword.startWith("")
            ) { new, confirm -> CreatePassword(new, confirm) }

            password
                .map { it.warning() }
                .distinctUntilChanged()
                .subscribe { this.passwordWarning.onNext(it) }
                .addToDisposable(disposables)

            password
                .map { it.isValid() }
                .distinctUntilChanged()
                .subscribe { this.saveButtonIsEnabled.onNext(it) }
                .addToDisposable(disposables)

            val createNewPasswordNotification = password
                .compose(takeWhenV2(this.submitPasswordClicked))
                .switchMap { np -> submit(np).materialize() }
                .share()

            createNewPasswordNotification
                .compose(errorsV2())
                .map { it.localizedMessage }
                .filter { ObjectUtils.isNotNull(it) }
                .map { ObjectUtils.requireNonNull(it) }
                .subscribe { this.error.onNext(it) }
                .addToDisposable(disposables)

            createNewPasswordNotification
                .compose(valuesV2())
                .map { it.updateUserAccount()?.user()?.email() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { ObjectUtils.requireNonNull(it) }
                .subscribe {
                    this.success.onNext(it)
                    this.analytics?.reset()
                }.addToDisposable(disposables)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        private fun submit(createPassword: CreatePassword): Observable<CreatePasswordMutation.Data> {
            return this.apolloClientV2.createPassword(createPassword.newPassword, createPassword.confirmPassword)
                .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

        override fun confirmPassword(confirmPassword: String) = this.confirmPassword.onNext(confirmPassword)

        override fun newPassword(newPassword: String) = this.newPassword.onNext(newPassword)

        override fun createPasswordClicked() = this.submitPasswordClicked.onNext(Unit)

        override fun error(): Observable<String> = this.error

        override fun passwordWarning(): Observable<Int> = this.passwordWarning

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun saveButtonIsEnabled(): Observable<Boolean> = this.saveButtonIsEnabled

        override fun success(): Observable<String> = this.success

        data class CreatePassword(val newPassword: String, val confirmPassword: String) {
            fun isValid(): Boolean {
                return isNotEmptyAndAtLeast6Chars(this.newPassword) &&
                    isNotEmptyAndAtLeast6Chars(this.confirmPassword) &&
                    this.confirmPassword == this.newPassword
            }

            fun warning(): Int =
                newPassword.newPasswordValidationWarnings(confirmPassword) ?: 0

            private fun isNotEmptyAndAtLeast6Chars(password: String) = !password.isEmpty() && password.length >= MINIMUM_PASSWORD_LENGTH
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreatePasswordViewModel(environment) as T
        }
    }
}
