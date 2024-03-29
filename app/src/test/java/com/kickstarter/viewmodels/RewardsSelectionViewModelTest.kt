package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.FlowUIState
import com.kickstarter.viewmodels.projectpage.RewardSelectionUIState
import com.kickstarter.viewmodels.projectpage.RewardsSelectionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RewardsSelectionViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: RewardsSelectionViewModel

    private fun createViewModel() {
        viewModel =
            RewardsSelectionViewModel.Factory().create(RewardsSelectionViewModel::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_providing_project_should_initialize_UIState() = runTest {
        createViewModel()

        val testRewards = (0..5).map { Reward.builder().title("$it").id(it.toLong()).build() }
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
                showAlertDialog = false
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_addOns_no_previous_backing() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).hasAddons(true).build() }
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
                showAlertDialog = false
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 2)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 1, expanded = true)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_no_addOns_no_previous_backing() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).hasAddons(false).build() }
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
                showAlertDialog = false
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 2)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 2, expanded = true)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_addOns_previous_backing_same_selection() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).hasAddons(true).build() }
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
                showAlertDialog = false
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 2)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 1, expanded = true)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_addOns_previous_backing_different_selection() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).hasAddons(true).build() }
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
                showAlertDialog = false
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 2)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 1, expanded = true)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_no_addOns_previous_backing_has_addOns() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).hasAddons(it != 2).build() }
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
                showAlertDialog = false
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 3)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 0,
                project = testProjectData,
                showAlertDialog = true
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_no_addOns_previous_backing_no_addOns() = runTest {
        createViewModel()

        val testRewards =
            (0..5).map { Reward.builder().title("$it").id(it.toLong()).hasAddons(false).build() }
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
                showAlertDialog = false
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 2)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 2, expanded = true)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_selecting_reward_with_addOns_previous_backing_has_addOns_different_shipping() =
        runTest {
            createViewModel()

            val testRewards = (0..5).map {
                Reward.builder().title("$it").id(it.toLong()).hasAddons(true).shippingType("$it")
                    .build()
            }
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
                    showAlertDialog = false
                )
            )

            viewModel.onUserRewardSelection(testRewards[2])

            assert(uiState.size == 3)
            assert(flowState.size == 0)
            assertEquals(
                uiState.last(),
                RewardSelectionUIState(
                    rewardList = testRewards,
                    initialRewardIndex = 0,
                    project = testProjectData,
                    showAlertDialog = true
                )
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_alert_dialog_positive_option_reward_has_add_ons() = runTest {
        createViewModel()

        val testRewards = (0..5).map {
            Reward.builder().title("$it").id(it.toLong()).hasAddons(true).shippingType("$it")
                .build()
        }
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
                showAlertDialog = false
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 3)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 0,
                project = testProjectData,
                showAlertDialog = true
            )
        )

        viewModel.onRewardCarouselAlertClicked(wasPositive = true)

        assert(uiState.size == 4)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 1, expanded = true)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_alert_dialog_positive_option_reward_no_add_ons() = runTest {
        createViewModel()

        val testRewards = (0..5).map {
            Reward.builder().title("$it").id(it.toLong()).hasAddons(it != 2).shippingType("$it")
                .build()
        }
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
                showAlertDialog = false
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 3)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 0,
                project = testProjectData,
                showAlertDialog = true
            )
        )

        viewModel.onRewardCarouselAlertClicked(wasPositive = true)

        assert(uiState.size == 4)
        assert(flowState.size == 1)
        assertEquals(
            flowState.last(),
            FlowUIState(currentPage = 2, expanded = true)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_alert_dialog_negative_option_reward_no_add_ons() = runTest {
        createViewModel()

        val testRewards = (0..5).map {
            Reward.builder().title("$it").id(it.toLong()).hasAddons(it != 2).shippingType("$it")
                .build()
        }
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
                showAlertDialog = false
            )
        )

        viewModel.onUserRewardSelection(testRewards[2])

        assert(uiState.size == 3)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 0,
                project = testProjectData,
                showAlertDialog = true
            )
        )

        viewModel.onRewardCarouselAlertClicked(wasPositive = false)

        assert(uiState.size == 4)
        assert(flowState.size == 0)
        assertEquals(
            uiState.last(),
            RewardSelectionUIState(
                rewardList = testRewards,
                initialRewardIndex = 0,
                project = testProjectData,
                showAlertDialog = false
            )
        )
    }
}
