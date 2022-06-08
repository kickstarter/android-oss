package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats
import com.kickstarter.models.Project
import org.junit.Test
import rx.observers.TestSubscriber

class DashboardRewardStatsRowHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: DashboardRewardStatsRowHolderViewModel.ViewModel

    private val rewardBackerCount = TestSubscriber<String>()
    private val projectAndRewardMinimum = TestSubscriber<Pair<Project, Int>>()
    private val percentageOfTotalPledged = TestSubscriber<String>()
    private val projectAndRewardPledged = TestSubscriber<Pair<Project, Float>>()

    protected fun setUpEnvironment(environment: Environment) {
        vm = DashboardRewardStatsRowHolderViewModel.ViewModel(environment)
        vm.outputs.rewardBackerCount().subscribe(rewardBackerCount)
        vm.outputs.projectAndRewardMinimum().subscribe(projectAndRewardMinimum)
        vm.outputs.percentageOfTotalPledged().subscribe(percentageOfTotalPledged)
        vm.outputs.projectAndRewardPledged().subscribe(projectAndRewardPledged)
    }

    @Test
    fun testRewardBackerCount() {
        val rewardStats = rewardStats()
            .toBuilder()
            .backersCount(10)
            .build()

        setUpEnvironment(environment())

        vm.inputs.projectAndRewardStats(Pair.create(project(), rewardStats))
        rewardBackerCount.assertValues(NumberUtils.format(10))
    }

    @Test
    fun testRewardMinimum() {
        val rewardStats = rewardStats()
            .toBuilder()
            .minimum(5)
            .build()

        setUpEnvironment(environment())
        val project = project()

        vm.inputs.projectAndRewardStats(Pair.create(project, rewardStats))
        projectAndRewardMinimum.assertValue(Pair.create(project, 5))
    }

    @Test
    fun testPercentageOfTotalPledged() {
        val project = project().toBuilder().pledged(100.0).build()
        val rewardStats = rewardStats()
            .toBuilder()
            .pledged(50f)
            .build()

        setUpEnvironment(environment())

        vm.inputs.projectAndRewardStats(Pair.create(project, rewardStats))
        percentageOfTotalPledged.assertValues("(50%)")
    }

    @Test
    fun testProjectAndPledgedForReward() {
        val project = project().toBuilder().pledged(100.0).build()
        val rewardStats = rewardStats()
            .toBuilder()
            .pledged(50f)
            .build()

        setUpEnvironment(environment())

        vm.inputs.projectAndRewardStats(Pair.create(project, rewardStats))
        projectAndRewardPledged.assertValue(Pair.create(project, 50f))
    }
}
