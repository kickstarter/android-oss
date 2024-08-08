package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRulesEnvelopeFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.FlowUIState
import com.kickstarter.viewmodels.projectpage.RewardSelectionUIState
import com.kickstarter.viewmodels.projectpage.RewardsSelectionViewModel
import com.kickstarter.viewmodels.usecases.GetShippingRulesUseCase
import com.kickstarter.viewmodels.usecases.ShippingRulesState
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RewardsSelectionViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: RewardsSelectionViewModel

    private fun createViewModel(environment: Environment = environment(), useCase: GetShippingRulesUseCase? = null) {
        viewModel =
            RewardsSelectionViewModel.Factory(environment, useCase).create(RewardsSelectionViewModel::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_providing_project_should_initialize_UIState() = runTest {
        createViewModel()

        val testRewards = (0..5).map { Reward.builder().title("$it").id(it.toLong()).isAvailable(true).build() }
        val testBacking =
            Backing.builder().reward(testRewards[2]).rewardId(testRewards[2].id()).build()
        val testProject = Project.builder().rewards(testRewards).backing(testBacking).build()
        val testProjectData = ProjectData.builder().project(testProject).build()

        viewModel.provideProjectData(testProjectData)

        val state = mutableListOf<RewardSelectionUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.rewardSelectionUIState.toList(state)
        }

        // 1 from initialization, 1 from providing project data
        assert(state.size == 2)
        assertEquals(
            state.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 2,
                project = testProjectData,
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_addOns_no_previous_backing() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).isAvailable(true).hasAddons(true).build() }
        val testProject = Project.builder().rewards(testRewards).build()
        val testProjectData = ProjectData.builder().project(testProject).build()

        viewModel.provideProjectData(testProjectData)

        val uiState = mutableListOf<RewardSelectionUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.rewardSelectionUIState.toList(uiState)
        }

        val flowState = mutableListOf<FlowUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.flowUIRequest.toList(flowState)
        }

        assert(uiState.size == 2)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 0,
                project = testProjectData,
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 3)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 1, expanded = true)
        )

        this@RewardsSelectionViewModelTest.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_no_addOns_no_previous_backing() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).isAvailable(true).hasAddons(false).build() }
        val testProject = Project.builder().rewards(testRewards).build()
        val testProjectData = ProjectData.builder().project(testProject).build()

        viewModel.provideProjectData(testProjectData)

        val uiState = mutableListOf<RewardSelectionUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.rewardSelectionUIState.toList(uiState)
        }

        val flowState = mutableListOf<FlowUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.flowUIRequest.toList(flowState)
        }

        assert(uiState.size == 2)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 0,
                project = testProjectData,
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 3)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 1, expanded = true)
        )

        this@RewardsSelectionViewModelTest.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_addOns_previous_backing_same_selection() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).isAvailable(true).hasAddons(true).build() }
        val testBacking =
            Backing.builder().reward(testRewards[2]).rewardId(testRewards[2].id()).build()
        val testProject =
            Project.builder().rewards(testRewards).backing(testBacking).isBacking(true).build()
        val testProjectData =
            ProjectData.builder().project(testProject).backing(testBacking).build()

        viewModel.provideProjectData(testProjectData)

        val uiState = mutableListOf<RewardSelectionUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.rewardSelectionUIState.toList(uiState)
        }

        val flowState = mutableListOf<FlowUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.flowUIRequest.toList(flowState)
        }

        assert(uiState.size == 2)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 2,
                project = testProjectData,
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 3)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 1, expanded = true)
        )

        this@RewardsSelectionViewModelTest.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_addOns_previous_backing_different_selection() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).isAvailable(true).hasAddons(true).build() }
        val testBacking =
            Backing.builder().reward(testRewards[3]).rewardId(testRewards[3].id()).build()
        val testProject =
            Project.builder().rewards(testRewards).backing(testBacking).isBacking(true).build()
        val testProjectData =
            ProjectData.builder().project(testProject).backing(testBacking).build()

        viewModel.provideProjectData(testProjectData)

        val uiState = mutableListOf<RewardSelectionUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.rewardSelectionUIState.toList(uiState)
        }

        val flowState = mutableListOf<FlowUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.flowUIRequest.toList(flowState)
        }

        assert(uiState.size == 2)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 3,
                project = testProjectData,
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 3)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 1, expanded = true)
        )

        this@RewardsSelectionViewModelTest.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_no_addOns_previous_backing_no_addOns() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).isAvailable(true).hasAddons(false).build() }
        val testBacking =
            Backing.builder().reward(testRewards[3]).rewardId(testRewards[3].id()).build()
        val testProject =
            Project.builder().rewards(testRewards).backing(testBacking).isBacking(true).build()
        val testProjectData =
            ProjectData.builder().project(testProject).backing(testBacking).build()

        viewModel.provideProjectData(testProjectData)

        val uiState = mutableListOf<RewardSelectionUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.rewardSelectionUIState.toList(uiState)
        }

        val flowState = mutableListOf<FlowUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.flowUIRequest.toList(flowState)
        }

        assert(uiState.size == 2)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 3,
                project = testProjectData,
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 3)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 1, expanded = true)
        )

        this@RewardsSelectionViewModelTest.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun `Test rewards list when given a list of rewards that contains unavailable rewards will produce a list of rewards with only rewards available`() = runTest {
        createViewModel()
        val testRewards = (0..8).map {
            if (it % 2 == 0)
                Reward.builder().title("$it").id(it.toLong()).isAvailable(true).hasAddons(it != 2).shippingType("$it")
                    .build()
            else
                Reward.builder().title("$it").id(it.toLong()).isAvailable(false).hasAddons(it != 2).shippingType("$it")
                    .build()
        }

        val testProject = Project.builder().rewards(testRewards).build()
        val testProjectData = ProjectData.builder().project(testProject).build()

        viewModel.provideProjectData(testProjectData)

        val uiState = mutableListOf<RewardSelectionUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.rewardSelectionUIState.toList(uiState)
        }

        val filteredRewards = testRewards.filter { it.isAvailable() }
        assertEquals(uiState.size, 2)

        // - make sure the uiState output reward list is filtered
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = filteredRewards,
                initialRewardIndex = 0,
                project = testProjectData,
            )
        )

        // - make sure the uiState output reward list is not the same as the provided reward list
        assertNotSame(uiState.last().rewardList, testRewards.size)
    }

    @Test
    fun `test send analytic event trackRewardsCarouselViewed() when the currentPage is rewards and is expanded mode`() {
        createViewModel()

        val projectData = ProjectDataFactory.project(ProjectFactory.project())

        viewModel.sendEvent(expanded = true, currentPage = 0, projectData)
        this@RewardsSelectionViewModelTest.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test analytic event trackRewardsCarouselViewed() not sent when the currentPage is not rewards or not expanded`() {
        createViewModel()

        val projectData = ProjectDataFactory.project(ProjectFactory.project())

        viewModel.sendEvent(expanded = true, currentPage = 1, projectData)
        this@RewardsSelectionViewModelTest.segmentTrack.assertNoValues()

        viewModel.sendEvent(expanded = false, currentPage = 0, projectData)
        this@RewardsSelectionViewModelTest.segmentTrack.assertNoValues()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Default Location when Backing Project is backed location, and list of shipping rules for "restricted" is all places available for all restricted rewrads without duplicated`() = runTest {
        val rw1 = RewardFactory
            .reward()
            .toBuilder()
            .id(1)
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name)
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
            .build()

        val rw2 = RewardFactory
            .reward()
            .toBuilder()
            .id(2)
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name)
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
            .build()

        val rw3 = RewardFactory
            .reward()
            .toBuilder()
            .id(3)
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name)
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
            .build()

        val testShippingRulesList = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val user = UserFactory.user()
        val backing = BackingFactory.backing(rw1).toBuilder()
            .location(testShippingRulesList.first().location())
            .locationId(testShippingRulesList.first().location()?.id())
            .locationName(testShippingRulesList.first().location()?.displayableName())
            .build()

        val project = ProjectFactory.project().toBuilder()
            .rewards(listOf(rw1, rw2, rw3))
            .backing(backing)
            .build()

        val projectData = ProjectDataFactory.project(project, null, null)

        val apolloClient = object : MockApolloClientV2() {
            override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
                if (reward.id() == 1L)
                    return Observable.just(ShippingRulesEnvelope.builder().shippingRules(listOf(testShippingRulesList.first())).build())
                if (reward.id() == 2L)
                    return Observable.just(ShippingRulesEnvelope.builder().shippingRules(listOf(testShippingRulesList.first())).build())
                if (reward.id() == 3L)
                    return Observable.just(ShippingRulesEnvelope.builder().shippingRules(listOf(testShippingRulesList[2])).build())

                return Observable.empty()
            }
        }

        val config = ConfigFactory.configForCA()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val env = environment()
            .toBuilder()
            .currentConfig2(currentConfig)
            .apolloClientV2(apolloClient)
            .currentUserV2(MockCurrentUserV2(user))
            .build()

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val shippingUiState = mutableListOf<ShippingRulesState>()
        backgroundScope.launch(dispatcher) {
            val useCase = GetShippingRulesUseCase(apolloClient, project, config, this, dispatcher)
            createViewModel(env, useCase)
            viewModel.provideProjectData(projectData)
            viewModel.shippingUIState.toList(shippingUiState)
        }

        advanceUntilIdle() // wait until all state emissions completed

        assertEquals(shippingUiState.size, 2)
        assertEquals(shippingUiState.last().selectedShippingRule.location()?.name(), testShippingRulesList.first().location()?.name())
        assertNotSame(shippingUiState.last().shippingRules, testShippingRulesList)
        assertEquals(shippingUiState.last().shippingRules.size, 2) // the 3 available shipping rules
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Default Location is BAcke when config is from Canada, and list of shipping Rules matches all available reward shipping without repeated`() = runTest {
        val rw = RewardFactory
            .reward()
            .toBuilder()
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED)
            .build()
        val user = UserFactory.user()
        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw, rw, rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)

        val testShippingRulesList = ShippingRulesEnvelopeFactory.shippingRules()
        val apolloClient = object : MockApolloClientV2() {
            override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
                return Observable.just(testShippingRulesList)
            }
        }

        val config = ConfigFactory.configForCA()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val env = environment()
            .toBuilder()
            .currentConfig2(currentConfig)
            .apolloClientV2(apolloClient)
            .currentUserV2(MockCurrentUserV2(user))
            .build()

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val shippingUiState = mutableListOf<ShippingRulesState>()
        backgroundScope.launch(dispatcher) {
            val useCase = GetShippingRulesUseCase(apolloClient, project, config, this, dispatcher)
            createViewModel(env, useCase)
            viewModel.provideProjectData(projectData)
            viewModel.shippingUIState.toList(shippingUiState)
        }

        advanceUntilIdle() // wait until all state emissions completed

        assertEquals(shippingUiState.size, 2)
        assertEquals(shippingUiState.last().selectedShippingRule.location()?.name(), "Canada")
        assertEquals(shippingUiState.last().shippingRules, testShippingRulesList.shippingRules())
    }
}
