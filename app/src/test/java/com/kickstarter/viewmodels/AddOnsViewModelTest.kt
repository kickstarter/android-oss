package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.AddOnsUIState
import com.kickstarter.viewmodels.projectpage.AddOnsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddOnsViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: AddOnsViewModel

    private fun createViewModel() {
        val env = environment().toBuilder().build()
        viewModel =
            AddOnsViewModel.Factory(env).create(AddOnsViewModel::class.java)
    }

    @Before
    fun setup() {
        createViewModel()

        val testRewards = (0..5).map { Reward.builder().title("$it").id(it.toLong()).build() }
        val testBacking =
            Backing.builder().reward(testRewards[2]).rewardId(testRewards[2].id()).build()
        val testProject = Project.builder().rewards(testRewards).backing(testBacking).build()
        val testProjectData = ProjectData.builder().project(testProject).build()

        viewModel.provideProjectData(testProjectData)
    }

    // Tests for UI events
    @Test
    fun `test_hide_location_selection_on_reward_not_shippable`() = runTest {
        val reward = RewardFactory
            .rewardHasAddOns()
            .toBuilder()
            .shippingPreference(Reward.ShippingPreference.NONE.name)
            .build()

        val uiState = mutableListOf<AddOnsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.addOnsUIState.toList(uiState)
        }

        viewModel.userRewardSelection(reward)

        assertEquals(
            uiState.last(),
            AddOnsUIState(
                shippingSelectorIsGone = true
            )
        )
    }

    @Test
    fun `test_show_location_selection_on_reward_is_shippable`() = runTest {
        val reward = RewardFactory
            .rewardWithShipping()
            .toBuilder()
            .build()

        val uiState = mutableListOf<AddOnsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.addOnsUIState.toList(uiState)
        }

        viewModel.userRewardSelection(reward)

        assertEquals(
            uiState.last(),
            AddOnsUIState(
                shippingSelectorIsGone = false
            )
        )
    }

    @Test
    fun `test_on_shipping_location_changed`() = runTest {
        val newShippingRule = ShippingRuleFactory.germanyShippingRule()

        val uiState = mutableListOf<AddOnsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.addOnsUIState.toList(uiState)
        }

        viewModel.onShippingLocationChanged(newShippingRule)

        assertEquals(
            uiState.last(),
            AddOnsUIState(
                currentShippingRule = newShippingRule,
                shippingSelectorIsGone = false
            )
        )
    }

    @Test
    fun `test_on_addons_added_or_removed`() = runTest {
        val addOnReward = RewardFactory.addOn()
        val aDifferentAddOnReward = RewardFactory.addOnSingle()
        val currentAddOnsSelections = mutableMapOf(
            addOnReward to 2,
            aDifferentAddOnReward to 1
        )

        val uiState = mutableListOf<AddOnsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.addOnsUIState.toList(uiState)
        }

        viewModel.onAddOnsAddedOrRemoved(currentAddOnsSelections)

        assertEquals(
            uiState.last(),
            AddOnsUIState(
                currentAddOnsSelection = currentAddOnsSelections
            )
        )

        // Decrement addOnReward to quantity 1
        currentAddOnsSelections[addOnReward] = 1

        viewModel.onAddOnsAddedOrRemoved(currentAddOnsSelections)

        assertEquals(
            uiState.last(),
            AddOnsUIState(
                currentAddOnsSelection = currentAddOnsSelections
            )
        )
    }
}
