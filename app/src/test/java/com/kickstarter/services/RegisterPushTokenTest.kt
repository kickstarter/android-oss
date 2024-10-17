package com.kickstarter.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.services.firebase.RefreshPushToken
import com.kickstarter.services.firebase.RefreshPushToken.Companion.KO_MESSAGE
import com.kickstarter.services.firebase.RefreshPushToken.Companion.OK_MESSAGE
import io.reactivex.Observable
import org.junit.Test

class RegisterPushTokenTest : KSRobolectricTestCase() {

    @Test
    fun `test with successful response for registerPushToken api call`() {
        val jObj = JsonObject()

        val apiClient = object : MockApiClientV2() {
            override fun registerPushToken(token: String): Observable<JsonObject> {
                jObj.addProperty("token", token)
                return Observable.just(jObj)
            }
        }

        val refreshPushToken = object : RefreshPushToken {}

        var successMessage = ""
        var error = ""
        refreshPushToken.invoke(
            apiClient = apiClient,
            newToken = "a new Token!!",
            gson = Gson(),
            successCallback = { message ->
                successMessage = message
            },
            errorCallback = { errorMessage ->
                error = errorMessage
            }
        )

        assertTrue(error.isEmpty())
        assert(successMessage == "$OK_MESSAGE $jObj")
    }

    @Test
    fun `test with errored response for registerPushToken api call`() {

        val apiClient = object : MockApiClientV2() {
            override fun registerPushToken(token: String): Observable<JsonObject> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val refreshPushToken = object : RefreshPushToken {}

        var successMessage = ""
        var error = ""
        refreshPushToken.invoke(
            apiClient = apiClient,
            newToken = "a new Token!!",
            gson = Gson(),
            successCallback = { message ->
                successMessage = message
            },
            errorCallback = { errorMessage ->
                error = errorMessage
            }
        )

        assertTrue(successMessage.isEmpty())
        val messageToAssert = "$KO_MESSAGE ${ApiExceptionFactory.badRequestException().errorEnvelope().errorMessage()}"
        assertEquals(error, messageToAssert)
    }
}
