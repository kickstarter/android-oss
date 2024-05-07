package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.viewmodels.projectpage.ConfirmDetailsUIState
import com.kickstarter.viewmodels.projectpage.ConfirmDetailsViewModel
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmDetailsViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: ConfirmDetailsViewModel

    @Before
    fun setUpWithEnvironment() {
        viewModel = ConfirmDetailsViewModel.Factory(environment = environment())
            .create(ConfirmDetailsViewModel::class.java)
    }

    @Test
    fun `test_when_project_provided_then_min_and_max_step_is_updated`() = runTest {
        val uiState = mutableListOf<ConfirmDetailsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.confirmDetailsUIState.toList(uiState)
        }

        // US Project, min amount $1, max amount $10,000
        val projectData1 = ProjectDataFactory.project(ProjectFactory.project()).toBuilder().build()
        viewModel.provideProjectData(projectData1)
        viewModel.onUserSelectedReward(RewardFactory.noReward())

        assertEquals(uiState.last().minStepAmount, 1.0)
        assertEquals(uiState.last().maxPledgeAmount, 10000.0)

        // MX Project, min amount $10, max amount $200,000
        val projectData2 =
            ProjectDataFactory.project(ProjectFactory.mxProject()).toBuilder().build()
        viewModel.provideProjectData(projectData2)
        viewModel.onUserSelectedReward(RewardFactory.noReward())

        assertEquals(uiState.last().minStepAmount, 10.0)
        assertEquals(uiState.last().maxPledgeAmount, 200000.0)
    }

    @Test
    fun `test_when_no_reward_selected_then_ui_state_is_correct`() = runTest {
        val uiState = mutableListOf<ConfirmDetailsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.confirmDetailsUIState.toList(uiState)
        }

        val projectData1 = ProjectDataFactory.project(ProjectFactory.project()).toBuilder().build()
        viewModel.provideProjectData(projectData1)
        viewModel.onUserSelectedReward(RewardFactory.noReward())

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(),
                initialBonusSupportAmount = 1.0,
                totalBonusSupportAmount = 1.0,
                shippingAmount = 0.0,
                totalAmount = 1.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )
    }

    @Test
    fun `test_when_reward_selected_then_ui_state_is_correct`() = runTest {
        val uiState = mutableListOf<ConfirmDetailsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.confirmDetailsUIState.toList(uiState)
        }

        val reward = RewardFactory.reward()
        val projectData1 = ProjectDataFactory.project(ProjectFactory.project()).toBuilder().build()

        viewModel.provideProjectData(projectData1)
        viewModel.onUserSelectedReward(reward)

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(reward),
                initialBonusSupportAmount = 0.0,
                totalBonusSupportAmount = 0.0,
                shippingAmount = 0.0,
                totalAmount = 20.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )
    }

    @Test
    fun `test_when_reward_and_add_ons_selected_then_ui_state_is_correct`() = runTest {
        val uiState = mutableListOf<ConfirmDetailsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.confirmDetailsUIState.toList(uiState)
        }

        val reward = RewardFactory.reward()
        val addOn = RewardFactory.addOn()
        val addOnQuantity_1 = addOn.toBuilder().quantity(1).build()
        val addOnQuantity_2 = addOn.toBuilder().quantity(2).build()
        val projectData1 = ProjectDataFactory.project(ProjectFactory.project()).toBuilder().build()

        viewModel.provideProjectData(projectData1)
        viewModel.onUserSelectedReward(reward)
        viewModel.onUserUpdatedAddOns(mapOf(Pair(addOn, 1)))

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(reward, addOnQuantity_1),
                initialBonusSupportAmount = 0.0,
                totalBonusSupportAmount = 0.0,
                shippingAmount = 0.0,
                totalAmount = 40.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )

        viewModel.onUserUpdatedAddOns(mapOf(Pair(addOn, 2)))

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(reward, addOnQuantity_2),
                initialBonusSupportAmount = 0.0,
                totalBonusSupportAmount = 0.0,
                shippingAmount = 0.0,
                totalAmount = 60.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )
    }

    @Test
    fun `test_when_bonus_amount_changes_then_total_is_updated`() = runTest {
        val uiState = mutableListOf<ConfirmDetailsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.confirmDetailsUIState.toList(uiState)
        }

        val reward = RewardFactory.reward()
        val addOn = RewardFactory.addOn()
        val addOnQuantity_1 = addOn.toBuilder().quantity(1).build()
        val projectData1 = ProjectDataFactory.project(ProjectFactory.project()).toBuilder().build()

        viewModel.provideProjectData(projectData1)
        viewModel.onUserSelectedReward(reward)
        viewModel.onUserUpdatedAddOns(mapOf(Pair(addOn, 1)))

        viewModel.incrementBonusSupport()

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(reward, addOnQuantity_1),
                initialBonusSupportAmount = 0.0,
                totalBonusSupportAmount = 1.0,
                shippingAmount = 0.0,
                totalAmount = 41.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )

        viewModel.incrementBonusSupport()

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(reward, addOnQuantity_1),
                initialBonusSupportAmount = 0.0,
                totalBonusSupportAmount = 2.0,
                shippingAmount = 0.0,
                totalAmount = 42.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )

        viewModel.decrementBonusSupport()

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(reward, addOnQuantity_1),
                initialBonusSupportAmount = 0.0,
                totalBonusSupportAmount = 1.0,
                shippingAmount = 0.0,
                totalAmount = 41.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )

        viewModel.decrementBonusSupport()

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(reward, addOnQuantity_1),
                initialBonusSupportAmount = 0.0,
                totalBonusSupportAmount = 0.0,
                shippingAmount = 0.0,
                totalAmount = 40.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )
    }

    @Test
    fun `test_when_shipping_rule_changes_then_total_and_shipping_amount_is_updated`() = runTest {
        val uiState = mutableListOf<ConfirmDetailsUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.confirmDetailsUIState.toList(uiState)
        }

        val reward = RewardFactory.rewardWithShipping()
        val addOn = RewardFactory.addOn()
        val addOnQuantity_1 = addOn.toBuilder().quantity(1).build()
        val projectData1 = ProjectDataFactory.project(ProjectFactory.project()).toBuilder().build()
        val shippingRule1 = ShippingRuleFactory.usShippingRule()
        val shippingRule2 = ShippingRuleFactory.germanyShippingRule()

        viewModel.provideProjectData(projectData1)
        viewModel.onUserSelectedReward(reward)
        viewModel.onUserUpdatedAddOns(mapOf(Pair(addOn, 1)))
        viewModel.provideCurrentShippingRule(shippingRule1)

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(reward, addOnQuantity_1),
                initialBonusSupportAmount = 0.0,
                totalBonusSupportAmount = 0.0,
                shippingAmount = 30.0,
                totalAmount = 70.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )

        viewModel.provideCurrentShippingRule(shippingRule2)

        assertEquals(
            uiState.last(),
            ConfirmDetailsUIState(
                rewardsAndAddOns = listOf(reward, addOnQuantity_1),
                initialBonusSupportAmount = 0.0,
                totalBonusSupportAmount = 0.0,
                shippingAmount = 40.0,
                totalAmount = 80.0,
                minStepAmount = 1.0,
                maxPledgeAmount = 10000.0,
                isLoading = false
            )
        )
    }

    @Test
    fun `test_when_continue_clicked_when_late_pledge_disabled_then_default_action_is_called`() =
        runTest {
            val uiState = mutableListOf<ConfirmDetailsUIState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.confirmDetailsUIState.toList(uiState)
            }

            val projectData1 = ProjectDataFactory.project(
                ProjectFactory
                    .project()
                    .toBuilder()
                    .isInPostCampaignPledgingPhase(false)
                    .build()
            ).toBuilder().build()

            var defaultActionCalled = false

            viewModel.provideProjectData(projectData1)

            val defaultAction: () -> Unit = {
                defaultActionCalled = true
            }

            viewModel.onContinueClicked(defaultAction)

            assertTrue(defaultActionCalled)
        }

    @Test
    fun `test_when_continue_clicked_when_late_pledge_enabled_then_create_checkout_is_called`() =
        runTest {
            var createCheckoutCalled = false
            viewModel = ConfirmDetailsViewModel.Factory(
                environment = environment().toBuilder()
                    .apolloClientV2(object : MockApolloClientV2() {
                        override fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment> {
                            createCheckoutCalled = true
                            return Observable.empty()
                        }
                    }).build()
            ).create(ConfirmDetailsViewModel::class.java)
            val uiState = mutableListOf<ConfirmDetailsUIState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.confirmDetailsUIState.toList(uiState)
            }

            val projectData1 = ProjectDataFactory.project(
                ProjectFactory
                    .project()
                    .toBuilder()
                    .isInPostCampaignPledgingPhase(true)
                    .postCampaignPledgingEnabled(true)
                    .build()
            ).toBuilder().build()
            viewModel.provideProjectData(projectData1)
            viewModel.onContinueClicked {}

            assertTrue(createCheckoutCalled)
        }

    @Test
    fun `test_when_continue_clicked_when_late_pledge_enabled_and_errors_then_error_action_occurs`() =
        runTest {
            var errorMessage: String? = "error"
            viewModel = ConfirmDetailsViewModel.Factory(
                environment = environment().toBuilder()
                    .apolloClientV2(object : MockApolloClientV2() {
                        override fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment> {
                            return Observable.error(Throwable("This is an error"))
                        }
                    }).build()
            ).create(ConfirmDetailsViewModel::class.java)
            val uiState = mutableListOf<ConfirmDetailsUIState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.confirmDetailsUIState.toList(uiState)
            }

            val projectData1 = ProjectDataFactory.project(
                ProjectFactory
                    .project()
                    .toBuilder()
                    .isInPostCampaignPledgingPhase(true)
                    .postCampaignPledgingEnabled(true)
                    .build()
            ).toBuilder().build()
            viewModel.provideProjectData(projectData1)
            viewModel.provideErrorAction { errorMessage = it }
            viewModel.onContinueClicked {}

            // Default message is null
            assertNull(errorMessage)
        }
}