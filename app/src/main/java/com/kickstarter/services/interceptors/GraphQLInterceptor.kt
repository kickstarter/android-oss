package com.kickstarter.services.interceptors

import com.kickstarter.libs.CurrentUserType
import okhttp3.Interceptor
import okhttp3.Response

class GraphQLInterceptor(private val currentUser: CurrentUserType) : Interceptor {
    override fun intercept(chain: Interceptor.Chain?): Response {
        val original = chain!!.request()
        val builder = original.newBuilder().method(original.method(), original.body())
        if (this.currentUser.exists()) {
            builder.addHeader("Authorization", "token " + currentUser.accessToken)
        }
        return chain.proceed(builder.build())
    }
}
