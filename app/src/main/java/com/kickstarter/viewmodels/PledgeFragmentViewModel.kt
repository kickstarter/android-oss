package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.IntegerUtils
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

        /**  */
        fun decreasePledgeButtonClicked()

        /**  */
        fun increasePledgeButtonClicked()

        /** Call when the new card button is clicked. */
        fun newCardButtonClicked()

        /** Call when the view has been laid out. */
        fun onGlobalLayout()

        /** Call when user clicks the pledge button. */
        fun pledgeButtonClicked()

        /** Call when user selects a card they want to pledge with. */
        fun selectCardButtonClicked(position: Int)
    }

    interface Outputs {
        /** Emits when the reward card should be animated. */
        fun animateRewardCard(): Observable<Pair<Reward, ScreenLocation>>

        fun additionalPledgeAmount(): Observable<String>

        fun additionalPledgeAmountIsGone(): Observable<Boolean>

        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<List<StoredCard>>

        /**  */
        fun decreasePledgeButtonIsEnabled(): Observable<Boolean>

        /** Emits the estimated delivery date string of the reward. */
        fun estimatedDelivery(): Observable<String>

        /**  */
        fun increasePledgeButtonIsEnabled(): Observable<Boolean>

        /** Emits the pledge amount string of the reward. */
        fun pledgeAmount(): Observable<String>

        /** Emits when the cards adapter should update selected position. */
        fun showPledgeCard(): Observable<Pair<Int, Boolean>>

        /** Emits when we should start the [com.kickstarter.ui.activities.NewCardActivity]. */
        fun startNewCardActivity(): Observable<Void>

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val closeCardButtonClicked = PublishSubject.create<Int>()
        private val decreasePledgeButtonClicked = PublishSubject.create<Void>()
        private val increasePledgeButtonClicked = PublishSubject.create<Void>()
        private val newCardButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val pledgeButtonClicked = PublishSubject.create<Void>()
        private val selectCardButtonClicked = PublishSubject.create<Int>()

        private val animateReward = BehaviorSubject.create<Pair<Reward, ScreenLocation>>()
        private val additionalPledgeAmount = BehaviorSubject.create<String>()
        private val additionalPledgeAmountIsGone = BehaviorSubject.create<Boolean>()
        private val cards = BehaviorSubject.create<List<StoredCard>>()
        private val decreasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val increasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val pledgeAmount = BehaviorSubject.create<String>()
        private val pledgeCardPosition = BehaviorSubject.create<Pair<Int, Boolean>>()
        private val startNewCardActivity = PublishSubject.create<Void>()

        private val client = environment.apolloClient()
        private val currentUser = environment.currentUser()
        private val ksCurrency = environment.ksCurrency()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val userIsLoggedIn = this.currentUser.isLoggedIn
                    .distinctUntilChanged()

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
                    .subscribe{ this.estimatedDelivery.onNext(it) }

            val additionalPledgeAmount = BehaviorSubject.create<Float>(0f)

            this.increasePledgeButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { additionalPledgeAmount.onNext(additionalPledgeAmount.value + 1) }

            this.decreasePledgeButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { additionalPledgeAmount.onNext(additionalPledgeAmount.value - 1) }

            additionalPledgeAmount
                    .compose<Pair<Float, Project>>(combineLatestPair(project))
                    .map<String> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP) }
                    .compose(bindToLifecycle())
                    .subscribe(this.additionalPledgeAmount)

            additionalPledgeAmount
                    .map { IntegerUtils.isZero(it.toInt()) }
                    .subscribe(this.additionalPledgeAmountIsGone)

            additionalPledgeAmount
                    .map { IntegerUtils.isZero(it.toInt()) }
                    .map { BooleanUtils.negate(it) }
                    .subscribe(this.decreasePledgeButtonIsEnabled)

            reward
                    .map { it.minimum() }
                    .compose<Pair<Float, Project>>(combineLatestPair(project))
                    .map<String> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP) }
                    .compose(bindToLifecycle())
                    .subscribe{ this.pledgeAmount.onNext(it) }

            rewardAndLocation
                    .compose<Pair<Reward, ScreenLocation>>(takeWhen(this.onGlobalLayout))
                    .compose(bindToLifecycle())
                    .subscribe { this.animateReward.onNext(it) }

            userIsLoggedIn
                    .filter { BooleanUtils.isTrue(it) }
                    .switchMap { getListOfStoredCards() }
                    .compose(bindToLifecycle())
                    .subscribe{ this.cards.onNext(it) }

            this.selectCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.pledgeCardPosition.onNext(Pair(it, true)) }

            this.closeCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.pledgeCardPosition.onNext(Pair(it, false)) }

            this.newCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.startNewCardActivity.onNext(it) }

            activityResult()
                    .filter { it.isRequestCode(ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD) }
                    .filter(ActivityResult::isOk)
                    .switchMap { getListOfStoredCards() }
                    .compose(bindToLifecycle())
                    .subscribe{ this.cards.onNext(it) }

        }

        override fun closeCardButtonClicked(position: Int) {
            this.closeCardButtonClicked.onNext(position)
        }

        override fun decreasePledgeButtonClicked() {
            this.decreasePledgeButtonClicked.onNext(null)
        }

        override fun increasePledgeButtonClicked() {
            this.increasePledgeButtonClicked.onNext(null)
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

        override fun selectCardButtonClicked(position: Int) {
            this.selectCardButtonClicked.onNext(position)
        }

        override fun animateRewardCard(): Observable<Pair<Reward, ScreenLocation>> = this.animateReward

        override fun additionalPledgeAmount(): Observable<String> = this.additionalPledgeAmount

        override fun additionalPledgeAmountIsGone(): Observable<Boolean> = this.additionalPledgeAmountIsGone

        override fun cards(): Observable<List<StoredCard>> = this.cards

        override fun decreasePledgeButtonIsEnabled(): Observable<Boolean> = this.decreasePledgeButtonIsEnabled

        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        override fun increasePledgeButtonIsEnabled(): Observable<Boolean> = this.increasePledgeButtonIsEnabled

        override fun pledgeAmount(): Observable<String> = this.pledgeAmount

        override fun showPledgeCard(): Observable<Pair<Int, Boolean>> = this.pledgeCardPosition

        override fun startNewCardActivity(): Observable<Void> = this.startNewCardActivity

        private fun getListOfStoredCards(): Observable<List<StoredCard>> {
            return this.client.getStoredCards()
                    .compose(bindToLifecycle())
                    .compose(neverError())
        }

    }
}
