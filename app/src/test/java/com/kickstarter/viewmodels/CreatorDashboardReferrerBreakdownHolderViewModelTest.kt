package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory.referralAggregates
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory.projectStatsEnvelope
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope
import org.junit.Test
import rx.observers.TestSubscriber

class CreatorDashboardReferrerBreakdownHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel

    private val breakdownViewIsGone = TestSubscriber<Boolean>()
    private val emptyViewIsGone = TestSubscriber<Boolean>()
    private val customReferrerPercent = TestSubscriber<Float>()
    private val externalReferrerPercent = TestSubscriber<Float>()
    private val kickstarterReferrerPercent = TestSubscriber<Float>()
    private val customReferrerPercentText = TestSubscriber<String>()
    private val externalReferrerPercentText = TestSubscriber<String>()
    private val kickstarterReferrerPercentText = TestSubscriber<String>()
    private val pledgedViaCustomLayoutIsGone = TestSubscriber<Boolean>()
    private val pledgedViaExternalLayoutIsGone = TestSubscriber<Boolean>()
    private val pledgedViaKickstarterLayoutIsGone = TestSubscriber<Boolean>()
    private val projectAndAveragePledge = TestSubscriber<Pair<Project, Int>>()
    private val projectAndCustomReferrerPledgedAmount = TestSubscriber<Pair<Project, Float>>()
    private val projectAndExternalReferrerPledgedAmount = TestSubscriber<Pair<Project, Float>>()
    private val projectAndKickstarterReferrerPledgedAmount = TestSubscriber<Pair<Project, Float>>()

    protected fun setUpEnvironment(environment: Environment) {
        vm = CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel(environment)

        vm.outputs.breakdownViewIsGone().subscribe(breakdownViewIsGone)
        vm.outputs.emptyViewIsGone().subscribe(emptyViewIsGone)
        vm.outputs.customReferrerPercent().subscribe(customReferrerPercent)
        vm.outputs.customReferrerPercentText().subscribe(customReferrerPercentText)
        vm.outputs.externalReferrerPercent().subscribe(externalReferrerPercent)
        vm.outputs.externalReferrerPercentText().subscribe(externalReferrerPercentText)
        vm.outputs.kickstarterReferrerPercent().subscribe(kickstarterReferrerPercent)
        vm.outputs.kickstarterReferrerPercentText().subscribe(kickstarterReferrerPercentText)
        vm.outputs.pledgedViaCustomLayoutIsGone().subscribe(pledgedViaCustomLayoutIsGone)
        vm.outputs.pledgedViaExternalLayoutIsGone().subscribe(pledgedViaExternalLayoutIsGone)
        vm.outputs.pledgedViaKickstarterLayoutIsGone()
            .subscribe(pledgedViaKickstarterLayoutIsGone)
        vm.outputs.projectAndAveragePledge().subscribe(projectAndAveragePledge)
        vm.outputs.projectAndCustomReferrerPledgedAmount().subscribe(
            projectAndCustomReferrerPledgedAmount
        )
        vm.outputs.projectAndExternalReferrerPledgedAmount().subscribe(
            projectAndExternalReferrerPledgedAmount
        )
        vm.outputs.projectAndKickstarterReferrerPledgedAmount()
            .subscribe(projectAndKickstarterReferrerPledgedAmount)
    }

    @Test
    fun testBreakdownViewIsGone_isTrue_whenStatsEmpty() {
        setUpEnvironmentAndInputProjectAndEmptyStats()
        breakdownViewIsGone.assertValues(true)
    }

    @Test
    fun testBreakdownViewIsGone_isFalse_whenStatsNotEmpty() {
        setUpEnvironmentAndInputProjectAndStats()
        breakdownViewIsGone.assertValues(false)
    }

    @Test
    fun testEmptyViewIsGone_isFalse_whenStatsEmpty() {
        setUpEnvironmentAndInputProjectAndEmptyStats()
        emptyViewIsGone.assertValues(false)
    }

    @Test
    fun testEmptyViewIsGone_isTrue_whenStatsNotEmpty() {
        setUpEnvironmentAndInputProjectAndStats()
        emptyViewIsGone.assertValues(true)
    }

    @Test
    fun testCustomReferrerPercent() {
        setUpEnvironmentAndInputProjectAndStats()
        customReferrerPercent.assertValues(.5f)
    }

    @Test
    fun testCustomReferrerPercentText() {
        setUpEnvironmentAndInputProjectAndStats()
        customReferrerPercentText.assertValues(NumberUtils.flooredPercentage(.5f * 100f))
    }

    @Test
    fun testExternalReferrerPercent() {
        setUpEnvironmentAndInputProjectAndStats()
        externalReferrerPercent.assertValues(.25f)
    }

    @Test
    fun testExternalReferrerPercentText() {
        setUpEnvironmentAndInputProjectAndStats()
        externalReferrerPercentText.assertValues(NumberUtils.flooredPercentage(.25f * 100f))
    }

    @Test
    fun testKickstarterReferrerPercent() {
        setUpEnvironmentAndInputProjectAndStats()
        kickstarterReferrerPercent.assertValues(.25f)
    }

    @Test
    fun testKickstarterReferrerPercentText() {
        setUpEnvironmentAndInputProjectAndStats()
        kickstarterReferrerPercentText.assertValues(NumberUtils.flooredPercentage(.25f * 100f))
    }

    @Test
    fun testPledgedViaCustomLayoutIsGone_isTrue_WhenStatsEmpty() {
        setUpEnvironmentAndInputProjectAndEmptyStats()
        pledgedViaCustomLayoutIsGone.assertValues(true)
    }

    @Test
    fun testPledgedViaCustomLayoutIsGone_isFalse_WhenStatsNotEmpty() {
        setUpEnvironmentAndInputProjectAndStats()
        pledgedViaCustomLayoutIsGone.assertValues(false)
    }

    @Test
    fun testPledgedViaExternalLayoutIsGone_isTrue_WhenStatsEmpty() {
        setUpEnvironmentAndInputProjectAndEmptyStats()
        pledgedViaExternalLayoutIsGone.assertValues(true)
    }

    @Test
    fun testPledgedViaExternalLayoutIsGone_isFalse_WhenStatsNotEmpty() {
        setUpEnvironmentAndInputProjectAndStats()
        pledgedViaExternalLayoutIsGone.assertValues(false)
    }

    @Test
    fun testPledgedViaKickstarterLayoutIsGone_isTrue_WhenStatsEmpty() {
        setUpEnvironmentAndInputProjectAndEmptyStats()
        pledgedViaKickstarterLayoutIsGone.assertValues(true)
    }

    @Test
    fun testPledgedViaKickstarterLayoutIsGone_isFalse_WhenStatsNotEmpty() {
        setUpEnvironmentAndInputProjectAndStats()
        pledgedViaKickstarterLayoutIsGone.assertValues(false)
    }

    @Test
    fun testProjectAndAveragePledge() {
        val project = project()
        val cumulativeStats = cumulativeStats()
            .toBuilder()
            .averagePledge(10f)
            .build()
        val statsEnvelope = projectStatsEnvelope()
            .toBuilder()
            .cumulative(cumulativeStats)
            .build()
        setUpEnvironment(environment())
        vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope))
        projectAndAveragePledge.assertValue(Pair.create(project, 10))
    }

    @Test
    fun testProjectCumulativeNull() {
        val project = project()
        val statsEnvelope = projectStatsEnvelope()
            .toBuilder()
            .cumulative(null)
            .build()
        setUpEnvironment(environment())
        vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope))
        projectAndAveragePledge.assertNoValues()
    }

    @Test
    fun testProjectAndCustomReferrerPledgedAmount() {
        val project = setUpEnvironmentAndInputProjectAndStats()
        projectAndCustomReferrerPledgedAmount.assertValue(Pair.create(project, 100f))
    }

    @Test
    fun testProjectAndExternalReferrerPledgedAmount() {
        val project = setUpEnvironmentAndInputProjectAndStats()
        projectAndExternalReferrerPledgedAmount.assertValue(Pair.create(project, 50f))
    }

    @Test
    fun testProjectAndKickstarterReferrerPledgedAmount() {
        val project = setUpEnvironmentAndInputProjectAndStats()
        projectAndKickstarterReferrerPledgedAmount.assertValue(Pair.create(project, 50f))
    }

    @Test
    fun testReferrerPercents() {
        val project = project()
        val referralAggregateStats = referralAggregates()
            .toBuilder()
            .custom(100f)
            .internal(50f)
            .external(50f)
            .build()
        val cumulativeStats = cumulativeStats()
            .toBuilder()
            .pledged(200f)
            .build()
        val projectStatsEnvelope = projectStatsEnvelope().toBuilder()
            .referralAggregates(referralAggregateStats)
            .cumulative(cumulativeStats)
            .build()
        setUpEnvironment(environment())
        vm.inputs.projectAndStatsInput(Pair.create(project, projectStatsEnvelope))
        customReferrerPercent.assertValues(.5f)
        externalReferrerPercent.assertValues(.25f)
        kickstarterReferrerPercent.assertValues(.25f)
    }

    private fun setUpEnvironmentAndInputProjectAndStats(): Project {
        val project = project()
        val projectStatsEnvelope = projectStatsEnvelope
        setUpEnvironment(environment())
        vm.inputs.projectAndStatsInput(Pair.create(project, projectStatsEnvelope))
        return project
    }

    private fun setUpEnvironmentAndInputProjectAndEmptyStats(): Project {
        val project = project()
        val projectStatsEnvelope = emptyProjectStatsEnvelope
        setUpEnvironment(environment())
        vm.inputs.projectAndStatsInput(Pair.create(project, projectStatsEnvelope))
        return project
    }

    private val emptyProjectStatsEnvelope: ProjectStatsEnvelope
        private get() {
            val referralAggregateStats = referralAggregates()
                .toBuilder()
                .custom(0f)
                .external(0f)
                .internal(0f)
                .build()
            val cumulativeStats = cumulativeStats()
                .toBuilder()
                .pledged(0f)
                .build()
            return projectStatsEnvelope()
                .toBuilder()
                .cumulative(cumulativeStats)
                .referralAggregates(referralAggregateStats)
                .referralDistribution(ListUtils.empty())
                .build()
        }

    private val projectStatsEnvelope: ProjectStatsEnvelope
        private get() {
            val referralAggregateStats = referralAggregates()
                .toBuilder()
                .custom(100f)
                .internal(50f)
                .external(50f)
                .build()
            val cumulativeStats = cumulativeStats()
                .toBuilder()
                .pledged(200f)
                .build()
            return projectStatsEnvelope().toBuilder()
                .referralAggregates(referralAggregateStats)
                .cumulative(cumulativeStats)
                .build()
        }
}
