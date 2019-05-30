package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.PledgeData
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

        /** Call when user clicks the decrease pledge button. */
        fun decreasePledgeButtonClicked()

        /** Call when user clicks the increase pledge button. */
        fun increasePledgeButtonClicked()

        /** Call when the new card button is clicked. */
        fun newCardButtonClicked()

        /** Call when the view has been laid out. */
        fun onGlobalLayout()

        /** Call when user clicks the pledge button. */
        fun pledgeButtonClicked()

        /** Call when user selects a card they want to pledge with. */
        fun selectCardButtonClicked(position: Int)

        /** Call when logged out user clicks the continue button. */
        fun continueButtonClicked()
    }

    interface Outputs {
        /** Emits the additional pledge amount string. */
        fun additionalPledgeAmount(): Observable<String>

        /** Emits when the additional pledge amount should be hidden. */
        fun additionalPledgeAmountIsGone(): Observable<Boolean>

        /** Emits when the reward card should be animated. */
        fun animateRewardCard(): Observable<PledgeData>

        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<List<StoredCard>>

        /**  Emits a boolean determining if the continue button should be hidden. */
        fun continueButtonIsGone(): Observable<Boolean>

        /**  Emits a boolean determining if the decrease pledge button should be enabled. */
        fun decreasePledgeButtonIsEnabled(): Observable<Boolean>

        /** Emits the estimated delivery date string of the reward. */
        fun estimatedDelivery(): Observable<String>

        /**  Emits a boolean determining if the estimated delivery info should be hidden. */
        fun estimatedDeliveryInfoIsGone(): Observable<Boolean>

        /**  Emits a boolean determining if the increase pledge button should be enabled.*/
        fun increasePledgeButtonIsEnabled(): Observable<Boolean>

        /**  Emits a boolean determining if the payment container should be hidden. */
        fun paymentContainerIsGone(): Observable<Boolean>

        /** Emits the pledge amount string of the reward. */
        fun pledgeAmount(): Observable<String>

        /** Emits when the cards adapter should update selected position. */
        fun showPledgeCard(): Observable<Pair<Int, Boolean>>

        /** Emits when we should start the [com.kickstarter.ui.activities.LoginToutActivity]. */
        fun startLoginToutActivity(): Observable<Void>

        /** Emits when we should start the [com.kickstarter.ui.activities.NewCardActivity]. */
        fun startNewCardActivity(): Observable<Void>

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val closeCardButtonClicked = PublishSubject.create<Int>()
        private val continueButtonClicked = PublishSubject.create<Void>()
        private val decreasePledgeButtonClicked = PublishSubject.create<Void>()
        private val increasePledgeButtonClicked = PublishSubject.create<Void>()
        private val newCardButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val pledgeButtonClicked = PublishSubject.create<Void>()
        private val selectCardButtonClicked = PublishSubject.create<Int>()

        private val additionalPledgeAmount = BehaviorSubject.create<String>()
        private val additionalPledgeAmountIsGone = BehaviorSubject.create<Boolean>()
        private val animateRewardCard = BehaviorSubject.create<PledgeData>()
        private val cards = BehaviorSubject.create<List<StoredCard>>()
        private val continueButtonIsGone = BehaviorSubject.create<Boolean>()
        private val decreasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val estimatedDeliveryInfoIsGone = BehaviorSubject.create<Boolean>()
        private val increasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val paymentContainerIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeAmount = BehaviorSubject.create<String>()
        private val pledgeCardPosition = BehaviorSubject.create<Pair<Int, Boolean>>()
        private val startLoginToutActivity = PublishSubject.create<Void>()
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

            val project = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_PROJECT) as Project }

            reward
                    .map { it.estimatedDeliveryOn() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { dateTime -> dateTime?.let { DateTimeUtils.estimatedDeliveryOn(it) } }
                    .compose(bindToLifecycle())
                    .subscribe { this.estimatedDelivery.onNext(it) }

            reward
                    .map { RewardUtils.isNoReward(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.estimatedDeliveryInfoIsGone)

            val additionalPledgeAmount = BehaviorSubject.create<Double>(0.0)

            this.increasePledgeButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { additionalPledgeAmount.onNext(additionalPledgeAmount.value + 1) }

            this.decreasePledgeButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { additionalPledgeAmount.onNext(additionalPledgeAmount.value - 1) }

            additionalPledgeAmount
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<String> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP) }
                    .compose(bindToLifecycle())
                    .distinctUntilChanged()
                    .subscribe(this.additionalPledgeAmount)

            additionalPledgeAmount
                    .map { IntegerUtils.isZero(it.toInt()) }
                    .distinctUntilChanged()
                    .subscribe(this.additionalPledgeAmountIsGone)

            additionalPledgeAmount
                    .map { IntegerUtils.isZero(it.toInt()) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .subscribe(this.decreasePledgeButtonIsEnabled)

            val rewardMinimum = reward
                    .map { it.minimum() }

            Observable
                    .merge(rewardMinimum, rewardMinimum.compose<Pair<Double, Double>>(combineLatestPair(additionalPledgeAmount)).map { it.first + it.second })
                    .map { RewardUtils.isMaxRewardAmount(it) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .subscribe(this.increasePledgeButtonIsEnabled)

            rewardMinimum
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<String> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP) }
                    .compose(bindToLifecycle())
                    .subscribe { this.pledgeAmount.onNext(it) }

            Observable.combineLatest(screenLocation, reward, project, ::PledgeData)
                    .compose<PledgeData>(takeWhen(this.onGlobalLayout))
                    .compose(bindToLifecycle())
                    .subscribe { this.animateRewardCard.onNext(it) }

            userIsLoggedIn
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.paymentContainerIsGone)

            userIsLoggedIn
                    .compose(bindToLifecycle())
                    .subscribe(this.continueButtonIsGone)

            userIsLoggedIn
                    .filter { BooleanUtils.isTrue(it) }
                    .switchMap { getListOfStoredCards() }
                    .compose(bindToLifecycle())
                    .subscribe { this.cards.onNext(it) }

            this.selectCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.pledgeCardPosition.onNext(Pair(it, true)) }

            this.closeCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.pledgeCardPosition.onNext(Pair(it, false)) }

            this.newCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.startNewCardActivity.onNext(it) }

            this.continueButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.startLoginToutActivity)

            activityResult()
                    .filter { it.isRequestCode(ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD) }
                    .filter(ActivityResult::isOk)
                    .switchMap { getListOfStoredCards() }
                    .compose(bindToLifecycle())
                    .subscribe { this.cards.onNext(it) }

        }

        override fun closeCardButtonClicked(position: Int) {
            this.closeCardButtonClicked.onNext(position)
        }

        override fun continueButtonClicked() {
            this.continueButtonClicked.onNext(null)
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

        override fun additionalPledgeAmount(): Observable<String> = this.additionalPledgeAmount

        override fun additionalPledgeAmountIsGone(): Observable<Boolean> = this.additionalPledgeAmountIsGone

        override fun animateRewardCard(): Observable<PledgeData> = this.animateRewardCard

        override fun cards(): Observable<List<StoredCard>> = this.cards

        override fun continueButtonIsGone(): Observable<Boolean> = this.continueButtonIsGone

        override fun decreasePledgeButtonIsEnabled(): Observable<Boolean> = this.decreasePledgeButtonIsEnabled

        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        override fun estimatedDeliveryInfoIsGone(): Observable<Boolean> = this.estimatedDeliveryInfoIsGone

        override fun increasePledgeButtonIsEnabled(): Observable<Boolean> = this.increasePledgeButtonIsEnabled

        override fun paymentContainerIsGone(): Observable<Boolean> = this.paymentContainerIsGone

        override fun pledgeAmount(): Observable<String> = this.pledgeAmount

        override fun showPledgeCard(): Observable<Pair<Int, Boolean>> = this.pledgeCardPosition

        override fun startLoginToutActivity(): Observable<Void> = this.startLoginToutActivity

        override fun startNewCardActivity(): Observable<Void> = this.startNewCardActivity

        private fun getListOfStoredCards(): Observable<List<StoredCard>> {
            return this.client.getStoredCards()
                    .compose(bindToLifecycle())
                    .compose(neverError())
        }

    }
}
