package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Backing
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.AddOnsUIState
import com.kickstarter.viewmodels.projectpage.AddOnsViewModel
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        setup()

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

    @Test
    fun `Test show AddOns Available for selected shipping Rule`() = runTest {

        val addOnReward = RewardFactory.addOn()
        val aDifferentAddOnReward = RewardFactory.addOnSingle()
        val addOnsList = listOf(addOnReward, aDifferentAddOnReward, addOnReward, aDifferentAddOnReward)

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

        setup(environment = env)

        val uiState = mutableListOf<AddOnsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.addOnsUIState.toList(uiState)
        }

        assertEquals(uiState.last().addOns, addOnsList)
    }
}
