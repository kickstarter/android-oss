package com.kickstarter.services.interceptors

import android.net.Uri
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.utils.WebUtils.userAgent
import com.kickstarter.libs.utils.extensions.isApiUri
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Builder
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ApiRequestInterceptor(
    private val clientId: String,
    private val currentUser: CurrentUserTypeV2,
    private val endpoint: String,
    private val build: Build
) : Interceptor {

    @Throws(IOException::class)

    override fun intercept(chain: Chain): Response {
        val response: Response = chain.proceed(request(chain.request()))
        return response
    }

    private fun request(initialRequest: Request): Request {
        if (!shouldIntercept(initialRequest)) {
            return initialRequest
        }

        val builder: Request.Builder = initialRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Kickstarter-Android-App-UUID", FirebaseHelper.identifier)
            .addHeader("User-Agent", userAgent(build))

        this.currentUser.accessToken?.let { token ->
            if (token.isNotEmpty()) builder.addHeader("X-Auth", "token $token")
        }

        return builder
            .url(url(initialRequest.url))
            .build()
    }

    private fun url(initialHttpUrl: HttpUrl): HttpUrl {
        val builder: Builder = initialHttpUrl.newBuilder()
            .setQueryParameter("client_id", clientId)
        return builder.build()
    }

    private fun shouldIntercept(request: Request): Boolean {
        return Uri.parse(request.url.toString()).isApiUri(endpoint)
    }
}
