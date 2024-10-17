package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.viewholders.AddCardViewHolderViewModel
import com.kickstarter.ui.viewholders.State
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class AddCardViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: AddCardViewHolderViewModel.ViewModel
    private val loading = TestSubscriber<State>()
    private val default = TestSubscriber<State>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        vm = AddCardViewHolderViewModel.ViewModel()
        this.vm.outputs.setLoadingState().subscribe { loading.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.setDefaultState().subscribe { default.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testLoadingState() {
        setUpEnvironment()

        this.vm.inputs.configureWith(State.LOADING)
        loading.assertValue(State.LOADING)
        default.assertNoValues()
    }

    fun testDefaultState() {
        setUpEnvironment()

        this.vm.inputs.configureWith(State.DEFAULT)
        default.assertValue(State.DEFAULT)
        loading.assertNoValues()
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
