package com.kickstarter.services

import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPrivacyQuery
import rx.Observable

interface ApolloClientType {
    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>

    fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data>

    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
}