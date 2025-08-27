package com.kickstarter.services

import com.google.gson.Gson
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.builder
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class ApiClientTest {

    @get:Rule
    val mockWebServer = MockWebServer()

    private lateinit var apiClient: ApiClient
    private lateinit var apiService: ApiService
    private val gson = Gson()

    @Before
    fun setUp() {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)
        apiClient = ApiClient(apiService, gson)
    }

    @Test
    fun `getConfig success - returns Config object`() = runTest {
        val config = ConfigFactory.config()
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(gson.toJson(config))
        mockWebServer.enqueue(mockResponse)

        // Execute call
        val result = apiClient.getConfig()

        // Assert result
        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull(), config)

        // Verify the request made to the server
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals(recordedRequest.method, "GET")
        assertEquals(recordedRequest.path, "/v1/app/android/config")
    }

    @Test
    fun `getConfig network timeout - returns failure`() = runTest {
        val config = ConfigFactory.config()

        val mockResponse = MockResponse()
            .setBodyDelay(10, TimeUnit.SECONDS) // Delay longer than OkHttp timeout
            .setResponseCode(200)
            .setBody(gson.toJson(config))
        mockWebServer.enqueue(mockResponse)

        val result = apiClient.getConfig()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IOException)
    }

    @Test
    fun `getConfig when coroutine is cancelled - throws CancellationException`() = runTest {
        val config = ConfigFactory.config()

        val mockResponse = MockResponse()
            .setBodyDelay(5, TimeUnit.SECONDS)
            .setResponseCode(200)
            .setBody(gson.toJson(config))
        mockWebServer.enqueue(mockResponse)

        // Act
        val job = launch { // Launch in a separate job to cancel it
            try {
                apiClient.getConfig()
                fail("Should have thrown CancellationException")
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Expected
                assertTrue(e is CancellationException)
            } catch (e: Exception) {
                fail("Caught unexpected exception: $e")
            }
        }
        job.cancelAndJoin() // Cancel the job and wait for it to complete
    }

    @Test
    fun `getConfig error 503 with empty error body - returns failure with ApiException`() = runTest {
        val envelope = builder()
            .ksrCode(ErrorEnvelope.TFA_FAILED)
            .httpCode(400)
            .build()
        val jsonString = gson.toJson(envelope)

        val mockResponse = MockResponse()
            .setResponseCode(400)
            .setBody(jsonString)
        mockWebServer.enqueue(mockResponse)

        val result = apiClient.getConfig()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is ApiException)
        val apiException = exception as ApiException
        assertNotNull(apiException.errorEnvelope())
        assertEquals(apiException.response().code(), 400)
    }

    @Test
    fun `mal formed json error`() = runTest {
        val message = "{malformed json}"
        val jsonString = gson.toJson(message)

        val mockResponse = MockResponse()
            .setResponseCode(503)
            .setBody(jsonString)
        mockWebServer.enqueue(mockResponse)

        val result = apiClient.getConfig()
        assertTrue(result.isFailure)

        val exception = result.exceptionOrNull()
        assertTrue(exception is Exception)
    }
}
