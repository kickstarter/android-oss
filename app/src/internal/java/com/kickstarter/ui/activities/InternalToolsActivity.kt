package com.kickstarter.ui.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.iid.FirebaseInstanceId
import com.jakewharton.processphoenix.ProcessPhoenix
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.databinding.InternalToolsLayoutBinding
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Build
import com.kickstarter.libs.Logout
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.qualifiers.ApiEndpointPreference
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.LoginHelper
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.WorkUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.services.firebase.ResetDeviceIdWorker
import com.kickstarter.ui.fragments.Callbacks
import com.kickstarter.viewmodels.InternalToolsViewModel
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RequiresActivityViewModel(InternalToolsViewModel::class)
class InternalToolsActivity : BaseActivity<InternalToolsViewModel>() {
    @JvmField
    @Inject
    @ApiEndpointPreference
    var apiEndpointPreference: StringPreferenceType? = null

    @JvmField
    @Inject
    var build: Build? = null

    @JvmField
    @Inject
    var logout: Logout? = null

    private lateinit var binding: InternalToolsLayoutBinding

    private val resetDeviceIdReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            showDeviceId(true)
            setupBuildInformationSection()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InternalToolsLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        (applicationContext as KSApplication).component().inject(this)

        setupBuildInformationSection()

        binding.playgroundButton.setOnClickListener {
            playgroundButtonClicked()
        }

        binding.pushNotificationsButton.setOnClickListener {
            pushNotificationsButtonClick()
        }

        binding.changeEndpointCustomButton.setOnClickListener {
            changeEndpointCustomButton()
        }

        binding.changeEndpointHivequeenButton.setOnClickListener {
            changeEndpointHivequeenButton()
        }

        binding.changeEndpointStagingButton.setOnClickListener {
            changeEndpointStagingButton()
        }

        binding.changeEndpointProductionButton.setOnClickListener {
            changeEndpointProductionButton()
        }

        binding.crashButton.setOnClickListener {
            crashButtonClicked()
        }

        binding.featureFlagsButton.setOnClickListener {
            featureFlagsClick()
        }

        binding.emailVerificationButton.setOnClickListener {
            emailVerificationInterstitialClick()
        }

        binding.resetDeviceId.setOnClickListener {
            resetDeviceIdClick()
        }
    }

    override fun onResume() {
        super.onResume()
        this.registerReceiver(resetDeviceIdReceiver, IntentFilter(ResetDeviceIdWorker.BROADCAST))
    }

    override fun onPause() {
        super.onPause()
        this.unregisterReceiver(resetDeviceIdReceiver)
    }

    private fun playgroundButtonClicked() {
        val intent = Intent(this, PlaygroundActivity::class.java)
        startActivity(intent)
    }

    private fun pushNotificationsButtonClick() {
        val view = View.inflate(this, R.layout.debug_push_notifications_layout, null)
        AlertDialog.Builder(this)
            .setTitle("Push notifications")
            .setView(view)
            .show()
    }

    private fun changeEndpointCustomButton() {
        showCustomEndpointDialog()
    }

    private fun changeEndpointHivequeenButton() {
        showHivequeenEndpointDialog()
    }

    private fun changeEndpointStagingButton() {
        setEndpointAndRelaunch(ApiEndpoint.STAGING)
    }

    private fun changeEndpointProductionButton() {
        setEndpointAndRelaunch(ApiEndpoint.PRODUCTION)
    }

    private fun crashButtonClicked() {
        throw RuntimeException("Forced a crash!")
    }

    private fun featureFlagsClick() {
        val featureFlagIntent = Intent(this, FeatureFlagsActivity::class.java)
        startActivity(featureFlagIntent)
    }

    private fun emailVerificationInterstitialClick() {
        LoginHelper.showInterstitialFragment(
            this.supportFragmentManager,
            R.id.email_verification_interstitial_fragment_container,
            object : Callbacks {
                override fun onDismiss() {
                    Log.i(this@InternalToolsActivity.localClassName, "Placeholder for callback function")
                }
            }
        )
    }

    private fun resetDeviceIdClick() {
        showDeviceId(false)
        val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(ResetDeviceIdWorker::class.java)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.SECONDS)
            .setConstraints(WorkUtils.baseConstraints)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork(ResetDeviceIdWorker.TAG, ExistingWorkPolicy.REPLACE, request)
    }

    private fun showCustomEndpointDialog() {
        val view = View.inflate(this, R.layout.custom_endpoint_layout, null)
        val customEndpointEditText: EditText = view.findViewById<EditText>(R.id.custom_endpoint_edit_text)
        AlertDialog.Builder(this)
            .setTitle("Change endpoint")
            .setView(view)
            .setPositiveButton(
                android.R.string.yes
            ) { _: DialogInterface?, _: Int ->
                val url: String = customEndpointEditText.text.toString()
                if (URLUtil.isValidUrl(url)) {
                    setEndpointAndRelaunch(ApiEndpoint.from(url))
                }
            }
            .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_alert))
            .show()
    }

    private fun showDeviceId(show: Boolean) {
        binding.resetDeviceId.isEnabled = !show
        ViewUtils.setInvisible(binding.deviceId, !show)
        ViewUtils.setInvisible(binding.deviceIdLoadingIndicatorLayout.deviceIdLoadingIndicator, show)
    }

    private fun showHivequeenEndpointDialog() {
        val view = View.inflate(this, R.layout.hivequeen_endpoint_layout, null)
        val hivequeenNameEditText: EditText = view.findViewById<EditText>(R.id.hivequeen_name_edit_text)
        AlertDialog.Builder(this)
            .setTitle("Change endpoint")
            .setView(view)
            .setPositiveButton(
                android.R.string.yes
            ) { _: DialogInterface?, _: Int ->
                val hivequeenName = hivequeenNameEditText.text.toString()
                if (hivequeenName.isNotEmpty()) {
                    setEndpointAndRelaunch(ApiEndpoint.from(Secrets.Api.Endpoint.hqHost(hivequeenName)))
                }
            }
            .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int ->
                dialog.cancel()
            }.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_alert))
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun setupBuildInformationSection() {
        binding.apiEndpoint.text = apiEndpointPreference?.get()
        binding.buildDate.text = build?.buildDate()?.toString(DateTimeFormat.forPattern("MMM dd, yyyy h:mm:ss aa zzz"))
        binding.commitSha.text = build?.sha()
        binding.deviceId.text = FirebaseInstanceId.getInstance().id
        binding.variant.text = build?.variant()
        binding.versionCode.text = build?.versionCode().toString()
        binding.versionName.text = build?.versionName()
    }

    private fun setEndpointAndRelaunch(apiEndpoint: ApiEndpoint) {
        apiEndpointPreference?.set(apiEndpoint.url())
        logout?.execute()
        try {
            Thread.sleep(500L)
        } catch (ignored: InterruptedException) {
        }
        ProcessPhoenix.triggerRebirth(this)
    }

    override fun exitTransition() = TransitionUtils.slideInFromLeft()
}
