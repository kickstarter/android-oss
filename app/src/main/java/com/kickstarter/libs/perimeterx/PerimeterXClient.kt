package com.kickstarter.libs.perimeterx

import android.content.Context
import com.kickstarter.libs.utils.Secrets
import com.perimeterx.msdk.CaptchaResultCallback
import com.perimeterx.msdk.PXManager
import com.perimeterx.msdk.PXResponse
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import rx.subjects.PublishSubject
import timber.log.Timber


class PerimeterXClient(
        private val context: Context
):PerimeterXClientType {

    private val headers: PublishSubject<HashMap<String, String>> = PublishSubject.create()
    private val isManagerReady: PublishSubject<Boolean> = PublishSubject.create()
    private val captchaSuccess: PublishSubject<Boolean> = PublishSubject.create()
    private val captchaCanceled: PublishSubject<CaptchaResultCallback.CancelReason> = PublishSubject.create()

    override fun getClient(): PXManager = PXManager.getInstance()

    override fun addHeaderTo(builder: Request.Builder?) {
        val headers = PXManager.httpHeaders()?.let { it.toMap() } ?: emptyMap()

        headers.forEach { (key, value) ->
            builder?.addHeader(key, value)
        }
    }

    override fun checkError(body: String): PXResponse = PXManager.checkError(body)

    override fun getVid(): String = PXManager.getInstance().vid

    override fun start() {
        getClient()
                .setNewHeadersCallback { newHeaders: HashMap<String, String> ->
                    Timber.d("${this.javaClass.canonicalName} NewHeadersCallback :$newHeaders")
                    this.headers.onNext(newHeaders)
                }
                .setManagerReadyCallback { headers: HashMap<String?, String?> ->
                    Timber.d("${this.javaClass.canonicalName} setManagerReadyCallback :$headers")
                    this.isManagerReady.onNext(true)
                }

        getClient().start(context, Secrets.PERIMETERX_APPID)
    }

    override fun intercep(response: Response): Response {
        Timber.d("${this.javaClass.canonicalName} intercept with VID :${this.visitorId()}")
        val code = response.code
        if (code != 200) {
            response.body?.let { responseBody ->
                val pxResponse = PXManager.checkError(responseBody.string())

                if (pxResponse.enforcement().name == "NOT_PX_BLOCK") {
                    // Error response not challenged by PerimeterX
                } else {
                    Timber.d("${this.javaClass.canonicalName} Response Challenged: $responseBody")
                    PXManager.handleResponse(pxResponse) { result: CaptchaResultCallback.Result?, reason: CaptchaResultCallback.CancelReason? ->
                        when (result) {
                            CaptchaResultCallback.Result.SUCCESS -> {
                                Timber.d("${this.javaClass.canonicalName} CaptchaResultCallback.Result.SUCCESS")
                                this.captchaSuccess.onNext(true)
                            }
                            CaptchaResultCallback.Result.CANCELED -> {
                                reason?.let { cancelReason ->
                                    Timber.d("${this.javaClass.canonicalName} CaptchaResultCallback.Result.CANCELED reason: ${cancelReason.name}")
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

    override fun visitorId(): String = PXManager.getInstance().vid

    override fun isCaptchaSucess(): Observable<Boolean> = this.captchaSuccess
    override fun isCaptchaCanceled(): Observable<CaptchaResultCallback.CancelReason> = this.captchaCanceled
}