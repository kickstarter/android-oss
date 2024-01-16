package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class PaginationErrorViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: PaginationErrorViewHolderViewModel.ViewModel

    private val isErrorCellVisible = TestSubscriber<Boolean>()
    private val disposables = CompositeDisposable()

    private fun setupEnvironment() {
        this.vm = PaginationErrorViewHolderViewModel.ViewModel()
        this.vm.outputs.isErrorPaginationVisible().subscribe { isErrorCellVisible.onNext(it) }
            .addToDisposable(disposables)
    }

    @Test
    fun isErrorCellUIVisible() {
        setupEnvironment()

        this.vm.inputs.configureWith(true)
        this.isErrorCellVisible.assertValue(true)
    }

    @Test
    fun isErrorCellUINotVisible() {
        setupEnvironment()

        this.vm.inputs.configureWith(false)
        this.isErrorCellVisible.assertValue(false)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
