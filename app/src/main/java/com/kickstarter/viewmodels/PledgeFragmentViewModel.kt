package com.kickstarter.viewmodels

import android.util.Log
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.fragments.PledgeFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode

interface PledgeFragmentViewModel {
    interface Inputs {
        /** Call when user deselects a card they want to pledge with. */
        fun closeCardButtonClicked(position: Int)

        /** Call when the new card button is clicked. */
        fun newCardButtonClicked()

        /** Call when the view has been laid out. */
        fun onGlobalLayout()

        /** Call when user clicks the pledge button. */
        fun pledgeButtonClicked()

        /** Call when a new card has been saved and the cards should be refreshed. */
        fun refreshCards()

        /** Call when user selects a card they want to pledge with. */
        fun selectCardButtonClicked(position: Int)
    }

    interface Outputs {
        /** Emits when the reward card should be animated. */
        fun animateRewardCard(): Observable<Pair<Reward, ScreenLocation>>

        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<List<StoredCard>>

        /** Emits the estimated delivery date string of the reward. */
        fun estimatedDelivery(): Observable<String>

        /** Emits when the cards adapter should return position to initial state. */
        fun hidePledgeCard(): Observable<Int>

        /** Emits the pledge amount string of the reward. */
        fun pledgeAmount(): Observable<String>

        /** Emits when the cards adapter should update selected position. */
        fun showPledgeCard(): Observable<Int>

        /** Emits when we should start the [com.kickstarter.ui.activities.NewCardActivity]. */
        fun startNewCardActivity(): Observable<Void>

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val closeCardButtonClicked = PublishSubject.create<Int>()
        private val newCardButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val pledgeButtonClicked = PublishSubject.create<Void>()
        private val refreshCards = PublishSubject.create<Void>()
        private val selectCardButtonClicked = PublishSubject.create<Int>()

        private val animateReward = PublishSubject.create<Pair<Reward, ScreenLocation>>()
        private val cards = BehaviorSubject.create<List<StoredCard>>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val hidePledgeCard = BehaviorSubject.create<Int>()
        private val pledgeAmount = BehaviorSubject.create<String>()
        private val showPledgeCard = BehaviorSubject.create<Int>()
        private val startNewCardActivity = PublishSubject.create<Void>()

        private val client = environment.apolloClient()
        private val ksCurrency = environment.ksCurrency()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val reward = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_REWARD) as Reward }

            val screenLocation = arguments()
                    .map { it.getSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION) as ScreenLocation }

            val rewardAndLocation = reward
                    .compose<Pair<Reward, ScreenLocation>>(combineLatestPair(screenLocation))

            val project = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_PROJECT) as Project }

            reward
                    .map { it.estimatedDeliveryOn() }
                    .map { dateTime -> dateTime?.let { DateTimeUtils.estimatedDeliveryOn(it) } }
                    .compose(bindToLifecycle())
                    .subscribe(this.estimatedDelivery)

            reward
                    .map { it.minimum() }
                    .compose<Pair<Float, Project>>(combineLatestPair(project))
                    .map<String> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeAmount)

            rewardAndLocation
                    .compose<Pair<Reward, ScreenLocation>>(takeWhen(this.onGlobalLayout))
                    .compose(bindToLifecycle())
                    .subscribe(this.animateReward)

            getListOfStoredCards()
                    .subscribe(this.cards)

            this.cards
                    .subscribe { Log.d("izzytest", "${it.size}") }

            this.selectCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeCard)

            this.closeCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.hidePledgeCard)

            this.newCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.startNewCardActivity)

            activityResult()
                    .filter { it.isRequestCode(ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD) }
                    .filter(ActivityResult::isOk)
                    .switchMap { getListOfStoredCards() }
                    .compose(bindToLifecycle())
                    .subscribe(this.cards)

        }

        override fun closeCardButtonClicked(position: Int) {
            this.closeCardButtonClicked.onNext(position)
        }

        override fun newCardButtonClicked() {
            this.newCardButtonClicked.onNext(null)
        }

        override fun onGlobalLayout() {
            this.onGlobalLayout.onNext(null)
        }

        override fun pledgeButtonClicked() {
            this.pledgeButtonClicked.onNext(null)
        }

        override fun refreshCards() {
            this.refreshCards.onNext(null)
        }

        override fun selectCardButtonClicked(position: Int) {
            this.selectCardButtonClicked.onNext(position)
        }

        override fun animateRewardCard(): Observable<Pair<Reward, ScreenLocation>> = this.animateReward

        override fun cards(): Observable<List<StoredCard>> = this.cards

        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        override fun hidePledgeCard(): Observable<Int> = this.hidePledgeCard

        override fun pledgeAmount(): Observable<String> = this.pledgeAmount

        override fun showPledgeCard(): Observable<Int> = this.showPledgeCard

        override fun startNewCardActivity(): Observable<Void> = this.startNewCardActivity

        private fun getListOfStoredCards(): Observable<List<StoredCard>> {
            return this.client.getStoredCards()
                    .compose(bindToLifecycle())
                    .compose(neverError())
        }

    }
}
