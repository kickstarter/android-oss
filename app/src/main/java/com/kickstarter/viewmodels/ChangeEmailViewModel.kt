package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.R
import com.kickstarter.SendEmailVerificationMutation
import com.kickstarter.UpdateUserEmailMutation
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isEmail
import com.kickstarter.libs.utils.extensions.isValidPassword
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ChangeEmailViewModel {

    interface Inputs {
        /** Call when the new email field changes.  */
        fun email(email: String)

        /** Call when the new email field focus changes.  */
        fun emailFocus(hasFocus: Boolean)

        /** Call when the current password field changes.  */
        fun password(password: String)

        /** Call when the send verification button is clicked. */
        fun sendVerificationEmail()

        /** Call when save button has been clicked.  */
        fun updateEmailClicked()
    }

    interface Outputs {
        /** Emits the logged in user's email address.  */
        fun currentEmail(): Observable<String>

        /** Emits a boolean that determines if the email address error should be shown.  */
        fun emailErrorIsVisible(): Observable<Boolean>

        /** Emits a string to display when email update fails.  */
        fun error(): Observable<String>

        /** Emits a boolean to display if the user's email is verified. */
        fun sendVerificationIsHidden(): Observable<Boolean>

        /** Emits a boolean that determines if update email call to server is executing.  */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits a boolean that determines if the email and password are valid.  */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the user's email is changed successfully. */
        fun success(): Observable<Unit>

        /** Emits the text for the verification button depending on whether the user is a backer or creator. */
        fun verificationEmailButtonText(): Observable<Int>

        /** Emits the warning text string depending on is an email is undeliverable or un-verified for creators. */
        fun warningText(): Observable<Int>

        /** Emits the text color for the warning text depending on if the email is undeliverable or unverified. */
        fun warningTextColor(): Observable<Int>
    }

    class ChangeEmailViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val email = PublishSubject.create<String>()
        private val emailFocus = PublishSubject.create<Boolean>()
        private val password = PublishSubject.create<String>()
        private val sendVerificationEmailClick = PublishSubject.create<Unit>()
        private val updateEmailClicked = PublishSubject.create<Unit>()

        private val currentEmail = BehaviorSubject.create<String>()
        private val emailErrorIsVisible = BehaviorSubject.create<Boolean>()
        private val sendVerificationIsHidden = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val showProgressBar = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<Unit>()
        private val warningText = BehaviorSubject.create<Int>()
        private val warningTextColor = BehaviorSubject.create<Int>()
        private val verificationEmailButtonText = BehaviorSubject.create<Int>()

        private val error = BehaviorSubject.create<String>()

        private val apolloClient = requireNotNull(environment.apolloClientV2())

        private val disposables = CompositeDisposable()

        init {
            // TODO: Replace userPrivacy with user from Environment when all fields are populated
            val userPrivacy = this.apolloClient.userPrivacy().compose(neverErrorV2())

            userPrivacy
                .subscribe {
                    it.email?.let { email ->
                        this.currentEmail.onNext(email)
                    }
                    it.isEmailVerified?.let { verified ->
                        this.sendVerificationIsHidden.onNext(verified)
                    }
                }
                .addToDisposable(disposables)

            userPrivacy
                .map {
                    getWarningText(
                        it.isDeliverable,
                        it.isEmailVerified
                    )
                }
                .subscribe {
                    it?.let { stringRes ->
                        this.warningText.onNext(stringRes)
                    }
                }
                .addToDisposable(disposables)

            userPrivacy
                .map { getWarningTextColor(it.isDeliverable) }
                .subscribe {
                    it?.let { colorRes ->
                        this.warningTextColor.onNext(colorRes)
                    }
                }
                .addToDisposable(disposables)

            userPrivacy
                .map { getVerificationText(it.isCreator) }
                .subscribe {
                    it?.let { stringRes ->
                        this.verificationEmailButtonText.onNext(stringRes)
                    }
                }
                .addToDisposable(disposables)

            this.emailFocus
                .compose(combineLatestPair(this.email))
                .map { !it.first && it.second.isNotEmpty() && !it.second.isEmail() }
                .distinctUntilChanged()
                .subscribe { this.emailErrorIsVisible.onNext(it) }
                .addToDisposable(disposables)

            val changeEmail = Observable.combineLatest(
                this.email,
                this.password
            ) { email, password -> ChangeEmail(email, password) }

            changeEmail
                .map { ce -> ce.isValid() }
                .distinctUntilChanged()
                .subscribe { this.saveButtonIsEnabled.onNext(it) }
                .addToDisposable(disposables)

            val updateEmailNotification = changeEmail
                .compose(takeWhenV2(this.updateEmailClicked))
                .switchMap { updateEmail(it).materialize() }
                .share()

            updateEmailNotification
                .compose(errorsV2())
                .subscribe {
                    it?.localizedMessage?.let { message ->
                        this.error.onNext(message)
                    }
                }
                .addToDisposable(disposables)

            updateEmailNotification
                .compose(valuesV2())
                .subscribe {
                    it.updateUserAccount?.user?.email?.let { email ->
                        this.currentEmail.onNext(email)
                    }
                    this.success.onNext(Unit)
                }
                .addToDisposable(disposables)

            val sendEmailNotification = this.sendVerificationEmailClick
                .switchMap { sendEmailVerification().materialize() }
                .share()

            sendEmailNotification
                .compose(errorsV2())
                .subscribe {
                    it?.localizedMessage?.let { message ->
                        this.error.onNext(message)
                    }
                }
                .addToDisposable(disposables)

            sendEmailNotification
                .compose(valuesV2())
                .subscribe {
                    this.success.onNext(Unit)
                }
                .addToDisposable(disposables)
        }

        override fun email(email: String) {
            this.email.onNext(email)
        }

        override fun emailFocus(hasFocus: Boolean) {
            this.emailFocus.onNext(hasFocus)
        }

        override fun password(password: String) {
            this.password.onNext(password)
        }

        override fun updateEmailClicked() {
            this.updateEmailClicked.onNext(Unit)
        }

        override fun sendVerificationEmail() {
            this.sendVerificationEmailClick.onNext(Unit)
        }

        override fun currentEmail(): Observable<String> = this.currentEmail

        override fun emailErrorIsVisible(): Observable<Boolean> = this.emailErrorIsVisible

        override fun error(): Observable<String> = this.error

        override fun sendVerificationIsHidden(): Observable<Boolean> = this.sendVerificationIsHidden

        override fun progressBarIsVisible(): Observable<Boolean> = this.showProgressBar

        override fun saveButtonIsEnabled(): Observable<Boolean> = this.saveButtonIsEnabled

        override fun success(): Observable<Unit> = this.success

        override fun warningText(): Observable<Int> = this.warningText

        override fun warningTextColor(): Observable<Int> = this.warningTextColor

        override fun verificationEmailButtonText(): Observable<Int> =
            this.verificationEmailButtonText

        private fun getWarningTextColor(isDeliverable: Boolean?): Int {
            return if (isDeliverable?.not() == true) {
                R.color.kds_alert
            } else {
                R.color.kds_support_400
            }
        }

        private fun getWarningText(isDeliverable: Boolean?, isVerified: Boolean?): Int {
            return if (isDeliverable?.not() == true) {
                R.string.We_ve_been_unable_to_send_email
            } else if (isVerified?.not() == true) {
                R.string.Email_unverified
            } else {
                0
            }
        }

        private fun getVerificationText(isCreator: Boolean?): Int {
            return if (isCreator?.not() == true) {
                R.string.Send_verfication_email
            } else {
                R.string.Resend_verification_email
            }
        }

        private fun sendEmailVerification(): Observable<SendEmailVerificationMutation.Data> {
            return this.apolloClient.sendVerificationEmail()
                .doOnSubscribe { this.showProgressBar.onNext(true) }
                .doAfterTerminate { this.showProgressBar.onNext(false) }
        }

        private fun updateEmail(changeEmail: ChangeEmail): Observable<UpdateUserEmailMutation.Data> {
            return this.apolloClient.updateUserEmail(changeEmail.email, changeEmail.password)
                .doOnSubscribe { this.showProgressBar.onNext(true) }
                .doAfterTerminate { this.showProgressBar.onNext(false) }
        }

        data class ChangeEmail(val email: String, val password: String) {
            fun isValid(): Boolean {
                return this.email.isEmail() && this.password.isValidPassword()
            }
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChangeEmailViewModel(environment) as T
        }
    }
}
