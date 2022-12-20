package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.viewmodels.projectpage.ProjectRiskViewModel.ProjectRiskViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ProjectRiskViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ProjectRiskViewModel

    private val projectRisks = TestSubscriber.create<String>()
    private val openLearnAboutAccountabilityOnKickstarter = TestSubscriber.create<String>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectRiskViewModel(environment)

        disposables.add(this.vm.outputs.projectRisks().subscribe { this.projectRisks.onNext(it) })
        disposables.add(
            this.vm.outputs.openLearnAboutAccountabilityOnKickstarter().subscribe {
                this.openLearnAboutAccountabilityOnKickstarter.onNext(it)
            }
        )
    }

    @After
    fun cleanUp() {
        disposables.clear()
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
