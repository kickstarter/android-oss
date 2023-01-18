package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockSharedPreferences
import com.kickstarter.ui.SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE
import com.kickstarter.viewmodels.ConsentManagementDialogFragmentViewModel.ConsentManagementDialogFragmentViewModel
import org.junit.Test

class ConsentManagementDialogFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ConsentManagementDialogFragmentViewModel
    private val mockPreferences = MockSharedPreferences()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ConsentManagementDialogFragmentViewModel(environment.toBuilder().sharedPreferences(mockPreferences).build())
    }

    @Test
    fun userConsentPreference_whenUserAllowsConsent_shouldWriteTrueToSharedPrefs(){
        setUpEnvironment(environment())

        this.vm.inputs.userConsentPreference(true)

        assertTrue(mockPreferences.getBoolean(CONSENT_MANAGEMENT_PREFERENCE, false))
    }

    @Test
    fun userConsentPreference_whenUserDenysConsent_shouldWriteFalseToSharedPrefs() {
        setUpEnvironment(environment())

        this.vm.inputs.userConsentPreference(false)

        assertFalse(mockPreferences.getBoolean(CONSENT_MANAGEMENT_PREFERENCE, true))
    }
}