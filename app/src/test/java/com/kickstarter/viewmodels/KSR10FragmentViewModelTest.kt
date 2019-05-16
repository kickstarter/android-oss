package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class KSR10FragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: KSR10FragmentViewModel.ViewModel
    private val dismiss = TestSubscriber<Void>()
    private val hasSeenKSR10BirthdayModal = TestSubscriber<Boolean>()
    private val startAnimations = TestSubscriber<Void>()

    private fun setUpEnvironment() {
        this.vm = KSR10FragmentViewModel.ViewModel(environment())
        this.vm.dismiss().subscribe(this.dismiss)
        this.vm.startAnimations().subscribe(this.startAnimations)
    }

    @Test
    fun testDismiss() {
        setUpEnvironment()

        this.vm.inputs.closeClicked()
        this.dismiss.assertValueCount(1)
    }

    @Test
    fun testKoala() {
        setUpEnvironment()

        this.koalaTest.assertValue("Viewed KSR10 Birthday Modal")
    }

    @Test
    fun testPreference() {
        setUpEnvironment()
        val hasSeenKSR10BirthdayModal = Observable.defer { Observable.just<Boolean>(environment().hasSeenKSR10BirthdayModal().get()) }
        hasSeenKSR10BirthdayModal.subscribe(this.hasSeenKSR10BirthdayModal)

        this.hasSeenKSR10BirthdayModal.assertValue(true)
    }

    @Test
    fun testStartAnimations() {
        setUpEnvironment()

        this.vm.inputs.onGlobalLayout()
        this.startAnimations.assertValueCount(1)
    }
}
