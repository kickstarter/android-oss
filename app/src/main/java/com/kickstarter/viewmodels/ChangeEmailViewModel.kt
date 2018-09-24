package com.kickstarter.viewmodels

import UpdateUserEmailMutation
import UserPrivacyQuery
import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.neverError
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.ChangeEmailActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ChangeEmailViewModel {

    interface Inputs {
        /** Call when the make network call button has been clicked.  */
        fun makeNetworkCallClicked()

        /** Call when the make network call with errors button has been clicked.  */
        fun makeNetworkCallWithErrorsClicked()

        /** Call when update button has been clicked.  */
        fun updateEmailClicked(newEmail: String, currentPassword: String)
    }

    interface Outputs {
        /** Emits the logged in user's email address.  */
        fun email(): Observable<String>

        /** Emits a boolean that determines if a network call is in progress.  */
        fun showProgressBar(): Observable<Boolean>

        /** Emits the logged in user's email which we use get the success of the mutation. */
        fun success(): Observable<String>
    }

    interface Errors {
        /** Emits a string to display when user could not be found.  */
        fun error(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ChangeEmailActivity>(environment), Inputs, Outputs, Errors {

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        private val makeNetworkCallClicked = PublishSubject.create<Void>()
        private val makeNetworkCallWithErrorsClicked = PublishSubject.create<Void>()
        private val updateEmail = PublishSubject.create<Pair<String, String>>()

        private val email = BehaviorSubject.create<String>()
        private val showProgressBar = BehaviorSubject.create<Boolean>()

        private val error = BehaviorSubject.create<String>()
        private val success = BehaviorSubject.create<String>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {

            this.apolloClient.userPrivacy()
                    .compose(bindToLifecycle())
                    .subscribe {
                        val email = it.me()?.email()
                        this@ViewModel.email.onNext(email)
                    }

            this.makeNetworkCallClicked
                    .flatMap { userPrivacy().compose<UserPrivacyQuery.Data>(neverError()) }
                    .compose(bindToLifecycle())
                    .subscribe({
                        emitData(it)
                    })

            val userPrivacyNotification = this.makeNetworkCallWithErrorsClicked
                    .switchMap { userPrivacy().materialize() }
                    .compose(bindToLifecycle())
                    .share()

            userPrivacyNotification
                    .compose(Transformers.errors())
                    .subscribe({ this.error.onNext(it.localizedMessage) })

            userPrivacyNotification
                    .compose(values())
                    .subscribe({
                        emitData(it)
                    })

            val updateEmailNotification = this.updateEmail
                    .switchMap { updateEmail(it).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            updateEmailNotification
                    .compose(Transformers.errors())
                    .subscribe({ this.error.onNext(it.localizedMessage) })

            updateEmailNotification
                    .compose(values())
                    .subscribe({
                        emitData(it)
                    })
        }

        override fun makeNetworkCallClicked() {
            this.makeNetworkCallClicked.onNext(null)
        }

        override fun makeNetworkCallWithErrorsClicked() {
            this.makeNetworkCallWithErrorsClicked.onNext(null)
        }

        override fun updateEmailClicked(newEmail: String, currentPassword: String) {
            this.updateEmail.onNext(Pair(newEmail, currentPassword))
        }

        override fun email(): Observable<String> = this.email

        override fun showProgressBar(): Observable<Boolean> = this.showProgressBar

        override fun success(): Observable<String> = this.success

        override fun error(): Observable<String> = this.error

        private fun emitData(it: UserPrivacyQuery.Data) {
            this.email.onNext(it.me()?.email())
        }

        private fun emitData(it: UpdateUserEmailMutation.Data) {
            this.email.onNext(it.updateUserAccount()?.user()?.email())
            this.success.onNext(it.updateUserAccount()?.user()?.email())
        }

        private fun updateEmail(emailAndPassword: Pair<String, String>): Observable<UpdateUserEmailMutation.Data> {
            return this.apolloClient.updateUserEmail(emailAndPassword.first, emailAndPassword.second)
                    .doOnSubscribe { this.showProgressBar.onNext(true) }
                    .doAfterTerminate { this.showProgressBar.onNext(false) }
        }

        private fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
            return this.apolloClient.userPrivacy()
                    .doOnSubscribe { this.showProgressBar.onNext(true) }
                    .doAfterTerminate { this.showProgressBar.onNext(false) }
        }
    }
}
