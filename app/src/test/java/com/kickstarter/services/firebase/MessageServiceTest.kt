package com.kickstarter.services.firebase

import com.google.gson.JsonObject
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.services.ApiException
import com.kickstarter.services.apiresponses.ErrorEnvelope
import io.reactivex.Observable
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.UnknownHostException

class MessageServiceTest : KSRobolectricTestCase() {

    private val refreshHandler = object : RefreshPushToken {}
    private val newToken = ""
    private val gson get() = environment().gson()!!

    private lateinit var successMessages: MutableList<String>
    private lateinit var errorMessages: MutableList<String>

    @Before
    fun before() {
        successMessages = mutableListOf()
        errorMessages = mutableListOf()
    }

    @Test
    fun `test successfully registering push token`() {
        val apiClient = object : MockApiClientV2() {
            override fun registerPushToken(token: String): Observable<JsonObject> {
                return Observable.just(JsonObject())
            }
        }

        refreshHandler.invoke(
            apiClient = apiClient,
            newToken = newToken,
            gson = gson,
            successCallback = { successMessages.add(it) },
            errorCallback = { errorMessages.add(it) }
        )

        assertEquals(1, successMessages.size)
        assertEquals(0, errorMessages.size)
    }

    @Test
    fun `test http 400 error is handled and error callback invoked`() {
        val apiClient = object : MockApiClientV2() {
            override fun registerPushToken(token: String): Observable<JsonObject> {
                return Observable.error(
                    ApiException(
                        ErrorEnvelope.builder().httpCode(400).build(),
                        Response.error<String>(400, "".toResponseBody())
                    )
                )
            }
        }

        refreshHandler.invoke(
            apiClient = apiClient,
            newToken = newToken,
            gson = gson,
            successCallback = { successMessages.add(it) },
            errorCallback = { errorMessages.add(it) }
        )

        assertEquals(0, successMessages.size)
        assertEquals(1, errorMessages.size)
    }

    @Test
    fun `test http 401 error is handled but error callback is not invoked`() {
        val apiClient = object : MockApiClientV2() {
            override fun registerPushToken(token: String): Observable<JsonObject> {
                return Observable.error(
                    ApiException(
                        ErrorEnvelope.builder().httpCode(401).build(),
                        Response.error<String>(401, "".toResponseBody())
                    )
                )
            }
        }

        refreshHandler.invoke(
            apiClient = apiClient,
            newToken = newToken,
            gson = gson,
            successCallback = { successMessages.add(it) },
            errorCallback = { errorMessages.add(it) }
        )

        assertEquals(0, successMessages.size)
        assertEquals(0, errorMessages.size)
    }

    @Test(
        expected = RuntimeException::class
    )
    fun `test intermittent internet connection`() {
        val apiClient = object : MockApiClientV2() {
            override fun registerPushToken(token: String): Observable<JsonObject> {
                return Observable.error(
                    UnknownHostException(
                        "Unable to resolve host \"api.kickstarter.com\": No address associated with hostname"
                    )
                )
            }
        }

        refreshHandler.invoke(
            apiClient = apiClient,
            newToken = newToken,
            gson = gson,
            successCallback = { successMessages.add(it) },
            errorCallback = { errorMessages.add(it) }
        )

        assertEquals(0, successMessages.size)
        assertEquals(0, errorMessages.size)
    }
}
