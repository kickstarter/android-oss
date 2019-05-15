package com.kickstarter.viewmodels

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.*
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ScreenLocation
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.util.*

class PledgeFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PledgeFragmentViewModel.ViewModel

    private val project = ProjectFactory.project()

    private val animateRewardCard = TestSubscriber<PledgeData>()
    private val cards = TestSubscriber<List<StoredCard>>()
    private val continueButtonIsGone = TestSubscriber<Boolean>()
    private val conversionText = TestSubscriber<String>()
    private val conversionTextViewIsGone = TestSubscriber<Boolean>()
    private val estimatedDelivery = TestSubscriber<String>()
    private val paymentContainerIsGone = TestSubscriber<Boolean>()
    private val pledgeAmount = TestSubscriber<String>()
    private val selectedShippingRule = TestSubscriber<ShippingRule>()
    private val shippingAmount = TestSubscriber<String>()
    private val shippingRuleAndProject = TestSubscriber<Pair<List<ShippingRule>, Project>>()
    private val shippingRulesSectionIsGone = TestSubscriber<Boolean>()
    private val showPledgeCard = TestSubscriber<Pair<Int, Boolean>>()
    private val startLoginToutActivity = TestSubscriber<Void>()
    private val startNewCardActivity = TestSubscriber<Void>()
    private val totalAmount = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PledgeFragmentViewModel.ViewModel(environment)

        this.vm.outputs.animateRewardCard().subscribe(this.animateRewardCard)
        this.vm.outputs.cards().subscribe(this.cards)
        this.vm.outputs.continueButtonIsGone().subscribe(this.continueButtonIsGone)
        this.vm.outputs.conversionText().subscribe(this.conversionText)
        this.vm.outputs.conversionTextViewIsGone().subscribe(this.conversionTextViewIsGone)
        this.vm.outputs.estimatedDelivery().subscribe(this.estimatedDelivery)
        this.vm.outputs.paymentContainerIsGone().subscribe(this.paymentContainerIsGone)
        this.vm.outputs.pledgeAmount().map { it.toString() }.subscribe(this.pledgeAmount)
        this.vm.outputs.selectedShippingRule().subscribe(this.selectedShippingRule)
        this.vm.outputs.shippingAmount().map { it.toString() }.subscribe(this.shippingAmount)
        this.vm.outputs.shippingRulesAndProject().subscribe(this.shippingRuleAndProject)
        this.vm.outputs.shippingRulesSectionIsGone().subscribe(this.shippingRulesSectionIsGone)
        this.vm.outputs.showPledgeCard().subscribe(this.showPledgeCard)
        this.vm.outputs.startLoginToutActivity().subscribe(this.startLoginToutActivity)
        this.vm.outputs.startNewCardActivity().subscribe(this.startNewCardActivity)
        this.vm.outputs.totalAmount().map { it.toString() }.subscribe(this.totalAmount)

        val reward = RewardFactory.rewardWithShipping()

        val bundle = Bundle()
        bundle.putSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION, ScreenLocation(0f, 0f, 0f, 0f))
        bundle.putParcelable(ArgumentsKey.PLEDGE_PROJECT, project)
        bundle.putParcelable(ArgumentsKey.PLEDGE_REWARD, reward)
        this.vm.arguments(bundle)
    }

    @Test
    fun testAnimateRewardCard() {
        setUpEnvironment(environment())

        this.vm.inputs.onGlobalLayout()
        this.animateRewardCard.assertValueCount(1)
    }

    @Test
    fun testCards() {
        val card = StoredCardFactory.discoverCard()
        val mockCurrentUser = MockCurrentUser(UserFactory.user())

        val environment = environment()
                .toBuilder()
                .currentUser(mockCurrentUser)
                .apolloClient(object : MockApolloClient() {
                    override fun getStoredCards(): Observable<List<StoredCard>> {
                        return Observable.just(Collections.singletonList(card))
                    }
                }).build()
        setUpEnvironment(environment)

        this.cards.assertValue(Collections.singletonList(card))

        this.vm.activityResult(ActivityResult.create(ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD, Activity.RESULT_OK, Intent()))

        this.cards.assertValueCount(2)
    }

    @Test
    fun testConversionHiddenForProject() {
        // Set the project currency and the user's chosen currency to the same value
        setUpEnvironment(environment())
        val project = ProjectFactory.project().toBuilder().currency("USD").currentCurrency("USD").build()
        val reward = RewardFactory.reward()

        // the conversion should be hidden.
//        this.vm.inputs.projectAndReward(project, reward)
        this.conversionText.assertValueCount(1)
        this.conversionTextViewIsGone.assertValue(true)
    }

    @Test
    fun testConversionShownForProject() {
        // Set the project currency and the user's chosen currency to different values
        setUpEnvironment(environment())
        val project = ProjectFactory.project().toBuilder().currency("CAD").currentCurrency("USD").build()
        val reward = RewardFactory.reward()

        // USD conversion should shown.
//        this.vm.inputs.projectAndReward(project, reward)
        this.conversionText.assertValueCount(1)
        this.conversionTextViewIsGone.assertValue(false)
    }

    @Test
    fun testPaymentForLoggedInUser() {
        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build())

        this.cards.assertValueCount(1)
        this.continueButtonIsGone.assertValue(true)
        this.paymentContainerIsGone.assertValue(false)
    }

    @Test
    fun testPaymentForLoggedOutUser() {
        setUpEnvironment(environment())

        this.cards.assertNoValues()
        this.continueButtonIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(true)
    }

    @Test
    fun testPaymentLoggingInUser() {
        val mockCurrentUser = MockCurrentUser()
        setUpEnvironment(environment().toBuilder().currentUser(mockCurrentUser).build())

        this.cards.assertNoValues()
        this.continueButtonIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(true)

        mockCurrentUser.refresh(UserFactory.user())

        this.cards.assertValueCount(1)
        this.continueButtonIsGone.assertValues(false, true)
        this.paymentContainerIsGone.assertValues(true, false)
    }

    @Test
    fun testEstimatedDelivery() {
        setUpEnvironment(environment())
        this.estimatedDelivery.assertValue("March 2019")
    }

    @Test
    fun testPledgeAmount() {
        setUpEnvironment(environment())
        this.pledgeAmount.assertValue("$20")
    }

    @Test
    fun testShippingAmount() {
        setUpEnvironmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        this.shippingAmount.assertValue("$30.00")
    }

    @Test
    fun testShippingRuleAndProject() {
        setUpEnvironmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())

        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        this.shippingRuleAndProject.assertValues(Pair.create(shippingRules, project))
    }

    @Test
    fun testShippingRuleSelection_NoShippingRules() {
        setUpEnvironmentForShippingRules(ShippingRulesEnvelopeFactory.emptyShippingRules())
        this.shippingRulesSectionIsGone.assertValues(true)
    }

    @Test
    fun testShippingRuleSelection_WithShippingRules() {
        setUpEnvironmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        this.shippingRulesSectionIsGone.assertValues(false)
    }

    @Test
    fun testShippingRuleSelection() {
        setUpEnvironmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        val defaultRule = ShippingRuleFactory.usShippingRule()

        this.selectedShippingRule.assertValues(defaultRule)

        val selectedRule = ShippingRuleFactory.germanyShippingRule()

        this.vm.inputs.shippingRuleSelected(selectedRule)

        this.selectedShippingRule.assertValues(defaultRule, selectedRule)
    }

    @Test
    fun testShowPledgeCard() {
        setUpEnvironment(environment())

        this.vm.inputs.selectCardButtonClicked(2)
        this.showPledgeCard.assertValuesAndClear(Pair(2, true))

        this.vm.inputs.closeCardButtonClicked(2)
        this.showPledgeCard.assertValue(Pair(2, false))
    }

    @Test
    fun testStartLoginToutActivity() {
        setUpEnvironment(environment())

        this.vm.inputs.continueButtonClicked()
        this.startLoginToutActivity.assertValueCount(1)
    }

    @Test
    fun testStartNewCardActivity() {
        setUpEnvironment(environment())

        this.vm.inputs.newCardButtonClicked()
        this.startNewCardActivity.assertValueCount(1)
    }

    @Test
    fun testTotalAmountWithShippingRules() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .build()
        setUpEnvironment(environment)

        this.totalAmount.assertValues("$20", "$50.00")
    }

    @Test
    fun testTotalAmountWithoutShippingRules() {
        setUpEnvironmentForShippingRules(ShippingRulesEnvelopeFactory.emptyShippingRules())
        this.totalAmount.assertValue("$20")
    }

    private fun setUpEnvironmentForShippingRules(envelope: ShippingRulesEnvelope) {
        val apiClient = object : MockApiClient() {
            override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                return Observable.just(envelope)
            }
        }

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val environment = environment().toBuilder()
                .apiClient(apiClient)
                .currentConfig(currentConfig)
                .build()
        setUpEnvironment(environment)
    }
}
