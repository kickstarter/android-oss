package com.kickstarter.viewmodels

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
        fun closeCardButtonClicked(position : Int)

        /** */
        fun newCardButtonClicked()

        /** */
        fun onGlobalLayout()

        /** Call when user clicks the pledge button. */
        fun pledgeButtonClicked()

        /** Call when a new card has been saved and the cards should be refreshed. */
        fun refreshCards()

        /** Call when user selects a card they want to pledge with. */
        fun selectCardButtonClicked(position : Int)
    }

    interface Outputs {
        /** Emits when the reward card should be animated. */
        fun animateReward(): Observable<Pair<Reward, ScreenLocation>>

        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<List<StoredCard>>

        /** Emits when the cards progress bar should be visible (during a network call). */
        fun cardsProgressBarIsVisible(): Observable<Boolean>

        /** Emits the estimated delivery date string of the reward. */
        fun estimatedDelivery(): Observable<String>

        /** Emits when the cards adapter should return position to initial state. */
        fun hidePledgeCard(): Observable<Int>

        /** Emits the pledge amount string of the reward. */
        fun pledgeAmount(): Observable<String>

        /**  */
        fun showMiniReward(): Observable<Void>

        /** Emits when the cards adapter should update selected position. */
        fun showPledgeCard(): Observable<Int>

        /**  */
        fun startNewCardActivity(): Observable<Void>

        /**  */
        fun startThanksActivity(): Observable<Void>

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val closeCardButtonClicked = PublishSubject.create<Int>()
        private val newCardButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val pledgeButtonClicked = PublishSubject.create<Void>()
        private val refreshCards = PublishSubject.create<Void>()
        private val selectCardButtonClicked = PublishSubject.create<Int>()

        private val animateReward = BehaviorSubject.create<Pair<Reward, ScreenLocation>>()
        private val cards = BehaviorSubject.create<List<StoredCard>>()
        private val cardsProgressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val hidePledgeCard = BehaviorSubject.create<Int>()
        private val pledgeAmount = BehaviorSubject.create<String>()
        private val showMiniReward = BehaviorSubject.create<Void>()
        private val showPledgeCard = BehaviorSubject.create<Int>()
        private val startNewCardActivity = BehaviorSubject.create<Void>()
        private val startThanksActivity = BehaviorSubject.create<Void>()

        private val client = environment.apolloClient()
        private val ksCurrency = environment.ksCurrency()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val reward = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_REWARD) as Reward }
                    .compose(bindToLifecycle())

            val screenLocation = arguments()
                    .map { it.getSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION) as ScreenLocation }
                    .compose(bindToLifecycle())

            val rewardAndLocation = reward
                    .compose<Pair<Reward, ScreenLocation>>(combineLatestPair(screenLocation))

            val project = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_PROJECT) as Project }
                    .compose(bindToLifecycle())

            reward
                    .map { it.estimatedDeliveryOn() }
                    .map { dateTime -> dateTime?.let { DateTimeUtils.estimatedDeliveryOn(it) } }
                    .subscribe(this.estimatedDelivery)

            reward
                    .map { it.minimum() }
                    .compose<Pair<Float,Project>>(combineLatestPair(project))
                    .map<String>{ ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP) }
                    .subscribe(this.pledgeAmount)

            rewardAndLocation
                    .compose<Pair<Reward, ScreenLocation>>(takeWhen(this.onGlobalLayout))
                    .compose(bindToLifecycle())
                    .subscribe(this.animateReward)

            this.view()
                    .switchMap { getListOfStoredCards() }
                    .subscribe(this.cards)

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

        override fun animateReward(): Observable<Pair<Reward, ScreenLocation>> = this.animateReward

        override fun cards(): Observable<List<StoredCard>> = this.cards

        override fun cardsProgressBarIsVisible(): Observable<Boolean> = this.cardsProgressBarIsVisible

        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        override fun hidePledgeCard(): Observable<Int> = this.hidePledgeCard

        override fun pledgeAmount(): Observable<String> = this.pledgeAmount

        override fun showMiniReward(): Observable<Void> = this.showMiniReward

        override fun showPledgeCard(): Observable<Int> = this.showPledgeCard

        override fun startNewCardActivity(): Observable<Void> =this.startNewCardActivity

        override fun startThanksActivity(): Observable<Void> = this.startThanksActivity

        private fun getListOfStoredCards(): Observable<List<StoredCard>> {
            return this.client.getStoredCards()
                    .doOnSubscribe { this.cardsProgressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.cardsProgressBarIsVisible.onNext(false) }
                    .compose(bindToLifecycle())
                    .compose(neverError())
        }

    }
}
