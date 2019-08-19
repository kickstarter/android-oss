package com.kickstarter.viewmodels

import android.os.Bundle
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.utils.StringUtils
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
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ScreenLocation
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.util.*

class PledgeFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PledgeFragmentViewModel.ViewModel

    private val addedCard = TestSubscriber<Pair<StoredCard, Project>>()
    private val additionalPledgeAmount = TestSubscriber<String>()
    private val additionalPledgeAmountIsGone = TestSubscriber<Boolean>()
    private val animateRewardCard = TestSubscriber<PledgeData>()
    private val baseUrlForTerms = TestSubscriber<String>()
    private val cancelPledgeButtonIsGone = TestSubscriber<Boolean>()
    private val cardsAndProject = TestSubscriber<Pair<List<StoredCard>, Project>>()
    private val changePaymentMethodButtonIsGone = TestSubscriber<Boolean>()
    private val continueButtonIsGone = TestSubscriber<Boolean>()
    private val conversionText = TestSubscriber<String>()
    private val conversionTextViewIsGone = TestSubscriber<Boolean>()
    private val decreasePledgeButtonIsEnabled = TestSubscriber<Boolean>()
    private val estimatedDelivery = TestSubscriber<String>()
    private val estimatedDeliveryInfoIsGone = TestSubscriber<Boolean>()
    private val increasePledgeButtonIsEnabled = TestSubscriber<Boolean>()
    private val paymentContainerIsGone = TestSubscriber<Boolean>()
    private val pledgeAmount = TestSubscriber<String>()
    private val pledgeHint = TestSubscriber<String>()
    private val pledgeTextColor = TestSubscriber<Int>()
    private val projectCurrencySymbol = TestSubscriber<String>()
    private val selectedShippingRule = TestSubscriber<ShippingRule>()
    private val shippingAmount = TestSubscriber<String>()
    private val shippingRuleAndProject = TestSubscriber<Pair<List<ShippingRule>, Project>>()
    private val shippingRulesSectionIsGone = TestSubscriber<Boolean>()
    private val showCancelPledge = TestSubscriber<Project>()
    private val showMinimumWarning = TestSubscriber<String>()
    private val showNewCardFragment = TestSubscriber<Project>()
    private val showPledgeCard = TestSubscriber<Pair<Int, CardState>>()
    private val showPledgeError = TestSubscriber<Void>()
    private val startChromeTab = TestSubscriber<String>()
    private val startLoginToutActivity = TestSubscriber<Void>()
    private val startThanksActivity = TestSubscriber<Project>()
    private val totalAmount = TestSubscriber<String>()
    private val totalContainerIsGone = TestSubscriber<Boolean>()
    private val totalTextColor = TestSubscriber<Int>()
    private val updatePledgeButtonIsGone = TestSubscriber<Boolean>()

    private fun setUpEnvironment(environment: Environment, reward: Reward? = RewardFactory.rewardWithShipping(),
                                 project: Project? = ProjectFactory.project()) {
        this.vm = PledgeFragmentViewModel.ViewModel(environment)

        this.vm.outputs.addedCard().subscribe(this.addedCard)
        this.vm.outputs.additionalPledgeAmount().subscribe(this.additionalPledgeAmount)
        this.vm.outputs.additionalPledgeAmountIsGone().subscribe(this.additionalPledgeAmountIsGone)
        this.vm.outputs.animateRewardCard().subscribe(this.animateRewardCard)
        this.vm.outputs.baseUrlForTerms().subscribe(this.baseUrlForTerms)
        this.vm.outputs.cancelPledgeButtonIsGone().subscribe(this.cancelPledgeButtonIsGone)
        this.vm.outputs.cardsAndProject().subscribe(this.cardsAndProject)
        this.vm.outputs.changePaymentMethodButtonIsGone().subscribe(this.changePaymentMethodButtonIsGone)
        this.vm.outputs.continueButtonIsGone().subscribe(this.continueButtonIsGone)
        this.vm.outputs.conversionText().subscribe(this.conversionText)
        this.vm.outputs.conversionTextViewIsGone().subscribe(this.conversionTextViewIsGone)
        this.vm.outputs.decreasePledgeButtonIsEnabled().subscribe(this.decreasePledgeButtonIsEnabled)
        this.vm.outputs.estimatedDelivery().subscribe(this.estimatedDelivery)
        this.vm.outputs.estimatedDeliveryInfoIsGone().subscribe(this.estimatedDeliveryInfoIsGone)
        this.vm.outputs.increasePledgeButtonIsEnabled().subscribe(this.increasePledgeButtonIsEnabled)
        this.vm.outputs.paymentContainerIsGone().subscribe(this.paymentContainerIsGone)
        this.vm.outputs.pledgeAmount().subscribe(this.pledgeAmount)
        this.vm.outputs.pledgeHint().subscribe(this.pledgeHint)
        this.vm.outputs.pledgeTextColor().subscribe(this.pledgeTextColor)
        this.vm.outputs.projectCurrencySymbol().map { StringUtils.trim(it.first.toString()) }.subscribe(this.projectCurrencySymbol)
        this.vm.outputs.selectedShippingRule().subscribe(this.selectedShippingRule)
        this.vm.outputs.shippingAmount().subscribe(this.shippingAmount)
        this.vm.outputs.shippingRulesAndProject().subscribe(this.shippingRuleAndProject)
        this.vm.outputs.shippingRulesSectionIsGone().subscribe(this.shippingRulesSectionIsGone)
        this.vm.outputs.showCancelPledge().subscribe(this.showCancelPledge)
        this.vm.outputs.showMinimumWarning().subscribe(this.showMinimumWarning)
        this.vm.outputs.showPledgeCard().subscribe(this.showPledgeCard)
        this.vm.outputs.showPledgeError().subscribe(this.showPledgeError)
        this.vm.outputs.startChromeTab().subscribe(this.startChromeTab)
        this.vm.outputs.startLoginToutActivity().subscribe(this.startLoginToutActivity)
        this.vm.outputs.showNewCardFragment().subscribe(this.showNewCardFragment)
        this.vm.outputs.startThanksActivity().subscribe(this.startThanksActivity)
        this.vm.outputs.totalAmount().map { it.toString() }.subscribe(this.totalAmount)
        this.vm.outputs.totalContainerIsGone().subscribe(this.totalContainerIsGone)
        this.vm.outputs.totalTextColor().subscribe(this.totalTextColor)
        this.vm.outputs.updatePledgeButtonIsGone().subscribe(this.updatePledgeButtonIsGone)

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
    fun testBaseUrlForTerms() {
        setUpEnvironment(environment().toBuilder()
                .webEndpoint("www.test.dev")
                .build())

        this.baseUrlForTerms.assertValue("www.test.dev")
    }

    @Test
    fun testCards_whenPhysicalReward() {
        val card = StoredCardFactory.discoverCard()
        val mockCurrentUser = MockCurrentUser(UserFactory.user())

        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(mockCurrentUser)
                .apolloClient(object : MockApolloClient() {
                    override fun getStoredCards(): Observable<List<StoredCard>> {
                        return Observable.just(Collections.singletonList(card))
                    }
                }).build()
        val project = ProjectFactory.project()

        setUpEnvironment(environment, project = project)

        this.cardsAndProject.assertValue(Pair(Collections.singletonList(card), project))

        val visa = StoredCardFactory.visa()
        this.vm.inputs.cardSaved(visa)
        this.vm.inputs.addedCardPosition(0)

        this.cardsAndProject.assertValue(Pair(Collections.singletonList(card), project))
        this.addedCard.assertValue(Pair(visa, project))
        this.showPledgeCard.assertValue(Pair(0, CardState.PLEDGE))
    }

    @Test
    fun testCards_digitalReward() {
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
        val project = ProjectFactory.project()
        setUpEnvironment(environment, RewardFactory.reward(), project)

        this.cardsAndProject.assertValue(Pair(Collections.singletonList(card), project))

        val visa = StoredCardFactory.visa()
        this.vm.inputs.cardSaved(visa)
        this.vm.inputs.addedCardPosition(0)

        this.cardsAndProject.assertValue(Pair(Collections.singletonList(card), project))
        this.addedCard.assertValue(Pair(visa, project))
        this.showPledgeCard.assertValue(Pair(0, CardState.PLEDGE))
    }

    @Test
    fun testPaymentForLoggedInUser_whenPhysicalReward() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment)

        this.cardsAndProject.assertValueCount(1)
        this.continueButtonIsGone.assertValue(true)
        this.paymentContainerIsGone.assertValue(false)
    }

    @Test
    fun testPaymentForLoggedInUser_whenDigitalReward() {
        val environment = environment()
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment, RewardFactory.reward())

        this.cardsAndProject.assertValueCount(1)
        this.continueButtonIsGone.assertValue(true)
        this.paymentContainerIsGone.assertValue(false)
    }

    @Test
    fun testPaymentForLoggedOutUser() {
        setUpEnvironment(environment())

        this.cardsAndProject.assertNoValues()
        this.continueButtonIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(true)
    }

    @Test
    fun testPaymentLoggingInUser_whenPhysicalReward() {
        val mockCurrentUser = MockCurrentUser()
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(mockCurrentUser)
                .build()
        setUpEnvironment(environment)

        this.cardsAndProject.assertNoValues()
        this.continueButtonIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(true)

        mockCurrentUser.refresh(UserFactory.user())

        this.cardsAndProject.assertValueCount(1)
        this.continueButtonIsGone.assertValues(false, true)
        this.paymentContainerIsGone.assertValues(true, false)
    }

    @Test
    fun testPaymentLoggingInUser_whenDigitalReward() {
        val mockCurrentUser = MockCurrentUser()
        setUpEnvironment(environment().toBuilder().currentUser(mockCurrentUser).build(), RewardFactory.reward())

        this.cardsAndProject.assertNoValues()
        this.continueButtonIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(true)

        mockCurrentUser.refresh(UserFactory.user())

        this.cardsAndProject.assertValueCount(1)
        this.continueButtonIsGone.assertValues(false, true)
        this.paymentContainerIsGone.assertValues(true, false)
    }

    @Test
    fun testEstimatedDelivery_whenPhysicalReward() {
        setUpEnvironment(environment())
        this.estimatedDelivery.assertValue("March 2019")
        this.estimatedDeliveryInfoIsGone.assertValue(false)
    }

    @Test
    fun testEstimatedDelivery_whenDigitalReward() {
        setUpEnvironment(environment(), reward = RewardFactory.reward())

        this.estimatedDelivery.assertValue("March 2019")
        this.estimatedDeliveryInfoIsGone.assertValue(false)
    }

    @Test
    fun testEstimatedDelivery_whenNoReward() {
        setUpEnvironment(environment(), RewardFactory.noReward())

        this.estimatedDelivery.assertNoValues()
        this.estimatedDeliveryInfoIsGone.assertValue(true)
    }

    @Test
    fun testManageYourPledgeUIOutputs() {
        setUpEnvironment(environment())
        this.cancelPledgeButtonIsGone.assertValuesAndClear(true)
        this.continueButtonIsGone.assertValuesAndClear(false)
        this.changePaymentMethodButtonIsGone.assertValuesAndClear(true)
        this.paymentContainerIsGone.assertValuesAndClear(true)
        this.totalContainerIsGone.assertValuesAndClear(false)
        this.updatePledgeButtonIsGone.assertValuesAndClear(true)

        val environmentWithLoggedInUser = environment().toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()

        setUpEnvironment(environmentWithLoggedInUser)
        this.cancelPledgeButtonIsGone.assertValuesAndClear(true)
        this.continueButtonIsGone.assertValuesAndClear(true)
        this.changePaymentMethodButtonIsGone.assertValuesAndClear(true)
        this.paymentContainerIsGone.assertValuesAndClear(false)
        this.totalContainerIsGone.assertValuesAndClear(false)
        this.updatePledgeButtonIsGone.assertValuesAndClear(true)

        val backing = BackingFactory.backing()
        val backedReward = backing.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()
        setUpEnvironment(environmentWithLoggedInUser, RewardFactory.reward(), backedProject)
        this.cancelPledgeButtonIsGone.assertValuesAndClear(true)
        this.continueButtonIsGone.assertValuesAndClear(true)
        this.changePaymentMethodButtonIsGone.assertValuesAndClear(true)
        this.paymentContainerIsGone.assertValuesAndClear(false)
        this.totalContainerIsGone.assertValuesAndClear(false)
        this.updatePledgeButtonIsGone.assertValuesAndClear(true)

        setUpEnvironment(environmentWithLoggedInUser, backedReward, backedProject)
        this.cancelPledgeButtonIsGone.assertValuesAndClear(false)
        this.continueButtonIsGone.assertValuesAndClear(true)
        this.changePaymentMethodButtonIsGone.assertValuesAndClear(false)
        this.paymentContainerIsGone.assertValuesAndClear(true)
        this.totalContainerIsGone.assertValuesAndClear(true)
        this.updatePledgeButtonIsGone.assertValuesAndClear(false)
    }

    @Test
    fun testUpdatingPledgeAmount_WithStepper_USProject_USDPref() {
        setUpEnvironment(environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules()))

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_USProject()

        this.vm.inputs.increasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.pledgeAmount.assertValues("20", "21")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertValue("30")
        this.totalAmount.assertValues("50", "51")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValues("$0", "$1")
        this.conversionText.assertValues("$50.00", "$51.00")
        this.conversionTextViewIsGone.assertValues(true)

        this.vm.inputs.decreasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.pledgeAmount.assertValues("20", "21", "20")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.totalAmount.assertValues("50", "51", "50")
        this.shippingAmount.assertValue("30")
        this.projectCurrencySymbol.assertValue("$")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.additionalPledgeAmount.assertValues("$0", "$1", "$0")
        this.conversionText.assertValues("$50.00", "$51.00", "$50.00")
        this.conversionTextViewIsGone.assertValues(true)
    }

    @Test
    fun testUpdatingPledgeAmount_WithInput_USProject_USDPref() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_USProject()

        this.vm.inputs.pledgeInput("40")

        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.pledgeAmount.assertValues("20", "40")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertValue("30")
        this.totalAmount.assertValues("50", "70")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValues("$0", "$20")
        this.conversionText.assertValues("$50.00", "$70.00")
        this.conversionTextViewIsGone.assertValues(true)

        this.vm.inputs.pledgeInput("10")

        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.additionalPledgeAmount.assertValues("$0", "$20", "$0")
        this.pledgeAmount.assertValues("20", "40", "10")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.shippingAmount.assertValue("30")
        this.totalAmount.assertValues("50", "70", "40")
        this.totalTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValues("$0", "$20", "$0")
        this.conversionText.assertValues("$50.00", "$70.00", "$40.00")
        this.conversionTextViewIsGone.assertValues(true)
    }

    @Test
    fun testUpdatingPledgeAmount_NoShipping_WithStepper_USProject_USDPref() {
        setUpEnvironment(environment(), RewardFactory.reward())

        assertInitialPledgeState_NoShipping()
        assertInitialPledgeCurrencyStates_NoShipping_USProject()

        this.vm.inputs.increasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.additionalPledgeAmount.assertValues("$0", "$1")
        this.pledgeAmount.assertValues("20", "21")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertNoValues()
        this.totalAmount.assertValues("20", "21")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.conversionText.assertValues("$20.00", "$21.00")
        this.conversionTextViewIsGone.assertValues(true)
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValues("$0", "$1")
        this.conversionText.assertValues("$20.00", "$21.00")
        this.conversionTextViewIsGone.assertValues(true)

        this.vm.inputs.decreasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.additionalPledgeAmount.assertValues("$0", "$1", "$0")
        this.pledgeAmount.assertValues("20", "21", "20")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertNoValues()
        this.totalAmount.assertValues("20", "21", "20")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.conversionText.assertValues("$20.00", "$21.00", "$20.00")
        this.conversionTextViewIsGone.assertValues(true)
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValues("$0", "$1", "$0")
        this.conversionText.assertValues("$20.00", "$21.00", "$20.00")
        this.conversionTextViewIsGone.assertValues(true)
    }

    @Test
    fun testUpdatingPledgeAmount_NoShipping_WithInput_USProject_USDPref() {
        setUpEnvironment(environment(), RewardFactory.reward())

        assertInitialPledgeState_NoShipping()
        assertInitialPledgeCurrencyStates_NoShipping_USProject()

        this.vm.inputs.pledgeInput("40")

        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.pledgeAmount.assertValues("20", "40")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertNoValues()
        this.totalAmount.assertValues("20", "40")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValues("$0", "$20")
        this.conversionText.assertValues("$20.00", "$40.00")
        this.conversionTextViewIsGone.assertValues(true)

        this.vm.inputs.pledgeInput("10")

        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.additionalPledgeAmount.assertValues("$0", "$20", "$0")
        this.pledgeAmount.assertValues("20", "40", "10")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.shippingAmount.assertNoValues()
        this.totalAmount.assertValues("20", "40", "10")
        this.totalTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValues("$0", "$20", "$0")
        this.conversionText.assertValues("$20.00", "$40.00", "$10.00")
        this.conversionTextViewIsGone.assertValues(true)
    }

    @Test
    fun testUpdatingPledgeAmount_WithShipping_USProject_USDPref() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment)

        val defaultRule = ShippingRuleFactory.usShippingRule()
        this.selectedShippingRule.assertValues(defaultRule)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_USProject()

        val selectedRule = ShippingRuleFactory.germanyShippingRule()
        this.vm.inputs.shippingRuleSelected(selectedRule)

        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true)
        this.pledgeAmount.assertValues("20")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500)
        this.shippingAmount.assertValues("30", "40")
        this.totalAmount.assertValues("50", "60")
        this.totalTextColor.assertValues(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValues("$0")
        this.conversionText.assertValues("$50.00", "$60.00")
        this.conversionTextViewIsGone.assertValues(true)
        this.selectedShippingRule.assertValues(defaultRule, selectedRule)
    }

    @Test
    fun testUpdatingPledgeAmount_WithStepper_MXProject_USDPref() {
        val project = ProjectFactory.mxProject().toBuilder().currentCurrency("USD").build()
        setUpEnvironment(environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules()), project = project)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_MXProject()

        this.vm.inputs.increasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.pledgeAmount.assertValues("20", "30")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertValue("30")
        this.totalAmount.assertValues("50", "60")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("MX$")
        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 10")
        this.conversionText.assertValues("$37.50", "$45.00")
        this.conversionTextViewIsGone.assertValues(false)

        this.vm.inputs.decreasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.pledgeAmount.assertValues("20", "30", "20")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertValue("30")
        this.totalAmount.assertValues("50", "60", "50")
        this.shippingAmount.assertValue("30")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("MX$")
        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 10", "MX$ 0")
        this.conversionText.assertValues("$37.50", "$45.00", "$37.50")
        this.conversionTextViewIsGone.assertValues(false)
    }

    @Test
    fun testUpdatingPledgeAmount_WithInput_MXProject_USDPref() {
        val project = ProjectFactory.mxProject().toBuilder().currentCurrency("USD").build()
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment, project = project)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_MXProject()

        this.vm.inputs.pledgeInput("40")

        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.pledgeAmount.assertValues("20", "40")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertValue("30")
        this.totalAmount.assertValues("50", "70")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 20")
        this.projectCurrencySymbol.assertValue("MX$")
        this.conversionText.assertValues("$37.50", "$52.50")
        this.conversionTextViewIsGone.assertValues(false)

        this.vm.inputs.pledgeInput("10")

        this.decreasePledgeButtonIsEnabled.assertValuesAndClear(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValuesAndClear(true, false, true)
        this.pledgeAmount.assertValues("20", "40", "10")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValuesAndClear(R.color.ksr_green_500, R.color.ksr_red_400)
        this.shippingAmount.assertValue("30")
        this.totalAmount.assertValuesAndClear("50", "70", "40")
        this.totalTextColor.assertValuesAndClear(R.color.ksr_green_500, R.color.ksr_red_400)
        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 20", "MX$ 0")
        this.projectCurrencySymbol.assertValue("MX$")
        this.conversionText.assertValues("$37.50", "$52.50", "$30.00")
        this.conversionTextViewIsGone.assertValues(false)
    }

    @Test
    fun testUpdatingPledgeAmount_WithShipping_MXProject_USDPref() {
        val project = ProjectFactory.mxProject().toBuilder().currentCurrency("USD").build()
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment, project = project)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_MXProject()
        val initialRule = ShippingRuleFactory.usShippingRule()
        this.selectedShippingRule.assertValues(initialRule)

        val selectedRule = ShippingRuleFactory.germanyShippingRule()
        this.vm.inputs.shippingRuleSelected(selectedRule)

        this.decreasePledgeButtonIsEnabled.assertValues(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValues(true)
        this.pledgeAmount.assertValues("20")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertValues("30", "40")
        this.totalAmount.assertValues("50", "60")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("MX$")
        this.additionalPledgeAmount.assertValue("MX$ 0")
        this.conversionText.assertValues("$37.50", "$45.00")
        this.conversionTextViewIsGone.assertValues(false)
        this.selectedShippingRule.assertValues(initialRule, selectedRule)
    }

    @Test
    fun testPledgeStepping_maxReward() {
        setUpEnvironment(environment(), RewardFactory.maxReward(Country.US))
        this.decreasePledgeButtonIsEnabled.assertValuesAndClear(false)
        this.increasePledgeButtonIsEnabled.assertValuesAndClear(false)
        this.additionalPledgeAmountIsGone.assertValuesAndClear(true)
        this.additionalPledgeAmount.assertValuesAndClear("$0")

        setUpEnvironment(environment(), RewardFactory.maxReward(Country.MX), ProjectFactory.mxProject())
        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(false)
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.additionalPledgeAmount.assertValue("MX$ 0")
    }

    @Test
    fun testPledgeStepping_almostMaxReward() {
        val almostMaxReward = RewardFactory.reward()
                .toBuilder()
                .minimum((Country.US.maxPledge - Country.US.minPledge).toDouble())
                .build()
        setUpEnvironment(environment(), almostMaxReward)

        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.additionalPledgeAmount.assertValue("$0")

        this.vm.inputs.increasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValuesAndClear(false, true)
        this.increasePledgeButtonIsEnabled.assertValuesAndClear(true, false)
        this.additionalPledgeAmountIsGone.assertValuesAndClear(true, false)
        this.additionalPledgeAmount.assertValuesAndClear("$0", "$1")

        val almostMaxMXReward = RewardFactory.reward()
                .toBuilder()
                .minimum((Country.MX.maxPledge - Country.MX.minPledge).toDouble())
                .build()
        setUpEnvironment(environment(), almostMaxMXReward, ProjectFactory.mxProject())

        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.additionalPledgeAmount.assertValue("MX$ 0")

        this.vm.inputs.increasePledgeButtonClicked()

        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValues(true, false)
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 10")
    }

    @Test
    fun testShippingAmount() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment)
    }

    @Test
    fun testShippingRulesAndProject_whenPhysicalReward() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        val project = ProjectFactory.project()
        setUpEnvironment(environment, project = project)

        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules()
        this.shippingRuleAndProject.assertValues(Pair.create(shippingRules, project))
    }

    @Test
    fun testShippingRulesAndProject_whenNoReward() {
        setUpEnvironment(environment(), RewardFactory.noReward())

        this.shippingRulesSectionIsGone.assertValues(true)
        this.shippingRuleAndProject.assertNoValues()
    }

    @Test
    fun testShippingRulesAndProject_whenDigitalReward() {
        setUpEnvironment(environment(), RewardFactory.reward())

        this.shippingRulesSectionIsGone.assertValues(true)
        this.shippingRuleAndProject.assertNoValues()
    }

    @Test
    fun testShippingRulesAndProject_error() {
        val environment = environment().toBuilder()
                .apiClient(object : MockApiClient() {
                    override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                        return Observable.error(Throwable("error"))
                    }
                })
                .build()
        val project = ProjectFactory.project()
        setUpEnvironment(environment, project = project)

        this.shippingRulesSectionIsGone.assertValues(false)
        this.shippingRuleAndProject.assertNoValues()
        this.totalAmount.assertNoValues()
    }

    @Test
    fun testShowCancelPledge() {
        val backedProject = ProjectFactory.backedProject()
        val backing = backedProject.backing() ?: BackingFactory.backing()
        val reward = backing.reward() ?: RewardFactory.reward()
        setUpEnvironment(environment(), reward, backedProject)

        this.vm.inputs.cancelPledgeButtonClicked()
        this.showCancelPledge.assertValue(backedProject)
    }

    @Test
    fun testShowNewCardFragment() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment(), project = project)

        this.vm.inputs.newCardButtonClicked()
        this.showNewCardFragment.assertValue(project)
    }

    @Test
    fun testStartChromeTab() {
        setUpEnvironment(environment().toBuilder()
                .webEndpoint("www.test.dev")
                .build())

        this.vm.inputs.linkClicked("www.test.dev/trust")
        this.startChromeTab.assertValuesAndClear("www.test.dev/trust")

        this.vm.inputs.linkClicked("www.test.dev/cookies")
        this.startChromeTab.assertValuesAndClear("www.test.dev/cookies")

        this.vm.inputs.linkClicked("www.test.dev/privacy")
        this.startChromeTab.assertValuesAndClear("www.test.dev/privacy")

        this.vm.inputs.linkClicked("www.test.dev/terms")
        this.startChromeTab.assertValuesAndClear("www.test.dev/terms")
    }

    @Test
    fun testStartLoginToutActivity() {
        setUpEnvironment(environment())

        this.vm.inputs.continueButtonClicked()

        //Login tout should start with a valid pledge amount
        this.startLoginToutActivity.assertValueCount(1)
        this.showMinimumWarning.assertValueCount(0)

        this.vm.inputs.pledgeInput("10")
        this.vm.inputs.continueButtonClicked()

        //Login tout should not start with a invalid pledge amount, warning should show
        this.startLoginToutActivity.assertValueCount(1)
        this.showMinimumWarning.assertValueCount(1)
    }

    @Test
    fun testTotalDoesNotEmit_whenNoSelectedShippingRule() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.emptyShippingRules())
        setUpEnvironment(environment)
        this.selectedShippingRule.assertNoValues()
        this.totalAmount.assertNoValues()

        this.vm.inputs.decreasePledgeButtonClicked()
        this.totalAmount.assertNoValues()

        this.vm.inputs.increasePledgeButtonClicked()
        this.totalAmount.assertNoValues()

        this.vm.inputs.pledgeInput("50")
        this.totalAmount.assertNoValues()
    }

    @Test
    fun testStartThanksActivity_whenNoReward() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment(), RewardFactory.noReward(), project)

        this.vm.inputs.pledgeInput("0")
        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        //Trying to pledge with an invalid amount should show warning
        this.showMinimumWarning.assertValueCount(1)
        this.showPledgeCard.assertNoValues()
        this.startThanksActivity.assertNoValues()
        this.showPledgeError.assertNoValues()

        this.vm.inputs.pledgeInput("1")
        this.vm.inputs.pledgeButtonClicked("t3st")

        //Successfully pledging with a valid amount should show the thanks page
        this.showMinimumWarning.assertValueCount(1)
        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.LOADING))
        this.startThanksActivity.assertValue(project)
        this.showPledgeError.assertNoValues()
    }

    @Test
    fun testStartThanksActivity_whenDigitalReward() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment(), reward, project)

        this.vm.inputs.pledgeInput(reward.minimum().minus(1.0).toString())
        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        //Trying to pledge with an invalid amount should show warning
        this.showMinimumWarning.assertValueCount(1)
        this.showPledgeCard.assertNoValues()
        this.startThanksActivity.assertNoValues()
        this.showPledgeError.assertNoValues()

        this.vm.inputs.pledgeInput(reward.minimum().toString())
        this.vm.inputs.pledgeButtonClicked("t3st")

        //Successfully pledging with a valid amount should show the thanks page
        this.showMinimumWarning.assertValueCount(1)
        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.LOADING))
        this.startThanksActivity.assertValue(project)
        this.showPledgeError.assertNoValues()
    }

    @Test
    fun testStartThanksActivity_whenPhysicalReward() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.rewardWithShipping()
        setUpEnvironment(environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules()), reward, project)

        this.vm.inputs.pledgeInput(reward.minimum().minus(1.0).toString())
        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        //Trying to pledge with an invalid amount should show warning
        this.showMinimumWarning.assertValueCount(1)
        this.showPledgeCard.assertNoValues()
        this.startThanksActivity.assertNoValues()
        this.showPledgeError.assertNoValues()

        this.vm.inputs.pledgeInput(reward.minimum().toString())
        this.vm.inputs.pledgeButtonClicked("t3st")

        //Successfully pledging with a valid amount should show the thanks page
        this.showMinimumWarning.assertValueCount(1)
        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.LOADING))
        this.startThanksActivity.assertValue(project)
        this.showPledgeError.assertNoValues()
    }

    @Test
    fun testStartThanksActivity_error() {
        val project = ProjectFactory.project()
        val environment = environment().toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun createBacking(project: Project, amount: String, paymentSourceId: String, locationId: String?, reward: Reward?): Observable<Boolean> {
                        return Observable.error(Throwable("error"))
                    }
                })
                .build()
        setUpEnvironment(environment, RewardFactory.noReward(), project)

        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.LOADING), Pair(0, CardState.PLEDGE))
        this.startThanksActivity.assertNoValues()
        this.showPledgeError.assertValueCount(1)
    }

    @Test
    fun testStartThanksActivity_unsuccessful() {
        val project = ProjectFactory.project()
        val environment = environment().toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun createBacking(project: Project, amount: String, paymentSourceId: String, locationId: String?, reward: Reward?): Observable<Boolean> {
                        return Observable.just(false)
                    }
                })
                .build()
        setUpEnvironment(environment, RewardFactory.noReward(), project)

        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.LOADING), Pair(0, CardState.PLEDGE))
        this.startThanksActivity.assertNoValues()
        this.showPledgeError.assertValueCount(1)
    }

    private fun assertInitialPledgeCurrencyStates_NoShipping_USProject() {
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValue("$0")
        this.conversionText.assertValue("$20.00")
        this.conversionTextViewIsGone.assertValues(true)
    }

    private fun assertInitialPledgeCurrencyStates_WithShipping_MXProject() {
        this.projectCurrencySymbol.assertValue("MX$")
        this.additionalPledgeAmount.assertValue("MX$ 0")
        this.conversionText.assertValue("$37.50")
        this.conversionTextViewIsGone.assertValues(false)
    }

    private fun assertInitialPledgeCurrencyStates_WithShipping_USProject() {
        this.projectCurrencySymbol.assertValue("$")
        this.additionalPledgeAmount.assertValue("$0")
        this.conversionText.assertValue("$50.00")
        this.conversionTextViewIsGone.assertValues(true)
    }

    private fun assertInitialPledgeState_NoShipping() {
        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.pledgeAmount.assertValues("20")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertNoValues()
        this.totalAmount.assertValues("20")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
    }

    private fun assertInitialPledgeState_WithShipping() {
        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.pledgeAmount.assertValues("20")
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertValue("30")
        this.totalAmount.assertValues("50")
        this.totalTextColor.assertValue(R.color.ksr_green_500)
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
