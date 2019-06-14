package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import org.junit.Test
import rx.observers.TestSubscriber

class HorizontalNoRewardViewHolderViewModelTest: KSRobolectricTestCase() {

    private lateinit var vm: HorizontalNoRewardViewHolderViewModel.ViewModel
    private val buttonIsGone = TestSubscriber.create<Boolean>()
    private val buttonTint = TestSubscriber.create<Int>()
    private val checkIsGone = TestSubscriber.create<Boolean>()
    private val showPledgeFragment = TestSubscriber<Pair<Project, Reward>>()
    private val startBackingActivity = TestSubscriber<Project>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = HorizontalNoRewardViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.buttonIsGone().subscribe(this.buttonIsGone)
        this.vm.outputs.buttonTint().subscribe(this.buttonTint)
        this.vm.outputs.checkIsGone().subscribe(this.checkIsGone)
        this.vm.outputs.showPledgeFragment().subscribe(this.showPledgeFragment)
        this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity)
    }

    @Test
    fun testButtonUIOutputs() {
        setUpEnvironment(environment())
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()

        this.vm.inputs.projectAndReward(project, reward)
        this.buttonIsGone.assertValue(false)
        this.buttonTint.assertValue(R.color.button_pledge_primary)

        this.vm.inputs.projectAndReward(project, RewardFactory.limitReached())
        this.buttonIsGone.assertValue(false)
        this.buttonTint.assertValue(R.color.button_pledge_primary)

        this.vm.inputs.projectAndReward(project, RewardFactory.ended())
        this.buttonIsGone.assertValue(false)
        this.buttonTint.assertValue(R.color.button_pledge_primary)

        val backedProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(backedProject, backedProject.backing()?.reward()?: RewardFactory.reward())
        this.buttonIsGone.assertValues(false)
        this.buttonTint.assertValue(R.color.button_pledge_primary)

        this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), reward)
        this.buttonIsGone.assertValues(false, true)
        this.buttonTint.assertValues(R.color.button_pledge_primary, R.color.button_pledge_ended)
    }

    @Test
    fun testCheckIsGone() {
        setUpEnvironment(environment())
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()

        this.vm.inputs.projectAndReward(project, reward)
        this.checkIsGone.assertValue(true)

        this.vm.inputs.projectAndReward(project, RewardFactory.limitReached())
        this.checkIsGone.assertValue(true)

        this.vm.inputs.projectAndReward(project, RewardFactory.ended())
        this.checkIsGone.assertValue(true)

        val successfulProject = ProjectFactory.successfulProject()
        this.vm.inputs.projectAndReward(successfulProject, reward)
        this.checkIsGone.assertValue(true)

        val backedLiveProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(backedLiveProject, backedLiveProject.backing()?.reward()?: RewardFactory.reward())
        this.checkIsGone.assertValue(true)

        val backedEndedProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(backedEndedProject, backedEndedProject.backing()?.reward()?: RewardFactory.reward())
        this.checkIsGone.assertValues(true, false)
    }

    @Test
    fun testShowPledgeFragmentWhenProjectIsSuccessful() {
        val project = ProjectFactory.successfulProject()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.showPledgeFragment.assertNoValues()

        this.vm.inputs.rewardClicked()
        this.showPledgeFragment.assertNoValues()
    }

    @Test
    fun testShowPledgeFragmentWhenProjectIsSuccessfulAndHasBeenBacked() {
        val project = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        val reward = project.backing()?.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward!!)
        this.showPledgeFragment.assertNoValues()

        this.vm.inputs.rewardClicked()
        this.showPledgeFragment.assertNoValues()
    }

    @Test
    fun testShowPledgeFragmentWhenProjectIsLive() {
        val reward = RewardFactory.reward()
        val liveProject = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(liveProject, reward)
        this.showPledgeFragment.assertNoValues()

        // When a reward from a live project is clicked, start checkout.
        this.vm.inputs.rewardClicked()
        this.showPledgeFragment.assertValue(Pair.create(liveProject, reward))
    }

    @Test
    fun testGoToViewPledge() {
        val liveProject = ProjectFactory.backedProject()
        val successfulProject = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(liveProject, liveProject.backing()!!.reward()!!)
        this.startBackingActivity.assertNoValues()

        // When the project is still live, don't go to 'view pledge'. Should go to checkout instead.
        this.vm.inputs.rewardClicked()
        this.startBackingActivity.assertNoValues()

        // When project is successful but not backed, don't go to view pledge.
        this.vm.inputs.projectAndReward(successfulProject, RewardFactory.reward())
        this.vm.inputs.rewardClicked()
        this.startBackingActivity.assertNoValues()

        // When project is successful and backed, go to view pledge.
        this.vm.inputs.projectAndReward(successfulProject, successfulProject.backing()!!.reward()!!)
        this.startBackingActivity.assertNoValues()
        this.vm.inputs.rewardClicked()
        this.startBackingActivity.assertValues(successfulProject)
    }
}
