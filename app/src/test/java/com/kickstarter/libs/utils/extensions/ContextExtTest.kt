package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class ContextExtTest : KSRobolectricTestCase() {
    @Test
    fun `test getGooglePayConfiguration()`() {
        val context = application()
        assertNull(context.getGooglePayConfiguration(false, "USD"))
        assertNull(context.getGooglePayConfiguration(true, null))
        assertNull(context.getGooglePayConfiguration(true, " "))
        assertNotNull(context.getGooglePayConfiguration(true, "EUR"))
    }
}
