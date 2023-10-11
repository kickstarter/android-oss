package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectEnvironmentalCommitmentFactory.Companion.getEnvironmentalCommitments
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.viewmodels.projectpage.ProjectEnvironmentalCommitmentsViewModel.ProjectEnvironmentalCommitmentsViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ProjectEnvironmentalCommitmentsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ProjectEnvironmentalCommitmentsViewModel

    private val projectEnvironmentalCommitment = TestSubscriber.create<List<EnvironmentalCommitment>>()
    private val openVisitOurEnvironmentalResourcesCenter = TestSubscriber.create<String>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = com.kickstarter.viewmodels.projectpage.ProjectEnvironmentalCommitmentsViewModel.Factory(
            environment
        ).create(
            ProjectEnvironmentalCommitmentsViewModel::class.java
        )

        this.vm.outputs.projectEnvironmentalCommitment().subscribe { this.projectEnvironmentalCommitment.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.openVisitOurEnvironmentalResourcesCenter().subscribe { this.openVisitOurEnvironmentalResourcesCenter.onNext(it) }.addToDisposable(disposables)
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }

    @Test
    fun testBindProjectEnvironmentalCommitmentList() {
        setUpEnvironment(environment())
        val environmentalCommitmentList = getEnvironmentalCommitments()

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
