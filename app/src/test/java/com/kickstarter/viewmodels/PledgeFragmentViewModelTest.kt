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
import com.kickstarter.ui.data.ScreenLocation
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import timber.log.Timber
import java.util.*

class PledgeFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PledgeFragmentViewModel.ViewModel

    private val project = ProjectFactory.project()

    private val animateRewardCard = TestSubscriber<Pair<Reward, ScreenLocation>>()
    private val cards = TestSubscriber<List<StoredCard>>()
    private val estimatedDelivery = TestSubscriber<String>()
    private val pledgeAmount = TestSubscriber<String>()
    private val selectedShippingRule = TestSubscriber<ShippingRule>()
    private val shippingAmount = TestSubscriber<String>()
    private val shippingRuleAndProject = TestSubscriber<Pair<List<ShippingRule>, Project>>()
    private val shippingRules = TestSubscriber<List<ShippingRule>>()
    private val shippingRulesSectionIsGone = TestSubscriber<Boolean>()
    private val showPledgeCard = TestSubscriber<Pair<Int, Boolean>>()
    private val startNewCardActivity = TestSubscriber<Void>()
    private val totalAmount = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PledgeFragmentViewModel.ViewModel(environment)

        this.vm.outputs.animateRewardCard().subscribe(this.animateRewardCard)
        this.vm.outputs.cards().subscribe(this.cards)
        this.vm.outputs.estimatedDelivery().subscribe(this.estimatedDelivery)
        this.vm.outputs.pledgeAmount().map { it.toString() }.subscribe(this.pledgeAmount)
        this.vm.outputs.selectedShippingRule().subscribe(this.selectedShippingRule)
        this.vm.outputs.shippingAmount().map { it.toString() }.subscribe(this.shippingAmount)
        this.vm.outputs.shippingRulesAndProject().subscribe(this.shippingRuleAndProject)
        this.vm.outputs.shippingRules().subscribe(this.shippingRules)
        this.vm.outputs.shippingRulesSectionIsGone().subscribe(this.shippingRulesSectionIsGone)
        this.vm.outputs.showPledgeCard().subscribe(this.showPledgeCard)
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
    fun testShippingRules() {
        setUpEnvironmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        this.shippingRules.assertValueCount(1)
    }

    @Test
    fun testShippingRuleAndProject() {
        setUpEnvironmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())

        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        this.shippingRuleAndProject.assertValues(Pair.create(shippingRules ,project))
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

        this.vm.inputs.shippingRuleSelected(defaultRule)

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
    fun testStartNewCardActivity() {
        setUpEnvironment(environment())

        this.vm.inputs.newCardButtonClicked()
        this.startNewCardActivity.assertValueCount(1)
    }

    @Test
    fun testTotalAmount() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .build()
        setUpEnvironment(environment)

        this.totalAmount.assertValue("$50.00")
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
