package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ErroredBackingFactory
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.joda.time.DateTime
import org.junit.After
import org.junit.Test

class ErroredBackingViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ErroredBackingViewHolderViewModel.ViewModel

    private val projectFinalCollectionDate = TestSubscriber.create<DateTime>()
    private val projectName = TestSubscriber.create<String>()
    private val notifyDelegateToStartFixPaymentMethod = TestSubscriber.create<String>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        this.vm = ErroredBackingViewHolderViewModel.ViewModel()

        this.vm.outputs.projectFinalCollectionDate()
            .subscribe { this.projectFinalCollectionDate.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.projectName().subscribe { this.projectName.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.notifyDelegateToStartFixPaymentMethod()
            .subscribe { this.notifyDelegateToStartFixPaymentMethod.onNext(it) }
            .addToDisposable(disposables)
    }

    @Test
    fun testProjectFinalCollectionDate() {
        setUpEnvironment()

        this.vm.inputs.configureWith(ErroredBackingFactory.erroredBacking())

        this.projectFinalCollectionDate.assertValue(DateTime.parse("2020-04-02T18:08:32Z"))
    }

    @Test
    fun testProjectName() {
        setUpEnvironment()

        this.vm.inputs.configureWith(ErroredBackingFactory.erroredBacking())

        this.projectName.assertValue("Some Project Name")
    }

    @Test
    fun testNotifyDelegateToStartFixPaymentMethod() {
        setUpEnvironment()

        this.vm.inputs.configureWith(ErroredBackingFactory.erroredBacking())

        this.vm.inputs.manageButtonClicked()
        this.notifyDelegateToStartFixPaymentMethod.assertValue("slug")
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
