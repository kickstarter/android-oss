package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.fragments.NewCardFragment
import com.stripe.android.model.Card
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface NewCardFragmentViewModel {
    interface Inputs {
        /** Call when the card validity changes. */
        fun card(card: Card?)

        /** Call when the name field changes. */
        fun name(name: String)

        /** Call when the postal code field changes. */
        fun postalCode(postalCode: String)

        /** Call when the user clicks the save icon. */
        fun saveCardClicked()

        fun cardFocus(hasFocus: Boolean)
    }

    interface Outputs {
        /** Emits when the card focus view should be enabled. */
        fun cardFocusIsEnabled(): Observable<Boolean>

        /** Emits when the password update was unsuccessful. */
        fun error(): Observable<String>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the save button should be enabled. */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the card was saved successfully. */
        fun success(): Observable<String>

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<NewCardFragment>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<Card?>()
        private val cardFocus = PublishSubject.create<Boolean>()
        private val name = PublishSubject.create<String>()
        private val postalCode = PublishSubject.create<String>()
        private val saveCardClicked = PublishSubject.create<Void>()

        private val cardFocusIsEnabled = BehaviorSubject.create<Boolean>()
        private val error = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()

        val inputs: NewCardFragmentViewModel.Inputs = this
        val outputs: NewCardFragmentViewModel.Outputs = this

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

            this.cardFocus
                    .subscribe { this.cardFocusIsEnabled.onNext(it) }

        }

        override fun card(card: Card?) {
            this.card.onNext(card)
        }

        override fun cardFocus(hasFocus: Boolean) {
            this.cardFocus.onNext(hasFocus)
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

        override fun cardFocusIsEnabled(): Observable<Boolean> {
            return this.cardFocusIsEnabled
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
                return card != null && card.validateNumber() && card.validateExpiryDate() && card.validateCVC()
            }

            private fun isNotEmpty(s: String): Boolean {
                return !s.isEmpty()
            }

            private fun isNotEmptyAndTwoWords(s: String): Boolean {
                //todo, this will pass "izzy "
                return isNotEmpty(s) && s.split(" ").size > 1
            }
        }
    }
}