package com.kickstarter.viewmodels

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.factories.ShippingRulesEnvelopeFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Checkout
import com.kickstarter.models.StoredCard
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.viewmodels.projectpage.CheckoutUIState
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel
import com.kickstarter.viewmodels.usecases.TPEventInputData
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito
import type.CreditCardPaymentType

class CrowdfundCheckoutViewModelTest : KSRobolectricTestCase() {
    private lateinit var viewModel: CrowdfundCheckoutViewModel

    private fun setUpEnvironment(environment: Environment, bundle: Bundle? = null) {
        viewModel = CrowdfundCheckoutViewModel.Factory(environment, bundle).create(
            CrowdfundCheckoutViewModel::class.java
        )
    }

    // Tests new pledge, with rw with shipping + addOns + bonus support, using saved payment methods
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test new pledge with rw with shipping + addOns + bonus support, selecting a saved payment method initial state`() = runTest {
        // - The test reward with shipping
        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val reward = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(shippingRules = shippingRules)
            .build()

        val addOns1 = RewardFactory.rewardWithShipping()
            .toBuilder()
            .isAddOn(true)
            .build()

        val addOn2 = RewardFactory.addOn()
            .toBuilder()
            .shippingRules(shippingRules)
            .build()

        // - AddOns shipping same as the reward
        val addOnsList = listOf(addOns1, addOn2)

        val project = ProjectFactory.project().toBuilder()
            .rewards(listOf(reward))
            .build()

        val cards = listOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard(), StoredCardFactory.fromPaymentSheetCard())

        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val projectData = ProjectDataFactory.project(project)

        val bundle = Bundle()

        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE),
            projectData,
            reward,
            addOnsList,
            ShippingRuleFactory.usShippingRule(),
            bonusAmount = 3.0
        )

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)

        // - Network mocks
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val uiState = mutableListOf<CheckoutUIState>()

        backgroundScope.launch(dispatcher) {
            viewModel.provideBundle(bundle)
            viewModel.userChangedPaymentMethodSelected(cards.first())

            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()
        // - Assert initial state of the screen before any user interaction
        assertEquals(uiState.size, 2)

        // uiState.last().selectedRewards
        assertEquals(uiState.last().shippingAmount, pledgeData.shippingCostIfShipping())
        assertEquals(uiState.last().checkoutTotal, pledgeData.checkoutTotalAmount())
        assertTrue(uiState.last().isPledgeButtonEnabled)
        assertFalse(uiState.last().isLoading)
        assertEquals(uiState.last().bonusAmount, 3.0)
        assertEquals(uiState.last().shippingRule, pledgeData.shippingRule())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.first().id())

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test user hits pledges button with rw + addOns + bonus support with shipping`() = runTest {
        // - The test reward with shipping
        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val reward = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(shippingRules = shippingRules)
            .build()

        val addOns1 = RewardFactory.rewardWithShipping()
            .toBuilder()
            .isAddOn(true)
            .build()

        val addOn2 = RewardFactory.addOn()
            .toBuilder()
            .shippingRules(shippingRules)
            .build()

        // - AddOns shipping same as the reward
        val addOnsList = listOf(addOns1, addOn2)

        val project = ProjectFactory.project().toBuilder()
            .rewards(listOf(reward))
            .build()

        val cards = listOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard(), StoredCardFactory.fromPaymentSheetCard())

        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val projectData = ProjectDataFactory.project(project)

        val bundle = Bundle()

        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE),
            projectData,
            reward,
            addOnsList,
            ShippingRuleFactory.usShippingRule(),
            bonusAmount = 3.0
        )

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)

        // - Network mocks
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout> {
                    val id = 33L
                    val backing = Checkout.Backing.builder()
                        .clientSecret("boop")
                        .requiresAction(false)
                        .build()
                    return Observable.just(
                        Checkout.builder()
                            .id(id)
                            .backing(backing)
                            .build()
                    )
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val checkoutState = mutableListOf<Pair<CheckoutData, PledgeData>>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        viewModel.provideBundle(bundle)
        viewModel.userChangedPaymentMethodSelected(cards.first())
        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.pledgeOrUpdatePledge()

            viewModel.checkoutResultState.toList(checkoutState)
        }
        advanceUntilIdle()

        // - Checkout Data
        assertEquals(checkoutState.last().first.id(), 33L)
        assertEquals(checkoutState.last().first.bonusAmount(), 3.0)
        assertEquals(checkoutState.last().first.shippingAmount(), pledgeData.shippingCostIfShipping())
        assertEquals(checkoutState.last().first.amount(), pledgeData.pledgeAmountTotal())
        assertEquals(checkoutState.last().first.paymentType(), CreditCardPaymentType.CREDIT_CARD)

        // - PledgeData
        assertEquals(checkoutState.last().second, pledgeData)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test sendThirdPartyEvent when a payment method selected`() = runTest {

        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)).thenReturn(true)

        val user = UserFactory.user()
        val currentUser = MockCurrentUserV2(initialUser = user)
        // - Network mocks
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> {
                    return Observable.just(Pair(true, "cosa"))
                }
            })
            .featureFlagClient(object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            })
            .sharedPreferences(sharedPreferences)
            .currentUserV2(currentUser)
            .build()

        val project = ProjectFactory.project().toBuilder().sendThirdPartyEvents(true).build()
        val bundle = Bundle()
        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE),
            ProjectDataFactory.project(project),
            RewardFactory.reward()
        )

        setUpEnvironment(environment)
        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        viewModel.provideBundle(bundle)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.userChangedPaymentMethodSelected(StoredCardFactory.visa())
        }
        advanceUntilIdle()

        assertTrue(viewModel.isThirdPartyEventSent().first)
        assertEquals(viewModel.isThirdPartyEventSent().second, "cosa")
    }
}
