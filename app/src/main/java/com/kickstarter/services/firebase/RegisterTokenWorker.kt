package com.kickstarter.services.firebase

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.crashlytics.android.Crashlytics
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
                logError(errorEnvelope)
                when (errorEnvelope.httpCode()) {
                    in 400..499 -> Result.failure()
                    else -> Result.retry()
                }
            } catch (exception: JsonSyntaxException) {
                Result.failure()
            }
        }
    }

    private fun logResponse() {
        val successMessage = "🔌 Successfully registered push token"
        if (this.build.isDebug) {
            Timber.d(successMessage)
        }
        Crashlytics.log(successMessage)
    }

    private fun logError(errorEnvelope: ErrorEnvelope) {
        val errorMessage = "📵 Failed to register push token ${errorEnvelope.httpCode()} ${errorEnvelope.errorMessages()?.firstOrNull()}"
        if (this.build.isDebug) {
            Timber.e(errorMessage)
        }
        Crashlytics.logException(Exception(errorMessage))
    }

    companion object {
        private const val TOPIC_GLOBAL = "global"
    }
}
