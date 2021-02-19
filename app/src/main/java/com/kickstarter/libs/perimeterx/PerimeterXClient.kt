package com.kickstarter.libs.perimeterx

import android.content.Context
import com.kickstarter.libs.Build
import com.kickstarter.libs.utils.Secrets
import com.perimeterx.msdk.CaptchaResultCallback
import com.perimeterx.msdk.PXManager
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import rx.subjects.PublishSubject
import timber.log.Timber
import java.util.*


private const val LOGTAG = "PerimeterXClient"
open class PerimeterXClient(private val build: Build):PerimeterXClientType {

    private val headers: PublishSubject<HashMap<String, String>> = PublishSubject.create()
    private val isManagerReady: PublishSubject<Boolean> = PublishSubject.create()
    private val captchaSuccess: PublishSubject<Boolean> = PublishSubject.create()
    private val captchaCanceled: PublishSubject<CaptchaResultCallback.CancelReason> = PublishSubject.create()

    override val headersAdded: Observable<HashMap<String, String>>
        get() = this.headers

    override val isCaptchaCanceled: Observable<CaptchaResultCallback.CancelReason>
        get() = this.captchaCanceled

    override val isCaptchaSuccess: Observable<Boolean>
        get() = this.isCaptchaSuccess

    override val isReady: Observable<Boolean>
        get() = this.isManagerReady

    private fun getClient(): PXManager = PXManager.getInstance()

    override fun httpHeaders(): MutableMap<String, String>? = PXManager.httpHeaders()

    override fun initialize() {
        getClient()
                .setNewHeadersCallback { newHeaders: HashMap<String, String> ->
                    if (build.isDebug) Timber.d("$LOGTAG NewHeadersCallback :$newHeaders")
                    this.headers.onNext(newHeaders)
                }
                .setManagerReadyCallback { headers: HashMap<String?, String?> ->
                    if (build.isDebug) Timber.d("$LOGTAG setManagerReadyCallback :$headers")
                    this.isManagerReady.onNext(true)
                }
    }

    override fun addHeaderTo(builder: Request.Builder?) {
        val headers = httpHeaders()?.toMap() ?: emptyMap()

        headers.forEach { (key, value) ->
            builder?.addHeader(key, value)
        }

        if (build.isDebug) Timber.d("$LOGTAG headers: $headers added to requestBuilder: ${builder?.toString()}")
    }

    override fun vId():String = getClient().vid

    override fun start(context: Context) = getClient().start(context, Secrets.PERIMETERX_APPID)

    override fun getCookieForWebView(): String {
        val date = Date()
        date.time = date.time + 60 * 60 * 1000 // - Set the expiration to one hour
        return "_pxmvid=${this.vId()} expires= $date;"
    }

    override fun intercept(response: Response) {
        if (build.isDebug) Timber.d("$LOGTAG intercepted response for request:${response.request.url} with VID :${this.vId()}")

        if (response.code == 403) {
            this.checkChallengedResponse(this.cloneResponse(response))
        }
    }

    /**
     * Accessing the body of the response more than once (down on the flow Retrofit/Apollo will access the response as well) will throw an IllegalStateException by OkHttp.
     * Clone the response and access the body via `peekBody` to get a lightweight copy of it
     */
    private fun cloneResponse(response: Response) = response.newBuilder()
            .code(response.code)
            .request(response.request)
            .body(response.peekBody(Long.MAX_VALUE))
            .build()

    private fun checkChallengedResponse(response: Response) {
        response.body?.string().let {
            val pxResponse = PXManager.checkError(it)

            if (pxResponse.enforcement().name == "NOT_PX_BLOCK") {
                // Error response not challenged by PerimeterX
            } else {
                if (build.isDebug) Timber.d("$LOGTAG Response Challenged for Request: ${response.request.url}")
                PXManager.handleResponse(pxResponse) { result: CaptchaResultCallback.Result?, reason: CaptchaResultCallback.CancelReason? ->
                    when (result) {
                        CaptchaResultCallback.Result.SUCCESS -> {
                            if (build.isDebug) Timber.d("$LOGTAG CaptchaResultCallback.Result.SUCCESS")
                            this.captchaSuccess.onNext(true)
                        }
                        CaptchaResultCallback.Result.CANCELED -> {
                            reason?.let { cancelReason ->
                                if (build.isDebug) Timber.d("$LOGTAG CaptchaResultCallback.Result.CANCELED reason: ${cancelReason.name}")
                                this.captchaCanceled.onNext(cancelReason)
                            }
                        }
                    }
                }
            }
        }
    }
}