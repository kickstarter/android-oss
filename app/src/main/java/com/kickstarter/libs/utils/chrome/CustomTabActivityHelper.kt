package com.kickstarter.libs.utils.chrome

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession


/**
 * This is a helper class to manage the customTabsServiceConnection to the Custom Tabs Service.
 */
class CustomTabActivityHelper : ServiceConnectionCallback {
    private var customTabsSession: CustomTabsSession? = null
    private var customTabsClient: CustomTabsClient? = null
    private var customTabsServiceConnection: CustomTabsServiceConnection? = null
    private var connectionCallback: ConnectionCallback? = null

    /**
     * Creates or retrieves an exiting CustomTabsSession.
     *
     * @return a CustomTabsSession.
     */
    val session: CustomTabsSession?
        get() {
            if (customTabsClient == null) {
                customTabsSession = null
            } else if (customTabsSession == null) {
                customTabsSession = customTabsClient!!.newSession(null)
            }
            return customTabsSession
        }

    /**
     * Unbinds the Activity from the Custom Tabs Service.
     * @param activity the activity that is connected to the service.
     */
    fun unbindCustomTabsService(activity: Activity) {
        if (customTabsServiceConnection == null) return
        activity.unbindService(customTabsServiceConnection)
        customTabsClient = null
        customTabsSession = null
        customTabsServiceConnection = null
    }

    /**
     * Binds the Activity to the Custom Tabs Service.
     * @param activity the activity to be binded to the service.
     */
    fun bindCustomTabsService(activity: Activity) {
        if (customTabsClient != null) return

        val packageName = CustomTabsHelper.getPackageNameToUse(activity) ?: return

        customTabsServiceConnection = ServiceConnection(this)
        CustomTabsClient.bindCustomTabsService(activity, packageName, customTabsServiceConnection)
    }

    override fun onServiceConnected(client: CustomTabsClient) {
        customTabsClient = client
        customTabsClient!!.warmup(0L)
        if (connectionCallback != null) connectionCallback!!.onCustomTabsConnected()
    }

    override fun onServiceDisconnected() {
        customTabsClient = null
        customTabsSession = null
        if (connectionCallback != null) connectionCallback!!.onCustomTabsDisconnected()
    }

    /**
     * A Callback for when the service is connected or disconnected. Use those callbacks to
     * handle UI changes when the service is connected or disconnected.
     */
    interface ConnectionCallback {
        /**
         * Called when the service is connected.
         */
        fun onCustomTabsConnected()

        /**
         * Called when the service is disconnected.
         */
        fun onCustomTabsDisconnected()
    }

    /**
     * To be used as a fallback to open the Uri when Custom Tabs is not available.
     */
    interface CustomTabFallback {
        /**
         *
         * @param activity The Activity that wants to open the Uri.
         * @param uri The uri to be opened by the fallback.
         */
        fun openUri(activity: Activity, uri: Uri)
    }

    companion object {

        /**
         * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
         *
         * @param activity The host activity.
         * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
         * @param uri the Uri to be opened.
         * @param fallback a CustomTabFallback to be used if Custom Tabs is not available.
         */
        fun openCustomTab(activity: Activity,
                          customTabsIntent: CustomTabsIntent,
                          uri: Uri,
                          fallback: CustomTabFallback?) {
            val packageName = CustomTabsHelper.getPackageNameToUse(activity)

            //If we cant find a package name, it means there's no browser that supports
            //Chrome Custom Tabs installed. So, we fallback to the webview
            if (packageName == null) {
                fallback?.openUri(activity, uri)
            } else {
                customTabsIntent.intent.setPackage(packageName)
                customTabsIntent.launchUrl(activity, uri)
            }
        }
    }

}