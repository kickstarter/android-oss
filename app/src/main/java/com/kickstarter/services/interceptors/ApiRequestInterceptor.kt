package com.kickstarter.services.interceptors

import android.net.Uri
import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.perimeterx.PerimeterXClientType
import com.kickstarter.libs.utils.WebUtils.userAgent
import com.kickstarter.models.User
import com.kickstarter.services.KSUri
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Builder
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ApiRequestInterceptor(
    private val clientId: String,
    private val currentUser: CurrentUserType,
    private val endpoint: String,
    private val pxManager: PerimeterXClientType,
    private val build: Build
) : Interceptor {

    @Throws(IOException::class)

    override fun intercept(chain: Chain): Response {
        val response: Response = chain.proceed(request(chain.request()))
        pxManager.intercept(response)
        return response
    }

    private fun request(initialRequest: Request): Request {
        if (!shouldIntercept(initialRequest)) {
            return initialRequest
        }

        val builder: Request.Builder = initialRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Kickstarter-Android-App-UUID", FirebaseInstanceId.getInstance().id)
            .addHeader("User-Agent", userAgent(build))

        pxManager.addHeaderTo(builder)

        return builder
            .url(url(initialRequest.url))
            .build()
    }

    private fun url(initialHttpUrl: HttpUrl): HttpUrl {
        val builder: Builder = initialHttpUrl.newBuilder()
            .setQueryParameter("client_id", clientId)
        currentUser.observable()
            .subscribe { user: User? -> builder.setQueryParameter("oauth_token", currentUser.accessToken) }
        return builder.build()
    }

    private fun shouldIntercept(request: Request): Boolean {
        return KSUri.isApiUri(Uri.parse(request.url.toString()), endpoint)
    }
}
