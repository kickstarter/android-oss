package com.kickstarter.ui.activities

import android.app.Activity
import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.lifecycle.lifecycleScope
import com.kickstarter.R
import com.kickstarter.models.chrome.ChromeTabsHelper
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OAuthActivity : AppCompatActivity() {

    private var customClient: CustomTabsClient? = null
    private var sessionReady: MutableSharedFlow<Boolean> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private var session: CustomTabsSession? = null

    val callback = object : CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            // means the X button has been clicked, therefore ChromeTab has been dismissed
            if (navigationEvent == TAB_HIDDEN) {
                finishWithAnimation()
            }
            Timber.d(this.javaClass.canonicalName, "onNavigationEvent: Code = $navigationEvent")
        }
    }
    val connection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(
            name: ComponentName,
            client: CustomTabsClient
        ) {
            customClient = client
            customClient?.warmup(0)
            session = customClient?.newSession(callback)
            session?.mayLaunchUrl(Uri.parse("https://www.kickstarter.com/oauth/authorizations"), null, null)
            sessionReady.tryEmit(true)
        }
        override fun onServiceDisconnected(name: ComponentName) {
            customClient = null
            session = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpConnectivityStatusCheck(lifecycle)

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }

        CustomTabsClient.bindCustomTabsService(
            this,
            ChromeTabsHelper.getPackageNameToUse(this) ?: "",
            connection
        )

        // TODO MBL-1168 the url will be retrieved from the VM,on MBL-1168
        val uri = Uri.parse("https://www.kickstarter.com/oauth/authorizations")
        val fallback = object : ChromeTabsHelperActivity.CustomTabFallback {
            override fun openUri(activity: Activity, uri: Uri) {
                activity.startActivity(intent)
            }
        }

        val context = this
        lifecycleScope.launch {
            sessionReady.collect {
                val builder = CustomTabsIntent.Builder(session)
                builder.setStartAnimations(context, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
                val customTabsIntent = builder.build()
                ChromeTabsHelperActivity.openCustomTab(context, customTabsIntent, uri, fallback)
            }
        }
    }
}
