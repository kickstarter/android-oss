package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ui.viewholders.RepliesStatusCellType
import org.junit.Test
import rx.observers.TestSubscriber

class RepliesStatusCellViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: RepliesStatusCellViewHolderViewModel.ViewModel

    private val isViewMoreRepliesPaginationVisible = TestSubscriber<Boolean>()
    private val isErrorPaginationVisible = TestSubscriber<Boolean>()

    private fun setupEnvironment() {
        this.vm = RepliesStatusCellViewHolderViewModel.ViewModel(environment())
        this.vm.outputs.isViewMoreRepliesPaginationVisible().subscribe(isViewMoreRepliesPaginationVisible)
        this.vm.outputs.isErrorPaginationVisible().subscribe(isErrorPaginationVisible)
    }

    @Test
    fun isViewMoreUIVisible() {
        setupEnvironment()

        this.vm.inputs.configureWith(RepliesStatusCellType.VIEW_MORE)
        this.isViewMoreRepliesPaginationVisible.assertValue(true)
        this.isErrorPaginationVisible.assertValue(false)
    }

    @Test
    fun isErrorPaginationVisible() {
        setupEnvironment()

        this.vm.inputs.configureWith(RepliesStatusCellType.PAGINATION_ERROR)
        this.isViewMoreRepliesPaginationVisible.assertValue(false)
        this.isErrorPaginationVisible.assertValue(true)
    }
}
