package com.kickstarter.services

import UpdateUserEmailMutation
import UserPrivacyQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import rx.Observable
import rx.subjects.PublishSubject

class KSApolloClient(val service: ApolloClient) : ApolloClientType {
    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserEmailMutation.Data>()
            service.mutate(UpdateUserEmailMutation.builder()
                    .email(email)
                    .current_password(currentPassword)
                    .build())
                    .enqueue(object : ApolloCall.Callback<UpdateUserEmailMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UpdateUserEmailMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UserPrivacyQuery.Data>()
            service.query(UserPrivacyQuery.builder().build())
                        .enqueue(object : ApolloCall.Callback<UserPrivacyQuery.Data>() {
                            override fun onFailure(exception: ApolloException) {
                                ps.onError(exception)
                            }

                            override fun onResponse(response: Response<UserPrivacyQuery.Data>) {
                                ps.onNext(response.data())
                                ps.onCompleted()
                            }
                        })
            return@defer ps
        }
    }
}
