package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.isNotEmptyAndAtLeast6Chars
import com.kickstarter.libs.utils.extensions.maskEmail
import com.kickstarter.libs.utils.extensions.newPasswordValidationWarnings
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.SetPasswordActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface SetPasswordViewModel {

    interface Inputs {
        /** Call when the user clicks the change password button. */
        fun changePasswordClicked()

        /** Call when the current password field changes.  */
        fun confirmPassword(confirmPassword: String)

        /** Call when the new password field changes.  */
        fun newPassword(newPassword: String)
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

        /** Emits a boolean that determines if the form is in the progress of being submitted. */
        fun isFormSubmitting(): Observable<Boolean>

        /** Emits when with user email to set password */
        fun setUserEmail(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<SetPasswordActivity>(environment), Inputs, Outputs {

        private val changePasswordClicked = PublishSubject.create<Void>()
        private val confirmPassword = PublishSubject.create<String>()
        private val newPassword = PublishSubject.create<String>()
        private val isFormSubmitting = PublishSubject.create<Boolean>()

        private val error = BehaviorSubject.create<String>()
        private val passwordWarning = BehaviorSubject.create<Int>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()
        private val setUserEmail = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient = requireNotNull(this.environment.apolloClient())
        private val currentUser: CurrentUserType = requireNotNull(environment.currentUser())

        init {
            intent()
                .filter { it.hasExtra(IntentKey.EMAIL) }
                .map {
                    it.getStringExtra(IntentKey.EMAIL)
                }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .map { it.maskEmail() }
                .compose(bindToLifecycle())
                .subscribe {
                    this.setUserEmail.onNext(it)
                }

            val setNewPassword = Observable.combineLatest(
                this.newPassword.startWith(""),
                this.confirmPassword.startWith("")
            ) { new, confirm -> SetNewPassword(new, confirm) }

            setNewPassword
                .map { it.warning() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.passwordWarning)

            setNewPassword
                .map { it.isValid() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.saveButtonIsEnabled)

            val setNewPasswordNotification = setNewPassword
                .compose(Transformers.takeWhen(this.changePasswordClicked))
                .switchMap { cp -> submit(cp).materialize() }
                .compose(bindToLifecycle())
                .share()

            setNewPasswordNotification
                .compose(Transformers.errors())
                .map { ErrorEnvelope.fromThrowable(it) }
                .map { it?.errorMessage() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    this.error.onNext(it)
                }

            val userHasPassword = setNewPasswordNotification
                .compose(Transformers.values())
                .filter { it.updateUserAccount()?.user()?.hasPassword() }

            this.currentUser.loggedInUser()
                .compose(Transformers.takePairWhen(userHasPassword))
                .distinctUntilChanged()
                .subscribe {
                    currentUser.accessToken?.let { it1 ->
                        currentUser.login(
                            it.first.toBuilder().needsPassword(false).build(),
                            it1
                        )
                    }
                    this.success.onNext(it.second.updateUserAccount()?.user()?.email())
                }
        }

        private fun submit(setNewPassword: SetNewPassword): Observable<UpdateUserPasswordMutation.Data> {
            return this.apolloClient.setUserPassword(setNewPassword.newPassword, setNewPassword.confirmPassword)
                .doOnSubscribe {
                    this.progressBarIsVisible.onNext(true)
                    this.isFormSubmitting.onNext(true)
                }
                .doAfterTerminate {
                    this.progressBarIsVisible.onNext(false)
                    this.isFormSubmitting.onNext(false)
                }
        }

        override fun changePasswordClicked() {
            this.changePasswordClicked.onNext(null)
        }

        override fun confirmPassword(confirmPassword: String) {
            this.confirmPassword.onNext(confirmPassword)
        }

        override fun newPassword(newPassword: String) {
            this.newPassword.onNext(newPassword)
        }

        override fun error(): Observable<String> = this.error
        override fun passwordWarning(): Observable<Int> = this.passwordWarning
        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible
        override fun saveButtonIsEnabled(): Observable<Boolean> = this.saveButtonIsEnabled
        override fun isFormSubmitting(): Observable<Boolean> = this.isFormSubmitting
        override fun success(): Observable<String> = this.success
        override fun setUserEmail(): Observable<String> = this.setUserEmail

        data class SetNewPassword(val newPassword: String, val confirmPassword: String) {
            fun isValid(): Boolean {
                return this.newPassword.isNotEmptyAndAtLeast6Chars() &&
                    this.confirmPassword.isNotEmptyAndAtLeast6Chars() &&
                    this.confirmPassword == this.newPassword
            }

            fun warning(): Int? =
                newPassword.newPasswordValidationWarnings(confirmPassword)
        }
    }
}
