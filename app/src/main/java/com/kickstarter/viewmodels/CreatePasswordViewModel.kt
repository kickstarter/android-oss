package com.kickstarter.viewmodels

import CreatePasswordMutation
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.extensions.MINIMUM_PASSWORD_LENGTH
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.CreatePasswordActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject


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

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<CreatePasswordActivity>(environment), Inputs, Outputs {

        private val confirmPassword = PublishSubject.create<String>()
        private val newPassword = PublishSubject.create<String>()
        private val submitPasswordClicked = PublishSubject.create<Void>()

        private val error = BehaviorSubject.create<String>()
        private val passwordWarning = BehaviorSubject.create<Int>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient: ApolloClientType = this.environment.apolloClient()
        private val analytics: AnalyticEvents = this.environment.analytics()

        init {

            val password = Observable.combineLatest(this.newPassword.startWith(""),
                    this.confirmPassword.startWith("")) { new, confirm -> CreatePassword(new, confirm)}

            password
                    .map { it.warning() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.passwordWarning)

            password
                    .map { it.isValid() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.saveButtonIsEnabled)

            val createNewPasswordNotification = password
                    .compose(takeWhen<CreatePassword, Void>(this.submitPasswordClicked))
                    .switchMap { np -> submit(np).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            createNewPasswordNotification
                    .compose(errors())
                    .subscribe{this.error.onNext(it.localizedMessage)}

            createNewPasswordNotification
                    .compose(values())
                    .map { it.updateUserAccount()?.user()?.email() }
                    .subscribe {
                        this.success.onNext(it)
                        this.analytics.reset()
                    }
        }

        private fun submit(createPassword: CreatePasswordViewModel.ViewModel.CreatePassword): Observable<CreatePasswordMutation.Data> {
            return this.apolloClient.createPassword(createPassword.newPassword, createPassword.confirmPassword)
                    .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

        override fun confirmPassword(confirmPassword: String) = this.confirmPassword.onNext(confirmPassword)

        override fun newPassword(newPassword: String) = this.newPassword.onNext(newPassword)

        override fun createPasswordClicked() = this.submitPasswordClicked.onNext(null)

        override fun error(): Observable<String> = this.error

        override fun passwordWarning(): Observable<Int> = this.passwordWarning

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun saveButtonIsEnabled(): Observable<Boolean> = this.saveButtonIsEnabled

        override fun success(): Observable<String> = this.success

        data class CreatePassword(val newPassword: String, val confirmPassword: String) {
            fun isValid(): Boolean {
                return  isNotEmptyAndAtLeast6Chars(this.newPassword)
                        && isNotEmptyAndAtLeast6Chars(this.confirmPassword)
                        && this.confirmPassword == this.newPassword
            }

            fun warning(): Int? {
                return if (newPassword.isNotEmpty() && newPassword.length in 1 until MINIMUM_PASSWORD_LENGTH)
                    R.string.Password_min_length_message
                else if (confirmPassword.isNotEmpty() && confirmPassword != newPassword)
                    R.string.Passwords_matching_message
                else null
            }

            private fun isNotEmptyAndAtLeast6Chars(password: String) = !password.isEmpty() && password.length >= MINIMUM_PASSWORD_LENGTH
        }
    }
}
