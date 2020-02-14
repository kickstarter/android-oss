package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import org.junit.Test
import rx.observers.TestSubscriber

class RewardsFragmentViewModelTest: KSRobolectricTestCase() {

    private lateinit var vm: RewardsFragmentViewModel.ViewModel
    private val backedRewardPosition = TestSubscriber.create<Int>()
    private val projectData = TestSubscriber.create<ProjectData>()
    private val rewardsCount = TestSubscriber.create<Int>()
    private val showPledgeFragment = TestSubscriber<Pair<PledgeData, PledgeReason>>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = RewardsFragmentViewModel.ViewModel(environment)
        this.vm.outputs.backedRewardPosition().subscribe(this.backedRewardPosition)
        this.vm.outputs.projectData().subscribe(this.projectData)
        this.vm.outputs.rewardsCount().subscribe(this.rewardsCount)
        this.vm.outputs.showPledgeFragment().subscribe(this.showPledgeFragment)
    }

    @Test
    fun testBackedRewardPosition() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))
        this.backedRewardPosition.assertNoValues()

        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))
        this.backedRewardPosition.assertValue(1)

        val backedSuccessfulProject = backedProject
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedSuccessfulProject))
        this.backedRewardPosition.assertValue(1)
    }

    @Test
    fun testProjectData() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        val projectData = ProjectDataFactory.project(project)
        this.vm.inputs.configureWith(projectData)
        this.projectData.assertValue(projectData)
    }

    @Test
    fun testShowPledgeFragment_whenBackingProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        val reward = RewardFactory.reward()
        this.vm.inputs.rewardClicked(reward)
        this.showPledgeFragment.assertValue(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.NEW_PLEDGE)
                .reward(reward)
                .projectData(ProjectDataFactory.project(project))
                .build(), PledgeReason.PLEDGE))
    }

    @Test
    fun testShowPledgeFragment_whenManagingPledge() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        val reward = RewardFactory.reward()
        this.vm.inputs.rewardClicked(reward)
        this.showPledgeFragment.assertValue(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.CHANGE_REWARD)
                .reward(reward)
                .projectData(ProjectDataFactory.project(project))
                .build(), PledgeReason.UPDATE_REWARD))
    }

    @Test
    fun testRewardsCount() {
        val project = ProjectFactory.project()
                .toBuilder()
                .rewards(listOf(RewardFactory.noReward(), RewardFactory.reward()))
                .build()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        this.rewardsCount.assertValue(2)
    }
}
