package com.kickstarter.viewmodels

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.StoredCard
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

    private val animateRewardCard = TestSubscriber<PledgeData>()
    private val cards = TestSubscriber<List<StoredCard>>()
    private val continueButtonIsGone = TestSubscriber<Boolean>()
    private val estimatedDelivery = TestSubscriber<String>()
    private val paymentContainerIsGone = TestSubscriber<Boolean>()
    private val pledgeAmount = TestSubscriber<String>()
    private val showPledgeCard = TestSubscriber<Pair<Int, Boolean>>()
    private val startLoginToutActivity = TestSubscriber<Void>()
    private val startNewCardActivity = TestSubscriber<Void>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PledgeFragmentViewModel.ViewModel(environment)

        this.vm.outputs.animateRewardCard().subscribe(this.animateRewardCard)
        this.vm.outputs.cards().subscribe(this.cards)
        this.vm.outputs.continueButtonIsGone().subscribe(this.continueButtonIsGone)
        this.vm.outputs.estimatedDelivery().subscribe(this.estimatedDelivery)
        this.vm.outputs.paymentContainerIsGone().subscribe(this.paymentContainerIsGone)
        this.vm.outputs.pledgeAmount().subscribe(this.pledgeAmount)
        this.vm.outputs.showPledgeCard().subscribe(this.showPledgeCard)
        this.vm.outputs.startLoginToutActivity().subscribe(this.startLoginToutActivity)
        this.vm.outputs.startNewCardActivity().subscribe(this.startNewCardActivity)

        val reward = RewardFactory.rewardWithShipping()
        val project = ProjectFactory.project()
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

}
