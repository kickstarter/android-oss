package com.kickstarter.services

import UpdateUserEmailMutation
import UserPrivacyQuery
import rx.Observable

interface ApolloClientType {
    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>

    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}