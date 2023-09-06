package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.databinding.SettingsLayoutBinding
import com.kickstarter.libs.Build
import com.kickstarter.libs.KSString
import com.kickstarter.libs.Logout
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.viewmodels.SettingsViewModel
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SettingsActivity : AppCompatActivity() {
    private lateinit var build: Build
    private lateinit var ksString: KSString
    private lateinit var logout: Logout
    private var logoutConfirmationDialog: AlertDialog? = null
    private lateinit var binding: SettingsLayoutBinding
    private lateinit var disposables: CompositeDisposable

    private lateinit var viewModelFactory: SettingsViewModel.Factory
    private val viewModel: SettingsViewModel.SettingsViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsLayoutBinding.inflate(layoutInflater)
        this.getEnvironment()?.let { env ->
            viewModelFactory = SettingsViewModel.Factory(env)
        }
        disposables = CompositeDisposable()

        setContentView(binding.root)

        setUpConnectivityStatusCheck(lifecycle)
        if (BuildConfig.DEBUG) {
            binding.editProfileRow.visibility = View.VISIBLE
        }

        this.build = requireNotNull(getEnvironment()?.build())
        this.ksString = requireNotNull(getEnvironment()?.ksString())
        this.logout = requireNotNull(getEnvironment()?.logout())

        binding.versionNameTextView.text = ksString.format(
            getString(R.string.profile_settings_version_number),
            "version_number", this.build.versionName()
        )

        this.viewModel.outputs.avatarImageViewUrl()
            .compose(Transformers.observeForUIV2())
            .subscribe { url ->
                Picasso.get().load(url).transform(CircleTransformation())
                    .into(binding.profilePictureImageView)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.logout()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { logout() }
            .addToDisposable(disposables)

        this.viewModel.outputs.showConfirmLogoutPrompt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { show ->
                if (show) {
                    lazyLogoutConfirmationDialog().show()
                } else {
                    lazyLogoutConfirmationDialog().dismiss()
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.userNameTextViewText()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.nameTextView.text = it }
            .addToDisposable(disposables)

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

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
