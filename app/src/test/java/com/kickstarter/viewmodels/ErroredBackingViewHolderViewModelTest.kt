package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ErroredBackingFactory
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class ErroredBackingViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ErroredBackingViewHolderViewModel.ViewModel

    private val projectFinalCollectionDate = TestSubscriber.create<DateTime>()
    private val projectName = TestSubscriber.create<String>()
    private val notifyDelegateToStartFixPaymentMethod = TestSubscriber.create<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ErroredBackingViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.projectFinalCollectionDate().subscribe(this.projectFinalCollectionDate)
        this.vm.outputs.projectName().subscribe(this.projectName)
        this.vm.outputs.notifyDelegateToStartFixPaymentMethod().subscribe(this.notifyDelegateToStartFixPaymentMethod)
    }

    @Test
    fun testProjectFinalCollectionDate() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ErroredBackingFactory.erroredBacking())

        this.projectFinalCollectionDate.assertValue(DateTime.parse("2020-04-02T18:08:32Z"))
    }

    @Test
    fun testProjectName() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ErroredBackingFactory.erroredBacking())

        this.projectName.assertValue("Some Project Name")
    }

    @Test
    fun testNotifyDelegateToStartFixPaymentMethod() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ErroredBackingFactory.erroredBacking())

        this.vm.inputs.manageButtonClicked()
        this.notifyDelegateToStartFixPaymentMethod.assertValue("slug")
    }

}
