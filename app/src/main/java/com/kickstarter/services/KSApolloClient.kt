package com.kickstarter.services

import UserPrivacyQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import rx.Observable
import rx.subjects.PublishSubject

class KSApolloClient(val service: ApolloClient) : ApolloClientType {
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
