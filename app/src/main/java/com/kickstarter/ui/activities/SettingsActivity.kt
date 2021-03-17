package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.KSString
import com.kickstarter.libs.Logout
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.SettingsViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.settings_layout.account_row
import kotlinx.android.synthetic.main.settings_layout.edit_profile_row
import kotlinx.android.synthetic.main.settings_layout.help_row
import kotlinx.android.synthetic.main.settings_layout.log_out_row
import kotlinx.android.synthetic.main.settings_layout.name_text_view
import kotlinx.android.synthetic.main.settings_layout.newsletters_row
import kotlinx.android.synthetic.main.settings_layout.notification_row
import kotlinx.android.synthetic.main.settings_layout.profile_picture_image_view
import kotlinx.android.synthetic.main.settings_layout.rate_us_row
import kotlinx.android.synthetic.main.settings_layout.version_name_text_view
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

        if (BuildConfig.DEBUG) {
            edit_profile_row.visibility = View.VISIBLE
        }

        this.build = environment().build()
        this.currentUser = environment().currentUser()
        this.ksString = environment().ksString()
        this.logout = environment().logout()

        version_name_text_view.text = ksString.format(
            getString(R.string.profile_settings_version_number),
            "version_number", this.build.versionName()
        )

        this.viewModel.outputs.avatarImageViewUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { url -> Picasso.get().load(url).transform(CircleTransformation()).into(profile_picture_image_view) }

        this.viewModel.outputs.logout()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { logout() }

        this.viewModel.outputs.showConfirmLogoutPrompt()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { show ->
                if (show) {
                    lazyLogoutConfirmationDialog().show()
                } else {
                    lazyLogoutConfirmationDialog().dismiss()
                }
            }

        this.viewModel.outputs.userNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { name_text_view.text = it }

        account_row.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        edit_profile_row.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        help_row.setOnClickListener {
            startActivity(Intent(this, HelpSettingsActivity::class.java))
        }

        log_out_row.setOnClickListener {
            this.viewModel.inputs.logoutClicked()
        }

        newsletters_row.setOnClickListener {
            startActivity(Intent(this, NewsletterActivity::class.java))
        }

        notification_row.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        rate_us_row.setOnClickListener { ViewUtils.openStoreRating(this, this.packageName) }
    }

    private fun logout() {
        this.logout.execute()
        ApplicationUtils.startNewDiscoveryActivity(this)
    }

    /**
     * Lazily creates a logout confirmation dialog and stores it in an instance variable.
     */
    private fun lazyLogoutConfirmationDialog(): AlertDialog {
        if (this.logoutConfirmationDialog == null) {
            this.logoutConfirmationDialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.profile_settings_logout_alert_title))
                .setMessage(getString(R.string.profile_settings_logout_alert_message))
                .setPositiveButton(getString(R.string.profile_settings_logout_alert_confirm_button)) { _, _ -> this.viewModel.inputs.confirmLogoutClicked() }
                .setNegativeButton(getString(R.string.profile_settings_logout_alert_cancel_button)) { _, _ -> this.viewModel.inputs.closeLogoutConfirmationClicked() }
                .setOnCancelListener { this.viewModel.inputs.closeLogoutConfirmationClicked() }
                .create()
        }
        return this.logoutConfirmationDialog!!
    }
}
