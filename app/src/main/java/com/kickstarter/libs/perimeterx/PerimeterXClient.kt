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


class PerimeterXClient(private val build: Build):PerimeterXClientType {

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
                    if (build.isDebug) Timber.d("${this.javaClass.canonicalName} NewHeadersCallback :$newHeaders")
                    this.headers.onNext(newHeaders)
                }
                .setManagerReadyCallback { headers: HashMap<String?, String?> ->
                    if (build.isDebug) Timber.d("${this.javaClass.canonicalName} setManagerReadyCallback :$headers")
                    this.isManagerReady.onNext(true)
                }
    }

    override fun addHeaderTo(builder: Request.Builder?) {
        val headers = httpHeaders()?.toMap() ?: emptyMap()

        headers.forEach { (key, value) ->
            builder?.addHeader(key, value)
        }

        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} addHeaders: $headers to builder:${builder?.toString()}")
    }

    override fun vId():String = getClient().vid

    override fun start(context: Context) = getClient().start(context, Secrets.PERIMETERX_APPID)

    override fun intercept(response: Response): Response {
        if (build.isDebug) Timber.d("${this.javaClass.canonicalName} intercept with VID :${this.vId()}")

        if (response.code != 200) {
            response.body?.let { responseBody ->
                val pxResponse = PXManager.checkError(responseBody.string())

                if (pxResponse.enforcement().name == "NOT_PX_BLOCK") {
                    // Error response not challenged by PerimeterX
                } else {
                    if (build.isDebug) Timber.d("${this.javaClass.canonicalName} Response Challenged: $responseBody")
                    PXManager.handleResponse(pxResponse) { result: CaptchaResultCallback.Result?, reason: CaptchaResultCallback.CancelReason? ->
                        when (result) {
                            CaptchaResultCallback.Result.SUCCESS -> {
                                if (build.isDebug) Timber.d("${this.javaClass.canonicalName} CaptchaResultCallback.Result.SUCCESS")
                                this.captchaSuccess.onNext(true)
                            }
                            CaptchaResultCallback.Result.CANCELED -> {
                                reason?.let { cancelReason ->
                                    if (build.isDebug) Timber.d("${this.javaClass.canonicalName} CaptchaResultCallback.Result.CANCELED reason: ${cancelReason.name}")
                                    this.captchaCanceled.onNext(cancelReason)
                                }
                            }
                        }
                    }
                }
            }
        }

        return response
    }
}