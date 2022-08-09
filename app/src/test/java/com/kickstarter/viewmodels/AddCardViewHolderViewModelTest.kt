package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.ui.viewholders.AddCardViewHolderViewModel
import com.kickstarter.ui.viewholders.State
import org.junit.Test
import rx.observers.TestSubscriber

class AddCardViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: AddCardViewHolderViewModel.ViewModel
    private val loading = TestSubscriber<State>()
    private val default = TestSubscriber<State>()

    private fun setUpEnvironment(environment: Environment) {
        vm = AddCardViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.setLoadingState().subscribe(loading)
        this.vm.outputs.setDefaultState().subscribe(default)
    }

    @Test
    fun testLoadingState() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(State.LOADING)
        loading.assertValue(State.LOADING)
        default.assertNoValues()
    }

    fun testDefaultState() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(State.DEFAULT)
        default.assertValue(State.DEFAULT)
        loading.assertNoValues()
    }
}
