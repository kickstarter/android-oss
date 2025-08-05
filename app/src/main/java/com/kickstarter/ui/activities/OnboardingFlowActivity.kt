package com.kickstarter.ui.activities

import OnboardingPage
import OnboardingScreen
import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ACTIVITY_TRACKING_PROMPT
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ENABLE_NOTIFICATIONS_PROMPT
import com.kickstarter.libs.utils.extensions.checkPermissions
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
                        onboardingCancelled = { onboardingPage -> onboardingCancelled(onboardingPage) },
                        turnOnNotifications = { permissionLauncher ->
                            viewModel.trackOnboardingEnableNotificationsPromptCtaClicked()
                            turnOnNotifications(permissionLauncher)
                        },
                        allowTracking = { fragmentManager ->
                            viewModel.trackOnboardingAllowTrackingPromptCtaClicked()
                            allowTracking(fragmentManager)
                        },
                        signupOrLogin = { signupOrLogin() },
                        analyticEvents = viewModel.analytics()
                    )
                }
            }
        }
    }

    fun onboardingCompleted() {
        ApplicationUtils.resumeDiscoveryActivity(this)
    }

    fun onboardingCancelled(onboardingPage: OnboardingPage) {
        viewModel.trackOnboardingCancelled(onboardingPage)
        ApplicationUtils.resumeDiscoveryActivity(this)
    }

    fun turnOnNotifications(permissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && this.checkPermissions(Manifest.permission.POST_NOTIFICATIONS)) {
            viewModel.trackOnboardingPromptViewed(ENABLE_NOTIFICATIONS_PROMPT.contextName)
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // NOTE: we would not expect to reach this else block in the onboarding flow as we only show onboarding flow to new users
            Toast.makeText(this, "Notifications permission already granted", Toast.LENGTH_SHORT).show()
        }

        viewModel.hasSeenNotificationsPermission(true)
    }

    fun allowTracking(fragmentManager: androidx.fragment.app.FragmentManager?) {
        val consentManagementDialogFragment = ConsentManagementDialogFragment()
        consentManagementDialogFragment.isCancelable = false
        fragmentManager?.let {
            viewModel.trackOnboardingPromptViewed(ACTIVITY_TRACKING_PROMPT.contextName)
            consentManagementDialogFragment.show(it, "consentManagementDialogFragment")
        }
    }

    fun signupOrLogin() {
        viewModel.trackSignUpOrLoginCtaClicked()
        val intent = Intent().getLoginActivityIntent(this, null, LoginReason.COMPLETED_ONBOARDING)
        startActivityWithTransition(intent, R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
        finish()
    }
}
