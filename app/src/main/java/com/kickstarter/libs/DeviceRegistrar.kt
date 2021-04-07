package com.kickstarter.libs

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.appboy.Appboy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.utils.PlayServicesCapability
import com.kickstarter.libs.utils.WorkUtils
import com.kickstarter.services.firebase.RegisterTokenWorker
import com.kickstarter.services.firebase.UnregisterTokenWorker
import com.kickstarter.ui.IntentKey
import java.util.concurrent.TimeUnit

class DeviceRegistrar(
    private val playServicesCapability: PlayServicesCapability,
    @param:ApplicationContext @field:ApplicationContext private val context: Context
) : DeviceRegistrarType {

    /**
     * If Play Services is available on this device, start a service to register it in the backend.
     */
    override fun registerDevice() {
        if (this.playServicesCapability.isCapable) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(
                OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        task.exception?.fillInStackTrace()?.let {
                            FirebaseCrashlytics.getInstance().recordException(it)
                        }
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result
                    registerToken(this.context, token)

                    // Braze register push notification token
                    Appboy.getInstance(this.context).registerAppboyPushMessages(token);
                }
            )
        }
    }

    /**
     * If Play Services is available on this device, delete the existing token.
     */
    override fun unregisterDevice() {
        if (this.playServicesCapability.isCapable) {
            unregisterToken()
        }
    }

    private fun unregisterToken() {
        val request = OneTimeWorkRequestBuilder<UnregisterTokenWorker>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY, TimeUnit.SECONDS)
            .setConstraints(WorkUtils.baseConstraints)
            .build()

        WorkManager.getInstance(this.context)
            .enqueueUniqueWork(UNREGISTER_TOKEN, ExistingWorkPolicy.REPLACE, request)
    }

    companion object {
        private const val BACKOFF_DELAY = 30L
        private const val REGISTER_TOKEN = "register_push_token"
        const val UNREGISTER_TOKEN = "unregister_push_token"

        fun registerToken(context: Context, token: String) {
            val data = workDataOf(IntentKey.PUSH_TOKEN to token)

            val request = OneTimeWorkRequestBuilder<RegisterTokenWorker>()
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY, TimeUnit.SECONDS)
                .setConstraints(WorkUtils.baseConstraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(REGISTER_TOKEN, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
