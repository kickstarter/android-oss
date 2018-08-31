package com.kickstarter.services

import UserPrivacyQuery
import com.apollographql.apollo.api.Response
import rx.Single


interface ApolloClientType {
    fun userPrivacy(): Single<Response<UserPrivacyQuery.Data>>
}