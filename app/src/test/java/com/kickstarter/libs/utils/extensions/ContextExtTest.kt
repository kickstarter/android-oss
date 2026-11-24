package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class ContextExtTest : KSRobolectricTestCase() {
    @Test
    fun `test getGooglePayConfiguration()`() {
        val context = application()
        val label = "arbitrary test label"
        assertNull(context.getGooglePayConfiguration(false, "US", "USD", label = label))
        assertNull(context.getGooglePayConfiguration(true, null, "USD", label = label))
        assertNull(context.getGooglePayConfiguration(true, " ", "USD", label = label))
        assertNull(context.getGooglePayConfiguration(true, "US", null, label = label))
        assertNull(context.getGooglePayConfiguration(true, "US", " ", label = label))
        assertNotNull(context.getGooglePayConfiguration(true, "DE", "EUR", label = label))
    }
}
