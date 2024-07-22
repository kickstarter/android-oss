package com.kickstarter.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Backing
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.AddOnsUIState
import com.kickstarter.viewmodels.projectpage.AddOnsViewModel
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddOnsViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: AddOnsViewModel

    private fun createViewModel(environment: Environment) {
        viewModel =
            AddOnsViewModel.Factory(environment).create(AddOnsViewModel::class.java)
    }

    fun setup(environment: Environment = environment()) {
        createViewModel(environment)

        val testRewards = (0..5).map { Reward.builder().title("$it").id(it.toLong()).build() }
        val testBacking =
            Backing.builder().reward(testRewards[2]).rewardId(testRewards[2].id()).build()
        val testProject = Project.builder().rewards(testRewards).backing(testBacking).build()
        val testProjectData = ProjectData.builder().project(testProject).build()

        viewModel.provideProjectData(testProjectData)
        viewModel.provideSelectedShippingRule(ShippingRuleFactory.canadaShippingRule())
    }

    @Test
    fun `test_on_addons_added_or_removed`() = runTest {

        val addOnReward = RewardFactory.addOn()
        val aDifferentAddOnReward = RewardFactory.addOnSingle()
        val addOnsList = listOf(addOnReward, aDifferentAddOnReward)

        val apolloClient = object : MockApolloClientV2() {
            override fun getProjectAddOns(
                slug: String,
                locationId: Location
            ): Observable<List<Reward>> {
                return Observable.just(addOnsList)
            }
        }

        val env = environment().toBuilder()
            .apolloClientV2(apolloClient)
            .build()

        setup(env)

        val uiState = mutableListOf<AddOnsUIState>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.addOnsUIState.toList(uiState)
        }

        // - Initial state addOns freshly loaded
        assertEquals(
            uiState.last(),
            AddOnsUIState(
                addOns = addOnsList,
                totalCount = 0,
                isLoading = false
            )
        )

        // Increment addOnReward to quantity 3
        viewModel.updateSelection(addOnsList.first().id(), 3)

        assertEquals(
            uiState.last(),
            AddOnsUIState(
                addOns = addOnsList,
                totalCount = 3,
                isLoading = false
            )
        )
    }

    @Test
    fun `test backed addOns total amount on start and update total amount`() = runTest {

        val addOnReward = RewardFactory.addOn()
        val aDifferentAddOnReward = RewardFactory.addOnSingle()
        val addOnsList = listOf(addOnReward, aDifferentAddOnReward)

        val apolloClient = object : MockApolloClientV2() {
            override fun getProjectAddOns(
                slug: String,
                locationId: Location
            ): Observable<List<Reward>> {
                return Observable.just(addOnsList)
            }
        }

        val env = environment().toBuilder()
            .apolloClientV2(apolloClient)
            .build()

        val backedAddOnq = aDifferentAddOnReward.toBuilder().quantity(4).build()
        val shippingRule = ShippingRuleFactory.canadaShippingRule()
        val backedReward = RewardFactory.reward().toBuilder().hasAddons(true).build()
        val backing = BackingFactory.backing(reward = backedReward).toBuilder().addOns(listOf(backedAddOnq)).build()
        val testProject = ProjectFactory.project().toBuilder().rewards(listOf(backedReward)).backing(backing).build()
        val testProjectData = ProjectData.builder().project(testProject).build()

        createViewModel(env)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(PledgeFlowContext.CHANGE_REWARD, testProjectData, backedReward, shippingRule = shippingRule))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_PLEDGE)

        viewModel.provideBundle(bundle)

        val uiState = mutableListOf<AddOnsUIState>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.addOnsUIState.toList(uiState)
        }

        advanceUntilIdle()

        // - Initial state queried for addOns and updated with backed information
        assertEquals(uiState.last().addOns.size, 2)
        assertEquals(uiState.last().addOns.last(), backedAddOnq)
        assertEquals(uiState.last().addOns.first(), addOnReward)
        assertEquals(uiState.last().totalCount, 4)

        // - Increment addOnReward to quantity 3, the addOn that was not backed
        viewModel.updateSelection(addOnsList.first().id(), 3)

        assertEquals(uiState.last().totalCount, 7)

        assertEquals(viewModel.getPledgeDataAndReason()?.first?.addOns()?.size, 2)
        assertEquals(viewModel.getPledgeDataAndReason()?.first?.addOns()?.first()?.id(), addOnReward.id())
        assertEquals(viewModel.getPledgeDataAndReason()?.first?.addOns()?.first()?.quantity(), 3)
        assertEquals(viewModel.getPledgeDataAndReason()?.first?.addOns()?.last(), backedAddOnq)
    }
}
