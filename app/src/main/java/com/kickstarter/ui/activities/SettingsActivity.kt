package com.kickstarter.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.isGone
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.databinding.SettingsLayoutBinding
import com.kickstarter.libs.Build
import com.kickstarter.libs.KSString
import com.kickstarter.libs.Logout
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.SettingsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SettingsActivity : AppCompatActivity() {
    private lateinit var build: Build
    private lateinit var ksString: KSString
    private lateinit var logout: Logout
    private var logoutConfirmationDialog: AlertDialog? = null
    private lateinit var binding: SettingsLayoutBinding
    private lateinit var disposables: CompositeDisposable
    private lateinit var spinner: AppCompatSpinner
    private var sharedPrefs: SharedPreferences? = null
    private var darkModeEnabled = false

    private lateinit var themeItems: Array<String>

    private lateinit var viewModelFactory: SettingsViewModel.Factory
    private val viewModel: SettingsViewModel.SettingsViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeItems = arrayOf(
            getString(R.string.match_system),
            getString(R.string.light),
            getString(R.string.dark)
        )
        binding = SettingsLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        this.getEnvironment()?.let { env ->
            viewModelFactory = SettingsViewModel.Factory(env)
            sharedPrefs = env.sharedPreferences()
            darkModeEnabled =
                env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
        }
        disposables = CompositeDisposable()

        setContentView(binding.root)

        spinner = binding.appThemeSpinner
        if (darkModeEnabled) {
            binding.appThemeContainer.visibility = View.VISIBLE
            setUpThemeSpinner()
        }

        setUpConnectivityStatusCheck(lifecycle)

        this.build = requireNotNull(getEnvironment()?.build())
        this.ksString = requireNotNull(getEnvironment()?.ksString())
        this.logout = requireNotNull(getEnvironment()?.logout())

        binding.versionNameTextView.text = ksString.format(
            getString(R.string.profile_settings_version_number),
            "version_number", this.build.versionName()
        )

        this.viewModel.outputs.avatarImageViewUrl()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { url ->
                binding.profilePictureImageView.loadCircleImage(url)
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
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.nameTextView.text = it }
            .addToDisposable(disposables)

        this.viewModel.isUserPresent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isPresent ->
                binding.editProfileRow.isGone = !BuildConfig.DEBUG || !isPresent
                binding.accountRow.isGone = !isPresent
                binding.notificationAndNewsletterContainer.isGone = !isPresent
                binding.logOutRow.isGone = !isPresent
            }
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

    private fun setUpThemeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, themeItems)
        spinner.adapter = adapter

        val currentSelection =
            sharedPrefs?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
        spinner.setSelection(currentSelection)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                sharedPrefs?.edit()?.putInt(SharedPreferenceKey.APP_THEME, p2)?.apply()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }
}

enum class AppThemes {
    MATCH_SYSTEM,
    LIGHT,
    DARK
}
