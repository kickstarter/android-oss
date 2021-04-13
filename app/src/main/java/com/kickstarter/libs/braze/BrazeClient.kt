package com.kickstarter.libs.braze

import android.app.Application
import android.content.Context
import android.util.Log
import com.appboy.Appboy
import com.appboy.AppboyFirebaseMessagingService
import com.appboy.AppboyLifecycleCallbackListener
import com.appboy.configuration.AppboyConfig
import com.appboy.support.AppboyLogger
import com.google.firebase.messaging.RemoteMessage
import com.kickstarter.libs.Build
import com.kickstarter.libs.utils.Secrets

/**
 * Remote PushNotifications specification
 */
interface RemotePushClientType {
    /**
     * Initial configuration requirements
     */
    fun init()

    /**
     * Should return the ID sender specific for each environment
     */
    fun getIdSender(): String

    /**
     * Register a Push token, should be called when
     * FirebaseMessaging.getInstance().token.addOnCompleteListener has a successful new token
     */
    fun registerPushMessages(context: Context, token: String)

    /**
     * Should be called on Firebase.MessageService.onMessageReceived
     *
     * @return true if the message is a braze message
     * @return false if the message is not from braze
     */
    fun handleRemoteMessages(context: Context, message: RemoteMessage): Boolean

    /**
     * Application Lifecycle events Callbacks,
     * Should be registered on Application.OnCreate
     */
    fun getLifeCycleCallbacks(): Application.ActivityLifecycleCallbacks
}

/**
 * Braze client SDK wrapper class.
 * @param context It needs the application context to be initialized,
 * @param build  the type of build will determine the IdSender from Firebase and the logs mode
 */
open class BrazeClient(
    private val context: Context,
    private val build: Build
) : RemotePushClientType {

    override fun init() {

        val appBoyConfig = AppboyConfig.Builder()
            .setIsFirebaseCloudMessagingRegistrationEnabled(true)
            .setFirebaseCloudMessagingSenderIdKey(getIdSender())
            // .setDefaultNotificationChannelName("General") --> TODO: Define notification channels for the new push notifications types
            // .setDefaultNotificationChannelDescription("Braze related push")
            .setHandlePushDeepLinksAutomatically(true)
            .build()
        Appboy.configure(context, appBoyConfig)

        if (this.build.isDebug || Build.isInternal()) {
            AppboyLogger.setLogLevel(Log.VERBOSE)
        }
    }

    override fun getIdSender(): String {
        var senderId = ""
        if (build.isRelease && Build.isExternal()) {
            senderId = Secrets.FirebaseSenderID.PRODUCTION
        }
        if (build.isDebug || Build.isInternal()) {
            senderId = Secrets.FirebaseSenderID.STAGING
        }

        return senderId
    }

    override fun registerPushMessages(context: Context, token: String) =
        Appboy.getInstance(context).registerAppboyPushMessages(token)

    override fun handleRemoteMessages(context: Context, message: RemoteMessage) =
        AppboyFirebaseMessagingService.handleBrazeRemoteMessage(context, message)

    override fun getLifeCycleCallbacks(): Application.ActivityLifecycleCallbacks =
        AppboyLifecycleCallbackListener(true, false)
}
