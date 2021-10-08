package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.EnvironmentalCommitmentCategories
import com.kickstarter.mock.factories.ProjectEnvironmentalCommitmentFactory
import org.junit.Test
import rx.observers.TestSubscriber

class EnvironmentalCommitmentsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: EnvironmentalCommitmentsViewHolderViewModel.ViewModel

    private val description = TestSubscriber.create<String>()
    private val category = TestSubscriber.create<Int>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = EnvironmentalCommitmentsViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.description().subscribe(this.description)
        this.vm.outputs.category().subscribe(this.category)
    }

    @Test
    fun testBindProjectEnvironmentalCommitment() {
        setUpEnvironment(environment())
        val environmentalCommitments = ProjectEnvironmentalCommitmentFactory
            .getEnvironmentalCommitments()[0]

        this.vm.configureWith(environmentalCommitments)

        this.description.assertValue(environmentalCommitments.description)
        this.category.assertValue(EnvironmentalCommitmentCategories.LONG_LASTING_DESIGN.title)
    }
}
