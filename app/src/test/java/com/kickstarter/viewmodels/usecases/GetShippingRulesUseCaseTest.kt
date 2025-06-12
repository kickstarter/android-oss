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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `no reward is always added first even if unavailable`() = runTest {
        val config = ConfigFactory.configForUSUser()
        val noReward = RewardFactory.noReward().toBuilder()
            .isAvailable(false) // Force it to be unavailable
            .build()

        val regularReward = RewardFactory.reward().toBuilder()
            .isAvailable(true)
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name)
            .shippingRules(listOf(ShippingRuleFactory.usShippingRule()))
            .build()

        val project = ProjectFactory.project()
            .toBuilder()
            .rewards(listOf(regularReward, noReward))
            .isInPostCampaignPledgingPhase(true)
            .postCampaignPledgingEnabled(true)
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

        val filtered = state.last().filteredRw
        assertTrue(filtered.isNotEmpty())
        assertEquals(noReward, filtered.first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `unavailable rewards are appended at the end`() = runTest {
        val config = ConfigFactory.configForUSUser()
        val availableReward = RewardFactory.reward().toBuilder()
            .isAvailable(true)
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
            .build()

        val unavailableReward = RewardFactory.reward().toBuilder()
            .isAvailable(false)
            .build()

        val project = ProjectFactory.project()
            .toBuilder()
            .rewards(listOf(availableReward, unavailableReward))
            .isInPostCampaignPledgingPhase(true)
            .postCampaignPledgingEnabled(true)
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

        val filtered = state.last().filteredRw
        assertEquals(2, filtered.size)
        assertEquals(unavailableReward, filtered.last())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `secret rewards are included if available and sorted by minimum pledge`() = runTest {
        val config = ConfigFactory.configForUSUser()

        val noReward = RewardFactory.noReward().toBuilder()
            .isAvailable(true)
            .build()

        val secretReward1 = RewardFactory.reward().toBuilder()
            .isSecretReward(true)
            .minimum(30.0)
            .isAvailable(true)
            .build()

        val secretReward2 = RewardFactory.reward().toBuilder()
            .isSecretReward(true)
            .minimum(10.0)
            .isAvailable(true)
            .build()

        val regularReward = RewardFactory.reward().toBuilder()
            .isAvailable(true)
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
            .build()

        val project = ProjectFactory.project()
            .toBuilder()
            .rewards(listOf(regularReward, secretReward1, secretReward2, noReward))
            .isInPostCampaignPledgingPhase(true)
            .postCampaignPledgingEnabled(true)
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

        val filtered = state.last().filteredRw
        assertEquals(4, filtered.size)
        assertEquals(noReward, filtered[0]) // "no reward" first
        assertEquals(secretReward2, filtered[1]) // secret reward with min = 10
        assertEquals(secretReward1, filtered[2]) // secret reward with min = 30
        assertEquals(regularReward, filtered[3]) // available regular reward at the end
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `unavailable secret rewards are added at the end with other unavailable rewards`() = runTest {
        val config = ConfigFactory.configForUSUser()

        val secretUnavailable = RewardFactory.reward().toBuilder()
            .isSecretReward(true)
            .isAvailable(false)
            .minimum(50.0)
            .build()

        val regularAvailable = RewardFactory.reward().toBuilder()
            .isAvailable(true)
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
            .build()

        val project = ProjectFactory.project()
            .toBuilder()
            .rewards(listOf(regularAvailable, secretUnavailable))
            .isInPostCampaignPledgingPhase(true)
            .postCampaignPledgingEnabled(true)
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

        val filtered = state.last().filteredRw

        assertEquals(2, filtered.size)
        assertEquals(regularAvailable, filtered[0]) // Eligible reward comes first
        assertEquals(secretUnavailable, filtered[1]) // Unavailable secret reward at the end
    }
}
