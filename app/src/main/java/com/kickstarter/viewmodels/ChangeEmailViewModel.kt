package com.kickstarter.viewmodels

import SendEmailVerificationMutation
import UpdateUserEmailMutation
import UserPrivacyQuery
import android.support.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.StringUtils
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.ChangeEmailActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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

        /** Emits a boolean to determine if the user is a creator. */
        fun isCreator(): Observable<Boolean>

        /** Emits a boolean to determine if a user's email is deliverable. */
        fun isDeliverable(): Observable<Boolean>

        /** Emits a boolean to display if the user's email is verified. */
        fun isEmailVerified(): Observable<Boolean>

        /** Emits a boolean that determines if update email call to server is executing.  */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits a boolean that determines if the email and password are valid.  */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the user's email is changed successfully. */
        fun success(): Observable<Void>

        fun warningText(): Observable<Int>

        fun warningTextColor(): Observable<Int>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ChangeEmailActivity>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val email = PublishSubject.create<String>()
        private val emailFocus = PublishSubject.create<Boolean>()
        private val password = PublishSubject.create<String>()
        private val sendVerificationEmailClick = PublishSubject.create<Void>()
        private val updateEmailClicked = PublishSubject.create<Void>()

        private val currentEmail = BehaviorSubject.create<String>()
        private val emailErrorIsVisible = BehaviorSubject.create<Boolean>()
        private val isCreator = BehaviorSubject.create<Boolean>()
        private val isDeliverable = BehaviorSubject.create<Boolean>()
        private val isEmailVerified = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val showProgressBar = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<Void>()
        private val warningText = BehaviorSubject.create<Int>()
        private val warningTextColor = BehaviorSubject.create<Int>()

        private val error = BehaviorSubject.create<String>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {

            val userPrivacy = this.apolloClient.userPrivacy()

            userPrivacy
                    .compose(neverError())
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.currentEmail.onNext(it.me()?.email())
                        this.isCreator.onNext(it.me()?.isCreator())
                        this.isDeliverable.onNext(it.me()?.isDeliverable())
                        this.isEmailVerified.onNext(it.me()?.isEmailVerified())
                    }

            userPrivacy
                    .map { getWarningText(it) }
                    .subscribe { this.warningText.onNext(it) }

            userPrivacy
                    .map { getWarningTextColor(it) }
                    .subscribe { this.warningTextColor.onNext(it) }

            this.emailFocus
                    .compose(combineLatestPair<Boolean, String>(this.email))
                    .map { !it.first && it.second.isNotEmpty() && !StringUtils.isEmail(it.second) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe { this.emailErrorIsVisible.onNext(it) }

            val changeEmail = Observable.combineLatest(this.email, this.password
            ) { email, password -> ChangeEmail(email, password) }

            changeEmail
                    .map { ce -> ce.isValid() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe { this.saveButtonIsEnabled.onNext(it) }

            val updateEmailNotification = changeEmail
                    .compose(takeWhen<ChangeEmail, Void>(this.updateEmailClicked))
                    .switchMap { updateEmail(it).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            updateEmailNotification
                    .compose(errors())
                    .subscribe { this.error.onNext(it.localizedMessage) }

            updateEmailNotification
                    .compose(values())
                    .subscribe {
                        this.currentEmail.onNext(it.updateUserAccount()?.user()?.email())
                        this.success.onNext(null)
                    }

            val sendEmailNotification = this.sendVerificationEmailClick
                    .compose(bindToLifecycle())
                    .switchMap { sendEmailVerification().materialize() }
                    .share()

            sendEmailNotification
                    .compose(errors())
                    .subscribe { this.error.onNext(it.localizedMessage) }

            sendEmailNotification
                    .compose(values())
                    .subscribe { this.success.onNext(null) }
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
            this.updateEmailClicked.onNext(null)
        }

        override fun sendVerificationEmail() {
            this.sendVerificationEmailClick.onNext(null)
        }

        override fun currentEmail(): Observable<String> = this.currentEmail

        override fun emailErrorIsVisible(): Observable<Boolean> = this.emailErrorIsVisible

        override fun error(): Observable<String> = this.error

        override fun isCreator(): Observable<Boolean> = this.isCreator

        override fun isDeliverable(): Observable<Boolean> = this.isDeliverable

        override fun isEmailVerified(): Observable<Boolean> = this.isEmailVerified

        override fun progressBarIsVisible(): Observable<Boolean> = this.showProgressBar

        override fun saveButtonIsEnabled(): Observable<Boolean> = this.saveButtonIsEnabled

        override fun success(): Observable<Void> = this.success

        override fun warningText(): Observable<Int> = this.warningText

        override fun warningTextColor(): Observable<Int> = this.warningTextColor

        private fun getWarningTextColor(userPrivacyData: UserPrivacyQuery.Data?): Int? {
            return if (!userPrivacyData?.me()?.isDeliverable!!) {
                R.color.ksr_red_400
            } else {
                R.color.ksr_dark_grey_400
            }
        }

        private fun getWarningText(userPrivacyData: UserPrivacyQuery.Data?): Int? {
            return if (!userPrivacyData?.me()?.isDeliverable!!) {
                R.string.We_ve_been_unable_to_send_email
            } else if (!userPrivacyData.me()?.isEmailVerified!!) {
                R.string.Email_unverified
            } else {
                null
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
                return StringUtils.isEmail(this.email) && StringUtils.isValidPassword(this.password)
            }
        }
    }
}
