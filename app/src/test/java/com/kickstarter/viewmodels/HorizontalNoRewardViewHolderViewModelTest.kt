package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import org.junit.Test
import rx.observers.TestSubscriber

class HorizontalNoRewardViewHolderViewModelTest: KSRobolectricTestCase() {

    private lateinit var vm: HorizontalNoRewardViewHolderViewModel.ViewModel
    private val startBackingActivity = TestSubscriber<Project>()
    private val startCheckoutActivity = TestSubscriber<Pair<Project, Reward>>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = HorizontalNoRewardViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity)
        this.vm.outputs.startCheckoutActivity().subscribe(this.startCheckoutActivity)
    }

    @Test
    fun testGoToCheckoutWhenProjectIsSuccessful() {
        val project = ProjectFactory.successfulProject()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.startCheckoutActivity.assertNoValues()

        this.vm.inputs.rewardClicked()
        this.startCheckoutActivity.assertNoValues()
    }

    @Test
    fun testGoToCheckoutWhenProjectIsSuccessfulAndHasBeenBacked() {
        val project = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        val reward = project.backing()?.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward!!)
        this.startCheckoutActivity.assertNoValues()

        this.vm.inputs.rewardClicked()
        this.startCheckoutActivity.assertNoValues()
    }

    @Test
    fun testGoToCheckoutWhenProjectIsLive() {
        val reward = RewardFactory.reward()
        val liveProject = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(liveProject, reward)
        this.startCheckoutActivity.assertNoValues()

        // When a reward from a live project is clicked, start checkout.
        this.vm.inputs.rewardClicked()
        this.startCheckoutActivity.assertValue(Pair.create(liveProject, reward))
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
