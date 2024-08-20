package com.kickstarter.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel

class CrowdfundCheckoutViewModelTest : KSRobolectricTestCase(){
    private lateinit var viewModel: CrowdfundCheckoutViewModel

    private fun setUpEnvironment(environment: Environment,  bundle: Bundle? = null) {
        viewModel = CrowdfundCheckoutViewModel.Factory(environment, bundle).create(
            CrowdfundCheckoutViewModel::class.java)
    }
}