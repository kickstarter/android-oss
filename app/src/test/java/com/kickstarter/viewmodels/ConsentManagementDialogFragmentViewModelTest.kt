package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import rx.observers.TestSubscriber
import com.kickstarter.viewmodels.ConsentManagementDialogFragmentViewModel.ConsentManagementDialogFragmentViewModel

class ConsentManagementDialogFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ConsentManagementDialogFragmentViewModel

    private val onAllow = TestSubscriber.create<Void>()
    private val onDeny = TestSubscriber.create<Void>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ConsentManagementDialogFragmentViewModel(environment)
    }


}