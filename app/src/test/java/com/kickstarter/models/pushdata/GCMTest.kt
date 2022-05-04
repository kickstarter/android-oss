package com.kickstarter.models.pushdata

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class GCMTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val gcm = GCM.builder()
            .alert("You've received a new push notification")
            .title("Hello")
            .build()

        assertEquals(gcm.title(), "Hello")
        assertEquals(gcm.alert(), "You've received a new push notification")
    }

    @Test
    fun testDefaultToBuilder() {
        val gcm1 =
            GCM.builder()
                .alert("You've received a new push notification")
                .title("Hello").build()

        val gcm2 = gcm1.toBuilder().title("test").build()
        assertTrue(gcm2.title() == "test")
    }

    @Test
    fun testGCM_equalFalse() {
        val gcm =
            GCM.builder()
                .alert("You've received a new push notification")
                .build()

        val gcm2 = GCM.builder().title("Hello").build()

        val gcm3 =
            GCM.builder()
                .build()

        assertFalse(gcm == gcm2)
        assertFalse(gcm == gcm3)

        assertFalse(gcm3 == gcm2)
    }

    @Test
    fun testGCM_equalsTrue() {
        val gcm1 =
            GCM.builder().build()

        val gcm2 = GCM.builder().build()

        assertTrue(gcm1 == gcm2)
    }
}
