package com.kickstarter.viewmodels

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockSharedPreferences
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.StringUtils
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.*
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.*
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ScreenLocation
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.net.CookieManager
import java.util.*

class PledgeFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PledgeFragmentViewModel.ViewModel

    private val addedCard = TestSubscriber<Pair<StoredCard, Project>>()
    private val additionalPledgeAmount = TestSubscriber<String>()
    private val additionalPledgeAmountIsGone = TestSubscriber<Boolean>()
    private val baseUrlForTerms = TestSubscriber<String>()
    private val cardsAndProject = TestSubscriber<Pair<List<StoredCard>, Project>>()
    private val continueButtonIsEnabled = TestSubscriber<Boolean>()
    private val continueButtonIsGone = TestSubscriber<Boolean>()
    private val conversionText = TestSubscriber<String>()
    private val conversionTextViewIsGone = TestSubscriber<Boolean>()
    private val decreasePledgeButtonIsEnabled = TestSubscriber<Boolean>()
    private val deliveryDividerIsGone = TestSubscriber<Boolean>()
    private val deliverySectionIsGone = TestSubscriber<Boolean>()
    private val estimatedDelivery = TestSubscriber<String>()
    private val estimatedDeliveryInfoIsGone = TestSubscriber<Boolean>()
    private val increasePledgeButtonIsEnabled = TestSubscriber<Boolean>()
    private val paymentContainerIsGone = TestSubscriber<Boolean>()
    private val pledgeAmount = TestSubscriber<String>()
    private val pledgeHint = TestSubscriber<String>()
    private val pledgeButtonIsEnabled = TestSubscriber<Boolean>()
    private val pledgeMaximum = TestSubscriber<String>()
    private val pledgeMaximumIsGone = TestSubscriber<Boolean>()
    private val pledgeMinimum = TestSubscriber<String>()
    private val pledgeSectionIsGone = TestSubscriber<Boolean>()
    private val pledgeSummaryAmount = TestSubscriber<CharSequence>()
    private val pledgeSummaryIsGone = TestSubscriber<Boolean>()
    private val pledgeTextColor = TestSubscriber<Int>()
    private val projectCurrencySymbol = TestSubscriber<String>()
    private val selectedShippingRule = TestSubscriber<ShippingRule>()
    private val shippingAmount = TestSubscriber<CharSequence>()
    private val shippingRuleAndProject = TestSubscriber<Pair<List<ShippingRule>, Project>>()
    private val shippingRulesSectionIsGone = TestSubscriber<Boolean>()
    private val shippingSummaryAmount = TestSubscriber<CharSequence>()
    private val shippingSummaryIsGone = TestSubscriber<Boolean>()
    private val shippingSummaryLocation = TestSubscriber<String>()
    private val showNewCardFragment = TestSubscriber<Project>()
    private val showPledgeCard = TestSubscriber<Pair<Int, CardState>>()
    private val showPledgeError = TestSubscriber<Void>()
    private val showPledgeSuccess = TestSubscriber<Void>()
    private val showSCAFlow = TestSubscriber<String>()
    private val showUpdatePaymentError = TestSubscriber<Void>()
    private val showUpdatePaymentSuccess = TestSubscriber<Void>()
    private val showUpdatePledgeError = TestSubscriber<Void>()
    private val showUpdatePledgeSuccess = TestSubscriber<Void>()
    private val snapshotIsGone = TestSubscriber<Boolean>()
    private val startChromeTab = TestSubscriber<String>()
    private val startLoginToutActivity = TestSubscriber<Void>()
    private val startRewardExpandAnimation = TestSubscriber<Void>()
    private val startRewardShrinkAnimation = TestSubscriber<PledgeData>()
    private val totalAmount = TestSubscriber<CharSequence>()
    private val totalAndDeadline = TestSubscriber<Pair<String, String>>()
    private val totalAndDeadlineIsVisible = TestSubscriber<Void>()
    private val totalDividerIsGone = TestSubscriber<Boolean>()
    private val updatePledgeButtonIsEnabled = TestSubscriber<Boolean>()
    private val updatePledgeButtonIsGone = TestSubscriber<Boolean>()
    private val updatePledgeProgressIsGone = TestSubscriber<Boolean>()

    private fun setUpEnvironment(environment: Environment,
                                 reward: Reward? = RewardFactory.rewardWithShipping(),
                                 project: Project? = ProjectFactory.project(),
                                 pledgeReason: PledgeReason? = PledgeReason.PLEDGE) {
        this.vm = PledgeFragmentViewModel.ViewModel(environment)

        this.vm.outputs.addedCard().subscribe(this.addedCard)
        this.vm.outputs.additionalPledgeAmount().subscribe(this.additionalPledgeAmount)
        this.vm.outputs.additionalPledgeAmountIsGone().subscribe(this.additionalPledgeAmountIsGone)
        this.vm.outputs.baseUrlForTerms().subscribe(this.baseUrlForTerms)
        this.vm.outputs.cardsAndProject().subscribe(this.cardsAndProject)
        this.vm.outputs.continueButtonIsEnabled().subscribe(this.continueButtonIsEnabled)
        this.vm.outputs.continueButtonIsGone().subscribe(this.continueButtonIsGone)
        this.vm.outputs.conversionText().subscribe(this.conversionText)
        this.vm.outputs.conversionTextViewIsGone().subscribe(this.conversionTextViewIsGone)
        this.vm.outputs.decreasePledgeButtonIsEnabled().subscribe(this.decreasePledgeButtonIsEnabled)
        this.vm.outputs.deliveryDividerIsGone().subscribe(this.deliveryDividerIsGone)
        this.vm.outputs.deliverySectionIsGone().subscribe(this.deliverySectionIsGone)
        this.vm.outputs.estimatedDelivery().subscribe(this.estimatedDelivery)
        this.vm.outputs.estimatedDeliveryInfoIsGone().subscribe(this.estimatedDeliveryInfoIsGone)
        this.vm.outputs.increasePledgeButtonIsEnabled().subscribe(this.increasePledgeButtonIsEnabled)
        this.vm.outputs.paymentContainerIsGone().subscribe(this.paymentContainerIsGone)
        this.vm.outputs.pledgeAmount().subscribe(this.pledgeAmount)
        this.vm.outputs.pledgeButtonIsEnabled().subscribe(this.pledgeButtonIsEnabled)
        this.vm.outputs.pledgeHint().subscribe(this.pledgeHint)
        this.vm.outputs.pledgeMaximum().subscribe(this.pledgeMaximum)
        this.vm.outputs.pledgeMaximumIsGone().subscribe(this.pledgeMaximumIsGone)
        this.vm.outputs.pledgeMinimum().subscribe(this.pledgeMinimum)
        this.vm.outputs.pledgeSectionIsGone().subscribe(this.pledgeSectionIsGone)
        this.vm.outputs.pledgeSummaryAmount().map { it.toString() }.subscribe(this.pledgeSummaryAmount)
        this.vm.outputs.pledgeSummaryIsGone().subscribe(this.pledgeSummaryIsGone)
        this.vm.outputs.pledgeTextColor().subscribe(this.pledgeTextColor)
        this.vm.outputs.projectCurrencySymbol().map { StringUtils.trim(it.first.toString()) }.subscribe(this.projectCurrencySymbol)
        this.vm.outputs.selectedShippingRule().subscribe(this.selectedShippingRule)
        this.vm.outputs.shippingAmount().map { it.toString() }.subscribe(this.shippingAmount)
        this.vm.outputs.shippingRulesAndProject().subscribe(this.shippingRuleAndProject)
        this.vm.outputs.shippingRulesSectionIsGone().subscribe(this.shippingRulesSectionIsGone)
        this.vm.outputs.shippingSummaryAmount().map { it.toString() }.subscribe(this.shippingSummaryAmount)
        this.vm.outputs.shippingSummaryIsGone().subscribe(this.shippingSummaryIsGone)
        this.vm.outputs.shippingSummaryLocation().subscribe(this.shippingSummaryLocation)
        this.vm.outputs.showNewCardFragment().subscribe(this.showNewCardFragment)
        this.vm.outputs.showPledgeCard().subscribe(this.showPledgeCard)
        this.vm.outputs.showPledgeError().subscribe(this.showPledgeError)
        this.vm.outputs.showPledgeSuccess().subscribe(this.showPledgeSuccess)
        this.vm.outputs.showSCAFlow().subscribe(this.showSCAFlow)
        this.vm.outputs.showUpdatePaymentError().subscribe(this.showUpdatePaymentError)
        this.vm.outputs.showUpdatePaymentSuccess().subscribe(this.showUpdatePaymentSuccess)
        this.vm.outputs.showUpdatePledgeError().subscribe(this.showUpdatePledgeError)
        this.vm.outputs.showUpdatePledgeSuccess().subscribe(this.showUpdatePledgeSuccess)
        this.vm.outputs.snapshotIsGone().subscribe(this.snapshotIsGone)
        this.vm.outputs.startChromeTab().subscribe(this.startChromeTab)
        this.vm.outputs.startLoginToutActivity().subscribe(this.startLoginToutActivity)
        this.vm.outputs.startRewardExpandAnimation().subscribe(this.startRewardExpandAnimation)
        this.vm.outputs.startRewardShrinkAnimation().subscribe(this.startRewardShrinkAnimation)
        this.vm.outputs.totalAmount().map { it.toString() }.subscribe(this.totalAmount)
        this.vm.outputs.totalAndDeadline().subscribe(this.totalAndDeadline)
        this.vm.outputs.totalAndDeadlineIsVisible().subscribe(this.totalAndDeadlineIsVisible)
        this.vm.outputs.totalDividerIsGone().subscribe(this.totalDividerIsGone)
        this.vm.outputs.updatePledgeButtonIsEnabled().subscribe(this.updatePledgeButtonIsEnabled)
        this.vm.outputs.updatePledgeButtonIsGone().subscribe(this.updatePledgeButtonIsGone)
        this.vm.outputs.updatePledgeProgressIsGone().subscribe(this.updatePledgeProgressIsGone)

        val bundle = Bundle()
        val screenLocation = if (pledgeReason == PledgeReason.PLEDGE || pledgeReason == PledgeReason.UPDATE_REWARD) ScreenLocation(0f, 0f, 0f, 0f) else null
        bundle.putSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION, screenLocation)

        bundle.putParcelable(ArgumentsKey.PLEDGE_PROJECT, project?.toBuilder()
                ?.deadline(DateTime.parse("2020-10-23T18:13:09Z"))
                ?.build())
        bundle.putParcelable(ArgumentsKey.PLEDGE_REWARD, reward)
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, pledgeReason)
        this.vm.arguments(bundle)

        this.vm.inputs.onGlobalLayout()
        this.startRewardShrinkAnimation.assertValueCount(1)
    }

    @Test
    fun testBaseUrlForTerms() {
        setUpEnvironment(environment().toBuilder()
                .webEndpoint("www.test.dev")
                .build())

        this.baseUrlForTerms.assertValue("www.test.dev")
    }

    @Test
    fun testCards_whenLoggedIn() {
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
    fun testCards_whenLoggedOut() {
        setUpEnvironment(environment())

        this.cardsAndProject.assertNoValues()
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
    fun testPledgeAmount_whenUpdatingPledge() {
        val reward = RewardFactory.rewardWithShipping()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(40.0)
                .shippingAmount(10f)
                .reward(reward)
                .rewardId(reward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment(), reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.pledgeAmount.assertValue("30")
    }

    @Test
    fun testPledgeScreenConfiguration_whenPledgingShippableRewardAndNotLoggedIn() {
        setUpEnvironment(environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules()))

        this.continueButtonIsEnabled.assertValue(true)
        this.continueButtonIsGone.assertValue(false)
        this.deliveryDividerIsGone.assertValue(false)
        this.deliverySectionIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(true)
        this.pledgeButtonIsEnabled.assertNoValues()
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeSectionIsGone.assertValue(false)
        this.pledgeSummaryIsGone.assertValue(true)
        this.shippingRulesSectionIsGone.assertValue(false)
        this.shippingSummaryIsGone.assertValue(true)
        this.snapshotIsGone.assertValue(false)
        this.totalDividerIsGone.assertValue(false)
        this.updatePledgeButtonIsEnabled.assertNoValues()
        this.updatePledgeButtonIsGone.assertValue(true)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertValue("Pledge Screen Viewed")
    }

    @Test
    fun testPledgeScreenConfiguration_whenPledgingDigitalRewardAndNotLoggedIn() {
        setUpEnvironment(environment(), reward = RewardFactory.noReward())

        this.continueButtonIsEnabled.assertValue(true)
        this.continueButtonIsGone.assertValue(false)
        this.deliveryDividerIsGone.assertValue(false)
        this.deliverySectionIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(true)
        this.pledgeButtonIsEnabled.assertNoValues()
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeSectionIsGone.assertValue(false)
        this.pledgeSummaryIsGone.assertValue(true)
        this.shippingRulesSectionIsGone.assertValue(true)
        this.shippingSummaryIsGone.assertValue(true)
        this.snapshotIsGone.assertValue(false)
        this.totalDividerIsGone.assertValue(false)
        this.updatePledgeButtonIsEnabled.assertNoValues()
        this.updatePledgeButtonIsGone.assertValue(true)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertValue("Pledge Screen Viewed")
    }

    @Test
    fun testPledgeScreenConfiguration_whenPledgingShippableRewardAndLoggedIn() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment)

        this.continueButtonIsEnabled.assertNoValues()
        this.continueButtonIsGone.assertValue(true)
        this.deliveryDividerIsGone.assertValue(false)
        this.deliverySectionIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(false)
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeSectionIsGone.assertValue(false)
        this.pledgeSummaryIsGone.assertValue(true)
        this.shippingRulesSectionIsGone.assertValue(false)
        this.shippingSummaryIsGone.assertValue(true)
        this.snapshotIsGone.assertValue(false)
        this.totalDividerIsGone.assertValue(false)
        this.updatePledgeButtonIsEnabled.assertNoValues()
        this.updatePledgeButtonIsGone.assertValue(true)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertValue("Pledge Screen Viewed")
    }

    @Test
    fun testPledgeScreenConfiguration_whenPledgingDigitalRewardAndLoggedIn() {
        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), RewardFactory.noReward())

        this.continueButtonIsEnabled.assertNoValues()
        this.continueButtonIsGone.assertValue(true)
        this.deliveryDividerIsGone.assertValue(false)
        this.deliverySectionIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(false)
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeSectionIsGone.assertValue(false)
        this.pledgeSummaryIsGone.assertValue(true)
        this.shippingRulesSectionIsGone.assertValue(true)
        this.shippingSummaryIsGone.assertValue(true)
        this.snapshotIsGone.assertValue(false)
        this.totalDividerIsGone.assertValue(false)
        this.updatePledgeButtonIsEnabled.assertNoValues()
        this.updatePledgeButtonIsGone.assertValue(true)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertValue("Pledge Screen Viewed")
    }

    @Test
    fun testPledgeScreenConfiguration_whenUpdatingPledgeOfShippableReward() {
        val shippableReward = RewardFactory.rewardWithShipping()
        val unitedStates = LocationFactory.unitedStates()
        val shippingRule = ShippingRuleFactory.usShippingRule().toBuilder().location(unitedStates).build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(50.0)
                .location(unitedStates)
                .locationId(unitedStates.id())
                .reward(shippableReward)
                .rewardId(shippableReward.id())
                .shippingAmount(shippingRule.cost().toFloat())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val shippingRulesEnvelope = ShippingRulesEnvelopeFactory.shippingRules()
                .toBuilder()
                .shippingRules(listOf(ShippingRuleFactory.germanyShippingRule(), shippingRule))
                .build()

        val environment = environmentForShippingRules(shippingRulesEnvelope)
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()

        setUpEnvironment(environment, shippableReward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.continueButtonIsEnabled.assertNoValues()
        this.continueButtonIsGone.assertValue(true)
        this.deliveryDividerIsGone.assertValue(true)
        this.deliverySectionIsGone.assertValue(true)
        this.paymentContainerIsGone.assertValue(true)
        this.pledgeButtonIsEnabled.assertNoValues()
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeSectionIsGone.assertValue(false)
        this.pledgeSummaryIsGone.assertValue(true)
        this.shippingRulesSectionIsGone.assertValue(false)
        this.shippingSummaryIsGone.assertValue(true)
        this.snapshotIsGone.assertValue(true)
        this.totalDividerIsGone.assertValue(false)
        this.updatePledgeButtonIsEnabled.assertValue(false)
        this.updatePledgeButtonIsGone.assertValue(false)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertNoValues()
    }

    @Test
    fun testPledgeScreenConfiguration_whenUpdatingPledgeOfDigitalReward() {
        val noReward = RewardFactory.noReward()
        val backing = BackingFactory.backing()
                .toBuilder()
                .reward(noReward)
                .rewardId(noReward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), noReward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.continueButtonIsEnabled.assertNoValues()
        this.continueButtonIsGone.assertValue(true)
        this.deliveryDividerIsGone.assertValue(true)
        this.deliverySectionIsGone.assertValue(true)
        this.paymentContainerIsGone.assertValue(true)
        this.pledgeButtonIsEnabled.assertNoValues()
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeSectionIsGone.assertValue(false)
        this.pledgeSummaryIsGone.assertValue(true)
        this.shippingRulesSectionIsGone.assertValue(true)
        this.shippingSummaryIsGone.assertValue(true)
        this.snapshotIsGone.assertValue(true)
        this.totalDividerIsGone.assertValue(false)
        this.updatePledgeButtonIsEnabled.assertValue(false)
        this.updatePledgeButtonIsGone.assertValue(false)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertNoValues()
    }

    @Test
    fun testPledgeScreenConfiguration_whenUpdatingPaymentOfShippableReward() {
        val shippableReward = RewardFactory.rewardWithShipping()
        val unitedStates = LocationFactory.unitedStates()
        val shippingRule = ShippingRuleFactory.usShippingRule().toBuilder().location(unitedStates).build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(50.0)
                .location(unitedStates)
                .locationId(unitedStates.id())
                .reward(shippableReward)
                .rewardId(shippableReward.id())
                .shippingAmount(shippingRule.cost().toFloat())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val shippingRulesEnvelope = ShippingRulesEnvelopeFactory.shippingRules()
                .toBuilder()
                .shippingRules(listOf(ShippingRuleFactory.germanyShippingRule(), shippingRule))
                .build()

        val environment = environmentForShippingRules(shippingRulesEnvelope)
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()

        setUpEnvironment(environment, shippableReward, backedProject, PledgeReason.UPDATE_PAYMENT)

        this.continueButtonIsEnabled.assertNoValues()
        this.continueButtonIsGone.assertValue(true)
        this.deliveryDividerIsGone.assertValue(true)
        this.deliverySectionIsGone.assertValue(true)
        this.paymentContainerIsGone.assertValue(false)
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeMaximumIsGone.assertNoValues()
        this.pledgeSectionIsGone.assertValue(true)
        this.pledgeSummaryIsGone.assertValue(false)
        this.shippingRulesSectionIsGone.assertValue(true)
        this.shippingSummaryIsGone.assertValue(false)
        this.snapshotIsGone.assertValue(true)
        this.totalDividerIsGone.assertValue(true)
        this.updatePledgeButtonIsEnabled.assertNoValues()
        this.updatePledgeButtonIsGone.assertValue(true)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertNoValues()
    }

    @Test
    fun testPledgeScreenConfiguration_whenUpdatingPaymentOfDigitalReward() {
        val noReward = RewardFactory.noReward()
        val backing = BackingFactory.backing()
                .toBuilder()
                .reward(noReward)
                .rewardId(noReward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), noReward, backedProject, PledgeReason.UPDATE_PAYMENT)

        this.continueButtonIsEnabled.assertNoValues()
        this.continueButtonIsGone.assertValue(true)
        this.deliveryDividerIsGone.assertValue(true)
        this.deliverySectionIsGone.assertValue(true)
        this.paymentContainerIsGone.assertValue(false)
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeMaximumIsGone.assertNoValues()
        this.pledgeSectionIsGone.assertValue(true)
        this.pledgeSummaryIsGone.assertValue(false)
        this.shippingRulesSectionIsGone.assertValue(true)
        this.shippingSummaryIsGone.assertValue(true)
        this.snapshotIsGone.assertValue(true)
        this.totalDividerIsGone.assertValue(true)
        this.updatePledgeButtonIsEnabled.assertNoValues()
        this.updatePledgeButtonIsGone.assertValue(true)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertNoValues()
    }

    @Test
    fun testPledgeScreenConfiguration_whenUpdatingRewardToShippableReward() {
        val shippableReward = RewardFactory.rewardWithShipping()

        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment, shippableReward, ProjectFactory.backedProject(), PledgeReason.UPDATE_REWARD)

        this.continueButtonIsEnabled.assertNoValues()
        this.continueButtonIsGone.assertValue(true)
        this.deliveryDividerIsGone.assertValue(false)
        this.deliverySectionIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(true)
        this.pledgeButtonIsEnabled.assertNoValues()
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeSectionIsGone.assertValue(false)
        this.pledgeSummaryIsGone.assertValue(true)
        this.shippingRulesSectionIsGone.assertValue(false)
        this.shippingSummaryIsGone.assertValue(true)
        this.snapshotIsGone.assertValue(false)
        this.totalDividerIsGone.assertValue(false)
        this.updatePledgeButtonIsEnabled.assertValue(true)
        this.updatePledgeButtonIsGone.assertValue(false)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertNoValues()
    }

    @Test
    fun testPledgeScreenConfiguration_whenUpdatingRewardToDigitalReward() {
        val noReward = RewardFactory.noReward()

        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), noReward, ProjectFactory.backedProject(), PledgeReason.UPDATE_REWARD)

        this.continueButtonIsEnabled.assertNoValues()
        this.continueButtonIsGone.assertValue(true)
        this.deliveryDividerIsGone.assertValue(false)
        this.deliverySectionIsGone.assertValue(false)
        this.paymentContainerIsGone.assertValue(true)
        this.pledgeButtonIsEnabled.assertNoValues()
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeSectionIsGone.assertValue(false)
        this.pledgeSummaryIsGone.assertValue(true)
        this.shippingRulesSectionIsGone.assertValue(true)
        this.shippingSummaryIsGone.assertValue(true)
        this.snapshotIsGone.assertValue(false)
        this.totalDividerIsGone.assertValue(false)
        this.updatePledgeButtonIsEnabled.assertValue(true)
        this.updatePledgeButtonIsGone.assertValue(false)
        this.updatePledgeProgressIsGone.assertNoValues()

        this.koalaTest.assertNoValues()
    }

    @Test
    fun testPledgeSummaryAmount() {
        val reward = RewardFactory.noReward()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(30.0)
                .reward(reward)
                .rewardId(reward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment(), reward, backedProject, PledgeReason.UPDATE_PAYMENT)

        this.pledgeSummaryAmount.assertValue("$30")
    }

    @Test
    fun testTotalAmount_whenUpdatingPledge() {
        val reward = RewardFactory.rewardWithShipping()
        val unitedStates = LocationFactory.unitedStates()
        val shippingRule = ShippingRuleFactory.usShippingRule().toBuilder().location(unitedStates).build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(40.0)
                .location(unitedStates)
                .locationId(unitedStates.id())
                .reward(reward)
                .rewardId(reward.id())
                .shippingAmount(shippingRule.cost().toFloat())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val shippingRulesEnvelope = ShippingRulesEnvelopeFactory.shippingRules()
                .toBuilder()
                .shippingRules(listOf(ShippingRuleFactory.germanyShippingRule(), shippingRule))
                .build()

        val environment = environmentForShippingRules(shippingRulesEnvelope)
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .apiClient(object : MockApiClient() {
                    override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                        return Observable.just(shippingRulesEnvelope)
                    }
                })
                .build()
        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.totalAmount.assertValue("$40")
    }

    @Test
    fun testTotalAmount_whenUpdatingPayment() {
        val reward = RewardFactory.rewardWithShipping()
        val unitedStates = LocationFactory.unitedStates()
        val shippingRule = ShippingRuleFactory.usShippingRule().toBuilder().location(unitedStates).build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(40.0)
                .location(unitedStates)
                .locationId(unitedStates.id())
                .reward(reward)
                .rewardId(reward.id())
                .shippingAmount(shippingRule.cost().toFloat())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val shippingRulesEnvelope = ShippingRulesEnvelopeFactory.shippingRules()
                .toBuilder()
                .shippingRules(listOf(ShippingRuleFactory.germanyShippingRule(), shippingRule))
                .build()

        val environment = environmentForShippingRules(shippingRulesEnvelope)
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .apiClient(object : MockApiClient() {
                    override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                        return Observable.just(shippingRulesEnvelope)
                    }
                })
                .build()
        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_PAYMENT)

        this.totalAmount.assertValue("$40")
    }

    @Test
    fun testTotalAmount_whenUpdatingReward() {
        val reward = RewardFactory.rewardWithShipping()
        val backedProject = ProjectFactory.backedProject()

        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_REWARD)

        this.totalAmount.assertValue("$50")
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
    fun testUpdatingPledgeAmount_WithStepper_USProject_USDPref() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_USProject()

        this.vm.inputs.increasePledgeButtonClicked()

        this.additionalPledgeAmount.assertValues("$0", "$1")
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "21")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$9,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertValue("$30")
        this.totalAmount.assertValues("$50", "$51")
        this.projectCurrencySymbol.assertValue("$")

        this.vm.inputs.decreasePledgeButtonClicked()

        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.additionalPledgeAmount.assertValues("$0", "$1", "$0")
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.pledgeButtonIsEnabled.assertValue(true)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "21", "20")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$9,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.totalAmount.assertValues("$50", "$51", "$50")
        this.shippingAmount.assertValue("$30")
    }

    @Test
    fun testUpdatingPledgeAmount_WithInput_USProject_USDPref() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_USProject()

        this.vm.inputs.pledgeInput("40")

        this.additionalPledgeAmount.assertValues("$0", "$20")
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "40")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$9,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.shippingAmount.assertValue("$30")
        this.totalAmount.assertValues("$50", "$70")
        this.totalAndDeadline.assertValues(Pair("$50", "October 23, 2020"), Pair("$70", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)

        this.vm.inputs.pledgeInput("10")

        this.additionalPledgeAmount.assertValues("$0", "$20", "$0")
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "40", "10")
        this.pledgeButtonIsEnabled.assertValues(true, false)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$9,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.projectCurrencySymbol.assertValue("$")
        this.shippingAmount.assertValue("$30")
        this.totalAmount.assertValues("$50", "$70", "$40")
        this.totalAndDeadline.assertValues(Pair("$50", "October 23, 2020"), Pair("$70", "October 23, 2020"),
                Pair("$40", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)

        this.vm.inputs.pledgeInput("10,000")

        this.continueButtonIsEnabled.assertNoValues()
        this.pledgeButtonIsEnabled.assertValues(true, false)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false, true)
        this.increasePledgeButtonIsEnabled.assertValues(true, false)
        this.additionalPledgeAmountIsGone.assertValues(true, false, true, false)
        this.additionalPledgeAmount.assertValues("$0", "$20", "$0", "$9,980")
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.pledgeAmount.assertValues("20", "40", "10", "10,000")
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$9,970")
        this.pledgeMaximumIsGone.assertValues(true, false)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.projectCurrencySymbol.assertValue("$")
        this.shippingAmount.assertValue("$30")
        this.totalAmount.assertValues("$50", "$70", "$40", "$10,030")
        this.totalAndDeadline.assertValues(Pair("$50", "October 23, 2020"), Pair("$70", "October 23, 2020"),
                Pair("$40", "October 23, 2020"), Pair("$10,030", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    @Test
    fun testUpdatingPledgeAmount_NoShipping_WithStepper_USProject_USDPref() {
        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), RewardFactory.reward())

        assertInitialPledgeState_NoShipping()
        assertInitialPledgeCurrencyStates_NoShipping_USProject()

        this.vm.inputs.increasePledgeButtonClicked()

        this.additionalPledgeAmount.assertValues("$0", "$1")
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "21")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$10,000")
        this.pledgeMaximumIsGone.assertValues(true)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.shippingAmount.assertValue("$0")
        this.totalAmount.assertValues("$20", "$21")
        this.totalAndDeadline.assertValues(Pair("$20", "October 23, 2020"), Pair("$21", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)

        this.vm.inputs.decreasePledgeButtonClicked()

        this.additionalPledgeAmount.assertValues("$0", "$1", "$0")
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "21", "20")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$10,000")
        this.pledgeMaximumIsGone.assertValues(true)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.shippingAmount.assertValue("$0")
        this.totalAmount.assertValues("$20", "$21", "$20")
        this.totalAndDeadline.assertValues(Pair("$20", "October 23, 2020"), Pair("$21", "October 23, 2020"),
                Pair("$20", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    @Test
    fun testUpdatingPledgeAmount_NoShipping_WithInput_USProject_USDPref() {
        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), RewardFactory.reward())

        assertInitialPledgeState_NoShipping()
        assertInitialPledgeCurrencyStates_NoShipping_USProject()

        this.vm.inputs.pledgeInput("40")

        this.additionalPledgeAmount.assertValues("$0", "$20")
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "40")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$10,000")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.shippingAmount.assertValue("$0")
        this.totalAmount.assertValues("$20", "$40")
        this.totalAndDeadline.assertValues(Pair("$20", "October 23, 2020"), Pair("$40", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)

        this.vm.inputs.pledgeInput("10")

        this.additionalPledgeAmount.assertValues("$0", "$20", "$0")
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "40", "10")
        this.pledgeButtonIsEnabled.assertValues(true, false)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$10,000")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.projectCurrencySymbol.assertValue("$")
        this.shippingAmount.assertValue("$0")
        this.totalAmount.assertValues("$20", "$40", "$10")
        this.totalAndDeadline.assertValues(Pair("$20", "October 23, 2020"), Pair("$40", "October 23, 2020"),
                Pair("$10", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)

        //US max is 10,000
        this.vm.inputs.pledgeInput("10001")

        this.additionalPledgeAmount.assertValues("$0", "$20", "$0", "$9,981")
        this.additionalPledgeAmountIsGone.assertValues(true, false, true, false)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false, true)
        this.increasePledgeButtonIsEnabled.assertValues(true, false)
        this.pledgeAmount.assertValues("20", "40", "10", "10,001")
        this.pledgeButtonIsEnabled.assertValues(true, false)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$10,000")
        this.pledgeMaximumIsGone.assertValues(true, false)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.projectCurrencySymbol.assertValue("$")
        this.shippingAmount.assertValue("$0")
        this.totalAmount.assertValues("$20", "$40", "$10", "$10,001")
        this.totalAndDeadline.assertValues(Pair("$20", "October 23, 2020"), Pair("$40", "October 23, 2020"),
                Pair("$10", "October 23, 2020"), Pair("$10,001", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    @Test
    fun testUpdatingPledgeAmount_WithShippingChange_USProject_USDPref() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment)

        val defaultRule = ShippingRuleFactory.usShippingRule()
        this.selectedShippingRule.assertValues(defaultRule)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_USProject()

        val selectedRule = ShippingRuleFactory.germanyShippingRule()
        this.vm.inputs.shippingRuleSelected(selectedRule)

        this.additionalPledgeAmountIsGone.assertValues(true)
        this.additionalPledgeAmount.assertValues("$0")
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("$9,970", "$9,960")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("$20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("$")
        this.selectedShippingRule.assertValues(defaultRule, selectedRule)
        this.shippingAmount.assertValues("$30", "$40")
        this.totalAmount.assertValues("$50", "$60")
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    @Test
    fun testUpdatingPledgeAmount_WithStepper_MXProject_USDPref() {
        val project = ProjectFactory.mxProject().toBuilder().currentCurrency("USD").build()
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment, project = project)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_MXProject()

        this.vm.inputs.increasePledgeButtonClicked()

        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 10")
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertValues("$37.50", "$45.00")
        this.conversionTextViewIsGone.assertValues(false)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "30")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("MX$ 199,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("MX$ 20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("MX$")
        this.shippingAmount.assertValue("MX$ 30")
        this.totalAmount.assertValues("MX$ 50", "MX$ 60")
        this.totalAndDeadline.assertValues(Pair("MX$ 50", "October 23, 2020"), Pair("MX$ 60", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)

        this.vm.inputs.decreasePledgeButtonClicked()

        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 10", "MX$ 0")
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertValues("$37.50", "$45.00", "$37.50")
        this.conversionTextViewIsGone.assertValues(false)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "30", "20")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("MX$ 199,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("MX$ 20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("MX$")
        this.shippingAmount.assertValue("MX$ 30")
        this.totalAmount.assertValues("MX$ 50", "MX$ 60", "MX$ 50")
        this.totalAndDeadline.assertValues(Pair("MX$ 50", "October 23, 2020"), Pair("MX$ 60", "October 23, 2020"),
                Pair("MX$ 50", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    @Test
    fun testUpdatingPledgeAmount_WithInput_MXProject_USDPref() {
        val project = ProjectFactory.mxProject().toBuilder().currentCurrency("USD").build()
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment, project = project)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_MXProject()

        this.vm.inputs.pledgeInput("40")

        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 20")
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertValues("$37.50", "$52.50")
        this.conversionTextViewIsGone.assertValues(false)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "40")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("MX$ 199,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("MX$ 20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("MX$")
        this.shippingAmount.assertValue("MX$ 30")
        this.totalAmount.assertValues("MX$ 50", "MX$ 70")
        this.totalAndDeadline.assertValues(Pair("MX$ 50", "October 23, 2020"), Pair("MX$ 70", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)

        this.vm.inputs.pledgeInput("10")

        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 20", "MX$ 0")
        this.additionalPledgeAmountIsGone.assertValues(true, false, true)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertValues("$37.50", "$52.50", "$30.00")
        this.conversionTextViewIsGone.assertValues(false)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20", "40", "10")
        this.pledgeButtonIsEnabled.assertValues(true, false)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("MX$ 199,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("MX$ 20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.projectCurrencySymbol.assertValue("MX$")
        this.shippingAmount.assertValue("MX$ 30")
        this.totalAmount.assertValues("MX$ 50", "MX$ 70", "MX$ 40")
        this.totalAndDeadline.assertValues(Pair("MX$ 50", "October 23, 2020"), Pair("MX$ 70", "October 23, 2020"),
                Pair("MX$ 40", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)

        //MX max is 200,000
        this.vm.inputs.pledgeInput("200000")

        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 20", "MX$ 0", "MX$ 199,980")
        this.additionalPledgeAmountIsGone.assertValues(true, false, true, false)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertValues("$37.50", "$52.50", "$30.00", "$150,022.50")
        this.conversionTextViewIsGone.assertValues(false)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true, false, true)
        this.increasePledgeButtonIsEnabled.assertValues(true, false)
        this.pledgeAmount.assertValues("20", "40", "10", "200,000")
        this.pledgeButtonIsEnabled.assertValues(true, false)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("MX$ 199,970")
        this.pledgeMaximumIsGone.assertValues(true, false)
        this.pledgeMinimum.assertValue("MX$ 20")
        this.pledgeTextColor.assertValues(R.color.ksr_green_500, R.color.ksr_red_400)
        this.projectCurrencySymbol.assertValue("MX$")
        this.shippingAmount.assertValue("MX$ 30")
        this.totalAmount.assertValues("MX$ 50", "MX$ 70", "MX$ 40", "MX$ 200,030")
        this.totalAndDeadline.assertValues(Pair("MX$ 50", "October 23, 2020"), Pair("MX$ 70", "October 23, 2020"),
                Pair("MX$ 40", "October 23, 2020"), Pair("MX$ 200,030", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    @Test
    fun testUpdatingPledgeAmount_WithShippingChange_MXProject_USDPref() {
        val project = ProjectFactory.mxProject().toBuilder().currentCurrency("USD").build()
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment, project = project)

        assertInitialPledgeState_WithShipping()
        assertInitialPledgeCurrencyStates_WithShipping_MXProject()
        val initialRule = ShippingRuleFactory.usShippingRule()
        this.selectedShippingRule.assertValues(initialRule)

        val selectedRule = ShippingRuleFactory.germanyShippingRule()
        this.vm.inputs.shippingRuleSelected(selectedRule)

        this.additionalPledgeAmount.assertValue("MX$ 0")
        this.additionalPledgeAmountIsGone.assertValues(true)
        this.continueButtonIsEnabled.assertNoValues()
        this.conversionText.assertValues("$37.50", "$45.00")
        this.conversionTextViewIsGone.assertValues(false)
        this.decreasePledgeButtonIsEnabled.assertValues(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeMaximum.assertValues("MX$ 199,970", "MX$ 199,960")
        this.pledgeMaximumIsGone.assertValue(true)
        this.pledgeMinimum.assertValue("MX$ 20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.projectCurrencySymbol.assertValue("MX$")
        this.selectedShippingRule.assertValues(initialRule, selectedRule)
        this.shippingAmount.assertValues("MX$ 30", "MX$ 40")
        this.totalAmount.assertValues("MX$ 50", "MX$ 60")
        this.totalAndDeadline.assertValues(Pair("MX$ 50", "October 23, 2020"), Pair("MX$ 60", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    @Test
    fun testPledgeStepping_maxReward_USProject() {
        setUpEnvironment(environment(), RewardFactory.maxReward(Country.US))
        this.additionalPledgeAmountIsGone.assertValuesAndClear(true)
        this.additionalPledgeAmount.assertValuesAndClear("$0")
        this.decreasePledgeButtonIsEnabled.assertValuesAndClear(false)
        this.increasePledgeButtonIsEnabled.assertValuesAndClear(false)
    }

    @Test
    fun testPledgeStepping_maxReward_MXProject() {
        setUpEnvironment(environment(), RewardFactory.maxReward(Country.MX), ProjectFactory.mxProject())
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.additionalPledgeAmount.assertValue("MX$ 0")
        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(false)
    }

    @Test
    fun testPledgeStepping_almostMaxReward_USProject() {
        val almostMaxReward = RewardFactory.reward()
                .toBuilder()
                .minimum((Country.US.maxPledge - Country.US.minPledge).toDouble())
                .build()
        setUpEnvironment(environment(), almostMaxReward)

        this.additionalPledgeAmount.assertValue("$0")
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)

        this.vm.inputs.increasePledgeButtonClicked()

        this.additionalPledgeAmount.assertValuesAndClear("$0", "$1")
        this.decreasePledgeButtonIsEnabled.assertValuesAndClear(false, true)
        this.increasePledgeButtonIsEnabled.assertValuesAndClear(true, false)
        this.additionalPledgeAmountIsGone.assertValuesAndClear(true, false)
    }

    @Test
    fun testPledgeStepping_almostMaxReward_MXProject() {
        val almostMaxMXReward = RewardFactory.reward()
                .toBuilder()
                .minimum((Country.MX.maxPledge - Country.MX.minPledge).toDouble())
                .build()
        setUpEnvironment(environment(), almostMaxMXReward, ProjectFactory.mxProject())

        this.additionalPledgeAmount.assertValue("MX$ 0")
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)

        this.vm.inputs.increasePledgeButtonClicked()

        this.additionalPledgeAmount.assertValues("MX$ 0", "MX$ 10")
        this.additionalPledgeAmountIsGone.assertValues(true, false)
        this.decreasePledgeButtonIsEnabled.assertValues(false, true)
        this.increasePledgeButtonIsEnabled.assertValues(true, false)
    }

    @Test
    fun testRefTagIsSent() {
        val project = ProjectFactory.project()
        val sharedPreferences: SharedPreferences = MockSharedPreferences()
        val cookieManager = CookieManager()

        val environment = environment()
                .toBuilder()
                .cookieManager(cookieManager)
                .sharedPreferences(sharedPreferences)
                .apolloClient(object : MockApolloClient() {
                    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout.Backing> {
                        //Assert that stored cookie is passed in
                        TestCase.assertEquals(createBackingData.refTag, RefTag.discovery())
                        return super.createBacking(createBackingData)
                    }
                })
                .build()

        //Store discovery ref tag for project
        RefTagUtils.storeCookie(RefTag.discovery(), project, cookieManager, sharedPreferences)

        setUpEnvironment(environment, RewardFactory.noReward(), project)

        this.vm.inputs.selectCardButtonClicked(0)
        this.vm.inputs.pledgeButtonClicked("t3st")

        this.koalaTest.assertValues("Pledge Screen Viewed", "Pledge Button Clicked")
    }

    @Test
    fun testShippingSummaryAmount() {
        val reward = RewardFactory.rewardWithShipping()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(30.0)
                .shippingAmount(10f)
                .reward(reward)
                .rewardId(reward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment(), reward, backedProject, PledgeReason.UPDATE_PAYMENT)

        this.shippingSummaryAmount.assertValue("$10")
    }

    @Test
    fun testShippingSummaryLocation() {
        val reward = RewardFactory.rewardWithShipping()
        val nigeria = LocationFactory.nigeria()
        val nigeriaShippingRule = ShippingRuleFactory.usShippingRule()
                .toBuilder()
                .location(nigeria)
                .build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(40.0)
                .location(nigeria)
                .locationId(nigeria.id())
                .reward(reward)
                .rewardId(reward.id())
                .shippingAmount(nigeriaShippingRule.cost().toFloat())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val shippingRulesEnvelope = ShippingRulesEnvelopeFactory.shippingRules()
                .toBuilder()
                .shippingRules(listOf(ShippingRuleFactory.germanyShippingRule(), nigeriaShippingRule))
                .build()

        val environment = environmentForShippingRules(shippingRulesEnvelope)
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .apiClient(object : MockApiClient() {
                    override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                        return Observable.just(shippingRulesEnvelope)
                    }
                })
                .build()
        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_PAYMENT)

        this.shippingSummaryLocation.assertValue("Nigeria")
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

        this.shippingRuleAndProject.assertNoValues()
    }

    @Test
    fun testShippingRulesAndProject_whenDigitalReward() {
        setUpEnvironment(environment(), RewardFactory.reward())

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

        this.shippingRuleAndProject.assertNoValues()
        this.totalAmount.assertNoValues()
    }

    @Test
    fun testShowNewCardFragment() {
        val project = ProjectFactory.project()
        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), RewardFactory.noReward(), project)

        this.vm.inputs.newCardButtonClicked()
        this.showNewCardFragment.assertValue(project)
        this.koalaTest.assertValues("Pledge Screen Viewed", "Add New Card Button Clicked")
    }

    @Test
    fun testShowNewCardFragment_whenUpdatingPaymentMethod() {
        val backedProject = ProjectFactory.backedProject()
        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), RewardFactory.noReward(), backedProject, PledgeReason.UPDATE_PAYMENT)

        this.vm.inputs.newCardButtonClicked()
        this.showNewCardFragment.assertValue(backedProject)
        this.koalaTest.assertNoValues()
    }

    @Test
    fun testShowUpdatePaymentError() {
        val environment = environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout.Backing> {
                        return Observable.error(Exception("womp"))
                    }
                })
                .build()
        setUpEnvironment(environment, RewardFactory.noReward(), ProjectFactory.backedProject(), PledgeReason.UPDATE_PAYMENT)

        this.vm.inputs.selectCardButtonClicked(0)
        this.showPledgeCard.assertValues(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        this.showPledgeCard.assertValues(Pair(0, CardState.PLEDGE), Pair(0, CardState.LOADING), Pair(0, CardState.PLEDGE))
        this.showUpdatePaymentError.assertValueCount(1)
        this.koalaTest.assertValues("Update Payment Method Button Clicked")
    }

    @Test
    fun testShowUpdatePaymentSuccess() {
        setUpEnvironment(environment(), RewardFactory.noReward(), ProjectFactory.backedProject(), PledgeReason.UPDATE_PAYMENT)

        this.vm.inputs.selectCardButtonClicked(0)
        this.showPledgeCard.assertValue(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")
        this.showPledgeCard.assertValues(Pair(0, CardState.PLEDGE), Pair(0, CardState.LOADING))

        this.showUpdatePaymentSuccess.assertValueCount(1)
        this.koalaTest.assertValues("Update Payment Method Button Clicked")
    }

    @Test
    fun testShowUpdatePaymentSuccess_whenRequiresAction_isSuccessful() {
        val environment = environmentForLoggedInUser(UserFactory.user())
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout.Backing> {
                        return Observable.just(CheckoutBackingFactory.requiresAction(true))
                    }
                })
                .build()
        setUpEnvironment(environment, RewardFactory.noReward(), ProjectFactory.backedProject(), PledgeReason.UPDATE_PAYMENT)

        this.vm.inputs.selectCardButtonClicked(0)
        this.showPledgeCard.assertValue(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")
        this.showPledgeCard.assertValues(Pair(0, CardState.PLEDGE), Pair(0, CardState.LOADING))
        this.showSCAFlow.assertValueCount(1)
        this.showUpdatePaymentError.assertNoValues()
        this.showUpdatePaymentSuccess.assertNoValues()

        this.vm.inputs.stripeSetupResultSuccessful()

        this.showPledgeCard.assertValues(Pair(0, CardState.PLEDGE), Pair(0, CardState.LOADING))
        this.showSCAFlow.assertValueCount(1)
        this.showUpdatePaymentError.assertNoValues()
        this.showUpdatePaymentSuccess.assertValueCount(1)
        this.koalaTest.assertValues("Update Payment Method Button Clicked")
    }

    @Test
    fun testShowUpdatePaymentSuccess_whenRequiresAction_isUnsuccessful() {
        val environment = environmentForLoggedInUser(UserFactory.user())
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout.Backing> {
                        return Observable.just(CheckoutBackingFactory.requiresAction(true))
                    }
                })
                .build()
        setUpEnvironment(environment, RewardFactory.noReward(), ProjectFactory.backedProject(), PledgeReason.UPDATE_PAYMENT)

        this.vm.inputs.selectCardButtonClicked(0)
        this.showPledgeCard.assertValue(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")
        this.showPledgeCard.assertValues(Pair(0, CardState.PLEDGE), Pair(0, CardState.LOADING))
        this.showSCAFlow.assertValueCount(1)
        this.showUpdatePaymentError.assertNoValues()
        this.showUpdatePaymentSuccess.assertNoValues()

        this.vm.inputs.stripeSetupResultUnsuccessful(Exception("eek"))

        this.showPledgeCard.assertValues(Pair(0, CardState.PLEDGE), Pair(0, CardState.LOADING), Pair(0, CardState.PLEDGE))
        this.showSCAFlow.assertValueCount(1)
        this.showUpdatePaymentError.assertValueCount(1)
        this.showUpdatePaymentSuccess.assertNoValues()
        this.koalaTest.assertValues("Update Payment Method Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeError_whenUpdatingPledgeWithShipping() {
        val reward = RewardFactory.rewardWithShipping()
        val unitedStates = LocationFactory.unitedStates()
        val backingShippingRule = ShippingRuleFactory.usShippingRule().toBuilder().location(unitedStates).build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(40.0)
                .location(unitedStates)
                .locationId(unitedStates.id())
                .reward(reward)
                .rewardId(reward.id())
                .shippingAmount(backingShippingRule.cost().toFloat())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val germanyShippingRule = ShippingRuleFactory.germanyShippingRule()
        val shippingRulesEnvelope = ShippingRulesEnvelopeFactory.shippingRules()
                .toBuilder()
                .shippingRules(listOf(germanyShippingRule, backingShippingRule))
                .build()

        val environment = environmentForShippingRules(shippingRulesEnvelope)
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .apiClient(object : MockApiClient() {
                    override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                        return Observable.just(shippingRulesEnvelope)
                    }
                })
                .apolloClient(object : MockApolloClient() {
                    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout.Backing> {
                        return Observable.error(Exception("womp"))
                    }
                })
                .build()

        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.vm.inputs.shippingRuleSelected(germanyShippingRule)
        this.vm.inputs.updatePledgeButtonClicked()

        this.updatePledgeProgressIsGone.assertValues(false, true)
        this.showUpdatePledgeError.assertValueCount(1)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeError_whenUpdatingPledgeWithNoShipping() {
        val reward = RewardFactory.noReward()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(30.0)
                .reward(reward)
                .rewardId(reward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val environment = environment().toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout.Backing> {
                        return Observable.error(Exception("womp"))
                    }
                })
                .build()
        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.vm.inputs.pledgeInput("31")
        this.vm.inputs.updatePledgeButtonClicked()

        this.updatePledgeProgressIsGone.assertValues(false, true)
        this.showUpdatePledgeError.assertValueCount(1)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeError_whenUpdatingRewardWithShipping() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout.Backing> {
                        return Observable.error(Exception("womp"))
                    }
                })
                .build()
        setUpEnvironment(environment, project = ProjectFactory.backedProject(), pledgeReason = PledgeReason.UPDATE_REWARD)

        this.vm.inputs.updatePledgeButtonClicked()

        this.updatePledgeProgressIsGone.assertValues(false, true)
        this.showUpdatePledgeError.assertValueCount(1)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeError_whenUpdatingRewardWithNoShipping() {
        val environment = environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout.Backing> {
                        return Observable.error(Exception("womp"))
                    }
                })
                .build()
        setUpEnvironment(environment, RewardFactory.noReward(), ProjectFactory.backedProject(), PledgeReason.UPDATE_REWARD)

        this.vm.inputs.updatePledgeButtonClicked()

        this.updatePledgeProgressIsGone.assertValues(false, true)
        this.showUpdatePledgeError.assertValueCount(1)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPledgeWithShipping() {
        val reward = RewardFactory.rewardWithShipping()
        val unitedStates = LocationFactory.unitedStates()
        val backingShippingRule = ShippingRuleFactory.usShippingRule().toBuilder().location(unitedStates).build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(40.0)
                .location(unitedStates)
                .locationId(unitedStates.id())
                .reward(reward)
                .rewardId(reward.id())
                .shippingAmount(backingShippingRule.cost().toFloat())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val germanyShippingRule = ShippingRuleFactory.germanyShippingRule()
        val shippingRulesEnvelope = ShippingRulesEnvelopeFactory.shippingRules()
                .toBuilder()
                .shippingRules(listOf(germanyShippingRule, backingShippingRule))
                .build()

        val environment = environmentForShippingRules(shippingRulesEnvelope)
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .apiClient(object : MockApiClient() {
                    override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                        return Observable.just(shippingRulesEnvelope)
                    }
                })
                .build()

        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.vm.inputs.shippingRuleSelected(germanyShippingRule)
        this.vm.inputs.updatePledgeButtonClicked()

        this.updatePledgeProgressIsGone.assertValues(false)
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPledgeWithNoShipping() {
        val reward = RewardFactory.noReward()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(30.0)
                .reward(reward)
                .rewardId(reward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment(), reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.vm.inputs.pledgeInput("31")
        this.vm.inputs.updatePledgeButtonClicked()

        this.updatePledgeProgressIsGone.assertValues(false)
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingRewardWithShipping() {
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
        setUpEnvironment(environment, project = ProjectFactory.backedProject(), pledgeReason = PledgeReason.UPDATE_REWARD)

        this.vm.inputs.updatePledgeButtonClicked()

        this.updatePledgeProgressIsGone.assertValues(false)
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingRewardWithNoShipping() {
        setUpEnvironment(environment(), RewardFactory.noReward(), ProjectFactory.backedProject(), PledgeReason.UPDATE_REWARD)

        this.vm.inputs.updatePledgeButtonClicked()

        this.updatePledgeProgressIsGone.assertValues(false)
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenRequiresAction_isSuccessful() {
        val reward = RewardFactory.noReward()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(30.0)
                .reward(reward)
                .rewardId(reward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val environment = environmentForLoggedInUser(UserFactory.user())
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout.Backing> {
                        return Observable.just(CheckoutBackingFactory.requiresAction(true))
                    }
                })
                .build()
        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.vm.inputs.pledgeInput("31")
        this.vm.inputs.updatePledgeButtonClicked()

        this.showSCAFlow.assertValueCount(1)
        this.showUpdatePledgeError.assertNoValues()
        this.showUpdatePledgeSuccess.assertNoValues()
        this.updatePledgeProgressIsGone.assertValue(false)

        this.vm.inputs.stripeSetupResultSuccessful()

        this.showSCAFlow.assertValueCount(1)
        this.showUpdatePledgeError.assertNoValues()
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.updatePledgeProgressIsGone.assertValue(false)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenRequiresAction_isUnsuccessful() {
        val reward = RewardFactory.noReward()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(30.0)
                .reward(reward)
                .rewardId(reward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val environment = environmentForLoggedInUser(UserFactory.user())
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout.Backing> {
                        return Observable.just(CheckoutBackingFactory.requiresAction(true))
                    }
                })
                .build()
        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.vm.inputs.pledgeInput("31")
        this.vm.inputs.updatePledgeButtonClicked()

        this.showSCAFlow.assertValueCount(1)
        this.showUpdatePledgeError.assertNoValues()
        this.showUpdatePledgeSuccess.assertNoValues()
        this.updatePledgeProgressIsGone.assertValue(false)

        this.vm.inputs.stripeSetupResultUnsuccessful(Exception("woops"))

        this.showSCAFlow.assertValueCount(1)
        this.showUpdatePledgeError.assertValueCount(1)
        this.showUpdatePledgeSuccess.assertNoValues()
        this.updatePledgeProgressIsGone.assertValues(false, true)
        this.koalaTest.assertValues("Update Pledge Button Clicked")
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
        setUpEnvironment(environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules()))
        this.continueButtonIsEnabled.assertValue(true)

        this.vm.inputs.pledgeInput("0")
        this.continueButtonIsEnabled.assertValues(true, false)

        this.vm.inputs.pledgeInput("20")
        this.continueButtonIsEnabled.assertValues(true, false, true)

        this.vm.inputs.continueButtonClicked()

        this.startLoginToutActivity.assertValueCount(1)
    }

    @Test
    fun testStartRewardExpandAnimation_whenBackPressed() {
        setUpEnvironment(environment())

        this.vm.inputs.backPressed()

        this.startRewardExpandAnimation.assertValueCount(1)
    }

    @Test
    fun testStartRewardExpandAnimation_whenMiniRewardClicked() {
        setUpEnvironment(environment())

        this.vm.inputs.miniRewardClicked()

        this.startRewardExpandAnimation.assertValueCount(1)
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
    fun testShowPledgeSuccess_whenNoReward() {
        val project = ProjectFactory.project()
        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), RewardFactory.noReward(), project)

        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        //Successfully pledging with a valid amount should show the thanks page
        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.LOADING))
        this.showPledgeSuccess.assertValueCount(1)
        this.showPledgeError.assertNoValues()
        this.koalaTest.assertValues("Pledge Screen Viewed", "Pledge Button Clicked")
    }

    @Test
    fun testShowPledgeSuccess_whenDigitalReward() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environmentForLoggedInUser(UserFactory.user()), reward, project)

        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        //Successfully pledging with a valid amount should show the thanks page
        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.LOADING))
        this.showPledgeSuccess.assertValueCount(1)
        this.showPledgeError.assertNoValues()
        this.koalaTest.assertValues("Pledge Screen Viewed", "Pledge Button Clicked")
    }

    @Test
    fun testShowPledgeSuccess_whenPhysicalReward() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.rewardWithShipping()
        val environment = environmentForShippingRules(ShippingRulesEnvelopeFactory.shippingRules())
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .build()
        setUpEnvironment(environment, reward, project)

        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        //Successfully pledging with a valid amount should show the thanks page
        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.LOADING))
        this.showPledgeSuccess.assertValueCount(1)
        this.showPledgeError.assertNoValues()
        this.koalaTest.assertValues("Pledge Screen Viewed", "Pledge Button Clicked")
    }

    @Test
    fun testShowPledgeSuccess_error() {
        val project = ProjectFactory.project()
        val environment = environmentForLoggedInUser(UserFactory.user())
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout.Backing> {
                        return Observable.error(Throwable("error"))
                    }
                })
                .build()
        setUpEnvironment(environment, RewardFactory.noReward(), project)

        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValue(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE), Pair(0, CardState.LOADING), Pair(0, CardState.PLEDGE))
        this.showPledgeSuccess.assertNoValues()
        this.showPledgeError.assertValueCount(1)
        this.showSCAFlow.assertNoValues()
        this.koalaTest.assertValues("Pledge Screen Viewed", "Pledge Button Clicked")
    }

    @Test
    fun testShowPledgeSuccess_whenRequiresAction_isSuccessful() {
        val project = ProjectFactory.project()
        val environment = environmentForLoggedInUser(UserFactory.user())
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout.Backing> {
                        return Observable.just(CheckoutBackingFactory.requiresAction(true))
                    }
                })
                .build()
        setUpEnvironment(environment, RewardFactory.noReward(), project)

        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.LOADING))
        this.showPledgeSuccess.assertNoValues()
        this.showPledgeError.assertNoValues()
        this.showSCAFlow.assertValueCount(1)
        this.koalaTest.assertValues("Pledge Screen Viewed", "Pledge Button Clicked")

        this.vm.inputs.stripeSetupResultSuccessful()

        this.showPledgeSuccess.assertValueCount(1)
        this.showPledgeError.assertNoValues()
    }

    @Test
    fun testShowPledgeSuccess_whenRequiresAction_isUnsuccessful() {
        val project = ProjectFactory.project()
        val environment = environmentForLoggedInUser(UserFactory.user())
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout.Backing> {
                        return Observable.just(CheckoutBackingFactory.requiresAction(true))
                    }
                })
                .build()
        setUpEnvironment(environment, RewardFactory.noReward(), project)

        this.vm.inputs.selectCardButtonClicked(0)

        this.showPledgeCard.assertValuesAndClear(Pair(0, CardState.PLEDGE))

        this.vm.inputs.pledgeButtonClicked("t3st")

        this.showPledgeCard.assertValue(Pair(0, CardState.LOADING))
        this.showPledgeSuccess.assertNoValues()
        this.showPledgeError.assertNoValues()
        this.showSCAFlow.assertValueCount(1)
        this.koalaTest.assertValues("Pledge Screen Viewed", "Pledge Button Clicked")

        this.vm.inputs.stripeSetupResultUnsuccessful(Exception("yikes"))

        this.showPledgeCard.assertValues(Pair(0, CardState.LOADING), Pair(0, CardState.PLEDGE))
        this.showPledgeSuccess.assertNoValues()
        this.showPledgeError.assertValueCount(1)
    }

    @Test
    fun testUpdatePledgeButtonIsEnabled_UpdatingPledge_whenAmountChanged() {
        val reward = RewardFactory.noReward()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(30.0)
                .reward(reward)
                .rewardId(reward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment(), reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.updatePledgeButtonIsEnabled.assertValues(false)

        this.vm.inputs.pledgeInput("31")
        this.updatePledgeButtonIsEnabled.assertValues(false, true)

        this.vm.inputs.pledgeInput("30")
        this.updatePledgeButtonIsEnabled.assertValues(false, true, false)
    }

    @Test
    fun testUpdatePledgeButtonIsEnabled_UpdatingPledge_whenShippingChanged() {
        val reward = RewardFactory.rewardWithShipping()
        val unitedStates = LocationFactory.unitedStates()
        val backingShippingRule = ShippingRuleFactory.usShippingRule().toBuilder().location(unitedStates).build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(50.0)
                .location(unitedStates)
                .locationId(unitedStates.id())
                .reward(reward)
                .rewardId(reward.id())
                .shippingAmount(backingShippingRule.cost().toFloat())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val germanyShippingRule = ShippingRuleFactory.germanyShippingRule()
        val shippingRulesEnvelope = ShippingRulesEnvelopeFactory.shippingRules()
                .toBuilder()
                .shippingRules(listOf(germanyShippingRule, backingShippingRule))
                .build()

        val environment = environmentForShippingRules(shippingRulesEnvelope)
                .toBuilder()
                .currentUser(MockCurrentUser(UserFactory.user()))
                .apiClient(object : MockApiClient() {
                    override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                        return Observable.just(shippingRulesEnvelope)
                    }
                })
                .build()

        setUpEnvironment(environment, reward, backedProject, PledgeReason.UPDATE_PLEDGE)

        this.selectedShippingRule.assertValue(backingShippingRule)
        this.updatePledgeButtonIsEnabled.assertValues(false)

        this.vm.inputs.shippingRuleSelected(germanyShippingRule)
        this.selectedShippingRule.assertValues(backingShippingRule, germanyShippingRule)
        this.updatePledgeButtonIsEnabled.assertValues(false, true)

        this.vm.inputs.shippingRuleSelected(backingShippingRule)
        this.selectedShippingRule.assertValues(backingShippingRule, germanyShippingRule, backingShippingRule)
        this.updatePledgeButtonIsEnabled.assertValues(false, true, false)

        this.vm.inputs.pledgeInput("500")
        this.updatePledgeButtonIsEnabled.assertValues(false, true, false, true)
    }

    private fun assertInitialPledgeCurrencyStates_NoShipping_USProject() {
        this.additionalPledgeAmount.assertValue("$0")
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.pledgeMaximum.assertValues("$10,000")
        this.pledgeMaximumIsGone.assertValue(true)
        this.projectCurrencySymbol.assertValue("$")
    }

    private fun assertInitialPledgeCurrencyStates_WithShipping_MXProject() {
        this.additionalPledgeAmount.assertValue("MX$ 0")
        this.conversionText.assertValue("$37.50")
        this.conversionTextViewIsGone.assertValues(false)
        this.pledgeMaximum.assertValues("MX$ 199,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.projectCurrencySymbol.assertValue("MX$")
        this.shippingAmount.assertValue("MX$ 30")
        this.totalAmount.assertValues("MX$ 50")
        this.totalAndDeadline.assertValue(Pair("MX$ 50", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    private fun assertInitialPledgeCurrencyStates_WithShipping_USProject() {
        this.additionalPledgeAmount.assertValue("$0")
        this.conversionText.assertNoValues()
        this.conversionTextViewIsGone.assertValues(true)
        this.pledgeMaximum.assertValues("$9,970")
        this.pledgeMaximumIsGone.assertValue(true)
        this.projectCurrencySymbol.assertValue("$")
        this.shippingAmount.assertValue("$30")
        this.totalAmount.assertValues("$50")
        this.totalAndDeadline.assertValue(Pair("$50", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    private fun assertInitialPledgeState_NoShipping() {
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.continueButtonIsEnabled.assertNoValues()
        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
        this.shippingAmount.assertValue("$0")
        this.totalAmount.assertValues("$20")
        this.totalAndDeadline.assertValue(Pair("$20", "October 23, 2020"))
        this.totalAndDeadlineIsVisible.assertValueCount(1)
    }

    private fun assertInitialPledgeState_WithShipping() {
        this.additionalPledgeAmountIsGone.assertValue(true)
        this.continueButtonIsEnabled.assertNoValues()
        this.decreasePledgeButtonIsEnabled.assertValue(false)
        this.increasePledgeButtonIsEnabled.assertValue(true)
        this.pledgeAmount.assertValues("20")
        this.pledgeButtonIsEnabled.assertValue(true)
        this.pledgeHint.assertValue("20")
        this.pledgeTextColor.assertValue(R.color.ksr_green_500)
    }

    private fun environmentForLoggedInUser(user: User) : Environment {
        return environment()
                .toBuilder()
                .currentUser(MockCurrentUser(user))
                .build()
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
