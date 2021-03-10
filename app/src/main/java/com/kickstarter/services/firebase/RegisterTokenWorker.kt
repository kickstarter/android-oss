package com.kickstarter.services.firebase

import android.content.Context
import android.os.Bundle
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import rx.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class RegisterTokenWorker(@ApplicationContext applicationContext: Context, private val params: WorkerParameters) : Worker(applicationContext, params) {

    @Inject
    lateinit var apiClient: ApiClientType
    @Inject
    lateinit var build: Build
    @Inject
    lateinit var gson: Gson

    private val token = this.params.inputData.getString(IntentKey.PUSH_TOKEN) as String

    override fun doWork(): Result {
        (applicationContext as KSApplication).component().inject(this)
        return handleResponse(this.apiClient
                .registerPushToken(this.token)
                .subscribeOn(Schedulers.io())
                .toBlocking()
                .first())
    }

    private fun handleResponse(response: JsonObject): Result {
        return if (IntegerUtils.isZero(response.size())) {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL)
            logResponse()
            Result.success()
        } else {
            try {
                val errorEnvelope = this.gson.fromJson(response, ErrorEnvelope::class.java)
                logError("📵 Failed to register push token ${errorEnvelope.httpCode()} ${errorEnvelope.errorMessages()?.firstOrNull()}")
                when (errorEnvelope.httpCode()) {
                    in 400..499 -> Result.failure()
                    else -> Result.retry()
                }
            } catch (exception: JsonSyntaxException) {
                logError("📵 Failed to deserialize push token error $response")
                Result.failure()
            }
        }
    }

    private fun logResponse() {
        val successMessage = "📲 Successfully registered push token"
        if (this.build.isDebug) {
            Timber.d(successMessage)
        }
        Firebase.analytics.logEvent("Successfully_registered_push_token", Bundle())
    }

    private fun logError(errorMessage: String) {
        if (this.build.isDebug) {
            Timber.e(errorMessage)
        }
        FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
    }

    companion object {
        private const val TOPIC_GLOBAL = "global"
    }
}
