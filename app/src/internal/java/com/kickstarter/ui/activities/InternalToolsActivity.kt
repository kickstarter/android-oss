package com.kickstarter.ui.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.jakewharton.processphoenix.ProcessPhoenix
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.databinding.InternalToolsLayoutBinding
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.Build
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.Logout
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.qualifiers.ApiEndpointPreference
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.WorkUtils
import com.kickstarter.services.firebase.ResetDeviceIdWorker
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InternalToolsActivity : AppCompatActivity() {
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

        binding.designSystemButton.setOnClickListener {
            designSystemButtonClicked()
        }

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

        binding.resetDeviceId.setOnClickListener {
            resetDeviceIdClick()
        }
    }

    override fun onResume() {
        super.onResume()
        // Specifying the RECEIVER_NOT_EXPORTED flag is required by apps targeting android 34, however
        // this flag parameter was added to registerReceiver in android 33,git while our min SDK is 23
        if (android.os.Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            this.registerReceiver(resetDeviceIdReceiver, IntentFilter(ResetDeviceIdWorker.BROADCAST), RECEIVER_NOT_EXPORTED)
        } else {
            this.registerReceiver(resetDeviceIdReceiver, IntentFilter(ResetDeviceIdWorker.BROADCAST))
        }
    }

    override fun onPause() {
        super.onPause()
        this.unregisterReceiver(resetDeviceIdReceiver)
    }

    private fun designSystemButtonClicked() {
        val intent = Intent(this, DesignSystemActivity::class.java)
        startActivity(intent)
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
        binding.deviceId.text = FirebaseHelper.identifier
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
}
