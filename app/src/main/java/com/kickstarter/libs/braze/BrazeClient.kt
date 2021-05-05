package com.kickstarter.libs.braze

import android.app.Application
import android.content.Context
import android.util.Log
import com.appboy.Appboy
import com.appboy.AppboyFirebaseMessagingService
import com.appboy.AppboyLifecycleCallbackListener
import com.appboy.configuration.AppboyConfig
import com.appboy.models.IInAppMessage
import com.appboy.models.MessageButton
import com.appboy.support.AppboyLogger
import com.appboy.ui.inappmessage.AppboyInAppMessageManager
import com.appboy.ui.inappmessage.InAppMessageCloser
import com.appboy.ui.inappmessage.InAppMessageOperation
import com.appboy.ui.inappmessage.listeners.AppboyDefaultInAppMessageManagerListener
import com.google.firebase.messaging.RemoteMessage
import com.kickstarter.libs.Build
import com.kickstarter.libs.Config
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.utils.ConfigFeatureName
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.kickstarter.libs.utils.extensions.registerActivityLifecycleCallbacks
import timber.log.Timber

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
 * @param configuration current configuration, it will be used mainly to check feature flag enable/disable
 */
open class BrazeClient(
    private val context: Context,
    private val build: Build,
    private val configuration: CurrentConfigType
) : RemotePushClientType {

    private var config: Config? = null
    private var initialized = false

    override val isInitialized: Boolean
        get() = initialized

    init {
        this.configuration.observable()
            .distinctUntilChanged()
            .subscribe { c ->
                // - Cache the most recent config
                this.config = c
                initialize()
            }
    }

    /**
     * - Do not initialize Braze SDK until the current configuration is loaded
     * and it has not previously being initialized
     */
    private fun initialize() {
        if (isSDKEnabled() && this.context.isKSApplication() && !this.isInitialized) {
            this.init()
        }
    }

    override fun init() {
        if (isSDKEnabled() && !this.initialized) {
            val appBoyConfig = AppboyConfig.Builder()
                .setIsFirebaseCloudMessagingRegistrationEnabled(true)
                .setFirebaseCloudMessagingSenderIdKey(getIdSender())
                // .setDefaultNotificationChannelName("General") --> TODO: Define notification channels for the new push notifications types
                // .setDefaultNotificationChannelDescription("Braze related push")
                .setHandlePushDeepLinksAutomatically(true)
                .build()
            Appboy.configure(context, appBoyConfig)
            Timber.d("AppBoy configured by me")

            if (this.build.isDebug || Build.isInternal()) {
                AppboyLogger.setLogLevel(Log.VERBOSE)
            }

            initialized = true
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
        Appboy.getInstance(context).registerAppboyPushMessages(token)
    }

    override fun handleRemoteMessages(context: Context, message: RemoteMessage): Boolean {
        var handleMessage = false
        if (isSDKEnabled())
            handleMessage = AppboyFirebaseMessagingService.handleBrazeRemoteMessage(context, message)

        return handleMessage
    }

    override fun getLifeCycleCallbacks(): Application.ActivityLifecycleCallbacks =
        AppboyLifecycleCallbackListener(true, true)

    override fun registerActivityLifecycleCallbacks(context: Context) {
        context.registerActivityLifecycleCallbacks(getLifeCycleCallbacks())
        AppboyInAppMessageManager.getInstance().setCustomControlInAppMessageManagerListener(Control())
    }

    private class Control(): AppboyDefaultInAppMessageManagerListener() {
        override fun beforeInAppMessageDisplayed(inAppMessage: IInAppMessage?): InAppMessageOperation {
            Timber.d("Display Always")
            return InAppMessageOperation.DISPLAY_NOW;
        }

        override fun onInAppMessageButtonClicked(inAppMessage: IInAppMessage?, button: MessageButton?, inAppMessageCloser: InAppMessageCloser?): Boolean {
            Timber.d("Button Clicked")
            return super.onInAppMessageButtonClicked(inAppMessage, button, inAppMessageCloser)
        }
    }
}
