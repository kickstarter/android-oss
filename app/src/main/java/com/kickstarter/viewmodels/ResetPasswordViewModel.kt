package com.kickstarter.viewmodels

import android.util.Log
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.isEmail
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ResetPasswordActivity
import rx.Notification
import rx.Observable
import rx.subjects.BehaviorSubject
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
        fun resetError(): Observable<Boolean>

        /** Fill the view's email address when it's supplied from the intent.  */
        fun prefillEmail(): Observable<String>
    }

    class ViewModel(val environment: Environment) : ActivityViewModel<ResetPasswordActivity>(environment), Inputs, Outputs {
        private val client: ApiClientType = environment.apiClient()

        private val email = PublishSubject.create<String>()
        private val resetPasswordClick = PublishSubject.create<Void>()

        private val isFormSubmitting = PublishSubject.create<Boolean>()
        private val isFormValid = PublishSubject.create<Boolean>()
        private val resetSuccess = PublishSubject.create<Void>()
        private val resetError = PublishSubject.create<Boolean>()
        private val prefillEmail = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            intent()
                    .filter { it.hasExtra(IntentKey.EMAIL) }
                    .map {
                        it.getStringExtra(IntentKey.EMAIL)
                    }
                    .compose(bindToLifecycle())
                    .subscribe(this.prefillEmail)

            this.email
                    .map { it.isEmail() }
                    .compose(bindToLifecycle())
                    .subscribe(this.isFormValid)

            val resetPasswordNotification = this.email
                    .compose<String>(takeWhen(this.resetPasswordClick))
                    .switchMap(this::submitEmail)
                    .share()


            resetPasswordNotification
                    .compose(values())
                    .compose(bindToLifecycle())
                    .subscribe { success() }

            resetPasswordNotification
                    .compose(errors())
                    .map {
                        // host -> internet related error or otherwise -> invalid email address
                        return@map it?.message!!.contains("host")
                    }
                    //.filter { ObjectUtils.isNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.resetError)

            this.lake.trackForgotPasswordPageViewed()
        }

        private fun unwrapNotificationEnvelopeError(notification: Notification<AccessTokenEnvelope>) =
                if (notification.hasThrowable()) notification.throwable else null

        private fun unwrapNotificationEnvelopeSuccess(notification: Notification<AccessTokenEnvelope>) =
                if (notification.hasValue()) notification.value else null

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

        override fun resetError(): Observable<Boolean> {
            return this.resetError
        }

        override fun prefillEmail(): BehaviorSubject<String> = this.prefillEmail
    }
}
