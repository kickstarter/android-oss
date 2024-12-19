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
import com.kickstarter.libs.utils.extensions.rewardsAndAddOnsList
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.PaymentIncrementFactory
import com.kickstarter.mock.factories.PaymentPlanFactory
import com.kickstarter.mock.factories.PaymentSourceFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.factories.ShippingRulesEnvelopeFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.BuildPaymentPlanData
import com.kickstarter.models.Checkout
import com.kickstarter.models.PaymentPlan
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.viewmodels.projectpage.CheckoutUIState
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel
import com.kickstarter.viewmodels.projectpage.PaymentSheetPresenterState
import com.kickstarter.viewmodels.usecases.TPEventInputData
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.joda.time.DateTime
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class CrowdfundCheckoutViewModelTest : KSRobolectricTestCase() {
    private lateinit var viewModel: CrowdfundCheckoutViewModel

    private fun setUpEnvironment(environment: Environment, bundle: Bundle? = null) {
        viewModel = CrowdfundCheckoutViewModel.Factory(environment, bundle).create(
            CrowdfundCheckoutViewModel::class.java
        )
    }

    @Test
    fun `test new pledge with rw with shipping + addOns + bonus support, selecting a saved payment method initial state`() = runTest {
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

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "hola@ksr.com", true, true, true, true, "USD")
                    )
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val uiState = mutableListOf<CheckoutUIState>()

        var errorActionCount = 0
        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideErrorAction {
                errorActionCount++
            }
            viewModel.provideBundle(bundle)
            viewModel.userChangedPaymentMethodSelected(cards.first())

            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()

        assertEquals(uiState.size, 3)

        assertEquals(uiState.last().shippingAmount, pledgeData.shippingCostIfShipping())
        assertEquals(uiState.last().checkoutTotal, pledgeData.checkoutTotalAmount())
        assertEquals(uiState.last().bonusAmount, 3.0)
        assertEquals(uiState.last().shippingRule, pledgeData.shippingRule())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.first().id())
        assertEquals(uiState.last().storeCards, cards)
        assertEquals(uiState.last().userEmail, "hola@ksr.com")
        assertEquals(uiState.last().selectedRewards, pledgeData.rewardsAndAddOnsList())

        assertEquals(errorActionCount, 0)

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test new pledge, when user switched to plot, ui state should have true incremental value`() = runTest {
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

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "hola@ksr.com", true, true, true, true, "USD")
                    )
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val uiState = mutableListOf<CheckoutUIState>()

        var errorActionCount = 0
        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideErrorAction {
                errorActionCount++
            }
            viewModel.provideBundle(bundle)

            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()

        assertEquals(uiState.size, 3)

        // default incremental value should be false
        assertEquals(uiState.last().shippingAmount, pledgeData.shippingCostIfShipping())
        assertEquals(uiState.last().checkoutTotal, pledgeData.checkoutTotalAmount())
        assertEquals(uiState.last().bonusAmount, 3.0)
        assertEquals(uiState.last().shippingRule, pledgeData.shippingRule())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.last().id())
        assertEquals(uiState.last().storeCards, cards)
        assertEquals(uiState.last().userEmail, "hola@ksr.com")
        assertEquals(uiState.last().selectedRewards, pledgeData.rewardsAndAddOnsList())
        assertEquals(uiState.last().isIncrementalPledge, false)

        assertEquals(errorActionCount, 0)

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)

        backgroundScope.launch(dispatcher) {
            viewModel.collectionPlanSelected(CollectionOptions.PLEDGE_OVER_TIME)
            viewModel.pledgeOrUpdatePledge()

            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }

        assertEquals(uiState.last().shippingAmount, pledgeData.shippingCostIfShipping())
        assertEquals(uiState.last().checkoutTotal, pledgeData.checkoutTotalAmount())
        assertEquals(uiState.last().bonusAmount, 3.0)
        assertEquals(uiState.last().shippingRule, pledgeData.shippingRule())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.last().id())
        assertEquals(uiState.last().storeCards, cards)
        assertEquals(uiState.last().userEmail, "hola@ksr.com")
        assertEquals(uiState.last().selectedRewards, pledgeData.rewardsAndAddOnsList())
        assertEquals(uiState.last().isIncrementalPledge, true)
    }

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
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun `test ui state when pledge amount does not meet PLOT minimum`() = runTest {
        // - The test reward with shipping
        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val reward = RewardFactory.rewardWithShipping().toBuilder()
            .pledgeAmount(10.0)
            .shippingRules(shippingRules = shippingRules)
            .build()

        val addOns1 = RewardFactory.rewardWithShipping()
            .toBuilder()
            .pledgeAmount(10.0)
            .isAddOn(true)
            .build()

        // - AddOns shipping same as the reward
        val addOnsList = listOf(addOns1)

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

                override fun buildPaymentPlan(buildPaymentPlanData: BuildPaymentPlanData): Observable<PaymentPlan> {
                    return Observable.just(
                        PaymentPlanFactory
                            .ineligibleAllowedPaymentPlan()
                    )
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val uiState = mutableListOf<CheckoutUIState>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideBundle(bundle)

            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()

        // default incremental value should be false
        assertEquals(uiState.last().shippingAmount, pledgeData.shippingCostIfShipping())
        assertEquals(uiState.last().checkoutTotal, pledgeData.checkoutTotalAmount())
        assertEquals(uiState.last().bonusAmount, 3.0)
        assertEquals(uiState.last().shippingRule, pledgeData.shippingRule())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.last().id())
        assertEquals(uiState.last().storeCards, cards)
        assertEquals(uiState.last().selectedRewards, pledgeData.rewardsAndAddOnsList())
        assertEquals(uiState.last().isIncrementalPledge, false)
        assertEquals(uiState.last().plotEligible, false)
        assertEquals(uiState.last().showPlotWidget, true)
    }

    @Test
    fun `test ui state when pledge amount meets PLOT minimum`() = runTest {
        // - The test reward with shipping
        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val reward = RewardFactory.rewardWithShipping().toBuilder()
            .pledgeAmount(10.0)
            .shippingRules(shippingRules = shippingRules)
            .build()

        val addOns1 = RewardFactory.rewardWithShipping()
            .toBuilder()
            .pledgeAmount(10.0)
            .isAddOn(true)
            .build()

        // - AddOns shipping same as the reward
        val addOnsList = listOf(addOns1)

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

        val paymentPlan = PaymentPlanFactory
            .eligibleAllowedPaymentPlan(
                listOf(
                    PaymentIncrementFactory.incrementUsdUncollected(DateTime.now(), "50.00"),
                    PaymentIncrementFactory.incrementUsdUncollected(DateTime.now(), "50.00")
                )
            )
        // - Network mocks
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun buildPaymentPlan(buildPaymentPlanData: BuildPaymentPlanData): Observable<PaymentPlan> {
                    return Observable.just(
                        paymentPlan
                    )
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val uiState = mutableListOf<CheckoutUIState>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideBundle(bundle)

            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()

        // default incremental value should be false
        assertEquals(uiState.last().shippingAmount, pledgeData.shippingCostIfShipping())
        assertEquals(uiState.last().checkoutTotal, pledgeData.checkoutTotalAmount())
        assertEquals(uiState.last().bonusAmount, 3.0)
        assertEquals(uiState.last().shippingRule, pledgeData.shippingRule())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.last().id())
        assertEquals(uiState.last().storeCards, cards)
        assertEquals(uiState.last().selectedRewards, pledgeData.rewardsAndAddOnsList())
        assertEquals(uiState.last().isIncrementalPledge, false)
        assertEquals(uiState.last().plotEligible, true)
        assertEquals(uiState.last().showPlotWidget, true)
        assertEquals(uiState.last().paymentIncrements, paymentPlan.paymentIncrements)
    }

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

    @Test
    fun `test update payment method with rw with shipping + addOns + bonus support backed, selecting a different payment method`() = runTest {
        // - The test reward with shipping
        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val reward = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(shippingRules = shippingRules)
            .build()

        val addOns1 = RewardFactory.rewardWithShipping()
            .toBuilder()
            .isAddOn(true)
            .quantity(2)
            .shippingRules(shippingRules)
            .build()

        val cards = listOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard(), StoredCardFactory.fromPaymentSheetCard())

        val backing = BackingFactory.backing(reward)
            .toBuilder()
            .addOns(listOf(addOns1))
            .location(shippingRules.first().location())
            .locationId(shippingRules.first().location()?.id())
            .bonusAmount(5.0)
            .amount(44.0)
            .shippingAmount(33f)
            .paymentSource(PaymentSourceFactory.visa())
            .build()

        val project = ProjectFactory.project().toBuilder()
            .backing(backing)
            .isBacking(true)
            .rewards(listOf(reward))
            .build()

        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val projectData = ProjectDataFactory.project(project)

        val bundle = Bundle()

        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_PAYMENT),
            projectData,
            reward
        )

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_PAYMENT)

        lateinit var data: UpdateBackingData
        // - Network mocks
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
                    data = updateBackingData
                    // reward + add on with quantity 2
                    assert(updateBackingData.rewardsIds?.size == 3)
                    val checkout = Checkout.builder().id(77L).backing(Checkout.Backing.builder().requiresAction(false).clientSecret("client").build()).build()
                    return Observable.just(checkout)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val uiState = mutableListOf<CheckoutUIState>()
        val checkout = mutableListOf<Pair<CheckoutData, PledgeData>>()

        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideBundle(bundle)
            viewModel.userChangedPaymentMethodSelected(cards.last())
            viewModel.pledgeOrUpdatePledge()
            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()

        backgroundScope.launch(dispatcher) {
            viewModel.checkoutResultState.toList(checkout)
        }

        // - Assert initial state of the screen before any user interaction
        assertEquals(uiState.size, 4)

        // - Amounts and selection should be the obtained from backing
        assertEquals(uiState.last().shippingAmount, 33.0)
        assertEquals(uiState.last().checkoutTotal, 44.0)
        assertTrue(uiState.last().isPledgeButtonEnabled)
        assertFalse(uiState.last().isLoading)
        assertEquals(uiState.last().bonusAmount, 5.0)
        assertEquals(uiState.last().shippingRule?.location(), backing.location())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.last().id())
        assertEquals(uiState.last().selectedRewards.last(), addOns1)
        assertEquals(uiState.last().selectedRewards.first(), reward)

        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)

        assertEquals(checkout.size, 2)
        assertEquals(checkout.last().first.id(), 77L)
        assertEquals(data.amount, null) // When updating payment method UpdateBacking mutation expects null as amount
    }

    @Test
    fun `test change reward from(rw with shipping + addOns + bonus support) to a reward no reward`() = runTest {
        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val rewardBacked = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(shippingRules = shippingRules)
            .build()

        val addOns1Backed = RewardFactory.rewardWithShipping()
            .toBuilder()
            .isAddOn(true)
            .shippingRules(shippingRules)
            .build()

        val cards = listOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard(), StoredCardFactory.fromPaymentSheetCard())

        val backing = BackingFactory.backing(rewardBacked)
            .toBuilder()
            .addOns(listOf(addOns1Backed))
            .location(shippingRules.first().location())
            .locationId(shippingRules.first().location()?.id())
            .bonusAmount(5.0)
            .amount(44.0)
            .shippingAmount(33f)
            .paymentSource(PaymentSourceFactory.visa())
            .build()

        val project = ProjectFactory.project().toBuilder()
            .backing(backing)
            .isBacking(true)
            .rewards(listOf(rewardBacked))
            .build()

        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val projectData = ProjectDataFactory.project(project)

        val bundle = Bundle()

        // On pledge data add the newly selected NO-Reward, plus Reason = UPDATE_REWARD
        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD),
            projectData,
            RewardFactory.noReward()
        )

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        lateinit var data: UpdateBackingData
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "hola@ksr.com", true, true, true, true, "USD")
                    )
                }

                override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
                    data = updateBackingData
                    val checkout = Checkout.builder().id(999L).backing(Checkout.Backing.builder().requiresAction(false).clientSecret("client").build()).build()
                    return Observable.just(checkout)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val uiState = mutableListOf<CheckoutUIState>()
        val checkout = mutableListOf<Pair<CheckoutData, PledgeData>>()

        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideBundle(bundle)
            viewModel.userChangedPaymentMethodSelected(cards.first())
            viewModel.pledgeOrUpdatePledge()

            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()

        backgroundScope.launch(dispatcher) {
            viewModel.checkoutResultState.toList(checkout)
        }

        assertEquals(uiState.size, 4)

        assertEquals(uiState.last().shippingAmount, 0.0)
        assertEquals(uiState.last().checkoutTotal, RewardFactory.noReward().pledgeAmount()) // TODO: REVIEW
        assertEquals(uiState.last().bonusAmount, 0.0)
        assertEquals(uiState.last().shippingRule, pledgeData.shippingRule())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.first().id())
        assertEquals(uiState.last().storeCards, cards)
        assertEquals(uiState.last().userEmail, "hola@ksr.com")
        assertEquals(uiState.last().selectedRewards, listOf(RewardFactory.noReward()))

        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)

        assertEquals(checkout.size, 2)
        assertEquals(checkout.last().first.id(), 999L)
        assertEquals(checkout.last().first.amount(), RewardFactory.noReward().pledgeAmount())
        assertEquals(checkout.last().first.shippingAmount(), 0.0)
        assertEquals(checkout.last().first.bonusAmount(), 0.0)

        assertEquals(data.rewardsIds, emptyList<Reward>()) // when calling updateBacking make with reward no reward make sure no rewardID's sent

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test change reward from(rw with shipping + addOns + bonus support) to another reward + bonus`() = runTest {
        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val rewardBacked = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(shippingRules = shippingRules)
            .build()

        val addOns1Backed = RewardFactory.rewardWithShipping()
            .toBuilder()
            .isAddOn(true)
            .shippingRules(shippingRules)
            .build()

        val cards = listOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard(), StoredCardFactory.fromPaymentSheetCard())

        val backing = BackingFactory.backing(rewardBacked)
            .toBuilder()
            .addOns(listOf(addOns1Backed))
            .location(shippingRules.first().location())
            .locationId(shippingRules.first().location()?.id())
            .bonusAmount(5.0)
            .amount(44.0)
            .shippingAmount(33f)
            .paymentSource(PaymentSourceFactory.visa())
            .build()

        val project = ProjectFactory.project().toBuilder()
            .backing(backing)
            .isBacking(true)
            .rewards(listOf(rewardBacked))
            .build()

        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val projectData = ProjectDataFactory.project(project)

        val bundle = Bundle()

        val secondReward = RewardFactory.rewardWithShipping()

        // On pledge data add the newly selected secondReward, bonus, plus Reason = UPDATE_REWARD
        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD),
            projectData,
            secondReward,
            bonusAmount = 7.0
        )

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        lateinit var data: UpdateBackingData
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "hola@ksr.com", true, true, true, true, "USD")
                    )
                }

                override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
                    data = updateBackingData
                    val checkout = Checkout.builder().id(22L).backing(Checkout.Backing.builder().requiresAction(false).clientSecret("client").build()).build()
                    return Observable.just(checkout)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val uiState = mutableListOf<CheckoutUIState>()
        val checkout = mutableListOf<Pair<CheckoutData, PledgeData>>()

        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideBundle(bundle)
            viewModel.userChangedPaymentMethodSelected(cards.first())
            viewModel.pledgeOrUpdatePledge()

            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()

        backgroundScope.launch(dispatcher) {
            viewModel.checkoutResultState.toList(checkout)
        }

        assertEquals(uiState.size, 4)

        assertEquals(uiState.last().shippingAmount, 0.0)
        assertEquals(uiState.last().checkoutTotal, secondReward.pledgeAmount() + 7.0)
        assertEquals(uiState.last().checkoutTotal, pledgeData.checkoutTotalAmount())
        assertEquals(uiState.last().bonusAmount, 7.0)
        assertEquals(uiState.last().shippingRule, pledgeData.shippingRule())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.first().id())
        assertEquals(uiState.last().storeCards, cards)
        assertEquals(uiState.last().userEmail, "hola@ksr.com")
        assertEquals(uiState.last().selectedRewards, listOf(secondReward))

        assertEquals(checkout.size, 2)
        assertEquals(checkout.last().first.id(), 22L)
        assertEquals(checkout.last().first.shippingAmount(), 0.0)
        assertEquals(checkout.last().first.bonusAmount(), 7.0)

        assertEquals(data.rewardsIds?.size, 1)
        assertEquals(data.rewardsIds?.first(), secondReward)
        assertEquals(data.amount, (secondReward.pledgeAmount() + 7.0).toString())

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test change reward from(rw with shipping + addOns + bonus support) to same reward + more add ons + bonus`() = runTest {
        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val rewardBacked = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(shippingRules = shippingRules)
            .build()

        val addOns1Backed = RewardFactory.rewardWithShipping()
            .toBuilder()
            .isAddOn(true)
            .shippingRules(shippingRules)
            .build()

        val cards = listOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard(), StoredCardFactory.fromPaymentSheetCard())

        val backing = BackingFactory.backing(rewardBacked)
            .toBuilder()
            .addOns(listOf(addOns1Backed))
            .location(shippingRules.first().location())
            .locationId(shippingRules.first().location()?.id())
            .bonusAmount(5.0)
            .amount(44.0)
            .shippingAmount(33f)
            .paymentSource(PaymentSourceFactory.visa())
            .build()

        val project = ProjectFactory.project().toBuilder()
            .backing(backing)
            .isBacking(true)
            .rewards(listOf(rewardBacked))
            .build()

        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val projectData = ProjectDataFactory.project(project)

        val bundle = Bundle()

        val secondReward = RewardFactory.rewardWithShipping()

        // On pledge data add the newly selected secondReward, bonus, plus Reason = UPDATE_REWARD
        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD),
            projectData,
            secondReward,
            bonusAmount = 7.0,
            addOns = listOf(addOns1Backed.toBuilder().quantity(5).build())
        )

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        lateinit var data: UpdateBackingData
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "hola@ksr.com", true, true, true, true, "USD")
                    )
                }

                override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
                    data = updateBackingData
                    val checkout = Checkout.builder().id(22L).backing(Checkout.Backing.builder().requiresAction(false).clientSecret("client").build()).build()
                    return Observable.just(checkout)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val uiState = mutableListOf<CheckoutUIState>()
        val checkout = mutableListOf<Pair<CheckoutData, PledgeData>>()

        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideBundle(bundle)
            viewModel.userChangedPaymentMethodSelected(cards.first())
            viewModel.pledgeOrUpdatePledge()

            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()

        backgroundScope.launch(dispatcher) {
            viewModel.checkoutResultState.toList(checkout)
        }

        assertEquals(uiState.size, 4)

        // add-ons shipping was 30/each * 5
        assertEquals(uiState.last().shippingAmount, 150.0)
        // total = reward + add-ons * quantity + bonus amount + shipping
        assertEquals(uiState.last().checkoutTotal, secondReward.pledgeAmount() + (addOns1Backed.pledgeAmount() * 5) + 7.0 + 150)
        assertEquals(uiState.last().checkoutTotal, pledgeData.checkoutTotalAmount())
        assertEquals(uiState.last().bonusAmount, 7.0)
        assertEquals(uiState.last().shippingRule, pledgeData.shippingRule())
        assertEquals(uiState.last().selectedPaymentMethod.id(), cards.first().id())
        assertEquals(uiState.last().storeCards, cards)
        assertEquals(uiState.last().userEmail, "hola@ksr.com")
        assertEquals(uiState.last().selectedRewards, listOf(secondReward, addOns1Backed.toBuilder().quantity(5).build()))

        assertEquals(checkout.size, 2)
        assertEquals(checkout.last().first.id(), 22L)
        assertEquals(checkout.last().first.shippingAmount(), 150.0)
        assertEquals(checkout.last().first.bonusAmount(), 7.0)

        // 1 reward + 5 add-ons flattened
        assertEquals(data.rewardsIds?.size, 6)
        assertEquals(data.rewardsIds?.first(), secondReward)
        assertEquals(data.amount, (secondReward.pledgeAmount() + (addOns1Backed.pledgeAmount() * 5) + 7.0 + 150).toString())

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test fix Pledge Flow`() = runTest {
        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        val rewardBacked = RewardFactory.rewardWithShipping().toBuilder()
            .shippingRules(shippingRules = shippingRules)
            .build()

        val addOns1Backed = RewardFactory.rewardWithShipping()
            .toBuilder()
            .isAddOn(true)
            .shippingRules(shippingRules)
            .build()

        val backing = BackingFactory.backing(rewardBacked)
            .toBuilder()
            .addOns(listOf(addOns1Backed))
            .location(shippingRules.first().location())
            .locationId(shippingRules.first().location()?.id())
            .bonusAmount(5.0)
            .amount(44.0)
            .shippingAmount(33f)
            .paymentSource(PaymentSourceFactory.visa())
            .build()

        val project = ProjectFactory.project().toBuilder()
            .backing(backing)
            .isBacking(true)
            .rewards(listOf(rewardBacked))
            .build()

        val cards = listOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard(), StoredCardFactory.fromPaymentSheetCard())

        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val projectData = ProjectDataFactory.project(project)

        val bundle = Bundle()

        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.FIX_PLEDGE),
            projectData,
            rewardBacked,
            bonusAmount = 7.0,
            addOns = listOf(addOns1Backed.toBuilder().quantity(5).build())
        )

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.FIX_PLEDGE)

        lateinit var data: UpdateBackingData
        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "hola@ksr.com", true, true, true, true, "USD")
                    )
                }

                override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
                    data = updateBackingData
                    val checkout = Checkout.builder().id(77L).backing(Checkout.Backing.builder().requiresAction(false).clientSecret("clientSecret").build()).build()
                    return Observable.just(checkout)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val checkout = mutableListOf<Pair<CheckoutData, PledgeData>>()

        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideBundle(bundle)
            viewModel.userChangedPaymentMethodSelected(cards.first())
            viewModel.pledgeOrUpdatePledge()
            viewModel.checkoutResultState.toList(checkout)
        }
        advanceUntilIdle()

        assertEquals(data.rewardsIds?.size, null)
        assertEquals(data.rewardsIds?.first(), null)
        assertEquals(data.amount, null)

        // Fix pledge flow should only payment ID anything else
        assertEquals(data.rewardsIds?.size, null)
        assertEquals(data.rewardsIds?.first(), null)
        assertEquals(data.amount, null)
        assertEquals(data.paymentSourceId, cards.first().id())
    }

    @Test
    fun `test adding new paymentMethod throw Stripe's paymentSheet`() = runTest {

        val reward = RewardFactory.reward()
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
        )

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "hola@ksr.com", true, true, true, true, "USD")
                    )
                }

                override fun createSetupIntent(project: Project?): Observable<String> {
                    return Observable.just("SetupIntent")
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val paymentSheetState = mutableListOf<PaymentSheetPresenterState>()
        val uiState = mutableListOf<CheckoutUIState>()

        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideBundle(bundle)
            viewModel.getSetupIntent()
            viewModel.presentPaymentSheetStates.toList(paymentSheetState)
        }
        advanceUntilIdle()

        assertEquals(paymentSheetState.size, 2)
        assertEquals(paymentSheetState.last().setupClientId, "SetupIntent")

        backgroundScope.launch(dispatcher) {
            viewModel.paymentSheetPresented(true)
            viewModel.newlyAddedPaymentMethod(StoredCardFactory.fromPaymentSheetCard())
            viewModel.paymentSheetResult(PaymentSheetResult.Completed)
            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }

        assertEquals(uiState.last().storeCards.first(), StoredCardFactory.fromPaymentSheetCard())
    }

    @Test
    fun `test adding new paymentMethod throw Stripe's paymentSheet, but result canceled or failed (3DS challenge failed)`() = runTest {

        val reward = RewardFactory.reward()
        val project = ProjectFactory.project().toBuilder()
            .rewards(listOf(reward))
            .build()
        val cards = listOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard())

        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val projectData = ProjectDataFactory.project(project)

        val bundle = Bundle()

        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE),
            projectData,
            reward,
        )

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cards)
                }

                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "hola@ksr.com", true, true, true, true, "USD")
                    )
                }

                override fun createSetupIntent(project: Project?): Observable<String> {
                    return Observable.just("SetupIntent")
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val paymentSheetState = mutableListOf<PaymentSheetPresenterState>()
        val uiState = mutableListOf<CheckoutUIState>()

        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideBundle(bundle)
            viewModel.getSetupIntent()
            viewModel.presentPaymentSheetStates.toList(paymentSheetState)
        }
        advanceUntilIdle()

        assertEquals(paymentSheetState.size, 2)
        assertEquals(paymentSheetState.last().setupClientId, "SetupIntent")

        backgroundScope.launch(dispatcher) {
            viewModel.paymentSheetPresented(true)
            viewModel.newlyAddedPaymentMethod(StoredCardFactory.fromPaymentSheetCard())
            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()

        backgroundScope.launch(dispatcher) {
            viewModel.paymentSheetResult(PaymentSheetResult.Failed(Throwable()))
        }

        assertEquals(uiState.last().storeCards, cards)
    }

    @Test
    fun `test some error occurred`() = runTest {
        val projectData = ProjectDataFactory.project(ProjectFactory.project())
        val reward = RewardFactory.reward()

        val bundle = Bundle()

        val pledgeData = PledgeData.with(
            PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE),
            projectData,
            reward,
        )

        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        bundle.putParcelable(
            ArgumentsKey.PLEDGE_PLEDGE_DATA,
            pledgeData
        )
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.error(Throwable("Oh no, no more chocolate!"))
                }

                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.error(Throwable("Oh no, we are out of coffee!"))
                }

                override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout> {
                    return Observable.error(Throwable("Oh no, the team rocket!"))
                }

                override fun createSetupIntent(project: Project?): Observable<String> {
                    return Observable.error(Throwable("Oh no, Godzilla!"))
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment)

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val uiState = mutableListOf<CheckoutUIState>()

        val errorList = mutableListOf<String>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideScopeAndDispatcher(this, dispatcher)
            viewModel.provideErrorAction { message ->
                message?.let { errorList.add(it) }
            }
            viewModel.provideBundle(bundle)
            viewModel.crowdfundCheckoutUIState.toList(uiState)
        }
        advanceUntilIdle()
        backgroundScope.launch(dispatcher) {
            viewModel.pledgeOrUpdatePledge()
        }
        advanceUntilIdle()
        backgroundScope.launch(dispatcher) {
            viewModel.getSetupIntent()
        }

        assertEquals(errorList.size, 4)
        assertEquals(errorList.last(), "Oh no, Godzilla!")
        assertEquals(errorList.first(), "Oh no, we are out of coffee!")
        assertEquals(errorList[1], "Oh no, no more chocolate!")
        assertEquals(errorList[2], "Oh no, the team rocket!")
    }
}
