package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.errors
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.StringUtils
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.activities.ResetPasswordActivity
import rx.Notification
import rx.Observable
import rx.subjects.PublishSubject

interface ResetPasswordViewModel {

    interface Inputs {
        /** Call when the email field changes. */
        fun email(emailInput: String)

        /** Call when the reset password button is clicked. */
        fun resetPasswordClick()
    }

    interface Outputs {
        /** Emits a boolean that determines if the form is in the progress of being submitted. */
        fun isFormSubmitting(): Observable<Boolean>

        /** Emits a boolean that determines if the form validation is passing. */
        fun isFormValid(): Observable<Boolean>

        /** Emits when password reset is completed successfully. */
        fun resetSuccess(): Observable<Void>

        /** Emits when password reset fails. */
        fun resetError(): Observable<String>
    }

    class ViewModel(val environment: Environment) : ActivityViewModel<ResetPasswordActivity>(environment), Inputs, Outputs {
        private val client: ApiClientType = environment.apiClient()

        private val email = PublishSubject.create<String>()
        private val resetPasswordClick = PublishSubject.create<Void>()

        private val isFormSubmitting = PublishSubject.create<Boolean>()
        private val isFormValid = PublishSubject.create<Boolean>()
        private val resetSuccess = PublishSubject.create<Void>()
        private val resetError = PublishSubject.create<ErrorEnvelope>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.email
                    .map(StringUtils::isEmail)
                    .compose(bindToLifecycle())
                    .subscribe(this.isFormValid)

            val resetPasswordNotification = this.email
                    .compose<String>(Transformers.takeWhen(this.resetPasswordClick))
                    .switchMap(this::submitEmail)

            resetPasswordNotification
                    .compose(values())
                    .compose(bindToLifecycle())
                    .subscribe { success() }

            resetPasswordNotification
                    .compose(errors())
                    .map { ErrorEnvelope.fromThrowable(it) }
                    .filter { ObjectUtils.isNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.resetError)

            this.resetError
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackResetPasswordError() }

            this.resetSuccess
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackResetPasswordSuccess() }

            this.koala.trackResetPasswordFormView()
            this.lake.trackForgotPasswordPageViewed()
        }

        private fun success() {
            this.resetSuccess.onNext(null)
        }

        private fun submitEmail(email: String): Observable<Notification<User>> {
            return this.client.resetPassword(email)
                    .doOnSubscribe { this.isFormSubmitting.onNext(true) }
                    .doAfterTerminate { this.isFormSubmitting.onNext(false) }
                    .materialize()
                    .share()
        }

        override fun email(emailInput: String) {
            this.email.onNext(emailInput)
        }

        override fun resetPasswordClick() {
            this.resetPasswordClick.onNext(null)
        }

        override fun isFormSubmitting(): Observable<Boolean> {
            return this.isFormSubmitting
        }

        override fun isFormValid(): Observable<Boolean> {
            return this.isFormValid
        }

        override fun resetSuccess(): Observable<Void> {
            return this.resetSuccess
        }

        override fun resetError(): Observable<String> {
            return this.resetError
                    .takeUntil(this.resetSuccess)
                    .map { it.errorMessage() }
        }
    }
}
