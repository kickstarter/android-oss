package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.NewCardActivity
import com.stripe.android.model.Card
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface NewCardViewModel {
    interface Inputs {
        /** Call when the card validity changes. */
        fun card(card: Card?)

        /** Call when the name field changes. */
        fun name(name: String)

        /** Call when the postal code field changes. */
        fun postalCode(postalCode: String)

        /** Call when the user clicks the save icon. */
        fun saveCardClicked()
    }

    interface Outputs {
        /** Emits when the password update was unsuccessful. */
        fun error(): Observable<String>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the save button should be enabled. */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the card was saved successfully. */
        fun success(): Observable<String>

    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<NewCardActivity>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<Card?>()
        private val name = PublishSubject.create<String>()
        private val postalCode = PublishSubject.create<String>()
        private val saveCardClicked = PublishSubject.create<Void>()

        private val error = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()

        val inputs: NewCardViewModel.Inputs = this
        val outputs: NewCardViewModel.Outputs = this

        private val apolloClient: ApolloClientType = this.environment.apolloClient()

        init {
            val cardForm = Observable.combineLatest(this.name.startWith(""),
                    this.card.startWith(null,null),
                    this.postalCode.startWith(""),
                    { name, card, postalCode -> CardForm(name, card, postalCode) })
                    .skip(1)

            cardForm
                    .map { it.isValid() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.saveButtonIsEnabled)
        }

        override fun card(card: Card?) {
            this.card.onNext(card)
        }

        override fun name(name: String) {
            this.name.onNext(name)
        }

        override fun postalCode(postalCode: String) {
            this.postalCode.onNext(postalCode)
        }

        override fun saveCardClicked() {
            this.saveCardClicked.onNext(null)
        }

        override fun error(): Observable<String> {
            return this.error
        }

        override fun progressBarIsVisible(): Observable<Boolean> {
            return this.progressBarIsVisible
        }

        override fun saveButtonIsEnabled(): Observable<Boolean> {
            return this.saveButtonIsEnabled
        }

        override fun success(): Observable<String> {
            return this.success
        }

        data class CardForm(val name: String, val card: Card?, val postalCode: String) {
            fun isValid(): Boolean {
                return isNotEmptyAndTwoWords(this.name)
                        && isNotEmpty(this.postalCode)
                        && isValidCard(this.card)
            }

            private fun isValidCard(card: Card?): Boolean {
                return card != null && card.validateCard()
            }

            private fun isNotEmpty(s: String): Boolean {
                return !s.isEmpty()
            }

            private fun isNotEmptyAndTwoWords(s: String): Boolean {
                return isNotEmpty(s) && s.split(" ").size > 1
            }
        }
    }
}