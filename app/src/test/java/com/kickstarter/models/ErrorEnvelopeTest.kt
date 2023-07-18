package com.kickstarter.models

import com.kickstarter.services.ApiException
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import junit.framework.TestCase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response
import java.lang.Exception

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
        val fbUser1 = ErrorEnvelope.FacebookUser.builder().build()
        val fbUser2 = fbUser1.toBuilder()
            .name("name")
            .email("email")
            .build()

        val envelope = builder()
            .ksrCode(ErrorEnvelope.TFA_REQUIRED)
            .httpCode(403)
            .facebookUser(fbUser1)
            .errorMessages(listOf("Two-factor authentication required."))
            .build()

        val envelope1 = envelope.toBuilder()
            .httpCode(503)
            .facebookUser(fbUser2)
            .build()

        assertNotSame(envelope, envelope1)
        assertEquals(envelope.errorMessage(), envelope1.errorMessage())
        assertNotSame(envelope.facebookUser(), envelope1.facebookUser())
        assertNotSame(envelope.httpCode(), envelope1.httpCode())
        assertEquals(envelope.ksrCode(), envelope1.ksrCode())
    }

    @Test
    fun testEmptyErrorEnvelope() {
        val envelope = builder().build()

        assertEquals(envelope.errorMessage(), "")
        assertEquals(envelope.facebookUser(), null)
        assertEquals(envelope.httpCode(), 0)
        assertEquals(envelope.ksrCode(), "")
    }

    @Test
    fun testEmptyErrorEnvelopeFromThrowable() {
        val envelope = builder().build()

        assertEquals(envelope.errorMessage(), "")
        assertEquals(envelope.facebookUser(), null)
        assertEquals(envelope.httpCode(), 0)
        assertEquals(envelope.ksrCode(), "")

        val throwable = Exception("errorMessage")
        val errorEnvelope = ErrorEnvelope.fromThrowable(throwable)

        assertEquals(errorEnvelope.errorMessage(), "errorMessage")
        assertEquals(errorEnvelope.httpCode(), 0)
        assertEquals(errorEnvelope.ksrCode(), "")
        assertEquals(errorEnvelope.facebookUser(), null)
    }

    @Test
    fun testAPIErrorEnvelopeFromThrowable() {
        val envelope = builder().build()

        assertEquals(envelope.errorMessage(), "")
        assertEquals(envelope.facebookUser(), null)
        assertEquals(envelope.httpCode(), 0)
        assertEquals(envelope.ksrCode(), "")

        val errorResponse = Response.error<Int>(
            400,
            "".toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )

        val throwable = ApiException(envelope, errorResponse)
        val errorEnvelope = ErrorEnvelope.fromThrowable(throwable)

        assertEquals(errorEnvelope.errorMessage(), "")
        assertEquals(errorEnvelope.httpCode(), 0)
        assertEquals(errorEnvelope.ksrCode(), "")
        assertEquals(errorEnvelope.facebookUser(), null)
    }
}
