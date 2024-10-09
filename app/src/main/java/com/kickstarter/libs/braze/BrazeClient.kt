package com.kickstarter.libs.braze

import android.app.Application
import android.content.Context
import android.util.Log
import com.braze.Braze
import com.braze.BrazeActivityLifecycleCallbackListener
import com.braze.configuration.BrazeConfig
import com.braze.push.BrazeFirebaseMessagingService
import com.braze.support.BrazeLogger
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.google.firebase.messaging.RemoteMessage
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.kickstarter.libs.utils.extensions.registerActivityLifecycleCallbacks

/**
 * Remote PushNotifications specification
 */
interface RemotePushClientType {

    /**
     * Will indicate if the internal SDK instance has been initialized
     */
    val isInitialized: Boolean

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
     * should be initialized and configured
     */
    fun isSDKEnabled(): Boolean

    /**
     * Will call internally getLifeCycleCallbacks for the current Application Context
     */
    fun registerActivityLifecycleCallbacks(context: Context)
}

/**
 * Braze client SDK wrapper class.
 * @param context It needs the application context to be initialized,
 * @param build the type of build will determine the IdSender from Firebase and the logs mode
 */
open class BrazeClient(
    private val context: Context,
    private val build: Build
) : RemotePushClientType {

    private var initialized = false

    override val isInitialized: Boolean
        get() = initialized

    init {
        init()
    }

    override fun init() {
        if (isSDKEnabled() && !this.initialized) {
            val appBoyConfig = BrazeConfig.Builder()
                .setIsFirebaseCloudMessagingRegistrationEnabled(true)
                .setFirebaseCloudMessagingSenderIdKey(getIdSender())
                // .setDefaultNotificationChannelName("General") --> TODO: Define notification channels for the new push notifications types
                // .setDefaultNotificationChannelDescription("Braze related push")
                .setHandlePushDeepLinksAutomatically(true)
                .build()
            Braze.configure(context, appBoyConfig)

            if (this.build.isDebug || Build.isInternal()) {
                BrazeLogger.logLevel = Log.VERBOSE
            }

            initialized = true
        }
    }

    override fun isSDKEnabled(): Boolean = true

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
            Braze.getInstance(context).registeredPushToken = token
    }

    override fun handleRemoteMessages(context: Context, message: RemoteMessage): Boolean {
        var handleMessage = false
        if (isSDKEnabled())
            handleMessage = BrazeFirebaseMessagingService.handleBrazeRemoteMessage(context, message)

        return handleMessage
    }

    override fun getLifeCycleCallbacks(): Application.ActivityLifecycleCallbacks =
        BrazeActivityLifecycleCallbackListener(true, false)

    override fun registerActivityLifecycleCallbacks(context: Context) {
        if (isSDKEnabled() && context.isKSApplication()) {
            context.registerActivityLifecycleCallbacks(getLifeCycleCallbacks())
        }
    }

    /**
     * Static initialization for the `setCustomInAppMessageManagerListener`
     * this method should be called once the Segment dependency finalized the integration
     * on the `onIntegrationReady` callback
     */
    companion object {
        fun setInAppCustomListener(currentUser: CurrentUserTypeV2, build: Build) {
            BrazeInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(InAppCustomListener(currentUser, build))
        }
    }
}
