package com.kickstarter.models.chrome

// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// https://github.com/GoogleChrome/custom-tabs-client

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import com.kickstarter.libs.utils.extensions.isNotNull
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber

class ChromeTabsHelperActivity {
    companion object {

        /**
         * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
         *
         * @param activity The host activity.
         * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
         * @param uri the Uri to be opened.
         * @param fallback a CustomTabFallback to be used if Custom Tabs is not available.
         */
        fun openCustomTab(
            activity: Activity,
            customTabsIntent: CustomTabsIntent,
            uri: Uri,
            fallback: CustomTabFallback?
        ) {
            val packageName = ChromeTabsHelper.getPackageNameToUse(activity)

            // If we can't find a package name, it means there's no browser that supports Chrome Custom Tabs installed.
            if (packageName != null) {
                customTabsIntent.intent.setPackage(packageName)
                customTabsIntent.launchUrl(activity, uri)
            } else {
                if (fallback != null) {
                    fallback.openUri(activity, uri)
                } else {
                    // With no fallback, default to opening in the browser.
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    activity.startActivity(intent)
                }
            }
        }
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

    /**
     * Helper class that will instantiate CustomTabsServiceConnection, once CustomTabsClient.bindCustomTabsService
     * has been called in host activity `onCustomTabsServiceConnected` will
     * provide the CustomTabsClient instance.
     *
     * CustomTabsClient will have onNavigationEvents callbacks, when detected TAB_HIDDEN
     * navigation event, execute callback parameter
     *
     * Trying to call `getSession` before isSessionReady emission
     * on `onCustomTabsServiceConnected` is true will result in null values.
     *
     * @param tabHiddenCallback the callback to be executed once the TAB_HIDDEN
     * navigation event has been detected
     */
    class CustomTabSessionAndClientHelper(
        context: Activity,
        uri: Uri,
        tabHiddenCallback: () -> Unit
    ) {
        private var customClient: CustomTabsClient? = null
        // - Do not expose mutable types
        private var sessionReady: MutableSharedFlow<Boolean> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        private val isSessionReady: Flow<Boolean> = sessionReady
        private var session: CustomTabsSession? = null

        private val callback = object : CustomTabsCallback() {
            override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                Timber.d("OAuth onNavigationEvent: Code = $navigationEvent")
                // - means the X button has been clicked, therefore ChromeTab has been dismissed
                if (navigationEvent == TAB_HIDDEN) {
                    tabHiddenCallback()
                }
            }
        }

        private val connection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            /**
             * Using `CustomTabsClient.warmup` and `CustomTabsSession.mayLaunchUrl` will make Custom Tabs pre-fetch the page and pre-render.
             * `CustomTabsClient.warmup` has no impact on performance
             * `CustomTabsSession.mayLaunchUrl` comes with a network and battery cost, avoid on non critical user journeys.
             */
            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                customClient = client
                customClient?.warmup(0)
                session = customClient?.newSession(callback)
                session?.mayLaunchUrl(uri, null, null)
                sessionReady.tryEmit(session.isNotNull())
                Timber.d("onCustomTabsServiceConnected")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Timber.d("onServiceDisconnected")
                customClient = null
                session = null
            }
        }

        init {
            // - Bind the connection to the system service be able to listen to navigation events
            CustomTabsClient.bindCustomTabsService(
                context,
                ChromeTabsHelper.getPackageNameToUse(context = context) ?: "",
                connection
            )
        }

        /**
         * Trying to call `getSession` before isSessionReady emission
         * on `onCustomTabsServiceConnected` is true will result in null values.
         */
        fun getSession() = session
        fun getConnection() = connection
        /**
         * Will emmit true once `onCustomTabsServiceConnected`
         * has provided a new CustomTabsClient and the session is ready
         */
        fun isSessionReady() = isSessionReady
    }
}
