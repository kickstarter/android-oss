package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.StoredCard
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutUIState
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel
import io.reactivex.Observable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LatePledgeCheckoutViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: LatePledgeCheckoutViewModel

    private fun setUpEnvironment(environment: Environment) {
        viewModel = LatePledgeCheckoutViewModel.Factory(environment).create(LatePledgeCheckoutViewModel::class.java)
    }

    @Test
    fun `test send PageViewed event`() = runTest {
        setUpEnvironment(environment())

        val rw = RewardFactory.rewardWithShipping().toBuilder().latePledgeAmount(34.0).build()
        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val addOns = listOf(rw, rw, rw)
        val rule = ShippingRuleFactory.germanyShippingRule().toBuilder().cost(3.0).build()
        val bonusAmount = 5.0

        val discover = StoredCardFactory.discoverCard()
        val visa = StoredCardFactory.visa()
        val cardsList = listOf(visa, discover)

        val currentUser = MockCurrentUserV2(UserFactory.user())
        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUserV2(currentUser) // - mock the user
                .apolloClientV2(object : MockApolloClientV2() {
                    override fun getStoredCards(): Observable<List<StoredCard>> { // - mock the stored cards
                        return Observable.just(cardsList)
                    }

                    override fun userPrivacy(): Observable<UserPrivacy> { // - mock the user email and name
                        return Observable.just(
                            UserPrivacy("Hola holita", "hola@gmail.com", true, true, true, true, "MXN")
                        )
                    }

                    override fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment> {
                        return Observable.just(CheckoutPayment(id = 3L, backing = BackingFactory.backing(rw), paymentUrl = "some url"))
                    }
                }).build()
        )

        val state = mutableListOf<LatePledgeCheckoutUIState>()
        val projectData = ProjectDataFactory.project(project = project)
        val pledgeData = PledgeData.with(PledgeFlowContext.LATE_PLEDGES, projectData, rw, addOns = addOns, bonusAmount = bonusAmount, shippingRule = rule)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {

            viewModel.providePledgeData(pledgeData)
            viewModel.userRewardSelection(rw)
            viewModel.sendPageViewedEvent()

            segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
            viewModel.latePledgeCheckoutUIState.toList(state)
        }
    }

    @Test
    fun `test send CTAClicked event`() = runTest {
        setUpEnvironment(environment())

        val rw = RewardFactory.rewardWithShipping().toBuilder().latePledgeAmount(34.0).build()
        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val addOns = listOf(rw, rw, rw)
        val rule = ShippingRuleFactory.germanyShippingRule().toBuilder().cost(3.0).build()
        val bonusAmount = 5.0

        val discover = StoredCardFactory.discoverCard()
        val visa = StoredCardFactory.visa()
        val cardsList = listOf(visa, discover)

        val currentUser = MockCurrentUserV2(UserFactory.user())
        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUserV2(currentUser) // - mock the user
                .apolloClientV2(object : MockApolloClientV2() {
                    override fun getStoredCards(): Observable<List<StoredCard>> { // - mock the stored cards
                        return Observable.just(cardsList)
                    }

                    override fun userPrivacy(): Observable<UserPrivacy> { // - mock the user email and name
                        return Observable.just(
                            UserPrivacy("Hola holita", "hola@gmail.com", true, true, true, true, "MXN")
                        )
                    }

                    override fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment> {
                        return Observable.just(CheckoutPayment(id = 3L, backing = BackingFactory.backing(rw), paymentUrl = "some url"))
                    }
                }).build()
        )

        val state = mutableListOf<LatePledgeCheckoutUIState>()
        val projectData = ProjectDataFactory.project(project = project)
        val pledgeData = PledgeData.with(PledgeFlowContext.LATE_PLEDGES, projectData, rw, addOns = addOns, bonusAmount = bonusAmount, shippingRule = rule)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {

            viewModel.providePledgeData(pledgeData)
            viewModel.sendSubmitCTAEvent()

            segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
            viewModel.latePledgeCheckoutUIState.toList(state)
        }
    }

    @Test
    fun `Test VM init state when user and stored cards requests succeed will generate state with saved cards and user email`() = runTest {
        val discover = StoredCardFactory.discoverCard()
        val visa = StoredCardFactory.visa()
        val cardsList = listOf(visa, discover)

        val currentUser = MockCurrentUserV2(UserFactory.user())
        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUserV2(currentUser) // - mock the user
                .apolloClientV2(object : MockApolloClientV2() {
                    override fun getStoredCards(): Observable<List<StoredCard>> { // - mock the stored cards
                        return Observable.just(cardsList)
                    }

                    override fun userPrivacy(): Observable<UserPrivacy> { // - mock the user email and name
                        return Observable.just(
                            UserPrivacy("Hola holita", "hola@gmail.com", true, true, true, true, "MXN")
                        )
                    }
                }).build()
        )

        val state = mutableListOf<LatePledgeCheckoutUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.latePledgeCheckoutUIState.toList(state)
        }

        assertEquals(state.size, 2)
        assertEquals(state.last().userEmail, "hola@gmail.com")
        assertEquals(state.last().storeCards, cardsList)
        assertEquals(state.last().storeCards.first(), cardsList.first())
        assertEquals(state.last().storeCards.last(), cardsList.last())
        assertEquals(state.last().isLoading, false)
    }

    @Test
    fun `Test VM error init state when user or stored cards requests fail will generate state without saved cards or user email`() = runTest {

        val currentUser = MockCurrentUserV2(UserFactory.user())
        setUpEnvironment(
            environment()
                .toBuilder()
                .currentUserV2(currentUser) // - mock the user
                .apolloClientV2(object : MockApolloClientV2() {
                    override fun getStoredCards(): Observable<List<StoredCard>> { // - mock the stored cards
                        return Observable.error(Throwable("Something went wrong"))
                    }

                    override fun userPrivacy(): Observable<UserPrivacy> { // - mock the user email and name
                        return Observable.error(Throwable("Something went wrong"))
                    }
                }).build()
        )

        val state = mutableListOf<LatePledgeCheckoutUIState>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.latePledgeCheckoutUIState.toList(state)
        }

        assertEquals(state.size, 2)
        assertEquals(state.last().userEmail, "")
        assertEquals(state.last().storeCards, emptyList<StoredCard>())
    }
}
