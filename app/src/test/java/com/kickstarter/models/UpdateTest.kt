package com.kickstarter.models

import com.kickstarter.mock.factories.UpdateFactory
import junit.framework.TestCase
import org.junit.Test

class UpdateTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val update = UpdateFactory.update()

        assertEquals(update.id(), 1234L)
        assertEquals(update.body(), "Update body")
        assertEquals(update.isPublic(), true)
        assertEquals(update.projectId(), 5678)
        assertEquals(update.title(), "First update")
        assertEquals(update.sequence(), 11111)
    }

    @Test
    fun testUpdate_equalFalse() {
        val update = Update.builder().build()
        val update2 = Update.builder().body("body2").build()
        val update3 = Update.builder().body("body3").id(5678L).build()
        val update4 = Update.builder().body("body4").build()

        assertFalse(update == update2)
        assertFalse(update == update3)
        assertFalse(update == update4)

        assertFalse(update3 == update2)
        assertFalse(update3 == update4)
    }

    @Test
    fun testUpdate_equalTrue() {
        val update1 = UpdateFactory.update()
        val update2 = UpdateFactory.update()

        assertEquals(update1, update2)
    }
}
