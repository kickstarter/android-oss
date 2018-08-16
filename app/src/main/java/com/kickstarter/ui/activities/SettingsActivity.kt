package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.kickstarter.R
import com.kickstarter.extensions.startActivityWithSlideUpTransition
import com.kickstarter.libs.*
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft

import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.settings_layout.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(SettingsViewModel.ViewModel::class)
class SettingsActivity : BaseActivity<SettingsViewModel.ViewModel>() {
    private lateinit var build: Build
    private lateinit var currentUser: CurrentUserType
    private lateinit var ksString: KSString
    private lateinit var logout: Logout
    private var logoutConfirmationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        this.build = environment().build()
        this.currentUser = environment().currentUser()
        this.ksString = environment().ksString()
        this.logout = environment().logout()

        version_name_text_view.text = this.build.versionName()

        this.viewModel.outputs.showConfirmLogoutPrompt()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ show ->
                    if (show) {
                        lazyLogoutConfirmationDialog().show()
                    } else {
                        lazyLogoutConfirmationDialog().dismiss()
                    }
                })

        this.viewModel.outputs.logout()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { logout() }

        help_row.setOnClickListener {
            startActivityWithSlideUpTransition(Intent(this, HelpNewActivity::class.java))
        }
        log_out_row.setOnClickListener { this.viewModel.inputs.logoutClicked() }
        newsletters_row.setOnClickListener {
            startActivityWithSlideUpTransition(Intent(this, NewsletterActivity::class.java))
        }
        notification_row.setOnClickListener {
            startActivityWithSlideUpTransition(Intent(this, NotificationsActivity::class.java))
        }
        privacy_row.setOnClickListener {
            startActivityWithSlideUpTransition(Intent(this, PrivacyActivity::class.java))
        }
        rate_us_row.setOnClickListener { ViewUtils.openStoreRating(this, this.packageName) }
    }

    private fun logout() {
        this.logout.execute()
        ApplicationUtils.startNewDiscoveryActivity(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val exit = slideInFromLeft()
        overridePendingTransition(exit.first, exit.second)
    }


    /**
     * Lazily creates a logout confirmation dialog and stores it in an instance variable.
     */
    private fun lazyLogoutConfirmationDialog(): AlertDialog {
        if (this.logoutConfirmationDialog == null) {
            this.logoutConfirmationDialog = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.profile_settings_logout_alert_title))
                    .setMessage(getString(R.string.profile_settings_logout_alert_message))
                    .setPositiveButton(getString(R.string.profile_settings_logout_alert_confirm_button)) { dialog, which -> this.viewModel.inputs.confirmLogoutClicked() }
                    .setNegativeButton(getString(R.string.profile_settings_logout_alert_cancel_button)) { dialog, which -> this.viewModel.inputs.closeLogoutConfirmationClicked() }
                    .setOnCancelListener { dialog -> this.viewModel.inputs.closeLogoutConfirmationClicked() }
                    .create()
        }
        return this.logoutConfirmationDialog!!
    }
}
