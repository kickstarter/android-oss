package com.kickstarter.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import com.kickstarter.R
import com.kickstarter.extensions.startActivityWithSlideLeftTransition
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.models.User
import com.kickstarter.viewmodels.HelpSettingsViewModel
import kotlinx.android.synthetic.main.activity_help_settings.*
import rx.android.schedulers.AndroidSchedulers
import java.util.*

@RequiresActivityViewModel(HelpSettingsViewModel.ViewModel::class)
class HelpSettingsActivity : BaseActivity<HelpSettingsViewModel.ViewModel>() {

    private lateinit var build: Build
    private lateinit var currentUser: CurrentUserType

    private val loggedOut = R.string.Logged_Out
    private val mailto = R.string.mailto
    private val supportEmail = R.string.support_email_to_android
    private val supportEmailBody = R.string.support_email_body
    private val supportEmailSubject = R.string.support_email_subject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_settings)

        this.build = environment().build()
        this.currentUser = environment().currentUser()

        contact.setOnClickListener {
            this.viewModel.contactClicked()

            this.currentUser.observable()
                    .take(1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::composeContactEmail)
        }

        cookie_policy.setOnClickListener{
            startActivityWithSlideLeftTransition(Intent(this, HelpActivity.CookiePolicy::class.java))
        }

        help_center.setOnClickListener {
            startActivityWithSlideLeftTransition(Intent(Intent.ACTION_VIEW, Uri.parse(Secrets.HelpCenter.ENDPOINT)))
        }

        privacy_policy.setOnClickListener {
            startActivityWithSlideLeftTransition(Intent(this, HelpActivity.Privacy::class.java))
        }

        terms_of_use.setOnClickListener {
            startActivityWithSlideLeftTransition(Intent(this, HelpActivity.Terms::class.java))
        }
    }

    private fun composeContactEmail(user: User?) {
        val debugInfo = Arrays.asList(
                user?.id()?.toString() ?: getString(this.loggedOut),
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
                .putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(getString(this.supportEmail)))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.support_email_chooser)))
        }
    }
}
