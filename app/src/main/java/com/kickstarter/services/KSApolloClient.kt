package com.kickstarter.services

import UserPrivacyQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import rx.Single


class KSApolloClient(val service: ApolloClient) : ApolloClientType {
    override fun userPrivacy(): Single<Response<UserPrivacyQuery.Data>> {

        return Single.create<Response<UserPrivacyQuery.Data>>({ e   ->
            service.query(UserPrivacyQuery.builder().build())
                    .enqueue(object : ApolloCall.Callback<UserPrivacyQuery.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            e.onError(exception)
                        }

                        override fun onResponse(response: Response<UserPrivacyQuery.Data>) {
                            e.onSuccess(response)
                        }
                    })
        }
        )
    }

}