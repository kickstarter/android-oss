package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class HelpSettingsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: HelpSettingsViewModel.ViewModel

    private fun setUpEnvironment() {
        this.vm = HelpSettingsViewModel.ViewModel(environment())
    }

    @Test
    fun testCreatorDigestFrequencyIsGone_IsFalseWhenUserHasBackingsEmails() {
        setUpEnvironment()

        this.vm.inputs.contactClicked()

        this.koalaTest.assertValue("Contact Email Clicked")
    }
}
