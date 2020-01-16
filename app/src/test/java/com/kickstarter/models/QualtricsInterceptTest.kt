package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import junit.framework.TestCase
import org.junit.Test

class QualtricsInterceptTest : KSRobolectricTestCase() {
    @Test
    fun testId() {
        TestCase.assertEquals("SI_3VjP7wWiUZg2wBf", QualtricsIntercept.NATIVE_APP_FEEDBACK.id("com.kickstarter.kickstarter"))
        TestCase.assertEquals("SI_6nSwomRDiWXeXEV", QualtricsIntercept.NATIVE_APP_FEEDBACK.id("com.kickstarter.kickstarter.debug"))
        TestCase.assertEquals("SI_6nSwomRDiWXeXEV", QualtricsIntercept.NATIVE_APP_FEEDBACK.id("com.kickstarter.kickstarter.internal"))
        TestCase.assertEquals("SI_6nSwomRDiWXeXEV", QualtricsIntercept.NATIVE_APP_FEEDBACK.id("com.kickstarter.kickstarter.internal.debug"))
        TestCase.assertEquals("SI_6nSwomRDiWXeXEV", QualtricsIntercept.NATIVE_APP_FEEDBACK.id("boop"))
    }

    @Test
    fun testImpressionCountKey() {
        TestCase.assertEquals("SI_3VjP7wWiUZg2wBf_impression_count", QualtricsIntercept.NATIVE_APP_FEEDBACK.impressionCountKey("com.kickstarter.kickstarter"))
        TestCase.assertEquals("SI_6nSwomRDiWXeXEV_impression_count", QualtricsIntercept.NATIVE_APP_FEEDBACK.impressionCountKey("com.kickstarter.kickstarter.debug"))
        TestCase.assertEquals("SI_6nSwomRDiWXeXEV_impression_count", QualtricsIntercept.NATIVE_APP_FEEDBACK.impressionCountKey("com.kickstarter.kickstarter.internal"))
        TestCase.assertEquals("SI_6nSwomRDiWXeXEV_impression_count", QualtricsIntercept.NATIVE_APP_FEEDBACK.impressionCountKey("com.kickstarter.kickstarter.internal.debug"))
        TestCase.assertEquals("SI_6nSwomRDiWXeXEV_impression_count", QualtricsIntercept.NATIVE_APP_FEEDBACK.impressionCountKey("boop"))
    }
}
