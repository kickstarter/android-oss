package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class HelpSettingsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: HelpSettingsViewModel.ViewModel

    private fun setUpEnvironment() {
        this.vm = HelpSettingsViewModel.ViewModel(environment())
    }
}
