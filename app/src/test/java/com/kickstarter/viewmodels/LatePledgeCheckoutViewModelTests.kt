package com.kickstarter.viewmodels

import android.util.Pair
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
import com.kickstarter.models.Backing
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.PaymentValidationResponse
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutUIState
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel
import io.reactivex.Observable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LatePledgeCheckoutViewModelTests : KSRobolectricTestCase() {

    private lateinit var viewModel: LatePledgeCheckoutViewModel

    private fun setUpWithEnvironment(environment: Environment) {
        viewModel =
            LatePledgeCheckoutViewModel.Factory(environment)
                .create(LatePledgeCheckoutViewModel::class.java)
    }

    @Test
    fun `test_when_loading_called_then_state_shows_loading`() = runTest {
        setUpWithEnvironment(environment())

        val state = mutableListOf<LatePledgeCheckoutUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.latePledgeCheckoutUIState.toList(state)
        }

        viewModel.loading()
        assertEquals(
            state.last(),
            LatePledgeCheckoutUIState(
                isLoading = true
            )
        )
    }

    @Test
    fun `test_when_user_logged_in_then_email_is_provided`() = runTest {
        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.empty()
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpWithEnvironment(environment)

        val state = mutableListOf<LatePledgeCheckoutUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.latePledgeCheckoutUIState.toList(state)
        }

        assertEquals(
            state.last(),
            LatePledgeCheckoutUIState(
                userEmail = "some@email.com"
            )
        )
    }

    @Test
    fun `test_when_user_logged_in_then_cards_are_fetched`() = runTest {
        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)
        val cardList = listOf(StoredCardFactory.visa())

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cardList)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpWithEnvironment(environment)

        val state = mutableListOf<LatePledgeCheckoutUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.latePledgeCheckoutUIState.toList(state)
        }

        assertEquals(
            state.last(),
            LatePledgeCheckoutUIState(
                storeCards = cardList,
                userEmail = "some@email.com"
            )
        )
    }

    @Test
    fun `test_when_user_clicks_add_new_card_then_setup_intent_is_called`() = runTest {
        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun createSetupIntent(project: Project?): Observable<String> {
                    return Observable.just("thisIsAClientSecret")
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpWithEnvironment(environment)

        val state = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.clientSecretForNewPaymentMethod.toList(state)
        }

        viewModel.onAddNewCardClicked(Project.builder().build())

        assertEquals(
            state.last(),
            "thisIsAClientSecret"
        )
    }

    @Test
    fun `test_when_new_card_added_then_payment_methods_are_refreshed`() = runTest {
        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)
        var cardList = mutableListOf(StoredCardFactory.visa())

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cardList)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpWithEnvironment(environment)

        val state = mutableListOf<LatePledgeCheckoutUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.latePledgeCheckoutUIState.toList(state)
        }

        // Before List changes
        assertEquals(
            state.last(),
            LatePledgeCheckoutUIState(
                storeCards = cardList,
                userEmail = "some@email.com"
            )
        )

        cardList = mutableListOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard())

        viewModel.onNewCardSuccessfullyAdded()

        // After list is updated
        assertEquals(
            state.last(),
            LatePledgeCheckoutUIState(
                storeCards = cardList,
                userEmail = "some@email.com"
            )
        )
    }

    @Test
    fun `test_when_new_card_adding_fails_then_state_emits`() = runTest {
        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)
        val cardList = mutableListOf(StoredCardFactory.visa())

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cardList)
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpWithEnvironment(environment)

        val state = mutableListOf<LatePledgeCheckoutUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.latePledgeCheckoutUIState.toList(state)
        }

        viewModel.onNewCardFailed()

        assertEquals(
            state.last(),
            LatePledgeCheckoutUIState(
                storeCards = cardList,
                userEmail = "some@email.com"
            )
        )

        assertEquals(state.size, 2)
    }

    @Test
    fun `test_when_pledge_clicked_and_checkout_id_not_provided_then_error_action_is_called`() =
        runTest {
            val user = UserFactory.user()
            val currentUserV2 = MockCurrentUserV2(initialUser = user)
            val cardList = mutableListOf(StoredCardFactory.visa())

            val environment = environment().toBuilder()
                .apolloClientV2(object : MockApolloClientV2() {
                    override fun getStoredCards(): Observable<List<StoredCard>> {
                        return Observable.just(cardList)
                    }
                })
                .currentUserV2(currentUserV2)
                .build()

            setUpWithEnvironment(environment)

            val state = mutableListOf<LatePledgeCheckoutUIState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.latePledgeCheckoutUIState.toList(state)
            }

            var errorActionCount = 0

            viewModel.provideErrorAction {
                errorActionCount++
            }

            viewModel.onPledgeButtonClicked(cardList.first(), ProjectFactory.project(), 100.0)

            assertEquals(
                state.last(),
                LatePledgeCheckoutUIState(
                    storeCards = cardList,
                    userEmail = "some@email.com"
                )
            )

            assertEquals(errorActionCount, 1)
        }

    @Test
    fun `test_when_pledge_clicked_and_checkout_id_provided_then_checkout_continues`() = runTest {
        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)
        val cardList = mutableListOf(StoredCardFactory.visa())

        var paymentIntentCalled = 0
        var validateCheckoutCalled = 0

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(cardList)
                }

                override fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): Observable<String> {
                    paymentIntentCalled++
                    return Observable.just("paymentIntent")
                }

                override fun validateCheckout(
                    checkoutId: String,
                    paymentIntentClientSecret: String,
                    paymentSourceId: String
                ): Observable<PaymentValidationResponse> {
                    validateCheckoutCalled++
                    return Observable.just(
                        PaymentValidationResponse(
                            isValid = true,
                            messages = listOf()
                        )
                    )
                }

                override fun completeOnSessionCheckout(
                    checkoutId: String,
                    paymentIntentClientSecret: String,
                    paymentSourceId: String?,
                    paymentSourceReusable: Boolean
                ): Observable<Pair<String, Boolean>> {
                    return Observable.just(Pair("Success", false))
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpWithEnvironment(environment)

        val state = mutableListOf<LatePledgeCheckoutUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.latePledgeCheckoutUIState.toList(state)
        }

        var errorActionCount = 0

        viewModel.provideErrorAction {
            errorActionCount++
        }

        viewModel.provideCheckoutIdAndBacking(100L, Backing.builder().id(101L).build())

        viewModel.onPledgeButtonClicked(cardList.first(), ProjectFactory.project(), 100.0)

        assertEquals(
            state.last(),
            LatePledgeCheckoutUIState(
                storeCards = cardList,
                userEmail = "some@email.com"
            )
        )

        // Stripe will give an error since this is mock data
        assertEquals(errorActionCount, 1)
        assertEquals(validateCheckoutCalled, 1)
        assertEquals(paymentIntentCalled, 1)
    }

    @Test
    fun `test_when_complete3DSCheckout_called_with_no_values_then_errors`() = runTest {
        val user = UserFactory.user()
        val currentUserV2 = MockCurrentUserV2(initialUser = user)

        var completeOnSessionCheckoutCalled = 0

        val environment = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun completeOnSessionCheckout(
                    checkoutId: String,
                    paymentIntentClientSecret: String,
                    paymentSourceId: String?,
                    paymentSourceReusable: Boolean
                ): Observable<Pair<String, Boolean>> {
                    completeOnSessionCheckoutCalled++
                    return Observable.just(Pair("Success", false))
                }
            })
            .currentUserV2(currentUserV2)
            .build()

        setUpWithEnvironment(environment)

        var errorActionCount = 0

        viewModel.provideErrorAction {
            errorActionCount++
        }

        viewModel.completeOnSessionCheckoutFor3DS()

        assertEquals(errorActionCount, 1)
        assertEquals(completeOnSessionCheckoutCalled, 0)
    }

    @Test
    fun `test_when_no_reward_selected_then_no_analytics_sent`() = runTest {
        setUpWithEnvironment(environment())

        viewModel.sendPageViewedEvent(
            ProjectDataFactory.project(ProjectFactory.project()),
            listOf(),
            ShippingRuleFactory.usShippingRule(),
            100.0,
            1000.0,
            100.0
        )

        this@LatePledgeCheckoutViewModelTests.segmentTrack.assertNoValues()

        viewModel.sendSubmitCTAEvent(
            ProjectDataFactory.project(ProjectFactory.project()),
            listOf(),
            ShippingRuleFactory.usShippingRule(),
            100.0,
            1000.0,
            100.0
        )

        this@LatePledgeCheckoutViewModelTests.segmentTrack.assertNoValues()
    }

    @Test
    fun `test_when_reward_selected_then_page_analytics_sent`() = runTest {
        setUpWithEnvironment(environment())

        viewModel.userRewardSelection(RewardFactory.reward())

        viewModel.sendPageViewedEvent(
            ProjectDataFactory.project(ProjectFactory.project()),
            listOf(),
            ShippingRuleFactory.usShippingRule(),
            100.0,
            1000.0,
            100.0
        )

        this@LatePledgeCheckoutViewModelTests.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test_when_reward_selected_then_cta_analytics_sent`() = runTest {
        setUpWithEnvironment(environment())

        viewModel.userRewardSelection(RewardFactory.reward())

        viewModel.sendSubmitCTAEvent(
            ProjectDataFactory.project(ProjectFactory.project()),
            listOf(),
            ShippingRuleFactory.usShippingRule(),
            100.0,
            1000.0,
            100.0
        )

        this@LatePledgeCheckoutViewModelTests.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }
}
