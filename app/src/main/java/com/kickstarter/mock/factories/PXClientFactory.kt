package com.kickstarter.mock.factories

import android.content.Context
import com.kickstarter.libs.Build
import com.kickstarter.libs.perimeterx.PerimeterXClient
import okhttp3.Response
import rx.Observable
import rx.subjects.PublishSubject
import java.util.HashMap

open class MockPXClient(build: Build) : PerimeterXClient(build) {

    private val headers: PublishSubject<HashMap<String, String>> = PublishSubject.create()
    private val isManagerReady: PublishSubject<Boolean> = PublishSubject.create()
    private val captchaSuccess: PublishSubject<Boolean> = PublishSubject.create()

    override val headersAdded: Observable<HashMap<String, String>>
        get() = this.headers

    override val isCaptchaSuccess: Observable<Boolean>
        get() = this.captchaSuccess

    override val isReady: Observable<Boolean>
        get() = this.isManagerReady

    override fun visitorId() = "VID"
    override fun httpHeaders(): MutableMap<String, String> = mutableMapOf("h1" to "value1", "h2" to "value2")
    override fun start(context: Context) {
        headers.onNext(hashMapOf())
        isManagerReady.onNext(true)
    }

    override fun isChallenged(body: String) = true

    override fun intercept(response: Response) {
        captchaSuccess.onNext(true)
    }
}

class MockPXClientNotChallenged(build: Build) : MockPXClient(build) {
    override fun isChallenged(body: String) = false
    override fun intercept(response: Response) {
        // no captcha emitted
    }
}

class MockPXClientCaptchaCanceled(build: Build) : MockPXClient(build) {
    private val captchaCanceled: PublishSubject<String> = PublishSubject.create()
    override val isCaptchaCanceled: Observable<String>
        get() = this.captchaCanceled

    override fun intercept(response: Response) {
        captchaCanceled.onNext("Back Button")
    }
}

class PXClientFactory private constructor() {
    companion object {
        fun pxChallengedSuccessful(build: Build) = MockPXClient(build)
        fun pxChallengedCanceled(build: Build) = MockPXClientCaptchaCanceled(build)
        fun pxNotChallenged(build: Build) = MockPXClientNotChallenged(build)
    }
}
