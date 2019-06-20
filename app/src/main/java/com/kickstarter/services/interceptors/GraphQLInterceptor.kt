package com.kickstarter.services.interceptors

import com.kickstarter.libs.CurrentUserType
import okhttp3.Interceptor
import okhttp3.Response

class GraphQLInterceptor(private val currentUser: CurrentUserType, private val clientId: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val initialRequest = chain.request()

        val url = initialRequest.url()
                .newBuilder()
                .setQueryParameter("client_id", this.clientId)
                .build()

        val builder = initialRequest.newBuilder()
                .url(url)
                .method(initialRequest.method(), initialRequest.body())

        if (this.currentUser.exists()) {
            builder.addHeader("Authorization", "token " + this.currentUser.accessToken)
        }

        return chain.proceed(builder.build())
    }
}
