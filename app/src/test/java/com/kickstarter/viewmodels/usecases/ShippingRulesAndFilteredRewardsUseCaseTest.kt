package com.kickstarter.viewmodels.usecases

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRulesEnvelopeFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Reward
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import io.reactivex.Observable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ShippingRulesAndFilteredRewardsUseCaseTest: KSRobolectricTestCase() {

    @Test
    fun `test GetShippingRulesUseCase to return rewards`() = runTest {
        val unlimitedReward = RewardFactory.rewardWithShipping()

        val rewards = listOf(
            unlimitedReward
        )
        val project = ProjectFactory.project().toBuilder().rewards(rewards).build()

        val config = ConfigFactory.configForCA()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val testShippingRulesList = ShippingRulesEnvelopeFactory.shippingRules()
        val apolloClient = object : MockApolloClientV2() {
            override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
                return Observable.just(testShippingRulesList)
            }
        }

        val state = mutableListOf<ShippingRulesState>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(dispatcher) {
            val useCase = ShippingRulesAndFilteredRewardsUseCase(apolloClient, project, config, this, dispatcher)
            useCase.invoke()
            useCase.shippingRulesState.toList(state)
        }

        assertEquals(state.size, 3)
        assertEquals(state.last().rewards, rewards)
    }
}