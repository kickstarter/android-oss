package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ScreenLocation
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*

class RewardsFragmentViewModelTest: KSRobolectricTestCase() {

    private lateinit var vm: RewardsFragmentViewModel.ViewModel
    private val backedRewardPosition = TestSubscriber.create<Int>()
    private val project = TestSubscriber.create<Project>()
    private val showPledgeFragment = TestSubscriber<PledgeData>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = RewardsFragmentViewModel.ViewModel(environment)
        this.vm.outputs.backedRewardPosition().subscribe(this.backedRewardPosition)
        this.vm.outputs.project().subscribe(this.project)
        this.vm.outputs.showPledgeFragment().subscribe(this.showPledgeFragment)
    }

    @Test
    fun testBackedRewardPosition() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.project(project)
        this.backedRewardPosition.assertNoValues()

        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(Arrays.asList(RewardFactory.noReward(), reward))
                .build()
        this.vm.inputs.project(backedProject)
        this.backedRewardPosition.assertValue(1)

        val backedSuccessfulProject = backedProject
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.project(backedSuccessfulProject)
        this.backedRewardPosition.assertValue(1)
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
