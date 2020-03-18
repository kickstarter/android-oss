package com.kickstarter.services

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

abstract class TrackingWorker(@ApplicationContext applicationContext: Context, private val params: WorkerParameters) : Worker(applicationContext, params) {
    @Inject
    lateinit var build: Build

    private val eventName = this.params.inputData.getString(IntentKey.EVENT_NAME) as String
    private val tag = this.params.inputData.getString(IntentKey.TRACKING_CLIENT_TYPE_TAG) as String

    protected val eventData = this.params.inputData.getString(IntentKey.EVENT_DATA) as String

    protected fun handleResult(response: Response<ResponseBody>): Result {
        return if (response.isSuccessful) {
            logResponse(response)
            Result.success()
        } else {
            logTrackingError(eventName)
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
            logTrackingError(eventName)
        }
    }

    private fun logTrackingError(eventName: String) {
        if (this.build.isDebug) {
            Timber.e("Failed to track $tag event: $eventName")
        }
        Crashlytics.logException(Exception("Failed to track $tag event: $eventName"))
    }
}
