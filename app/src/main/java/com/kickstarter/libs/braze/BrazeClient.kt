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
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.Config
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.utils.ConfigFeatureName
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled

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

    /**
     * Flag that will indicate if the internal instance of the SDK
     * should be initialized
     */
    fun isSDKEnabled(): Boolean

    /**
     * Will call internally getLifeCycleCallbacks for the current Application Context
     */
    fun registerActivityLifecycleCallbacks()
}

/**
 * Braze client SDK wrapper class.
 * @param context It needs the application context to be initialized,
 * @param build the type of build will determine the IdSender from Firebase and the logs mode
 * @param configObserver current configuration, it will be used mainly to check feature flag enable/disable
 */
open class BrazeClient(
    private val context: Context,
    private val build: Build,
    private val configObserver: CurrentConfigType
) : RemotePushClientType {

    private var config: Config? = null

    init {
        // - Cache the most recent config
        this.configObserver.observable()
            .subscribe { c ->
                this.config = c
            }
    }

    override fun init() {
        if (isSDKEnabled()) {
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
    }

    override fun isSDKEnabled(): Boolean =
        config?.isFeatureFlagEnabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName) ?: false

    override fun getIdSender(): String {
        var senderId = ""

        if (isSDKEnabled()) {
            if (build.isRelease && Build.isExternal()) {
                senderId = Secrets.FirebaseSenderID.PRODUCTION
            }
            if (build.isDebug || Build.isInternal()) {
                senderId = Secrets.FirebaseSenderID.STAGING
            }
        }

        return senderId
    }

    override fun registerPushMessages(context: Context, token: String) {
        if (isSDKEnabled())
            Appboy.getInstance(context).registerAppboyPushMessages(token)
    }

    override fun handleRemoteMessages(context: Context, message: RemoteMessage): Boolean {
        var handleMessage = false
        if (isSDKEnabled())
            handleMessage = AppboyFirebaseMessagingService.handleBrazeRemoteMessage(context, message)

        return handleMessage
    }

    override fun getLifeCycleCallbacks(): Application.ActivityLifecycleCallbacks =
        AppboyLifecycleCallbackListener(true, false)

    override fun registerActivityLifecycleCallbacks() {
        if (isSDKEnabled() && (this.context is KSApplication)) {
            this.context.registerActivityLifecycleCallbacks(getLifeCycleCallbacks())
        }
    }
}
