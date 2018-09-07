package com.kickstarter.services

import UserPrivacyQuery
import rx.Observable

interface ApolloClientType {
    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}