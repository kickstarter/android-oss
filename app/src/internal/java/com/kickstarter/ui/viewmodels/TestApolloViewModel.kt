package com.kickstarter.ui.viewmodels

import UserPrivacyQuery
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.TestApolloActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class TestApolloViewModel {
    interface Inputs {
        /** Call when the make network call button has been clicked.  */
        fun makeNetworkCallClicked()

        /** Call when the make network call with errors button has been clicked.  */
        fun makeNetworkCallWithErrorsClicked()
    }

    interface Outputs {
        /** Emits the logged in user's email address.  */
        fun email(): Observable<String>

        /** Emits the logged in user's name.  */
        fun name(): Observable<String>

        /** Emits a boolean that determines if a network call is in progress.  */
        fun showProgressBar(): Observable<Boolean>
    }

    interface Errors {
        /** Emits a string to display when user could not be found.  */
        fun error(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<TestApolloActivity>(environment), Inputs, Outputs, Errors {

        val inputs: TestApolloViewModel.Inputs = this
        val outputs: TestApolloViewModel.Outputs = this
        val errors: TestApolloViewModel.Errors = this

        private val makeNetworkCallClicked = PublishSubject.create<Void>()
        private val makeNetworkCallWithErrorsClicked = PublishSubject.create<Void>()

        private val email = BehaviorSubject.create<String>()
        private val name = BehaviorSubject.create<String>()
        private val showProgressBar = BehaviorSubject.create<Boolean>()

        private val error = BehaviorSubject.create<String>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {

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
                    .compose(errors())
                    .subscribe({ this.error.onNext(it.localizedMessage) })

            userPrivacyNotification
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

        override fun email(): Observable<String> = this.email

        override fun name(): Observable<String> = this.name

        override fun showProgressBar(): Observable<Boolean> = this.showProgressBar

        override fun error(): Observable<String> = this.error

        private fun emitData(it: UserPrivacyQuery.Data) {
            this.email.onNext(it.me()?.email())
            this.name.onNext(it.me()?.name())
        }

        private fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
            return this.apolloClient.userPrivacy()
                    .doOnSubscribe { this.showProgressBar.onNext(true) }
                    .doAfterTerminate { this.showProgressBar.onNext(false) }
        }
    }
}
