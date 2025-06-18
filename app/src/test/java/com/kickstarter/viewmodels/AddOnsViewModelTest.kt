package com.kickstarter.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.checkout.data.AddOnsEnvelope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.factories.UserFactory
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
import kotlinx.coroutines.CoroutineDispatcher
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

    private fun createViewModel(environment: Environment, dispatcher: CoroutineDispatcher? = null) {
        viewModel =
            AddOnsViewModel.Factory(environment, testDispatcher = dispatcher).create(AddOnsViewModel::class.java)
    }

    fun setup(environment: Environment = environment(), dispatcher: CoroutineDispatcher? = null) {
        createViewModel(environment, dispatcher)

        val testRewards = (0..5).map { Reward.builder().hasAddons(true).title("$it").id(it.toLong()).build() }
        val testBacking =
            Backing.builder().reward(testRewards[2]).rewardId(testRewards[2].id()).build()
        val testProject = Project.builder().rewards(testRewards).backing(testBacking).build()
        val testProjectData = ProjectData.builder().project(testProject).build()

        viewModel.provideProjectData(testProjectData)
    }

    @Test
    fun `analytic event sent for crowdfund checkout, should be sent when calling provideBundle()`() {
        val addOnReward = RewardFactory.addOn().toBuilder().id(1L).build()
        val aDifferentAddOnReward = RewardFactory.addOnSingle().toBuilder().id(2L).build()
        val addOnsList = listOf(addOnReward, aDifferentAddOnReward)

        val apolloClient = object : MockApolloClientV2() {
            override suspend fun getRewardAllowedAddOns(
                locationId: Location,
                rewardId: Reward,
                cursor: String?
            ): Result<AddOnsEnvelope> {
                return Result.success(AddOnsEnvelope(addOnsList = addOnsList))
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

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `analytic event sent for late pledges, should be sent wen calling sendEvent()`() {
        val addOnReward = RewardFactory.addOn()
        val aDifferentAddOnReward = RewardFactory.addOnSingle()
        val addOnsList = listOf(addOnReward, aDifferentAddOnReward)

        val apolloClient = object : MockApolloClientV2() {
            override suspend fun getRewardAllowedAddOns(
                locationId: Location,
                rewardId: Reward,
                cursor: String?
            ): Result<AddOnsEnvelope> {
                return Result.success(AddOnsEnvelope(addOnsList = addOnsList))
            }
        }

        val env = environment().toBuilder()
            .apolloClientV2(apolloClient)
            .build()

        setup(env)

        viewModel.sendEvent()
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

//    @Test
//    fun `test amount of backed addOns has been increased plus loads second addOns page`() = runTest {
//        val addOnReward = RewardFactory.addOn().toBuilder().id(1L).build()
//        val aDifferentAddOnReward = RewardFactory.addOnSingle().toBuilder().id(2L).build()
//        val addOnsList = listOf(addOnReward, aDifferentAddOnReward)
//
//        val addOnReward2 = RewardFactory.addOn().toBuilder().id(7L).build()
//        val aDifferentAddOnReward2 = RewardFactory.addOnSingle().toBuilder().id(8L).build()
//        val secondPageAddOns = listOf(addOnReward2, aDifferentAddOnReward2)
//
//        var page = 0
//
//        val rw = RewardFactory.reward().toBuilder()
//            .hasAddons(true)
//            .id(99L)
//            .pledgeAmount(20.0)
//            .build()
//
//        val uiState = mutableListOf<AddOnsUIState>()
//        val dispatcher = UnconfinedTestDispatcher(testScheduler)
//
//        val apolloClient = object : MockApolloClientV2() {
//            override suspend fun getRewardAllowedAddOns(
//                locationId: Location,
//                rewardId: Reward,
//                cursor: String?
//            ): Result<AddOnsEnvelope> {
//                page++
//                assertEquals(99L, rewardId.id())
//                val returnedList = if (page == 1) addOnsList else secondPageAddOns
//                return Result.success(AddOnsEnvelope(addOnsList = returnedList))
//            }
//        }
//
//        val env = environment().toBuilder()
//            .apolloClientV2(apolloClient)
//            .build()
//        setup(env, dispatcher)
//
//        backgroundScope.launch(dispatcher) {
//            viewModel.userRewardSelection(rw)
//            viewModel.provideSelectedShippingRule(ShippingRuleFactory.canadaShippingRule())
//
//            viewModel.updateSelection(addOnsList.first().id(), 3)
//
//            viewModel.loadMore()
//            viewModel.addOnsUIState.toList(uiState)
//        }
//
//        advanceUntilIdle()
//
//        val total = viewModel.getPledgeDataAndReason()?.first?.pledgeAmountTotal()?.toDouble() ?: 0.0
//
//        val paginatedList = addOnsList + secondPageAddOns
//        assertTrue(page == 2)
//        assertEquals(
//            uiState.last(),
//            AddOnsUIState(
//                addOns = paginatedList,
//                totalCount = 3,
//                isLoading = false,
//                shippingRule = ShippingRuleFactory.canadaShippingRule(),
//                totalPledgeAmount = total
//            )
//        )
//    }

    @Test
    fun `test add bonus Support without selecting addOns`() = runTest {
        val addOnReward = RewardFactory.addOn().toBuilder().id(1L).build()
        val aDifferentAddOnReward = RewardFactory.addOnSingle().toBuilder().id(2L).build()
        val addOnsList = listOf(addOnReward, aDifferentAddOnReward)

        val rw = RewardFactory.reward().toBuilder()
            .id(99L)
            .hasAddons(true)
            .pledgeAmount(55.0)
            .build()

        val uiState = mutableListOf<AddOnsUIState>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val apolloClient = object : MockApolloClientV2() {
            override suspend fun getRewardAllowedAddOns(
                locationId: Location,
                rewardId: Reward,
                cursor: String?
            ): Result<AddOnsEnvelope> {
                assertEquals(99L, rewardId.id())
                return Result.success(AddOnsEnvelope(addOnsList = addOnsList))
            }
        }

        val env = environment().toBuilder()
            .apolloClientV2(apolloClient)
            .build()

        setup(env, dispatcher)
        backgroundScope.launch(dispatcher) {
            viewModel.userRewardSelection(rw)
            viewModel.provideSelectedShippingRule(ShippingRuleFactory.canadaShippingRule())
            viewModel.addOnsUIState.toList(uiState)
        }

        advanceUntilIdle()
        val loadedState = uiState.last { !it.isLoading }

        assertEquals(
            AddOnsUIState(
                addOns = addOnsList,
                totalCount = 0,
                isLoading = false,
                shippingRule = ShippingRuleFactory.canadaShippingRule(),
                totalBonusAmount = 0.0,
                totalPledgeAmount = rw.pledgeAmount()
            ),
            loadedState
        )

        viewModel.bonusAmountUpdated(3.0)

        advanceUntilIdle()
        val bonusUpdatedState = uiState.last { it.totalBonusAmount == 3.0 }

        assertEquals(
            AddOnsUIState(
                addOns = addOnsList,
                totalCount = 0,
                isLoading = false,
                shippingRule = ShippingRuleFactory.canadaShippingRule(),
                totalBonusAmount = 3.0,
                totalPledgeAmount = rw.pledgeAmount() + 3.0
            ),
            bonusUpdatedState
        )
    }

    @Test
    fun `test backed addOns total amount on start`() = runTest {
        val aDifferentAddOnReward = RewardFactory.addOnSingle().toBuilder().id(2L).build()

        val backedAddOnq = aDifferentAddOnReward.toBuilder().quantity(4).build()

        val shippingRule = ShippingRuleFactory.canadaShippingRule()
        val backedReward = RewardFactory.reward().toBuilder()
            .hasAddons(true)
            .id(99L)
            .build()

        val backing = BackingFactory.backing(reward = backedReward)
            .toBuilder()
            .addOns(listOf(backedAddOnq))
            .build()

        val testProject = ProjectFactory.project()
            .toBuilder()
            .rewards(listOf(backedReward))
            .backing(backing)
            .build()

        val testProjectData = ProjectData.builder().project(testProject).build()

        val bundle = Bundle()
        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            PledgeData.with(
                PledgeFlowContext.CHANGE_REWARD,
                testProjectData,
                backedReward,
                shippingRule = shippingRule
            )
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_PLEDGE)

        val uiState = mutableListOf<AddOnsUIState>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val apolloClient = object : MockApolloClientV2() {
            override suspend fun getRewardAllowedAddOns(
                locationId: Location,
                rewardId: Reward,
                cursor: String?
            ): Result<AddOnsEnvelope> {
                assertEquals(99L, rewardId.id())
                return Result.success(AddOnsEnvelope(addOnsList = listOf(aDifferentAddOnReward)))
            }
        }

        val env = environment().toBuilder()
            .apolloClientV2(apolloClient)
            .build()
        createViewModel(env, dispatcher)
        backgroundScope.launch(dispatcher) {
            viewModel.provideBundle(bundle)
            viewModel.addOnsUIState.toList(uiState)
        }

        advanceUntilIdle()

        val pledgeDataAndReason = viewModel.getPledgeDataAndReason()
        val pledgeData = pledgeDataAndReason?.first

        assertEquals(1, uiState.last().addOns.size)
        assertEquals(aDifferentAddOnReward.id(), uiState.last().addOns.first().id())

        assertEquals(4, uiState.last().totalCount)

        assertEquals(shippingRule, pledgeData?.shippingRule())
        assertEquals(1, pledgeData?.addOns()?.size)
        assertEquals(backedAddOnq.id(), pledgeData?.addOns()?.first()?.id())
        assertEquals(backedAddOnq.quantity(), pledgeData?.addOns()?.first()?.quantity())
    }

//    @Test
//    fun `test backed addOns total amount when the amount has been updated`() = runTest {
//        val addOnReward = RewardFactory.addOn().toBuilder().id(1L).build()
//        val aDifferentAddOnReward = RewardFactory.addOnSingle().toBuilder().id(2L).build()
//        val addOnsList = listOf(addOnReward, aDifferentAddOnReward)
//
//        val backedAddOnq = aDifferentAddOnReward.toBuilder().quantity(4).build()
//        val shippingRule = ShippingRuleFactory.canadaShippingRule()
//        val backedReward = RewardFactory.reward().toBuilder()
//            .hasAddons(true)
//            .id(99L)
//            .build()
//
//        val backing = BackingFactory.backing(reward = backedReward)
//            .toBuilder()
//            .addOns(listOf(backedAddOnq))
//            .build()
//
//        val testProject = ProjectFactory.project()
//            .toBuilder()
//            .rewards(listOf(backedReward))
//            .backing(backing)
//            .build()
//
//        val testProjectData = ProjectData.builder().project(testProject).build()
//
//        val bundle = Bundle()
//        bundle.putParcelable(
//            ArgumentsKey.PLEDGE_PLEDGE_DATA,
//            PledgeData.with(
//                PledgeFlowContext.CHANGE_REWARD,
//                testProjectData,
//                backedReward,
//                shippingRule = shippingRule
//            )
//        )
//        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_PLEDGE)
//
//        val uiState = mutableListOf<AddOnsUIState>()
//        val dispatcher = UnconfinedTestDispatcher(testScheduler)
//
//        val apolloClient = object : MockApolloClientV2() {
//            override suspend fun getRewardAllowedAddOns(
//                locationId: Location,
//                rewardId: Reward,
//                cursor: String?
//            ): Result<AddOnsEnvelope> {
//                assertEquals(99L, backedReward.id())
//                return Result.success(AddOnsEnvelope(addOnsList = addOnsList))
//            }
//        }
//
//        val env = environment().toBuilder()
//            .apolloClientV2(apolloClient)
//            .build()
//        createViewModel(env, dispatcher)
//
//        backgroundScope.launch(dispatcher) {
//            viewModel.provideBundle(bundle)
//            viewModel.updateSelection(addOnReward.id(), 4)
//            viewModel.addOnsUIState.toList(uiState)
//        }
//        advanceUntilIdle()
//
//        val pledgeDataAndReason = viewModel.getPledgeDataAndReason()
//        val pledgeData = pledgeDataAndReason?.first
//
//        assertEquals(8, uiState.last().totalCount)
//        assertEquals(2, pledgeData?.addOns()?.size)
//
//        val firstAddOn = pledgeData?.addOns()?.first()
//        val secondAddOn = pledgeData?.addOns()?.last()
//
//        assertEquals(addOnReward.id(), firstAddOn?.id())
//        assertEquals(4, firstAddOn?.quantity())
//
//        assertEquals(backedAddOnq.id(), secondAddOn?.id())
//        assertEquals(backedAddOnq.quantity(), secondAddOn?.quantity())
//    }

    @Test
    fun `test if the VM has being reached by a loggedOut user`() = runTest {
        val env = environment().toBuilder()
            .currentUserV2(MockCurrentUserV2()) // empty user
            .build()

        createViewModel(env)
        assertFalse(viewModel.isUserLoggedIn())
    }

    @Test
    fun `test if the VM has being reached by a loggedIn user`() = runTest {
        val env = environment().toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user())) // empty user
            .build()

        createViewModel(env)
        assertTrue(viewModel.isUserLoggedIn())
    }
}
