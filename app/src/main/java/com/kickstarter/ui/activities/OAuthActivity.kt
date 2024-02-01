package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import kotlinx.coroutines.launch

class OAuthActivity : AppCompatActivity() {

    private lateinit var helper: ChromeTabsHelperActivity.CustomTabSessionAndClientHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpConnectivityStatusCheck(lifecycle)

        // TODO MBL-1168 the url will be retrieved from the VM,on MBL-1168 alongside PKCE paramenters
        val uri = Uri.parse("https://www.kickstarter.com/oauth/authorizations")

        // BindCustomTabsService, obtain CustomTabsClient and Client, listens to navigation events
        helper = ChromeTabsHelperActivity.CustomTabSessionAndClientHelper(this, uri) {
            finish()
        }

        // - Fallback in case Chrome is not installed, open WebViewActivity
        val fallback = object : ChromeTabsHelperActivity.CustomTabFallback {
            override fun openUri(activity: Activity, uri: Uri) {
                val intent: Intent = Intent(activity, WebViewActivity::class.java)
                    .putExtra(IntentKey.URL, uri.toString())

                activity.startActivity(intent)
                TransitionUtils.slideInFromRight()
            }
        }

        lifecycleScope.launch {
            // - Once the session is ready and client warmed-up load the url
            helper.isSessionReady().collect {
                val tabIntent = CustomTabsIntent.Builder(helper.getSession()).build()
                ChromeTabsHelperActivity.openCustomTab(this@OAuthActivity, tabIntent, uri, fallback)
            }
        }
    }
}
