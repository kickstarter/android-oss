package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.fragments.PledgeFragment
import rx.Observable
import rx.subjects.BehaviorSubject

interface PledgeFragmentViewModel {
    interface Inputs {

    }

    interface Outputs {
        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<List<StoredCard>>

        /** Emits when the cards progress bar should be visible (during a network call). */
        fun cardsProgressBarIsVisible(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val cards = BehaviorSubject.create<List<StoredCard>>()
        private val cardsProgressBarIsVisible = BehaviorSubject.create<Boolean>()

        private val client = environment.apolloClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            getListOfStoredCards()
                    .subscribe { this.cards.onNext(it) }

        }

        override fun cards(): Observable<List<StoredCard>> = this.cards

        override fun cardsProgressBarIsVisible(): Observable<Boolean> = this.cardsProgressBarIsVisible

        private fun getListOfStoredCards(): Observable<List<StoredCard>> {
            return this.client.getStoredCards()
                    .doOnSubscribe { this.cardsProgressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.cardsProgressBarIsVisible.onNext(false) }
                    .compose(bindToLifecycle())
                    .compose(Transformers.neverError())
        }

    }
}
