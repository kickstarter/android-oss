package com.kickstarter.services

import UserPrivacyQuery
import rx.Single

interface ApolloClientType {
    fun userPrivacy(): Single<UserPrivacyQuery.Data>
}