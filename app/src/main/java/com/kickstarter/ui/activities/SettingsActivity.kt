package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.databinding.SettingsLayoutBinding
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
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(SettingsViewModel.ViewModel::class)
class SettingsActivity : BaseActivity<SettingsViewModel.ViewModel>() {
    private var build: Build ? = null
    private lateinit var currentUser: CurrentUserType
    private lateinit var ksString: KSString
    private var logout: Logout? = null
    private var logoutConfirmationDialog: AlertDialog? = null
    private lateinit var binding: SettingsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (BuildConfig.DEBUG) {
            binding.editProfileRow.visibility = View.VISIBLE
        }

        this.build = environment().build()
        this.currentUser = environment().currentUser()
        this.ksString = environment().ksString()
        this.logout = environment().logout()

        this.build?.versionName()?.let { versionName ->
            binding.versionNameTextView.text = ksString.format(
                getString(R.string.profile_settings_version_number),
                "version_number", versionName
            )
        }

        this.viewModel.outputs.avatarImageViewUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { url -> Picasso.get().load(url).transform(CircleTransformation()).into(binding.profilePictureImageView) }

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
            .subscribe { binding.nameTextView.text = it }

        binding.accountRow.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        binding.editProfileRow.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.helpRow.setOnClickListener {
            startActivity(Intent(this, HelpSettingsActivity::class.java))
        }

        binding.logOutRow.setOnClickListener {
            this.viewModel.inputs.logoutClicked()
        }

        binding.newslettersRow.setOnClickListener {
            startActivity(Intent(this, NewsletterActivity::class.java))
        }

        binding.notificationRow.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.rateUsRow.setOnClickListener { ViewUtils.openStoreRating(this, this.packageName) }
    }

    private fun logout() {
        this.logout?.execute()
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
