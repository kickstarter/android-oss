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
    suspend fun getConfig(): Config

    @GET("/v1/app/android/config")
    suspend fun getConfigResponse(): Response<Config>
}

class MockApiClient() : ApiClientType {
    override suspend fun getConfig(): Result<Config> {
        TODO("Not yet implemented")
    }
}

interface ApiClientType {
    suspend fun getConfig(): Result<Config>
}

class ApiClient(
    private val service: ApiService,
    private val gson: Gson
) : ApiClientType {
    // TODO: generic error handling like in Apollo, trying to use some of the
    // defined error classes with the else branch on :54
    private suspend fun <T> executeForResult(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }

    override suspend fun getConfig(): Result<Config> {
        val response = service.getConfigResponse()

        return if (response.isSuccessful)
            response.body()?.let { config ->
                Result.success(config)
            } ?: Result.failure(Exception())
        else {
            val envelope: ErrorEnvelope? = try {
                gson.fromJson(response.errorBody()?.string(), ErrorEnvelope::class.java)
            } catch (e: Exception) {
                null
            }
            envelope?.let {
                Result.failure(ApiException(envelope, response))
            } ?: Result.failure(ResponseException(response))
        }
    }
}
