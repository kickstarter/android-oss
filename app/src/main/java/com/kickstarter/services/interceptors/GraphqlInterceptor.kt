package com.kickstarter.services.interceptors

import com.kickstarter.libs.CurrentUserType
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class GraphqlInterceptor(private val clientId: String, private val currentUser: CurrentUserType) : Interceptor {

    override fun intercept(chain: Interceptor.Chain) = chain.proceed(request(chain.request()))

    private fun request(initialRequest: Request): Request {
        val builder = initialRequest.newBuilder()
        if (currentUser.exists()) {
            builder.header("Authorization", "token " + currentUser.accessToken)
        }

        return builder
                .url(url(initialRequest.url()))
                .build()
    }

    private fun url(initialHttpUrl: HttpUrl): HttpUrl {
        val builder = initialHttpUrl.newBuilder()
                .setQueryParameter("client_id", clientId)
        return builder.build()
    }
}
