package com.kickstarter.services.interceptors

import android.net.Uri
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.perimeterx.PerimeterXClientType
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.WebUtils.userAgent
import com.kickstarter.models.User
import com.kickstarter.services.KSUri
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor for web requests to Kickstarter, not API requests. Used by web views and the web client.
 */
class WebRequestInterceptor(
    private val currentUser: CurrentUserType,
    private val endpoint: String,
    private val internalTools: InternalToolsType,
    private val build: Build,
    private val pxManager: PerimeterXClientType
) : Interceptor {

    private var loggedInUser: User? = null

    init {
        currentUser.observable()
            .subscribe {
                loggedInUser = it
            }
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

        if (isStaging(initialRequest)) {
            requestBuilder.header(
                name = "Authorization",
                Credentials.basic(
                    Secrets.WebEndpoint.CredentialsStaging.USER,
                    Secrets.WebEndpoint.CredentialsStaging.PASS
                )
            )
        }

        pxManager.addHeaderTo(requestBuilder)
        return requestBuilder.build()
    }

    private fun shouldIntercept(request: Request) = KSUri.isWebUri(Uri.parse(request.url.toString()), endpoint)

    private fun shouldAddBasicAuthorizationHeader(request: Request) =
        if (loggedInUser == null) {
            KSUri.isHivequeenUri(Uri.parse(request.url.toString()), endpoint)
        } else false

    private fun isStaging(request: Request): Boolean =
        KSUri.isStagingUri(Uri.parse(request.url.toString()), endpoint)
}
