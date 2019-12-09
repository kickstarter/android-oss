package com.kickstarter.services.interceptors

import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.utils.WebUtils
import okhttp3.Interceptor
import okhttp3.Response

class GraphQLInterceptor(private val clientId: String,
                         private val currentUser: CurrentUserType,
                         private val build: Build) : Interceptor {
    override fun intercept(chain: Interceptor.Chain?): Response {
        val original = chain!!.request()
        val builder = original.newBuilder().method(original.method(), original.body())
        if (this.currentUser.exists()) {
            builder.addHeader("Authorization", "token " + this.currentUser.accessToken)
        }
        builder.addHeader("User-Agent", WebUtils.userAgent(this.build))
                .addHeader("X-KICKSTARTER-CLIENT", this.clientId)
        return chain.proceed(builder.build())
    }
}
