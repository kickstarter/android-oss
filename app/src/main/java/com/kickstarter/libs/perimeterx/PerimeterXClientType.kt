package com.kickstarter.libs.perimeterx

import com.perimeterx.msdk.CaptchaResultCallback
import com.perimeterx.msdk.PXManager
import com.perimeterx.msdk.PXResponse
import okhttp3.Request
import okhttp3.Response
import rx.Observable

interface PerimeterXClientType {
    fun getClient(): PXManager?
    fun addHeaderTo(builder: Request.Builder?)
    fun checkError(body: String): PXResponse
    fun getVid(): String?
    fun start()
    fun intercep(response: Response): Response

    fun visitorId(): String
    fun isCaptchaSucess(): Observable<Boolean>
    fun isCaptchaCanceled(): Observable<CaptchaResultCallback.CancelReason>
}