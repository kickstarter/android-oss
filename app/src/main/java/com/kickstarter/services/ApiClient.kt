package com.kickstarter.services

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.kickstarter.libs.Config
import com.kickstarter.services.apiresponses.ErrorEnvelope
import retrofit2.Response
import retrofit2.http.GET
import kotlin.coroutines.cancellation.CancellationException

interface ApiService {
    @GET("/v1/app/android/config")
    suspend fun getConfigResponse(): Response<Config>
}

interface ApiClientType {
    suspend fun getConfig(): Result<Config>
}

class ApiClient(
    private val service: ApiService,
    private val gson: Gson
) : ApiClientType {

    override suspend fun getConfig(): Result<Config> =
        executeHttpForResult { service.getConfigResponse() }

    class EmptyBodyException(val response: Response<*>) :
        IllegalStateException("Response body was null for a successful request")

    private suspend inline fun <reified T> executeHttpForResult(
        crossinline call: suspend () -> Response<T>
    ): Result<T> =
        try {
            // - execute call
            val response = call()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    val ex = EmptyBodyException(response)
                    FirebaseCrashlytics.getInstance().recordException(ex)
                    Result.failure(ex)
                }
            } else {
                try {
                    val env = gson.fromJson(response.errorBody()?.string(), ErrorEnvelope::class.java)
                    val apiException = ApiException(env, response)
                    FirebaseCrashlytics.getInstance().recordException(apiException)
                    Result.failure(apiException)
                } catch (e: Exception) {
                    throw e
                }
            }
        } catch (ce: CancellationException) {
            throw ce // - Always relaunch Cancellation exceptions
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }
}

/**
 * Testing Mock for ApiClient, meant to be use at VM tests level
 */
class MockApiClient() : ApiClientType {
    override suspend fun getConfig(): Result<Config> {
        TODO("Not yet implemented")
    }
}
