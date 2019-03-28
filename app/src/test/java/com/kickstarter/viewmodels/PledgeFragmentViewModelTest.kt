package com.kickstarter.viewmodels

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.ScreenLocation
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.util.*

class PledgeFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: PledgeFragmentViewModel.ViewModel

    private val animateRewardCard = TestSubscriber<Pair<Reward, ScreenLocation>>()
    private val cards = TestSubscriber<List<StoredCard>>()
    private val estimatedDelivery = TestSubscriber<String>()
    private val pledgeAmount = TestSubscriber<String>()
    private val showPledgeCard = TestSubscriber<Pair<Int, Boolean>>()
    private val startNewCardActivity = TestSubscriber<Void>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = PledgeFragmentViewModel.ViewModel(environment)

        this.vm.outputs.animateRewardCard().subscribe(this.animateRewardCard)
        this.vm.outputs.cards().subscribe(this.cards)
        this.vm.outputs.estimatedDelivery().subscribe(this.estimatedDelivery)
        this.vm.outputs.pledgeAmount().subscribe(this.pledgeAmount)
        this.vm.outputs.showPledgeCard().subscribe(this.showPledgeCard)
        this.vm.outputs.startNewCardActivity().subscribe(this.startNewCardActivity)

        val reward = RewardFactory.rewardWithShipping()
        val project = ProjectFactory.project()
        val bundle = Bundle()
        bundle.putSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION, ScreenLocation(0f, 0f, 0, 0))
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

        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getStoredCards(): Observable<List<StoredCard>> {
                return Observable.just(Collections.singletonList(card))
            }
        }).build())

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

}
