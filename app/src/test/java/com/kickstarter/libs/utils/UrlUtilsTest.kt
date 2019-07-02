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

    @Test
    fun testWrapInATag() {
        assertEquals("<a href=\"www.test.com\">Test</a>", UrlUtils.wrapInATag("Test", "www.test.com"))
        assertEquals("<a href=\"http://www.test.com\">Test</a>", UrlUtils.wrapInATag("Test", "http://www.test.com"))
        assertEquals("<a href=\"http://www.test.com/path/to\">Test</a>", UrlUtils.wrapInATag("Test", "http://www.test.com/path/to"))
    }
}
