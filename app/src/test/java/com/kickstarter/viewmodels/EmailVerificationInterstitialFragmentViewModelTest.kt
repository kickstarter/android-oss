package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import junit.framework.TestCase
import rx.observers.TestSubscriber

class EmailVerificationInterstitialFragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: EmailVerificationInterstitialFragmentViewModel.ViewModel
    private val startEmailActivity = TestSubscriber.create<Void>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = EmailVerificationInterstitialFragmentViewModel.ViewModel(environment)
        this.vm.outputs.startEmailActivity().subscribe(startEmailActivity)
    }


}