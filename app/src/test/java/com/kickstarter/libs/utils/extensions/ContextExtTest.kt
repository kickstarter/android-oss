package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class ContextExtTest : KSRobolectricTestCase() {
    @Test
    fun `test getGooglePayConfiguration()`() {
        val context = application()
        assertNull(context.getGooglePayConfiguration(false, "US", "USD"))
        assertNull(context.getGooglePayConfiguration(true, null, "USD"))
        assertNull(context.getGooglePayConfiguration(true, " ", "USD"))
        assertNull(context.getGooglePayConfiguration(true, "US", null))
        assertNull(context.getGooglePayConfiguration(true, "US", " "))
        assertNotNull(context.getGooglePayConfiguration(true, "DE", "EUR"))
    }
}
