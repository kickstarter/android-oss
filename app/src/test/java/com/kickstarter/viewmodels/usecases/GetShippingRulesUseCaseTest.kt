package com.kickstarter.viewmodels.usecases

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetShippingRulesUseCaseTest : KSRobolectricTestCase() {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test useCase returns project reward list unfiltered when not collecting`() = runTest {
        val apolloClient = MockApolloClientV2()
        val config = ConfigFactory.configForCA()
        val project = ProjectFactory.project()
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .isInPostCampaignPledgingPhase(false)
            .postCampaignPledgingEnabled(false)
            .build()

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val scope = backgroundScope

        val useCase = GetShippingRulesUseCase(project, config, project.rewards() ?: emptyList(), scope, dispatcher)

        val state = mutableListOf<ShippingRulesState>()
        scope.launch(dispatcher) {
            useCase.invoke()
            useCase.shippingRulesState.toList(state)
        }
        advanceUntilIdle()

        assertEquals(state.size, 2)
        assertEquals(state.last().filteredRw, project.rewards())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test useCase returns project reward list filtered when collecting late pledges`() = runTest {
        val apolloClient = MockApolloClientV2()
        val config = ConfigFactory.configForUSUser()

        val shippingRule1 = ShippingRuleFactory.canadaShippingRule()
        val shippingRule2 = ShippingRuleFactory.germanyShippingRule()
        val shippingRule3 = ShippingRuleFactory.usShippingRule()

        val reward = RewardFactory.reward().toBuilder()
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name)
            .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS)
            .isAvailable(true)
            .shippingRules(listOf(shippingRule1))
            .build()
        val reward2 = reward.toBuilder()
            .shippingRules(listOf(shippingRule1, shippingRule2, shippingRule3))
            .build()

        val rwList = listOf(reward, reward2)
        val project = ProjectFactory.project()
            .toBuilder()
            .rewards(rwList)
            .state(Project.STATE_SUCCESSFUL)
            .isInPostCampaignPledgingPhase(true)
            .postCampaignPledgingEnabled(true)
            .build()

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val scope = backgroundScope

        val useCase = GetShippingRulesUseCase(project, config, rwList, scope, dispatcher)

        val state = mutableListOf<ShippingRulesState>()
        scope.launch(dispatcher) {
            useCase.invoke()
            useCase.shippingRulesState.toList(state)
        }
        advanceUntilIdle()

        assertEquals(state.size, 2)
        assertNotSame(state.last().filteredRw, project.rewards())
        assertEquals(state.last().filteredRw.size, 1)
        assertEquals(state.last().filteredRw.first(), reward2)
    }
}
