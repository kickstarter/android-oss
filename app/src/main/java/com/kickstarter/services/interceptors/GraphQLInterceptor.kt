package com.kickstarter.services.interceptors

import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import okhttp3.Interceptor
import okhttp3.Response

class GraphQLInterceptor(private val clientId: String,
                         private val currentUser: CurrentUserType,
                         private val build: Build) : Interceptor {
    override fun intercept(chain: Interceptor.Chain?): Response {
        val original = chain!!.request()
        val builder = original.newBuilder().method(original.method(), original.body())
        val accessToken = this.currentUser.accessToken
        if (accessToken != null) {
            builder.addHeader("Authorization", "token $accessToken")
        }
        builder.addHeader("User-Agent", WebRequestInterceptor.userAgent(this.build))
                .addHeader("X-KICKSTARTER-CLIENT", this.clientId)
        return chain.proceed(builder.build())
    }
}
