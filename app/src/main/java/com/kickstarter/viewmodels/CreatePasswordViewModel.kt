package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.CreatePasswordMutation
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.MINIMUM_PASSWORD_LENGTH
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.newPasswordValidationWarnings
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface CreatePasswordViewModel {

    interface Inputs {
        /** Call when the user clicks the submit password button. */
        fun createPasswordClicked()
    }

    interface Outputs {
        /** Emits when the password update was unsuccessful. */
        fun error(): Observable<String>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the password update was successful. */
        fun success(): Observable<String>
    }

    class CreatePasswordViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {

        private val confirmPassword = PublishSubject.create<String>()
        private val newPassword = PublishSubject.create<String>()
        private val submitPasswordClicked = PublishSubject.create<Unit>()

        private val error = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
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

            val createNewPasswordNotification = password
                .compose(takeWhenV2(this.submitPasswordClicked))
                .switchMap { np -> submit(np).materialize() }
                .share()

            createNewPasswordNotification
                .compose(errorsV2())
                .map { it.localizedMessage }
                .filter { it.isNotNull() }
                .map { it }
                .subscribe { this.error.onNext(it) }
                .addToDisposable(disposables)

            createNewPasswordNotification
                .compose(valuesV2())
                .map { it.updateUserAccount?.user?.email }
                .filter { it.isNotNull() }
                .map { it }
                .subscribe {
                    this.success.onNext(it)
                    this.analytics?.reset()
                }.addToDisposable(disposables)
        }

        fun updatePasswordData(newPassword: String) {
            this.newPassword.onNext(newPassword)
            this.confirmPassword.onNext(newPassword)
        }
        fun resetError() {
            this.error.onNext("")
        }

        override fun onCleared() {
            apolloClientV2.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        private fun submit(createPassword: CreatePassword): Observable<CreatePasswordMutation.Data> {
            return this.apolloClientV2.createPassword(createPassword.newPassword, createPassword.confirmPassword)
                .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

        override fun createPasswordClicked() = this.submitPasswordClicked.onNext(Unit)

        override fun error(): Observable<String> = this.error

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

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
