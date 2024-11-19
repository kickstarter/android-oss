package com.kickstarter.services.interceptors

import android.net.Uri
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.utils.WebUtils.userAgent
import com.kickstarter.libs.utils.extensions.isHivequeenUri
import com.kickstarter.libs.utils.extensions.isStagingUri
import com.kickstarter.libs.utils.extensions.isWebUri
import com.kickstarter.models.User
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor for web requests to Kickstarter, not API requests. Used by web views and the web client.
 */
class WebRequestInterceptor(
    private val currentUser: CurrentUserTypeV2,
    private val endpoint: String,
    private val internalTools: InternalToolsType,
    private val build: Build
) : Interceptor {

    private var loggedInUser: User? = null

    init {
        currentUser.observable()
            .filter { it.isPresent() }
            .map { loggedInUser = it.getValue() }
            .subscribe()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        return chain.proceed(request(chain.request()))
    }

    private fun request(initialRequest: Request): Request {
        if (!shouldIntercept(initialRequest)) {
            return initialRequest
        }
        val requestBuilder: Request.Builder = initialRequest.newBuilder()
            .header("User-Agent", userAgent(build))

        val basicAuthorizationHeader = internalTools.basicAuthorizationHeader()

        loggedInUser?.let {
            requestBuilder.addHeader("Authorization", "token " + this.currentUser.accessToken)
        } ?: basicAuthorizationHeader?.let {
            if (shouldAddBasicAuthorizationHeader(initialRequest))
                requestBuilder.addHeader("Authorization", it)
        }

//        if (isStaging(initialRequest)) {
//            requestBuilder.header(
//                name = "Authorization",
//                Credentials.basic(
//                    Secrets.WebEndpoint.CredentialsStaging.USER,
//                    Secrets.WebEndpoint.CredentialsStaging.PASS
//                )
//            )
//        }

        return requestBuilder.build()
    }

    private fun shouldIntercept(request: Request) = Uri.parse(request.url.toString()).isWebUri(endpoint)

    private fun shouldAddBasicAuthorizationHeader(request: Request) =
        if (loggedInUser == null) {
            Uri.parse(request.url.toString()).isHivequeenUri(endpoint)
        } else false

    private fun isStaging(request: Request): Boolean =
        Uri.parse(request.url.toString()).isStagingUri(endpoint)
}
