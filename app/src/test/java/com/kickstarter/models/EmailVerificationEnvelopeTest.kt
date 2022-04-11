package com.kickstarter.models

import com.kickstarter.services.apiresponses.EmailVerificationEnvelope
import junit.framework.TestCase
import org.junit.Test

class EmailVerificationEnvelopeTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val message = "test"

        val emailVerificationEnvelope = EmailVerificationEnvelope.builder()
            .message(message)
            .code(1)
            .build()

        assertEquals(emailVerificationEnvelope.message(), message)

        assertEquals(emailVerificationEnvelope.code(), 1)
    }

    @Test
    fun testEmailVerificationEnvelope_equalFalse() {
        val message = "test"
        val emailVerificationEnvelope = EmailVerificationEnvelope.builder().build()
        val emailVerificationEnvelope2 =
            EmailVerificationEnvelope.builder().message(message).build()
        val emailVerificationEnvelope3 = EmailVerificationEnvelope.builder().code(3).build()

        assertFalse(emailVerificationEnvelope == emailVerificationEnvelope2)
        assertFalse(emailVerificationEnvelope == emailVerificationEnvelope3)

        assertFalse(emailVerificationEnvelope3 == emailVerificationEnvelope2)
    }

    @Test
    fun testEmailVerificationEnvelope_equalTrue() {
        val emailVerificationEnvelope1 = EmailVerificationEnvelope.builder().build()
        val emailVerificationEnvelope2 = EmailVerificationEnvelope.builder().build()

        assertEquals(emailVerificationEnvelope1, emailVerificationEnvelope2)
    }

    @Test
    fun testEmailVerificationEnvelopeToBuilder() {
        val message = "test"
        val emailVerificationEnvelope = EmailVerificationEnvelope.builder().build().toBuilder()
            .message(message).build()

        assertEquals(emailVerificationEnvelope.message(), message)
    }
}
