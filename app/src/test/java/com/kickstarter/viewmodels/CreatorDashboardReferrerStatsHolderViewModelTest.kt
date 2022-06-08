package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.ReferrerStats
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.ArrayList

class CreatorDashboardReferrerStatsHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CreatorDashboardReferrerStatsHolderViewModel.ViewModel

    private val projectOutput = TestSubscriber<Project>()
    private val referrerStatsListIsGone = TestSubscriber<Boolean>()
    private val referrerStatsOutput = TestSubscriber<List<ReferrerStats>>()
    private val referrersTitleIsLimitedCopy = TestSubscriber<Boolean>()

    protected fun setUpEnvironment(environment: Environment) {
        vm = CreatorDashboardReferrerStatsHolderViewModel.ViewModel(environment)
        vm.outputs.projectAndReferrerStats()
            .map {
                PairUtils.first(it)
            }.subscribe(
                projectOutput
            )
        vm.outputs.projectAndReferrerStats()
            .map {
                PairUtils.second(it)
            }.subscribe(
                referrerStatsOutput
            )
        vm.outputs.referrerStatsListIsGone().subscribe(referrerStatsListIsGone)
        vm.outputs.referrersTitleIsTopTen().subscribe(referrersTitleIsLimitedCopy)
    }

    @Test
    fun testProjectAndReferrerStats() {
        val project = project()
        val referrerWithOnePledged = referrerStats().toBuilder().pledged(1f).build()
        val referrerWithTwoPledged = referrerStats().toBuilder().pledged(2f).build()
        val referrerWithThreePledged = referrerStats().toBuilder().pledged(3f).build()
        val unsortedReferrerList =
            listOf(referrerWithOnePledged, referrerWithThreePledged, referrerWithTwoPledged)
        val sortedReferrerList =
            listOf(referrerWithThreePledged, referrerWithTwoPledged, referrerWithOnePledged)

        setUpEnvironment(environment())

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project, unsortedReferrerList))
        projectOutput.assertValues(project)
        referrerStatsOutput.assertValue(sortedReferrerList)
    }

    @Test
    fun testReferrerStatsListIsGone() {

        setUpEnvironment(environment())

        val project = project()

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project, ArrayList()))

        referrerStatsListIsGone.assertValue(true)
        referrersTitleIsLimitedCopy.assertValue(false)

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project, listOf(referrerStats())))

        referrerStatsListIsGone.assertValues(true, false)
        referrersTitleIsLimitedCopy.assertValue(false)
    }

    @Test
    fun testReferrersTitleIsLimitedCopy() {

        setUpEnvironment(environment())

        val project = project()

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project, listOf(referrerStats())))
        referrersTitleIsLimitedCopy.assertValue(false)

        val maxStats: MutableList<ReferrerStats> = ArrayList()

        for (i in 1..10) {
            maxStats.add(referrerStats().toBuilder().pledged(i.toFloat()).build())
        }

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project, maxStats))

        referrersTitleIsLimitedCopy.assertValues(false)

        maxStats.add(referrerStats().toBuilder().pledged(11f).build())

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project, maxStats))

        referrersTitleIsLimitedCopy.assertValues(false, true)
    }
}
