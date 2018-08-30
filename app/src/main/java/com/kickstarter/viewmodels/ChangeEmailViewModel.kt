package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.ChangeEmailActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ChangeEmailViewModel {

    interface Inputs {

    }

    interface Outputs {
        /** Emits the user's avatar photo url for display.  */
        fun userEmail(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ChangeEmailActivity>(environment), Inputs, Outputs {

        private var userEmail = BehaviorSubject.create<String>()
        private var email: String? = null
        private val client: ApiClientType = environment.apiClient()
        private val apolloClient: ApolloClient = environment.apolloClient()
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
                    .query(UserPrivacyQuery.builder().build())
                    .enqueue(object : ApolloCall.Callback<UserPrivacyQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            Log.e("izzytest", e.localizedMessage)
                        }

                        override fun onResponse(response: Response<UserPrivacyQuery.Data>) {
                            email = response.data()?.me()?.email()
                            this@ViewModel.userEmail.onNext(email)
                        }

                    })
        }

        override fun userEmail() = userEmail
    }

}