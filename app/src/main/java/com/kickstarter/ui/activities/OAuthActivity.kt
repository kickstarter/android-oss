package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.kickstarter.libs.utils.CodeVerifier
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import kotlinx.coroutines.launch

class OAuthActivity : AppCompatActivity() {

    private lateinit var helper: ChromeTabsHelperActivity.CustomTabSessionAndClientHelper

    val redirectUri = "ksrauth2://authorize"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpConnectivityStatusCheck(lifecycle)

        // TODO: Will be moved to VM all the URI parameters building on MBL-1169
        val codeVerifier = CodeVerifier.generateRandomCodeVerifier(entropyBytes = CodeVerifier.MAX_CODE_VERIFIER_ENTROPY)
        val authParams = mapOf(
            "redirect_uri" to redirectUri,
            "response_type" to "code",
            "code_challenge" to CodeVerifier.generateCodeChallenge(codeVerifier), // Set the code challenge
            "code_challenge_method" to "S256"
        ).map { (k, v) -> "${(k)}=$v" }.joinToString("&")
        val uri = Uri.parse("https://www.kickstarter.com/oauth/authorizations?$authParams")

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
