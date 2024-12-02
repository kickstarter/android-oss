package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.UpdateUserPasswordMutation
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.maskEmail
import com.kickstarter.libs.utils.extensions.newPasswordValidationWarnings
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.usecases.LoginUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface SetPasswordViewModel {

    interface Inputs {
        fun configureWith(intent: Intent)

        /** Call when the user clicks the change password button. */
        fun savePasswordClicked()

        /** Call when the current password field changes.  */
        fun confirmPassword(confirmPassword: String)

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

        /** Emits a boolean that determines if the form is in the progress of being submitted. */
        fun isFormSubmitting(): Observable<Boolean>

        /** Emits when with user email to set password */
        fun setUserEmail(): Observable<String>
    }

    class SetPasswordViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {

        private val savePasswordClicked = PublishSubject.create<Unit>()
        private val confirmPassword = PublishSubject.create<String>()
        private val newPassword = PublishSubject.create<String>()
        private val isFormSubmitting = PublishSubject.create<Boolean>()
        private val disposables = CompositeDisposable()

        private val intent = BehaviorSubject.create<Intent>()
        private val error = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()
        private val setUserEmail = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClientV2 = requireNotNull(this.environment.apolloClientV2())
        private val currentUserV2 = requireNotNull(environment.currentUserV2())
        private val loginUserCase = LoginUseCase(environment)
        init {
            intent
                .filter { it.hasExtra(IntentKey.EMAIL) }
                .map {
                    it.getStringExtra(IntentKey.EMAIL)
                }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .map { it.maskEmail() }
                .subscribe {
                    this.setUserEmail.onNext(it)
                }.addToDisposable(disposables)

            val setNewPassword = Observable.combineLatest(
                this.newPassword.startWith(""),
                this.confirmPassword.startWith("")
            ) { new, confirm -> SetNewPassword(new, confirm) }

            val setNewPasswordNotification = setNewPassword
                .compose(takeWhenV2(this.savePasswordClicked))
                .switchMap { cp -> submit(cp).materialize() }
                .share()

            val apiError = setNewPasswordNotification
                .compose(Transformers.errorsV2())
                .filter { it.localizedMessage.isNotNull() }
                .map {
                    requireNotNull(it.localizedMessage)
                }

            val error = setNewPasswordNotification
                .compose(Transformers.errorsV2())
                .map { ErrorEnvelope.fromThrowable(it) }
                .filter { it.errorMessage().isNotNull() }
                .map {
                    requireNotNull(it.errorMessage())
                    requireNotNull(it.errorMessage())
                }

            Observable.merge(apiError, error)
                .distinctUntilChanged()
                .subscribe { this.error.onNext(it) }
                .addToDisposable(disposables)

            val userHasPassword = setNewPasswordNotification
                .compose(Transformers.valuesV2())
                .filter { it.updateUserAccount?.user?.hasPassword ?: false }

            this.currentUserV2.loggedInUser()
                .compose(Transformers.takePairWhenV2(userHasPassword))
                .distinctUntilChanged()
                .subscribe {
                    currentUserV2.accessToken?.let { accessToken ->
                        loginUserCase.setToken(accessToken)
                        loginUserCase.setUser(
                            it.first.toBuilder().needsPassword(false).build()
                        )
                    }
                    this.success.onNext(it.second.updateUserAccount?.user?.email ?: "")
                }.addToDisposable(disposables)
        }

        override fun onCleared() {
            apolloClientV2.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        private fun submit(setNewPassword: SetNewPassword): Observable<UpdateUserPasswordMutation.Data> {
            return this.apolloClientV2.updateUserPassword("", setNewPassword.newPassword, setNewPassword.confirmPassword)
                .doOnSubscribe {
                    this.progressBarIsVisible.onNext(true)
                    this.isFormSubmitting.onNext(true)
                }
                .doAfterTerminate {
                    this.progressBarIsVisible.onNext(false)
                    this.isFormSubmitting.onNext(false)
                }
        }

        fun resetError() {
            this.error.onNext("")
        }

        // - Inputs
        override fun configureWith(intent: Intent) = this.intent.onNext(intent)

        override fun savePasswordClicked() {
            this.savePasswordClicked.onNext(Unit)
        }

        override fun confirmPassword(confirmPassword: String) {
            this.confirmPassword.onNext(confirmPassword)
        }

        override fun newPassword(newPassword: String) {
            this.newPassword.onNext(newPassword)
        }

        // - Outputs
        override fun error(): Observable<String> = this.error
        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible
        override fun isFormSubmitting(): Observable<Boolean> = this.isFormSubmitting
        override fun success(): Observable<String> = this.success
        override fun setUserEmail(): Observable<String> = this.setUserEmail

        data class SetNewPassword(val newPassword: String, val confirmPassword: String) {

            fun warning(): Int =
                newPassword.newPasswordValidationWarnings(confirmPassword) ?: 0
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SetPasswordViewModel(environment) as T
        }
    }
}
