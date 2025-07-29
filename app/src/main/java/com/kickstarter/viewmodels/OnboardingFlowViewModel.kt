package com.kickstarter.viewmodels

import OnboardingPage
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.ui.SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import kotlin.coroutines.EmptyCoroutineContext

class OnboardingFlowViewModel(
    environment: Environment,
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {
    private val currentUser = requireNotNull(environment.currentUserV2())
    private val sharedPreferences = requireNotNull(environment.sharedPreferences())
    private val analytics = requireNotNull(environment.analytics())

    private var isUserLoggedIn = false
    private var deviceNeedsNotificationPermissions = false
    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    init {
        deviceNeedsNotificationPermissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !sharedPreferences.getBoolean(HAS_SEEN_NOTIF_PERMISSIONS, false)

        scope.launch {
            currentUser.observable().asFlow()
                .collectLatest {
                    isUserLoggedIn = it.isPresent()
                }
        }
    }

    fun isUserLoggedIn(): Boolean = isUserLoggedIn
    fun deviceNeedsNotificationPermissions(): Boolean = deviceNeedsNotificationPermissions

    fun hasSeenNotificationsPermission(hasSeen: Boolean) {
        sharedPreferences.edit().putBoolean(HAS_SEEN_NOTIF_PERMISSIONS, hasSeen).apply()
    }

    fun analytics() = analytics // Expose for Compose screen
    fun trackSignUpOrLoginCtaClicked() {
        analytics.trackOnboardingSignupLoginCTAClicked()
    }
    fun trackOnboardingPromptViewed(prompt: String) {
        analytics.trackOnboardingPageViewed(prompt)
    }
    fun trackOnboardingAllowTrackingPromptCtaClicked() {
        analytics.trackOnboardingAllowTrackingCTAClicked()
    }
    fun trackOnboardingEnableNotificationsPromptCtaClicked() {
        analytics.trackOnboardingGetNotifiedCTAClicked()
    }
    fun trackOnboardingCancelled(onboardingPage: OnboardingPage) {
        analytics.trackOnboardingCloseCTAClicked(onboardingPage.analyticsSectionName)
    }
    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingFlowViewModel(environment, testDispatcher) as T
        }
    }
}
