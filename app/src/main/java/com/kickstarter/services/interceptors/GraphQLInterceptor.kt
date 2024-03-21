package com.kickstarter.services.interceptors

import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.utils.WebUtils
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

/**
 * Headers specific for the GraphQL client
 * see @see <a href="https://square.github.io/okhttp/interceptors/">https://square.github.io/okhttp/interceptors/</a>
 */
class GraphQLInterceptor(
    private val clientId: String,
    private val currentUser: CurrentUserTypeV2,
    private val build: Build
) : Interceptor {
    override fun intercept(chain: Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder().method(original.method, original.body)

        this.currentUser.accessToken?.let {
            builder.addHeader("Authorization", "token $it")
        }

        builder.addHeader("User-Agent", WebUtils.userAgent(this.build))
            .addHeader("X-KICKSTARTER-CLIENT", this.clientId)
            .addHeader("Kickstarter-Android-App-UUID", FirebaseHelper.identifier)

        return chain.proceed(builder.build())
    }
}
