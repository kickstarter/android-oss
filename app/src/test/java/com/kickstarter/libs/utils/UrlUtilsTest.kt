package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class UrlUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testAppendPath() {
        assertEquals("www.test.com/path", UrlUtils.appendPath("www.test.com", "path"))
        assertEquals("www.test.com/path/to/path", UrlUtils.appendPath("www.test.com/path/to", "path"))
        assertEquals("http://www.test.com/path", UrlUtils.appendPath("http://www.test.com", "path"))
        assertEquals("http://www.test.com/path/to/path", UrlUtils.appendPath("http://www.test.com/path/to", "path"))
    }

    @Test
    fun testAppendQueryParameter() {
        assertEquals("www.test.com/path?key=value", UrlUtils.appendQueryParameter("www.test.com", "key", "value"))
        assertEquals("www.test.com/path/to?key=value", UrlUtils.appendQueryParameter("www.test.com/path/to", "key", "value"))
        assertEquals("http://www.test.com/path?key=value", UrlUtils.appendQueryParameter("http://www.test.com", "key", "value"))
        assertEquals("http://www.test.com/path/to?key=value", UrlUtils.appendQueryParameter("http://www.test.com/path/to", "key", "value"))
    }
}
