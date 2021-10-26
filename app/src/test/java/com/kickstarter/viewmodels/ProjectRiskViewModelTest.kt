package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.viewmodels.projectpage.ProjectRiskViewModel
import org.junit.Test
import rx.observers.TestSubscriber

class ProjectRiskViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ProjectRiskViewModel.ViewModel

    private val projectRisks = TestSubscriber.create<String>()
    private val openLearnAboutAccountabilityOnKickstarter = TestSubscriber.create<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectRiskViewModel.ViewModel(environment)

        this.vm.outputs.projectRisks().subscribe(this.projectRisks)
        this.vm.outputs.openLearnAboutAccountabilityOnKickstarter().subscribe(this.openLearnAboutAccountabilityOnKickstarter)
    }

    @Test
    fun testBindProjectRisks() {
        setUpEnvironment(environment())
        val risks = "Risks and challenges"

        this.vm.configureWith(ProjectDataFactory.project(ProjectFactory.project()))

        this.projectRisks.assertValue(risks)
    }

    @Test
    fun testOpenLearnAboutAccountabilityOnKickstarter() {
        setUpEnvironment(environment())

        this.vm.inputs.onLearnAboutAccountabilityOnKickstarterClicked()

        this.openLearnAboutAccountabilityOnKickstarter.assertValueCount(1)
    }
}
