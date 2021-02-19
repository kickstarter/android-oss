package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.perimeterx.PerimeterXClient
import okhttp3.Request
import org.junit.Test

class PerimeterXTest : KSRobolectricTestCase()  {

    internal lateinit var pxClient: MockPXClient

    override fun setUp() {
        super.setUp()
        pxClient = MockPXClient(environment().build())
    }

    @Test
    fun testCookieForWebView() {
        val cookie = pxClient.getCookieForWebView()
        val assertValue = "_pxmvid=${pxClient.vId()}"
        assert(cookie.contains(assertValue))
    }

    @Test
    fun testVisitiorId() {
        val vid = pxClient.vId()
        assertNotNull(vid)
        assertEquals(vid, "VID")
    }

    @Test
    fun testHeaders() {
        val pxHeader = pxClient.httpHeaders()
        val headerVariable = mutableMapOf("h1" to "value1", "h2" to "value2")
        assert(pxHeader.isNotEmpty())
        assertEquals(pxHeader.size, 2)
        assertEquals(pxHeader.keys, headerVariable.keys)
        assertEquals(pxHeader.values.toString(), headerVariable.values.toString())
    }

    @Test
    fun testAddHeaderToBuilder() {
        val mockRequest = Request.Builder()
                .url("http://url.com")

        val headerVariable = mutableMapOf("h1" to "value1", "h2" to "value2")

        pxClient.addHeaderTo(mockRequest)

        val request = mockRequest.build()
        val first = request.headers[headerVariable.keys.first()]
        val last = request.headers[headerVariable.keys.last()]

        assertEquals(first, headerVariable[headerVariable.keys.first()])
        assertEquals(last, headerVariable[headerVariable.keys.last()])
    }

    internal class MockPXClient(build: Build): PerimeterXClient(build) {
        override fun vId() = "VID"
        override fun httpHeaders(): MutableMap<String, String> = mutableMapOf("h1" to "value1", "h2" to "value2")
    }
}