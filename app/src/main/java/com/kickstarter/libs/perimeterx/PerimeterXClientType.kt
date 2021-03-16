package com.kickstarter.libs.perimeterx

import android.content.Context
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import kotlin.collections.HashMap

/**
 * Wrapper interface to handle the implementation with the PerimeterX SDK
 */
interface PerimeterXClientType {

    /**
     * Captcha challenge successful
     */
    val isCaptchaSuccess: Observable<Boolean>

    /**
     * Captcha challenge canceled
     */
    val isCaptchaCanceled: Observable<String>

    /**
     * Every time a new header is added
     */
    val headersAdded: Observable<HashMap<String, String>>

    /**
     * Will emit once the SDK is ready
     */
    val isReady: Observable<Boolean>

    /**
     * The perimeterX headers will be added to the given Request Builder
     */
    fun addHeaderTo(builder: Request.Builder?)

    /**
     * Getter for the PerimeterX headers, null in case no headers found
     */
    fun httpHeaders(): MutableMap<String, String>?

    /**
     * Start SDK with the given context.
     */
    fun start(context: Context)

    /**
     * Intercept a response,
     * will call internally the following methods:
     *
     * - @see fun isChallenged
     * - @see fun handleChallengedResponse
     */
    fun intercept(response: Response)

    /**
     * created `_pxmvid` cookie, with 1 hour expiration time
     * @see {@link https://docs.perimeterx.com/pxconsole/docs/android-sdk-integration-guide#section-web-view-integration}
     */
    fun getCookieForWebView(): String

    /**
     * Identification for the PerimeterX sdk
     */
    fun visitorId(): String

    /**
     * Will evaluate the body of a response
     * and say id that response was challenged or not
     */
    fun isChallenged(body: String): Boolean

    /**
     * Will take the response body from a challenged response,
     * and internally handle any outcome:
     * - response intercepted and successfully passed the challenge:
     * @isCaptchaSuccess will emit
     *
     * - response intercepted but challenge canceled:
     * @isCaptchaCanceled will emit
     *
     * - response not challenged none of the above will emit
     */
    fun handleChallengedResponse(body: String)
}
