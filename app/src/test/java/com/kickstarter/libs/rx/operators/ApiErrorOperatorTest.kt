package com.kickstarter.libs.rx.operators

import com.google.gson.Gson
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import java.lang.Exception

class ApiErrorOperatorTest : KSRobolectricTestCase() {
    @Test
    fun testErrorResponse() {
        val gson = Gson()
        val response = PublishSubject.create<Response<Int>>()
        val result = response.lift(Operators.apiError(gson))
        val resultTest = TestSubscriber<Int>()
        result.subscribe(resultTest)
        response.onNext(Response.error(400, ResponseBody.create(null, "")))
        resultTest.assertNoValues()
        assertEquals(1, resultTest.onErrorEvents.size)
    }

    @Test
    fun testErrorResponseErrorBodyJSON() {
        val gson = Gson()
        val response = PublishSubject.create<Response<Int>>()
        val result = response.lift(Operators.apiError(gson))
        val resultTest = TestSubscriber<Int>()
        result.subscribe(resultTest)
        val envelope = builder()
            .ksrCode(ErrorEnvelope.TFA_FAILED)
            .httpCode(400)
            .build()
        val jsonString = Gson().toJson(envelope)
        response.onNext(
            Response.error(
                400,
                jsonString.toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
        )
        resultTest.assertNoValues()
        assertEquals(1, resultTest.onErrorEvents.size)
    }

    @Test
    fun testErrorResponseBadJSON() {
        val gson = Gson()
        val response = PublishSubject.create<Response<Int>>()
        val result = response.lift(Operators.apiError(gson))
        val resultTest = TestSubscriber<Int>()
        result.subscribe(resultTest)
        val message = "{malformed json}"
        val body = message.toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
        response.onNext(Response.error(503, body))
        resultTest.assertNoValues()
        assertEquals(1, resultTest.onErrorEvents.size)
    }

    @Test
    fun testResponseNull() {
        val gson = Gson()
        val response = PublishSubject.create<Response<Int>?>()
        val result = response.lift(Operators.apiError(gson))
        val resultTest = TestSubscriber<Int>()
        result.subscribe(resultTest)
        response.onNext(null)
        resultTest.assertNoValues()
        assertEquals(1, resultTest.onErrorEvents.size)
    }

    @Test
    fun testExceptionErrorResponse() {
        val gson = Gson()
        val response = PublishSubject.create<Response<Int>>()
        val result = response.lift(Operators.apiError(gson))
        val resultTest = TestSubscriber<Int>()
        result.subscribe(resultTest)
        response.onError(Exception())
        resultTest.assertNoValues()
        assertEquals(1, resultTest.onErrorEvents.size)
    }

    @Test
    fun testSuccessResponse() {
        val gson = Gson()
        val response = PublishSubject.create<Response<Int>>()
        val result = response.lift(Operators.apiError(gson))
        val resultTest = TestSubscriber<Int>()
        result.subscribe(resultTest)
        response.onNext(Response.success(42))
        resultTest.assertValues(42)
        resultTest.assertCompleted()
    }
}
