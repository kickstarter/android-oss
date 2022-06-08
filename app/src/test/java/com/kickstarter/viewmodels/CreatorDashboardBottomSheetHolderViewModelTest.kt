package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory.project
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class CreatorDashboardBottomSheetHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: CreatorDashboardBottomSheetHolderViewModel.ViewModel
    private val projectName = TestSubscriber<String>()
    private val projectLaunchDate = TestSubscriber<DateTime>()

    private fun setUpEnvironment(environment: Environment) {
        vm = CreatorDashboardBottomSheetHolderViewModel.ViewModel(environment)
        vm.outputs.projectNameText().subscribe(projectName)
        vm.outputs.projectLaunchDate().subscribe(projectLaunchDate)
    }

    @Test
    fun testProjectNameText() {
        setUpEnvironment(environment())
        val projectName = "Test Project"
        val now = DateTime.now()
        val project = project().toBuilder().name(projectName).launchedAt(now).build()
        vm.inputs.projectInput(project)
        this.projectName.assertValues(projectName)
        projectLaunchDate.assertValue(now)
    }
}
