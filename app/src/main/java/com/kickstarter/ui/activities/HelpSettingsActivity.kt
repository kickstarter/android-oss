package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.databinding.ActivityHelpSettingsBinding
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.UrlUtils.baseCustomTabsIntent
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.User
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import rx.android.schedulers.AndroidSchedulers

class HelpSettingsActivity : AppCompatActivity() {

    private var environment: Environment? = null
    private lateinit var build: Build
    private lateinit var currentUser: CurrentUserType

    private val mailto = R.string.mailto
    private val supportEmail = R.string.support_email_to_android
    private val supportEmailBody = R.string.support_email_body
    private val supportEmailSubject = R.string.support_email_subject

    private lateinit var binding: ActivityHelpSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        environment = this.getEnvironment()
        this.build = requireNotNull(environment?.build())
        this.currentUser = requireNotNull(environment?.currentUser())

        binding.contact.setOnClickListener {
            this.currentUser.observable()
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::composeContactEmail)
        }

        binding.accessibilityStatement.setOnClickListener {
            startChromeTab(
                buildWebEndpointUrl(HelpActivity.ACCESSIBILITY),
                Intent(this, HelpActivity.AccessibilityStatement::class.java)
            )
        }

        binding.cookiePolicy.setOnClickListener {
            startChromeTab(
                buildWebEndpointUrl(HelpActivity.COOKIES),
                Intent(this, HelpActivity.CookiePolicy::class.java)
            )
        }

        binding.helpCenter.setOnClickListener {
            startChromeTab(Secrets.HelpCenter.ENDPOINT, null)
        }

        binding.privacyPolicy.setOnClickListener {
            startChromeTab(
                buildWebEndpointUrl(HelpActivity.PRIVACY),
                Intent(this, HelpActivity.Privacy::class.java)
            )
        }

        binding.termsOfUse.setOnClickListener {
            startChromeTab(
                buildWebEndpointUrl(HelpActivity.TERMS_OF_USE),
                Intent(this, HelpActivity.Terms::class.java)
            )
        }
    }

    private fun buildWebEndpointUrl(path: String): String {
        return environment?.let {
            UrlUtils.appendPath(it.webEndpoint(), path)
        } ?: run {
            return ""
        }
    }

    private fun composeContactEmail(user: User?) {
        val debugInfo = listOf(
            user?.id() ?: getString(R.string.Logged_Out),
            this.build.versionName(),
            android.os.Build.VERSION.RELEASE + " (SDK " + Integer.toString(android.os.Build.VERSION.SDK_INT) + ")",
            android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
        )
        val body = StringBuilder()
            .append(getString(this.supportEmailBody))
            .append(TextUtils.join(" | ", debugInfo))
            .toString()
        val intent = Intent(Intent.ACTION_SENDTO)
            .setData(Uri.parse(getString(this.mailto)))
            .putExtra(Intent.EXTRA_SUBJECT, "[Android] " + getString(this.supportEmailSubject))
            .putExtra(Intent.EXTRA_TEXT, body)
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(this.supportEmail)))

        val emailIntent = Intent.createChooser(intent, getString(R.string.support_email_chooser))

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        }
    }

    private fun startChromeTab(url: String, helpActivityIntent: Intent?) {
        val uri = Uri.parse(url)

        val fallback = when {
            helpActivityIntent != null -> object : ChromeTabsHelperActivity.CustomTabFallback {
                override fun openUri(activity: Activity, uri: Uri) {
                    activity.startActivity(helpActivityIntent)
                }
            }
            else -> null
        }
        ChromeTabsHelperActivity.openCustomTab(this, baseCustomTabsIntent(this), uri, fallback)
    }
}
