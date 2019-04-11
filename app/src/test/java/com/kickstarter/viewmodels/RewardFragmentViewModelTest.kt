package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ScreenLocation
import org.junit.Test
import rx.observers.TestSubscriber

class RewardFragmentViewModelTest: KSRobolectricTestCase() {

    private lateinit var vm: RewardFragmentViewModel.ViewModel
    private val project = TestSubscriber.create<Project>()
    private val showPledgeFragment = TestSubscriber<PledgeData>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = RewardFragmentViewModel.ViewModel(environment)
        this.vm.outputs.project().subscribe(this.project)
        this.vm.outputs.showPledgeFragment().subscribe(this.showPledgeFragment)
    }

    @Test
    fun testProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.project(project)
        this.project.assertValue(project)
    }

    @Test
    fun testProjectViewModel_ShowPledgeFragment() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.project(project)

        val screenLocation = ScreenLocation(0f, 0f, 0f, 0f)
        val reward = RewardFactory.reward()
        this.vm.inputs.rewardClicked(screenLocation, reward)
        this.showPledgeFragment.assertValue(PledgeData(screenLocation, reward, project))
    }
}
