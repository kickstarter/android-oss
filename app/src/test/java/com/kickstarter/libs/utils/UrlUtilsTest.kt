package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import org.junit.Test

class UrlUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testAppendQueryParameter() {
        assertEquals("www.test.com?key=value", UrlUtils.appendQueryParameter("www.test.com", "key", "value"))
        assertEquals("www.test.com/path/to?key=value", UrlUtils.appendQueryParameter("www.test.com/path/to", "key", "value"))
        assertEquals("http://www.test.com?key=value", UrlUtils.appendQueryParameter("http://www.test.com", "key", "value"))
        assertEquals("http://www.test.com/path/to?key=value", UrlUtils.appendQueryParameter("http://www.test.com/path/to", "key", "value"))
        assertEquals("https://www.test.com?key=value", UrlUtils.appendQueryParameter("https://www.test.com", "key", "value"))
        assertEquals("https://www.test.com/path/to?key=value", UrlUtils.appendQueryParameter("https://www.test.com/path/to", "key", "value"))
    }
    @Test
    fun testAppendPath() {
        assertEquals("www.test.com/path", UrlUtils.appendPath("www.test.com", "path"))
        assertEquals("www.test.com/path/to/path", UrlUtils.appendPath("www.test.com/path/to", "path"))
        assertEquals("http://www.test.com/path", UrlUtils.appendPath("http://www.test.com", "path"))
        assertEquals("http://www.test.com/path/to/path", UrlUtils.appendPath("http://www.test.com/path/to", "path"))
        assertEquals("https://www.test.com/path", UrlUtils.appendPath("https://www.test.com", "path"))
        assertEquals("https://www.test.com/path/to/path", UrlUtils.appendPath("https://www.test.com/path/to", "path"))
    }

    @Test
    fun testAppendRefTag() {
        assertEquals("www.test.com?ref=activity", UrlUtils.appendRefTag("www.test.com", RefTag.activity().tag()))
        assertEquals("www.test.com/path/to?ref=activity", UrlUtils.appendRefTag("www.test.com/path/to", RefTag.activity().tag()))
        assertEquals("http://www.test.com?ref=activity", UrlUtils.appendRefTag("http://www.test.com", RefTag.activity().tag()))
        assertEquals("http://www.test.com/path/to?ref=activity", UrlUtils.appendRefTag("http://www.test.com/path/to", RefTag.activity().tag()))
        assertEquals("https://www.test.com?ref=activity", UrlUtils.appendRefTag("https://www.test.com", RefTag.activity().tag()))
        assertEquals("https://www.test.com/path/to?ref=activity", UrlUtils.appendRefTag("https://www.test.com/path/to", RefTag.activity().tag()))
    }

    @Test
    fun testRefTag() {
        assertEquals(null, UrlUtils.refTag("www.test.com"))
        assertEquals("activity", UrlUtils.refTag("www.test.com?ref=activity"))
        assertEquals(null, UrlUtils.refTag("www.test.com/path/to"))
        assertEquals("activity", UrlUtils.refTag("www.test.com/path/to?ref=activity"))
        assertEquals(null, UrlUtils.refTag("http://www.test.com"))
        assertEquals("activity", UrlUtils.refTag("http://www.test.com?ref=activity"))
        assertEquals(null, UrlUtils.refTag("http://www.test.com/path/to"))
        assertEquals("activity", UrlUtils.refTag("http://www.test.com/path/to?ref=activity"))
        assertEquals(null, UrlUtils.refTag("https://www.test.com/path/to"))
        assertEquals("activity", UrlUtils.refTag("https://www.test.com/path/to?ref=activity"))
    }
}
