package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.fragments.PledgeFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode

interface PledgeFragmentViewModel {
    interface Inputs {
        /** Call when user clicks the pledge button. */
        fun pledgeButtonClicked()

        /** Call when user selects a card they want to pledge with. */
        fun selectCardButtonClicked(position : Int)
    }

    interface Outputs {
        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<List<StoredCard>>

        /** Emits when the cards progress bar should be visible (during a network call). */
        fun cardsProgressBarIsVisible(): Observable<Boolean>

        /** Emits the estimated delivery date string of the reward. */
        fun estimatedDelivery(): Observable<String>

        /** Emits the pledge amount string of the reward. */
        fun pledgeAmount(): Observable<String>

        /**  */
        fun showThanksActivity(): Observable<Void>

        /** Emits when the cards adapter should update selected position */
        fun updateSelectedPosition(): Observable<Int>

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val pledgeButtonClicked = PublishSubject.create<Void>()
        private val selectCardButtonClicked = PublishSubject.create<Int>()

        private val cards = BehaviorSubject.create<List<StoredCard>>()
        private val cardsProgressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val pledgeAmount = BehaviorSubject.create<String>()
        private val showThanksActivity = BehaviorSubject.create<Void>()
        private val updateSelectedPosition = BehaviorSubject.create<Int>()

        private val client = environment.apolloClient()
        private val ksCurrency = environment.ksCurrency()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val reward = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_REWARD) as Reward }
                    .compose(bindToLifecycle())

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

            this.selectCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.updateSelectedPosition)

            getListOfStoredCards()
                    .subscribe { this.cards.onNext(it) }

        }

        override fun pledgeButtonClicked() {
            this.pledgeButtonClicked.onNext(null)
        }

        override fun selectCardButtonClicked(position: Int) {
            this.selectCardButtonClicked.onNext(position)
        }

        override fun cards(): Observable<List<StoredCard>> = this.cards

        override fun cardsProgressBarIsVisible(): Observable<Boolean> = this.cardsProgressBarIsVisible

        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        override fun pledgeAmount(): Observable<String> = this.pledgeAmount

        override fun showThanksActivity(): Observable<Void> = this.showThanksActivity

        override fun updateSelectedPosition(): Observable<Int> = this.updateSelectedPosition

        private fun getListOfStoredCards(): Observable<List<StoredCard>> {
            return this.client.getStoredCards()
                    .doOnSubscribe { this.cardsProgressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.cardsProgressBarIsVisible.onNext(false) }
                    .compose(bindToLifecycle())
                    .compose(Transformers.neverError())
        }

    }
}
