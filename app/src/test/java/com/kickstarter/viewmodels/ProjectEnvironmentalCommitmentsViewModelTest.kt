package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectEnvironmentalCommitmentFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.viewmodels.projectpage.ProjectEnvironmentalCommitmentsViewModel
import org.junit.Test
import rx.observers.TestSubscriber

class ProjectEnvironmentalCommitmentsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ProjectEnvironmentalCommitmentsViewModel.ViewModel

    private val projectEnvironmentalCommitment = TestSubscriber.create<List<EnvironmentalCommitment>>()
    private val openVisitOurEnvironmentalResourcesCenter = TestSubscriber.create<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectEnvironmentalCommitmentsViewModel.ViewModel(environment)

        this.vm.outputs.projectEnvironmentalCommitment().subscribe(this.projectEnvironmentalCommitment)
        this.vm.outputs.openVisitOurEnvironmentalResourcesCenter().subscribe(this.openVisitOurEnvironmentalResourcesCenter)
    }

    @Test
    fun testBindProjectEnvironmentalCommitmentList() {
        setUpEnvironment(environment())
        val environmentalCommitmentList = ProjectEnvironmentalCommitmentFactory.getEnvironmentalCommitments()

        this.vm.configureWith(ProjectDataFactory.project(ProjectFactory.project()))

        this.projectEnvironmentalCommitment.assertValue(environmentalCommitmentList)
    }

    @Test
    fun testOpenVisitOurEnvironmentalResourcesCenter() {
        setUpEnvironment(environment())

        this.vm.inputs.onVisitOurEnvironmentalResourcesCenterClicked()
        this.openVisitOurEnvironmentalResourcesCenter.assertValueCount(1)
    }
}
