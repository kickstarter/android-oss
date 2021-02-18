package com.kickstarter.libs.perimeterx

import android.content.Context
import com.perimeterx.msdk.CaptchaResultCallback
import com.perimeterx.msdk.PXResponse
import okhttp3.Request
import okhttp3.Response
import rx.Observable

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
    val isCaptchaCanceled: Observable<CaptchaResultCallback.CancelReason>

    /**
     * Every time a new header is added it will be emitted
     */
    val headersAdded: Observable<HashMap<String, String>>

    /**
     * Will emit once the Perimeter SDK is ready
     */
    val isReady: Observable<Boolean>

    /**
     * Initialization for the PerimeterX SDK
     */
    fun initialize()

    /**
     * The perimeter X headers will be added to the given Request Builder
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
     * Check if the given response should be intercepted
     * - response intercepted and successfully passed the challenge:
     * @isCaptchaSuccess will emit
     *
     * - if challenged but challenge canceled
     * @isCaptchaCanceled will emit
     *
     * - response not challenged none of the above will emit
     */
    fun intercept(response: Response): Response

    /**
     * Added `_pxmvid` cookie to each loaded url, with 1 hour expiration time
     * @see {@link https://docs.perimeterx.com/pxconsole/docs/android-sdk-integration-guide#section-web-view-integration}
     */
    fun getCookieForWebView(): String

    /**
     * Identification for the PerimeterX sdk
     */
    fun vId(): String

}