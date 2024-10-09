package com.kickstarter.services.firebase

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.ui.IntentKey
import javax.inject.Inject

class RegisterTokenWorker(@ApplicationContext applicationContext: Context, private val params: WorkerParameters) : Worker(applicationContext, params) {

    @Inject
    lateinit var apiClient: ApiClientTypeV2

    @Inject
    lateinit var build: Build

    @Inject
    lateinit var gson: Gson

    private val token = this.params.inputData.getString(IntentKey.PUSH_TOKEN) as String

    override fun doWork(): Result {
        (applicationContext as KSApplication).component().inject(this)
        val refreshHandler = object : RefreshPushToken {}
        var result: Result = Result.failure()
        refreshHandler.invoke(
            apiClient = apiClient,
            newToken = this.token,
            gson = gson,
            successCallback = { message ->
                FirebaseCrashlytics.getInstance().log(message)
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL)
                result = Result.success()
            },
            errorCallback = { errorMessage ->
                FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                result = Result.failure()
            }
        )

        return result
    }

    companion object {
        private const val TOPIC_GLOBAL = "global"
    }
}
