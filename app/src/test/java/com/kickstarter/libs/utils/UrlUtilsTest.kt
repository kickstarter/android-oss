package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class UrlUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testBuildUrl() {
        assertEquals("www.test.com/path", UrlUtils.buildUrl("www.test.com", "path"))
        assertEquals("www.test.com/path/to/path", UrlUtils.buildUrl("www.test.com/path/to", "path"))
        assertEquals("http://www.test.com/path", UrlUtils.buildUrl("http://www.test.com", "path"))
        assertEquals("http://www.test.com/path/to/path", UrlUtils.buildUrl("http://www.test.com/path/to", "path"))
    }
}
