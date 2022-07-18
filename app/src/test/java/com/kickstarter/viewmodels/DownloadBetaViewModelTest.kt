package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.apiresponses.InternalBuildEnvelope
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.observers.TestSubscriber

class DownloadBetaViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: DownloadBetaViewModel
    private val internalBuildEnvelope = TestSubscriber<InternalBuildEnvelope>()

    fun setUpEnvironment() {
        vm = DownloadBetaViewModel(environment())
        vm.outputs.internalBuildEnvelope().subscribe(internalBuildEnvelope)
    }

    @Test
    fun testInternalBuildEnvelope() {
        setUpEnvironment()

        val internalEnvelope = InternalBuildEnvelope.builder().newerBuildAvailable(true).build()
        vm.intent(Intent().putExtra(IntentKey.INTERNAL_BUILD_ENVELOPE, internalEnvelope))

        internalBuildEnvelope.assertValueCount(1)
    }
}
