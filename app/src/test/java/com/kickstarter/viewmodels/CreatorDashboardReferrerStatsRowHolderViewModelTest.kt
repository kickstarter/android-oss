package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.ReferrerStats
import org.junit.Test
import rx.observers.TestSubscriber

class CreatorDashboardReferrerStatsRowHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel

    private val projectAndPledgedForReferrer = TestSubscriber<Pair<Project, Float>>()
    private val referrerBackerCount = TestSubscriber<String>()
    private val referrerSourceColorId = TestSubscriber<Int>()
    private val referrerSourceName = TestSubscriber<String>()

    protected fun setUpEnvironment(environment: Environment) {
        vm = CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel(environment)

        vm.outputs.projectAndPledgedForReferrer().subscribe(projectAndPledgedForReferrer)
        vm.outputs.referrerBackerCount().subscribe(referrerBackerCount)
        vm.outputs.referrerSourceColorId().subscribe(referrerSourceColorId)
        vm.outputs.referrerSourceName().subscribe(referrerSourceName)
    }

    @Test
    fun testProjectAndPledgedForReferrer() {
        val project = project().toBuilder().pledged(100.0).build()
        val referrerStats = referrerStats()
            .toBuilder()
            .pledged(50f)
            .build()
        val pledgedFloat = referrerStats.pledged()

        setUpEnvironment(environment())

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project, referrerStats))
        projectAndPledgedForReferrer.assertValue(Pair.create(project, pledgedFloat))
    }

    @Test
    fun testReferrerBackerCount() {
        val referrerStats = referrerStats()
            .toBuilder()
            .backersCount(10)
            .build()

        setUpEnvironment(environment())

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project(), referrerStats))
        referrerBackerCount.assertValues(NumberUtils.format(10))
    }

    @Test
    fun testReferrerSourceColor_WhenCustom() {
        val referrerStats = getReferrerStat("custom")

        setUpEnvironment(environment())

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project(), referrerStats))
        referrerSourceColorId.assertValues(R.color.kds_create_300)
    }

    @Test
    fun testReferrerSourceColor_WhenExternal() {
        val referrerStats = getReferrerStat("external")

        setUpEnvironment(environment())

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project(), referrerStats))
        referrerSourceColorId.assertValues(R.color.kds_create_700)
    }

    @Test
    fun testReferrerSourceColor_WhenKickstarter() {
        val referrerStats = getReferrerStat("kickstarter")

        setUpEnvironment(environment())

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project(), referrerStats))
        referrerSourceColorId.assertValues(R.color.kds_create_700)
    }

    @Test
    fun testReferrerSourceName() {
        val referrerStats = referrerStats()
            .toBuilder()
            .referrerName("Friends Backed Email")
            .build()

        setUpEnvironment(environment())

        vm.inputs.projectAndReferrerStatsInput(Pair.create(project(), referrerStats))
        referrerSourceName.assertValues("Friends Backed Email")
    }

    private fun getReferrerStat(referrerType: String): ReferrerStats {
        return referrerStats()
            .toBuilder()
            .referrerType(referrerType)
            .build()
    }
}
