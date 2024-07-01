package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.StoredCard
import com.kickstarter.models.UserPrivacy
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
    fun `test send PageViewed event`() {
        setUpEnvironment(environment())

        val rw = RewardFactory.rewardWithShipping()
        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val addOns = listOf(rw, rw, rw)
        val rule = ShippingRuleFactory.germanyShippingRule()
        val shipAmount = 3.0
        val totalAmount = 300.0
        val bonusAmount = 5.0

        val projectData = ProjectDataFactory.project(project = project)

        viewModel.userRewardSelection(rw)
        viewModel.sendPageViewedEvent(
            projectData,
            addOns,
            rule,
            shipAmount,
            totalAmount,
            bonusAmount
        )

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test send CTAClicked event`() {
        setUpEnvironment(environment())

        val rw = RewardFactory.rewardWithShipping()
        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val addOns = listOf(rw, rw, rw)
        val rule = ShippingRuleFactory.germanyShippingRule()
        val shipAmount = 3.0
        val totalAmount = 300.0
        val bonusAmount = 5.0

        val projectData = ProjectDataFactory.project(project = project)
        viewModel.userRewardSelection(rw)
        viewModel.sendSubmitCTAEvent(
            projectData,
            addOns,
            rule,
            shipAmount,
            totalAmount,
            bonusAmount
        )

        this.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
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
