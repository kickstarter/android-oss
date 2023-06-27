package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.EnvironmentalCommitmentCategories
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectEnvironmentalCommitmentFactory
import io.reactivex.disposables.CompositeDisposable
import org.junit.After
import org.junit.Test
import rx.observers.TestSubscriber

class EnvironmentalCommitmentsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: EnvironmentalCommitmentsViewHolderViewModel.EnvironmentalCommitmentsViewHolderViewModel

    private val description = TestSubscriber.create<String>()
    private val category = TestSubscriber.create<Int>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        this.vm = EnvironmentalCommitmentsViewHolderViewModel.EnvironmentalCommitmentsViewHolderViewModel()

        this.vm.outputs.description().subscribe {this.description.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.category().subscribe { this.category.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testBindProjectEnvironmentalCommitment() {
        setUpEnvironment()
        val environmentalCommitments = ProjectEnvironmentalCommitmentFactory
            .getEnvironmentalCommitments()[0]

        this.vm.configureWith(environmentalCommitments)

        this.description.assertValue(environmentalCommitments.description)
        this.category.assertValue(EnvironmentalCommitmentCategories.LONG_LASTING_DESIGN.title)
    }

    @After
    fun cleanUp() {
        vm.clearDisposables()
        disposables.clear()
    }
}
