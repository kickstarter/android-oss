package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test
import rx.observers.TestSubscriber

class PaginationViewMoreViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: PaginationViewMoreViewHolderViewModel.ViewModel

    private val isViewMoreRepliesPaginationVisible = TestSubscriber<Boolean>()

    private fun setupEnvironment() {
        this.vm = PaginationViewMoreViewHolderViewModel.ViewModel(environment())
        this.vm.outputs.isViewMoreRepliesPaginationVisible().subscribe(isViewMoreRepliesPaginationVisible)
    }

    @Test
    fun isViewMoreUIVisible() {
        setupEnvironment()

        this.vm.inputs.configureWith(true)
        this.isViewMoreRepliesPaginationVisible.assertValue(true)
    }

    @Test
    fun isEViewMoreUINotVisible() {
        setupEnvironment()

        this.vm.inputs.configureWith(false)
        this.isViewMoreRepliesPaginationVisible.assertValue(false)
    }
}
