package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockStatsigClient
import com.kickstarter.libs.featureflag.StatsigExperiments
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.viewmodels.projectpage.ExperimentsViewModel
import com.statsig.androidsdk.DynamicConfig
import com.statsig.androidsdk.StatsigUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class ExperimentsViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: ExperimentsViewModel

    private fun mockStatsigClient(
        getExperiment: (String) -> Unit
    ) = object : MockStatsigClient(context = application(), startReady = false) {
        override fun getExperiment(experimentName: String): DynamicConfig {
            getExperiment(experimentName)
            return super.getExperiment(experimentName)
        }
    }

    private fun setUpEnvironment(environment: Environment, dispatcher: CoroutineDispatcher) {
        viewModel = ExperimentsViewModel.Factory(environment, dispatcher)
            .create(ExperimentsViewModel::class.java)
    }

    @Test
    fun `test getExperiment is only called when StatsigClient is ready`() = runTest {
        var count = 0
        val statsigClient = mockStatsigClient { experimentName ->
            count++
        }

        val environment = environment().toBuilder()
            .statsigClient(statsigClient)
            .build()

        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        setUpEnvironment(environment, unconfinedDispatcher)

        assertEquals(0, count)

        statsigClient.triggerReady()

        assertEquals(2, count)
    }

    @Test
    fun `test getExperiment is called when StatsigUser is emitted`() = runTest {
        val countMap = mutableMapOf<String, Int>()
        val statsigClient = mockStatsigClient { experimentName ->
            countMap[experimentName] = (countMap[experimentName] ?: 0) + 1
        }

        val environment = environment().toBuilder()
            .statsigClient(statsigClient)
            .build()

        val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)
        setUpEnvironment(environment, unconfinedDispatcher)

        statsigClient.triggerReady()

        assertEquals(1, countMap[StatsigExperiments.NoOpAuthenticatedUsers.name])
        assertEquals(1, countMap[StatsigExperiments.NoOpAnonymousUsers.name])

        statsigClient.setStatsigUser(StatsigUser(UserFactory.user().id().toString()))

        assertEquals(2, countMap[StatsigExperiments.NoOpAuthenticatedUsers.name])
        assertEquals(2, countMap[StatsigExperiments.NoOpAnonymousUsers.name])
    }
}
