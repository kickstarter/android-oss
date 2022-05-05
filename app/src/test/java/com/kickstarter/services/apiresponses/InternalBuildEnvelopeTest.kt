package com.kickstarter.services.apiresponses

import junit.framework.TestCase
import org.junit.Test

class InternalBuildEnvelopeTest : TestCase() {

    @Test
    fun testInternalBuildEnvelopeDefaultInit() {
        val internalBuildEnvelope = InternalBuildEnvelope.builder().build(343).changelog("changelog").newerBuildAvailable(true).build()

        assertEquals(internalBuildEnvelope.build(), 343)
        assertEquals(internalBuildEnvelope.changelog(), "changelog")
        assertEquals(internalBuildEnvelope.newerBuildAvailable(), true)
    }

    @Test
    fun testInternalBuildEnvelopeEquals_whenFieldsDontMatch_returnsFalse() {
        val internalBuildEnvelope1 = InternalBuildEnvelope.builder()
            .build(343)
            .changelog("changelog1")
            .newerBuildAvailable(true)
            .build()

        val internalBuildEnvelope2 = internalBuildEnvelope1.toBuilder()
            .build(876)
            .build()

        val internalBuildEnvelope3 = internalBuildEnvelope1.toBuilder()
            .changelog("changelog3")
            .build()

        val internalBuildEnvelope4 = internalBuildEnvelope1.toBuilder()
            .newerBuildAvailable(false)
            .build()

        assertFalse(internalBuildEnvelope1 == internalBuildEnvelope2)
        assertFalse(internalBuildEnvelope1 == internalBuildEnvelope3)
        assertFalse(internalBuildEnvelope1 == internalBuildEnvelope4)
        assertFalse(internalBuildEnvelope2 == internalBuildEnvelope3)
        assertFalse(internalBuildEnvelope2 == internalBuildEnvelope4)
        assertFalse(internalBuildEnvelope3 == internalBuildEnvelope4)
    }

    @Test
    fun testInternalBuildEnvelopeEquals_whenFieldsMatch_returnsTrue() {
        val internalBuildEnvelope1 = InternalBuildEnvelope.builder()
            .build(343)
            .changelog("changelog1")
            .newerBuildAvailable(true)
            .build()

        val internalBuildEnvelope2 = internalBuildEnvelope1
        assertTrue(internalBuildEnvelope1 == internalBuildEnvelope2)
    }

    @Test
    fun testProjectsEnvelopToBuilder() {
        val internalBuildEnvelope = InternalBuildEnvelope.builder()
            .build(343)
            .changelog("changelog1")
            .newerBuildAvailable(true)
            .build()
            .toBuilder()
            .changelog("another_changelog")
            .build()

        assertEquals(internalBuildEnvelope.changelog(), "another_changelog")
    }
}
