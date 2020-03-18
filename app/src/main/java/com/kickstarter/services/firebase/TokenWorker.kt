package com.kickstarter.services.firebase

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.crashlytics.android.Crashlytics
import com.kickstarter.libs.Build
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.ui.IntentKey
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class TokenWorker(@ApplicationContext applicationContext: Context, private val params: WorkerParameters) : Worker(applicationContext, params) {
    @Inject
    lateinit var build: Build

    private val register = this.params.inputData.getBoolean(IntentKey.EVENT_NAME, false)

    protected fun handleResult(response: Response<ResponseBody>): Result {
        return if (response.isSuccessful) {
            logResponse(response)
            Result.success()
        } else {
            logError(eventName)
            Result.retry()
        }
    }

    private fun logResponse(it: Response<ResponseBody>) {
        if (it.isSuccessful) {
            if (this.build.isDebug) {
                Timber.d("Successfully tracked $tag event: $eventName")
            }
            Crashlytics.log(eventName)
        } else {
            logError(eventName)
        }
    }

    private fun logTrackingError(eventName: String) {
        if (this.build.isDebug) {
            Timber.e("Failed to track $tag event: $eventName")
        }
        Crashlytics.logException(Exception("Failed to track $tag event: $eventName"))
    }
}
