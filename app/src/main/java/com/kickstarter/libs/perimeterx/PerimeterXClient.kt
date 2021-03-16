package com.kickstarter.libs.perimeterx

import android.content.Context
import com.kickstarter.libs.Build
import com.kickstarter.libs.utils.Secrets
import com.perimeterx.msdk.CaptchaResultCallback
import com.perimeterx.msdk.PXManager
import com.perimeterx.msdk.PXResponse
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import rx.subjects.PublishSubject
import timber.log.Timber
import java.util.Date

private const val LOGTAG = "PerimeterXClient"
open class PerimeterXClient(private val build: Build) : PerimeterXClientType {

    private val headers: PublishSubject<HashMap<String, String>> = PublishSubject.create()
    private val isManagerReady: PublishSubject<Boolean> = PublishSubject.create()
    private val captchaSuccess: PublishSubject<Boolean> = PublishSubject.create()
    private val captchaCanceled: PublishSubject<String> = PublishSubject.create()

    private val pxManager = PXManager.getInstance()
    private var pxResponse: PXResponse? = null

    override val headersAdded: Observable<HashMap<String, String>>
        get() = this.headers

    override val isCaptchaCanceled: Observable<String>
        get() = this.captchaCanceled

    override val isCaptchaSuccess: Observable<Boolean>
        get() = this.captchaSuccess

    override val isReady: Observable<Boolean>
        get() = this.isManagerReady

    override fun getCookieForWebView(): String {
        val date = Date()
        date.time = date.time + 60 * 60 * 1000 // - Set the expiration to one hour
        return "_pxmvid=${this.visitorId()} expires= $date;"
    }

    override fun httpHeaders(): MutableMap<String, String>? = PXManager.httpHeaders()

    private fun initialize() {
        pxManager
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

    override fun visitorId(): String {
        val visitorID: String? = pxManager.vid
        return visitorID?.let { it } ?: ""
    }

    override fun start(context: Context) {
        initialize()
        pxManager.start(context, Secrets.PERIMETERX_APPID)
    }

    override fun intercept(response: Response) {
        if (build.isDebug) Timber.d("$LOGTAG intercepted response for request:${response.request.url} with VID :${this.visitorId()}")

        if (response.code == 403) {
            this.cloneResponse(response).body?.string()?.let {
                if (this.isChallenged(it)) {
                    if (build.isDebug) Timber.d("$LOGTAG Response Challenged for Request: ${response.request.url}")
                    this.handleChallengedResponse(it)
                }
            }
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

    override fun isChallenged(body: String): Boolean {
        this.pxResponse = PXManager.checkError(body)
        return pxResponse?.enforcement()?.name != "NOT_PX_BLOCK"
    }

    override fun handleChallengedResponse(body: String) {
        pxResponse?.let { response ->
            PXManager.handleResponse(response) { result: CaptchaResultCallback.Result?, reason: CaptchaResultCallback.CancelReason? ->
                when (result) {
                    CaptchaResultCallback.Result.SUCCESS -> {
                        if (build.isDebug) Timber.d("$LOGTAG CaptchaResultCallback.Result.SUCCESS")
                        this.captchaSuccess.onNext(true)
                    }
                    CaptchaResultCallback.Result.CANCELED -> {
                        reason?.let { cancelReason ->
                            if (build.isDebug) Timber.d("$LOGTAG CaptchaResultCallback.Result.CANCELED reason: ${cancelReason.name}")
                            this.captchaCanceled.onNext(cancelReason.name)
                        }
                    }
                }
            }
        }
    }
}
