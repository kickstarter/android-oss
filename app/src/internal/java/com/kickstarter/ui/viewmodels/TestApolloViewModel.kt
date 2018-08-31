package com.kickstarter.ui.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.neverError
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

        private val error = BehaviorSubject.create<String>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {
            this.makeNetworkCallClicked
                    .switchMap { this.apolloClient.userPrivacy().toObservable() }
                    .compose(bindToLifecycle())
                    .compose(neverError())
                    .subscribe({
                        this.email.onNext(it.me()?.email())
                        this.name.onNext(it.me()?.name())
                    })

            this.makeNetworkCallWithErrorsClicked
                    .switchMap { this.apolloClient.userPrivacy().toObservable() }
                    .compose(bindToLifecycle())
                    .subscribe({
                        this.email.onNext(it.me()?.email())
                        this.name.onNext(it.me()?.name())
                    }, {
                        this.error.onNext(it.localizedMessage)
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

        override fun error(): Observable<String> = this.error
    }
}
