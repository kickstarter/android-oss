package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.viewholders.RepliesStatusCellType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class RepliesStatusCellViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: RepliesStatusCellViewHolderViewModel.ViewModel

    private val isViewMoreRepliesPaginationVisible = TestSubscriber<Boolean>()
    private val isErrorPaginationVisible = TestSubscriber<Boolean>()
    private val disposables = CompositeDisposable()

    private fun setupEnvironment() {
        this.vm = RepliesStatusCellViewHolderViewModel.ViewModel()
        this.vm.outputs.isViewMoreRepliesPaginationVisible()
            .subscribe { isViewMoreRepliesPaginationVisible.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.isErrorPaginationVisible().subscribe { isErrorPaginationVisible.onNext(it) }
            .addToDisposable(disposables)
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

    @Test
    fun isEmptyState() {
        setupEnvironment()

        this.vm.inputs.configureWith(RepliesStatusCellType.EMTPY)
        this.isViewMoreRepliesPaginationVisible.assertValue(false)
        this.isErrorPaginationVisible.assertValue(false)
    }
}
