package com.kickstarter.services.firebase

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.services.ApiClientType
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class ResetDeviceIdWorker(@ApplicationContext applicationContext: Context, params: WorkerParameters) : Worker(applicationContext, params) {
    @Inject
    lateinit var build: Build
    @Inject
    lateinit var apiClient: ApiClientType

    override fun doWork(): Result {
        (applicationContext as KSApplication).component().inject(this)
        return try {
            FirebaseInstanceId.getInstance().deleteInstanceId()
            logSuccess()
            applicationContext.sendBroadcast(Intent(BROADCAST))
            Result.success()
        } catch (e: IOException) {
            logError(e)
            Result.failure()
        }
    }

    private fun logSuccess() {
        val successMessage = "Successfully reset Firebase device ID"
        if (this.build.isDebug) {
            Timber.d(successMessage)
        }
        FirebaseCrashlytics.getInstance().log(successMessage)
    }

    private fun logError(ioException: IOException) {
        if (this.build.isDebug) {
            Timber.e(ioException.localizedMessage)
        }
        FirebaseCrashlytics.getInstance().recordException(ioException)
    }

    companion object {
        const val BROADCAST = "reset_device_id_success"
        const val TAG = "ResetDeviceIdWorker"
    }
}
