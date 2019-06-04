package com.kickstarter.viewmodels

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.utils.RewardUtils
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

    private val additionalPledgeAmount = TestSubscriber<String>()
    private val additionalPledgeAmountIsGone = TestSubscriber<Boolean>()
    private val animateRewardCard = TestSubscriber<PledgeData>()
    private val cards = TestSubscriber<List<StoredCard>>()
    private val continueButtonIsGone = TestSubscriber<Boolean>()
    private val conversionText = TestSubscriber<String>()
    private val conversionTextViewIsGone = TestSubscriber<Boolean>()
    private val decreasePledgeButtonIsEnabled = TestSubscriber<Boolean>()
    private val estimatedDelivery = TestSubscriber<String>()
    private val increasePledgeButtonIsEnabled = TestSubscriber<Boolean>()
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

    private fun setUpEnvironment(environment: Environment, reward: Reward? = RewardFactory.rewardWithShipping(),
                                 project: Project? = ProjectFactory.project()) {
        this.vm = PledgeFragmentViewModel.ViewModel(environment)

        this.vm.outputs.additionalPledgeAmount().subscribe(this.additionalPledgeAmount)
        this.vm.outputs.additionalPledgeAmountIsGone().subscribe(this.additionalPledgeAmountIsGone)
        this.vm.outputs.animateRewardCard().subscribe(this.animateRewardCard)
        this.vm.outputs.cards().subscribe(this.cards)
        this.vm.outputs.continueButtonIsGone().subscribe(this.continueButtonIsGone)
        this.vm.outputs.conversionText().subscribe(this.conversionText)
        this.vm.outputs.conversionTextViewIsGone().subscribe(this.conversionTextViewIsGone)
        this.vm.outputs.decreasePledgeButtonIsEnabled().subscribe(this.decreasePledgeButtonIsEnabled)
        this.vm.outputs.estimatedDelivery().subscribe(this.estimatedDelivery)
        this.vm.outputs.increasePledgeButtonIsEnabled().subscribe(this.increasePledgeButtonIsEnabled)
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
    fun testConversionHiddenForPledgeTotal() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())

        // Set the project currency and the user's chosen currency to the same value
        val project = ProjectFactory.project().toBuilder().currency("USD").currentCurrency("USD").build()
        val reward = RewardFactory.reward()

        setUpEnvironment(environment, reward, project)

        // the conversion should be hidden.
        this.conversionText.assertValueCount(1)
        this.conversionTextViewIsGone.assertValue(true)
    }

    @Test
    fun testConversionShownForPledgeTotal() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())

        // Set the project currency and the user's chosen currency to different values
        val project = ProjectFactory.project().toBuilder().currency("CAD").currentCurrency("USD").build()
        val reward = RewardFactory.reward()

        setUpEnvironment(environment, reward, project)

        // USD conversion should shown.
        this.conversionText.assertValueCount(1)
        this.conversionTextViewIsGone.assertValue(false)
    }

    @Test
    fun testConversionText_WhenStepperChangesValue() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())

        // Set the project currency and the user's chosen currency to different values
        val project = ProjectFactory.project().toBuilder().currency("USD").currentCurrency("CAD").build()
        val reward = RewardFactory.reward()

        setUpEnvironment(environment, reward, project)

        this.conversionText.assertValue("CA$ 50.00")

        this.vm.decreasePledgeButtonClicked()
        this.conversionText.assertValues("CA$ 50.00", "CA$ 49.00")
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
    fun testPledgeStepping() {
        setUpEnvironment(environment())

        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.additionalPledgeAmount.assertValue("$0")

        this.vm.inputs.increasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.additionalPledgeAmount.assertValues("$0", "$1")

        this.vm.inputs.decreasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.additionalPledgeAmount.assertValues("$0", "$1", "$0")
    }

    @Test
    fun testPledgeStepping_maxReward() {
        setUpEnvironment(environment(), RewardFactory.maxReward())

        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(false)
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.additionalPledgeAmount.assertValue("$0")
    }

    @Test
    fun testPledgeStepping_almostMaxReward() {
        val oneLessThanMax = RewardUtils.MAX_REWARD_LIMIT - 1
        setUpEnvironment(environment(), RewardFactory.reward().toBuilder().minimum(oneLessThanMax).build())

        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.additionalPledgeAmount.assertValue("$0")

        this.vm.inputs.increasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValues(true, false)
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.additionalPledgeAmount.assertValues("$0", "$1")
    }

    @Test
    fun testShippingAmount() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment)
        this.shippingAmount.assertValue("$30.00")
    }

    @Test
    fun testShippingRuleAndProject() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        val project = ProjectFactory.project()
        setUpEnvironment(environment, project = project)

        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        this.shippingRuleAndProject.assertValues(Pair.create(shippingRules, project))
    }

    @Test
    fun testShippingRuleSelection_NoShippingRules() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.emptyShippingRules())
        setUpEnvironment(environment)
        this.shippingRulesSectionIsGone.assertValues(true)
    }

    @Test
    fun testShippingRuleSelection_WithShippingRules() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment)
        this.shippingRulesSectionIsGone.assertValues(false)
    }

    @Test
    fun testShippingRuleSelection() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment)

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

        this.totalAmount.assertValues("$50.00")
    }

    @Test
    fun testTotalAmountWithShippingRules_WhenStepperChangesValue() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .build()
        setUpEnvironment(environment)

        this.totalAmount.assertValues("$50.00")
        this.vm.decreasePledgeButtonClicked()
        this.totalAmount.assertValues("$50.00", "$49.00")
        this.vm.increasePledgeButtonClicked()
        this.totalAmount.assertValues("$50.00", "$49.00", "$50.00")
    }

    @Test
    fun testTotalAmountWithoutShippingRules() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.emptyShippingRules())
        setUpEnvironment(environment)
        this.totalAmount.assertValue("$20.00")
    }

    @Test
    fun testTotalAmountWithoutShippingRules_WhenStepperChangesValue() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.emptyShippingRules())
        setUpEnvironment(environment)
        this.totalAmount.assertValue("$20.00")
        this.vm.decreasePledgeButtonClicked()
        this.totalAmount.assertValues("$20.00", "$19.00")
        this.vm.increasePledgeButtonClicked()
        this.totalAmount.assertValues("$20.00", "$19.00", "$20.00")
    }

    private fun environmentForShippingRules(envelope: ShippingRulesEnvelope): Environment {
        val apiClient = object : MockApiClient() {
            override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                return Observable.just(envelope)
            }
        }

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        return environment().toBuilder()
                .apiClient(apiClient)
                .currentConfig(currentConfig)
                .build()
    }
}
