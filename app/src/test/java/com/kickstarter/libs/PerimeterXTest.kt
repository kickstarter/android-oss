package com.kickstarter.libs

import androidx.test.core.app.ApplicationProvider
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.PXClientFactory
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.HashMap

class PerimeterXTest : KSRobolectricTestCase() {

    lateinit var build: Build

    private val mockRequest = Request.Builder()
        .url("http://url.com")
        .build()

    private val mockResponse = Response.Builder()
        .request(mockRequest)
        .protocol(Protocol.HTTP_2)
        .message("")
        .code(403)
        .body("body".toResponseBody())
        .build()

    private val mockBody = mockResponse.body?.string() ?: ""

    override fun setUp() {
        super.setUp()
        build = environment().build()
    }

    @Test
    fun testCookieForWebView() {
        val pxClient = PXClientFactory.pxChallengedSuccessful(build)
        val cookie = pxClient.getCookieForWebView()
        val assertValue = "_pxmvid=${pxClient.visitorId()}"

        assert(cookie.contains(assertValue))
    }

    @Test
    fun testVisitiorId() {
        val pxClient = PXClientFactory.pxChallengedSuccessful(build)
        val vid = pxClient.visitorId()

        assertNotNull(vid)
        assertEquals(vid, "VID")
    }

    @Test
    fun testHeaders() {
        val pxClient = PXClientFactory.pxChallengedSuccessful(build)
        val pxHeader = pxClient.httpHeaders()
        val headerVariable = mutableMapOf("h1" to "value1", "h2" to "value2")

        assert(pxHeader.isNotEmpty())
        assertEquals(pxHeader.size, 2)
        assertEquals(pxHeader.keys, headerVariable.keys)
        assertEquals(pxHeader.values.toString(), headerVariable.values.toString())
    }

    @Test
    fun testAddHeaderToBuilder() {
        val pxClient = PXClientFactory.pxChallengedSuccessful(build)
        val mockRequestBuilder = Request.Builder()
            .url("http://url.com")

        val headerVariable = mutableMapOf("h1" to "value1", "h2" to "value2")

        pxClient.addHeaderTo(mockRequestBuilder)

        val request = mockRequestBuilder.build()
        val first = request.headers[headerVariable.keys.first()]
        val last = request.headers[headerVariable.keys.last()]

        assertEquals(first, headerVariable[headerVariable.keys.first()])
        assertEquals(last, headerVariable[headerVariable.keys.last()])
    }

    @Test
    fun testChallengedResponse() {
        val clientNotChallenged = PXClientFactory.pxNotChallenged(environment().build())
        val clientWithChallenged = PXClientFactory.pxChallengedSuccessful(environment().build())

        val challenge1 = clientWithChallenged.isChallenged(mockBody)
        val challenge2 = clientNotChallenged.isChallenged(mockBody)

        assertTrue(challenge1)
        assertFalse(challenge2)
    }

    @Test
    fun testIntercept_whenCaptchaSuccess() {
        val pxClient = PXClientFactory.pxChallengedSuccessful(build)

        val testIsManagerReady = TestSubscriber.create<Boolean>()
        val testCaptchaSuccess = TestSubscriber.create<Boolean>()
        val testCaptchaCanceled = TestSubscriber.create<String>()
        val testHeadersAdded = TestSubscriber.create<HashMap<String, String>>()

        pxClient.isReady.subscribe(testIsManagerReady)
        pxClient.isCaptchaSuccess.subscribe(testCaptchaSuccess)
        pxClient.headersAdded.subscribe(testHeadersAdded)
        pxClient.isCaptchaCanceled.subscribe(testCaptchaCanceled)

        pxClient.start(ApplicationProvider.getApplicationContext())
        pxClient.intercept(mockResponse)

        testIsManagerReady.assertValue(true)
        testCaptchaSuccess.assertValue(true)
        testCaptchaCanceled.assertNoValues()
        testHeadersAdded.assertValueCount(1)
    }

    @Test
    fun testIntercept_whenCaptchaCanceled() {
        val client = PXClientFactory.pxChallengedCanceled(environment().build())
        val testCaptchaSuccess = TestSubscriber.create<Boolean>()
        val testCaptchaCanceled = TestSubscriber.create<String>()

        client.isCaptchaSuccess.subscribe(testCaptchaSuccess)
        client.isCaptchaCanceled.subscribe(testCaptchaCanceled)

        client.start(ApplicationProvider.getApplicationContext())
        client.intercept(mockResponse)

        testCaptchaCanceled.assertValue("Back Button")
        testCaptchaSuccess.assertNoValues()
    }

    @Test
    fun testIntercept_whenNoChallenged_NoCaptcha() {
        val client = PXClientFactory.pxNotChallenged(build)
        val testCaptchaSuccess = TestSubscriber.create<Boolean>()
        val testCaptchaCanceled = TestSubscriber.create<String>()

        client.isCaptchaSuccess.subscribe(testCaptchaSuccess)
        client.isCaptchaCanceled.subscribe(testCaptchaCanceled)

        client.start(ApplicationProvider.getApplicationContext())
        client.intercept(mockResponse)

        assertFalse(client.isChallenged(mockBody))
        testCaptchaCanceled.assertNoValues()
        testCaptchaSuccess.assertNoValues()
    }
}
