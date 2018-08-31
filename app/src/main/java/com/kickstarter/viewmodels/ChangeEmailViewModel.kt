package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.ChangeEmailActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface ChangeEmailViewModel {

    interface Inputs {

    }

    interface Outputs {
        /** Emits the user's avatar photo url for display.  */
        fun userEmail(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ChangeEmailActivity>(environment), Inputs, Outputs {

        private var userEmail = BehaviorSubject.create<String>()
        private val client: ApiClientType = environment.apiClient()
        private val apolloClient: ApolloClientType = environment.apolloClient()
        private val currentUser: CurrentUserType = environment.currentUser()
        private val userOutput = BehaviorSubject.create<User>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.client.fetchCurrentUser()
                    .retry(2)
                    .compose(Transformers.neverError())
                    .compose(bindToLifecycle())
                    .subscribe { this.currentUser.refresh(it) }

            this.currentUser.observable()
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe({ this.userOutput.onNext(it) })

            this.apolloClient
                    .userPrivacy()
                    .compose(singleBindToLifecycle())
                    .subscribe({
                        val email = it.me()?.email()
                        this@ViewModel.userEmail.onNext(email)
                    })
        }

        override fun userEmail(): Observable<String> = userEmail
    }
}
