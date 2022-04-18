package com.kickstarter.models

import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import junit.framework.TestCase
import org.junit.Test

class ErrorEnvelopeTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val envelope = builder()
            .ksrCode(ErrorEnvelope.TFA_FAILED)
            .httpCode(400)
            .build()

        val envelope1 = ErrorEnvelope.builder().build()

        assertEquals(envelope.errorMessage(), envelope1.errorMessage())
        assertEquals(envelope.facebookUser(), envelope1.facebookUser())
        assertNotSame(envelope.httpCode(), envelope1.httpCode())
        assertNotSame(envelope.ksrCode(), envelope1.ksrCode())
        assertNotSame(envelope, envelope1)
    }

    @Test
    fun testEquals() {
        val envelope = builder()
            .ksrCode(ErrorEnvelope.TFA_REQUIRED)
            .httpCode(403)
            .errorMessages(listOf("Two-factor authentication required."))
            .build()

        val envelope1 = envelope

        assertEquals(envelope, envelope1)
        assertEquals(envelope.errorMessage(), envelope1.errorMessage())
        assertEquals(envelope.facebookUser(), envelope1.facebookUser())
        assertEquals(envelope.httpCode(), envelope1.httpCode())
        assertEquals(envelope.ksrCode(), envelope1.ksrCode())
    }

    @Test
    fun testEqualsNotEquals() {
        val envelope = builder()
            .ksrCode(ErrorEnvelope.TFA_REQUIRED)
            .httpCode(403)
            .errorMessages(listOf("Two-factor authentication required."))
            .build()

        val envelope1 = envelope.toBuilder()
            .httpCode(503)
            .build()

        assertNotSame(envelope, envelope1)
        assertEquals(envelope.errorMessage(), envelope1.errorMessage())
        assertEquals(envelope.facebookUser(), envelope1.facebookUser())
        assertNotSame(envelope.httpCode(), envelope1.httpCode())
        assertEquals(envelope.ksrCode(), envelope1.ksrCode())
    }
}
