package com.kickstarter.ui.activities

import OnboardingScreen
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kickstarter.R
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getLoginActivityIntent
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.ui.fragments.ConsentManagementDialogFragment
import com.kickstarter.viewmodels.OnboardingFlowViewModel

class OnboardingFlowActivity : AppCompatActivity() {
    private lateinit var viewModelFactory: OnboardingFlowViewModel.Factory
    private val viewModel: OnboardingFlowViewModel by viewModels {
        viewModelFactory
    }
    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = OnboardingFlowViewModel.Factory(env)
            setContent {
                KickstarterApp(useDarkTheme = false) { // Fix colors to light theme because our assets are only light themed
                    OnboardingScreen(
                        isUserLoggedIn = viewModel.isUserLoggedIn(),
                        deviceNeedsNotificationPermissions = viewModel.deviceNeedsNotificationPermissions(),
                        onboardingCompleted = { onboardingCompleted() },
                        onboardingCancelled = { onboardingCancelled() },
                        turnOnNotifications = { permissionLauncher -> turnOnNotifications(permissionLauncher) },
                        allowTracking = { fragmentManager -> allowTracking(fragmentManager) },
                        signupOrLogin = { signupOrLogin() }
                    )
                }
            }
        }
    }

    fun onboardingCompleted() {
        ApplicationUtils.resumeDiscoveryActivity(this)
    }

    fun onboardingCancelled() {
        ApplicationUtils.resumeDiscoveryActivity(this)
    }

    fun turnOnNotifications(permissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted — launch the request
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        viewModel.hasSeenNotificationsPermission(true)
    }

    fun allowTracking(fragmentManager: androidx.fragment.app.FragmentManager?) {
        val consentManagementDialogFragment = ConsentManagementDialogFragment()
        consentManagementDialogFragment.isCancelable = false
        fragmentManager?.let {
            consentManagementDialogFragment.show(it, "consentManagementDialogFragment")
        }
    }

    fun signupOrLogin() {
        val intent = Intent().getLoginActivityIntent(this, null, LoginReason.COMPLETED_ONBOARDING)
        startActivityWithTransition(intent, R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
        finish()
    }
}
