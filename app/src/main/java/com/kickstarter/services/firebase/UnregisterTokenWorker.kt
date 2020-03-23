package com.kickstarter.services.firebase

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.crashlytics.android.Crashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.services.ApiClientType
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class UnregisterTokenWorker(@ApplicationContext applicationContext: Context, private val params: WorkerParameters) : Worker(applicationContext, params) {
    @Inject
    lateinit var build: Build
    @Inject
    lateinit var apiClient: ApiClientType

    override fun doWork(): Result {
        (applicationContext as KSApplication).component().inject(this)
        return try {
            FirebaseInstanceId.getInstance().deleteToken(FirebaseInstanceId.getInstance().id, FirebaseMessaging.INSTANCE_ID_SCOPE)
            logSuccess()
            Result.success()
        } catch (e: IOException){
            logError(e)
            Result.failure()
        }
    }

    private fun logSuccess() {
        val successMessage = "Successfully unregistered push token"
        if (this.build.isDebug) {
            Timber.d(successMessage)
        }
        Crashlytics.log(successMessage)
    }

    private fun logError(ioException: IOException) {
        if (this.build.isDebug) {
            Timber.e(ioException.localizedMessage)
        }
        Crashlytics.logException(ioException)
    }
}
