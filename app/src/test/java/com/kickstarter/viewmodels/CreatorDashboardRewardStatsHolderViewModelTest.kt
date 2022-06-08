package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.RewardStats
import org.junit.Test
import rx.observers.TestSubscriber

class CreatorDashboardRewardStatsHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CreatorDashboardRewardStatsHolderViewModel.ViewModel

    private val projectOutput = TestSubscriber<Project>()
    private val rewardStatsOutput = TestSubscriber<List<RewardStats>>()
    private val rewardsStatsListIsGone = TestSubscriber<Boolean>()
    private val rewardsStatsTruncatedTextIsGone = TestSubscriber<Boolean>()
    private val rewardsTitleIsLimitedCopy = TestSubscriber<Boolean>()
    protected fun setUpEnvironment(environment: Environment) {
        vm = CreatorDashboardRewardStatsHolderViewModel.ViewModel(environment)
        vm.outputs.projectAndRewardStats()
            .map {
                PairUtils.first(it)
            }.subscribe(
                projectOutput
            )
        vm.outputs.projectAndRewardStats()
            .map {
                PairUtils.second(it)
            }.subscribe(
                rewardStatsOutput
            )
        vm.outputs.rewardsStatsListIsGone().subscribe(rewardsStatsListIsGone)
        vm.outputs.rewardsStatsTruncatedTextIsGone().subscribe(rewardsStatsTruncatedTextIsGone)
        vm.outputs.rewardsTitleIsTopTen().subscribe(rewardsTitleIsLimitedCopy)
    }

    @Test
    fun testProjectAndRewardStats() {
        val project = project()
        val rewardWith10Pledged = rewardStats().toBuilder().pledged(10f).build()
        val rewardWith15Pledged = rewardStats().toBuilder().pledged(15f).build()
        val rewardWith20Pledged = rewardStats().toBuilder().pledged(20f).build()
        val unsortedRewardStatsList =
            listOf(rewardWith15Pledged, rewardWith10Pledged, rewardWith20Pledged)
        val sortedRewardStatsList =
            listOf(rewardWith20Pledged, rewardWith15Pledged, rewardWith10Pledged)

        setUpEnvironment(environment())

        vm.inputs.projectAndRewardStatsInput(Pair.create(project, unsortedRewardStatsList))

        projectOutput.assertValue(project)
        rewardStatsOutput.assertValue(sortedRewardStatsList)
    }

    @Test
    fun testRewardsStatsListIsGone() {

        setUpEnvironment(environment())

        val project = project()

        vm.inputs.projectAndRewardStatsInput(Pair.create(project, ArrayList()))
        rewardsStatsListIsGone.assertValue(true)
        rewardsStatsTruncatedTextIsGone.assertValue(true)

        vm.inputs.projectAndRewardStatsInput(Pair.create(project, listOf(rewardStats())))
        rewardsStatsListIsGone.assertValues(true, false)
        rewardsStatsTruncatedTextIsGone.assertValue(true)
    }

    @Test
    fun testRewardsStatsTruncatedTextIsGone() {
        setUpEnvironment(environment())
        val project = project()

        vm.inputs.projectAndRewardStatsInput(Pair.create(project, listOf(rewardStats())))
        rewardsStatsTruncatedTextIsGone.assertValue(true)

        val maxStats: MutableList<RewardStats> = ArrayList()

        for (i in 1..10) {
            maxStats.add(rewardStats().toBuilder().pledged(i.toFloat()).build())
        }

        vm.inputs.projectAndRewardStatsInput(Pair.create(project, maxStats))
        rewardsStatsTruncatedTextIsGone.assertValues(true)
        maxStats.add(rewardStats().toBuilder().pledged(11f).build())

        vm.inputs.projectAndRewardStatsInput(Pair.create(project, maxStats))
        rewardsStatsTruncatedTextIsGone.assertValues(true, false)
    }

    @Test
    fun rewardsTitleIsLimitedCopy() {
        setUpEnvironment(environment())
        val project = project()

        vm.inputs.projectAndRewardStatsInput(Pair.create(project, listOf(rewardStats())))
        rewardsTitleIsLimitedCopy.assertValue(false)

        val maxStats: MutableList<RewardStats> = ArrayList()
        for (i in 1..10) {
            maxStats.add(rewardStats().toBuilder().pledged(i.toFloat()).build())
        }

        vm.inputs.projectAndRewardStatsInput(Pair.create(project, maxStats))
        rewardsTitleIsLimitedCopy.assertValues(false)
        maxStats.add(rewardStats().toBuilder().pledged(11f).build())

        vm.inputs.projectAndRewardStatsInput(Pair.create(project, maxStats))
        rewardsTitleIsLimitedCopy.assertValues(false, true)
    }
}
