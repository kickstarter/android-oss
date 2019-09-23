package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.fragments.NewCardFragment
import com.stripe.android.ApiResultCallback
import com.stripe.android.CardUtils
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import type.PaymentTypes

interface NewCardFragmentViewModel {
    interface Inputs {
        /** Call when the card validity changes. */
        fun card(card: Card?)

        /** Call when the card number text changes. */
        fun cardNumber(cardNumber: String)

        /** Call when the name field changes. */
        fun name(name: String)

        /** Call when the postal code field changes. */
        fun postalCode(postalCode: String)

        /** Call when the reusable switch is toggled. */
        fun reusable(reusable: Boolean)

        /** Call when the user clicks the save icon. */
        fun saveCardClicked()

        /** Call when the card input has focus. */
        fun cardFocus(hasFocus: Boolean)
    }

    interface Outputs {
        /** Emits a string resource and project to display warning. */
        fun allowedCardWarning(): Observable<Pair<Int?, Project?>>

        /** Emits a boolean determining if the allowed card warning should be visible. */
        fun allowedCardWarningIsVisible(): Observable<Boolean>

        /** Emits a boolean determining if the AppBarLayout should have elevation. */
        fun appBarLayoutHasElevation(): Observable<Boolean>

        /** Emits a drawable to be shown based on when the card widget has focus. */
        fun cardWidgetFocusDrawable(): Observable<Int>

        /** Emits a boolean determining if the form divider should be visible. */
        fun dividerIsVisible(): Observable<Boolean>

        /** Emits when saving the card was unsuccessful and the fragment is not modal. */
        fun error(): Observable<Void>

        /** Emits when saving the card was unsuccessful and the fragment is modal. */
        fun modalError(): Observable<Void>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits a boolean determining if the reusable switch should be visible. */
        fun reusableContainerIsVisible(): Observable<Boolean>

        /** Emits a boolean determining if the save button should be enabled. */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the card was saved successfully. */
        fun success(): Observable<StoredCard>

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<NewCardFragment>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<Card?>()
        private val cardFocus = PublishSubject.create<Boolean>()
        private val cardNumber = PublishSubject.create<String>()
        private val name = PublishSubject.create<String>()
        private val postalCode = PublishSubject.create<String>()
        private val reusable = PublishSubject.create<Boolean>()
        private val saveCardClicked = PublishSubject.create<Void>()

        private val allowedCardWarning = BehaviorSubject.create<Pair<Int?, Project?>>()
        private val allowedCardWarningIsVisible = BehaviorSubject.create<Boolean>()
        private val appBarLayoutHasElevation = BehaviorSubject.create<Boolean>()
        private val cardWidgetFocusDrawable = BehaviorSubject.create<Int>()
        private val dividerIsVisible = BehaviorSubject.create<Boolean>()
        private val error = BehaviorSubject.create<Void>()
        private val modalError = BehaviorSubject.create<Void>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val reusableContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<StoredCard>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient = this.environment.apolloClient()
        private val stripe = this.environment.stripe()

        init {
            val modal = arguments()
                    .map { it?.getBoolean(ArgumentsKey.NEW_CARD_MODAL)?: false }
                    .distinctUntilChanged()

            val project = arguments()
                    .map<Project?> { it?.getParcelable(ArgumentsKey.NEW_CARD_PROJECT)?: null }
                    .distinctUntilChanged()

            modal
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.appBarLayoutHasElevation)

            modal
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.dividerIsVisible)

            modal
                    .compose(bindToLifecycle())
                    .subscribe(this.reusableContainerIsVisible)

            val initialReusable = modal
                    .map { BooleanUtils.negate(it) }

            val reusable = Observable.merge(initialReusable, this.reusable)

            val cardForm = Observable.combineLatest(this.name,
                    this.card,
                    this.cardNumber,
                    this.postalCode,
                    reusable)
            { name, card, cardNumber, postalCode, reusable -> CardForm(name, card, cardNumber, postalCode, reusable) }

            cardForm
                    .compose<Pair<CardForm, Project?>>(combineLatestPair(project))
                    .map { it.first.isValid(it.second) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.saveButtonIsEnabled)

            val warning = this.cardNumber
                    .compose<Pair<String, Project?>>(combineLatestPair(project))
                    .map<Pair<Int?, Project?>> { Pair(CardForm.warning(it.first, it.second), it.second) }
                    .distinctUntilChanged()

            warning
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.allowedCardWarning)

            warning
                    .map { ObjectUtils.isNotNull(it.first) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.allowedCardWarningIsVisible)

            this.allowedCardWarningIsVisible
                    .startWith(false)
                    .distinctUntilChanged()
                    .compose<Pair<Boolean, Boolean>>(combineLatestPair(this.cardFocus.startWith(false).distinctUntilChanged()))
                    .map {
                        val cardNotAllowed = it.first
                        val hasFocus = it.second
                        when {
                            cardNotAllowed -> R.drawable.divider_red_400_horizontal
                            hasFocus -> R.drawable.divider_green_horizontal
                            else -> R.drawable.divider_dark_grey_500_horizontal
                        }
                    }
                    .distinctUntilChanged()
                    .subscribe { this.cardWidgetFocusDrawable.onNext(it) }

            val saveCardNotification = cardForm
                    .map {
                        it.card?.let { card ->
                            card.toBuilder()
                                    .name(it.name)
                                    .addressZip(it.postalCode)
                                    .build()
                        }
                    }
                    .compose<Pair<Card, Boolean>>(combineLatestPair(reusable))
                    .compose<Pair<Card, Boolean>>(takeWhen(this.saveCardClicked))
                    .switchMap { createTokenAndSaveCard(it).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            saveCardNotification
                    .compose(values())
                    .subscribe {
                        this.success.onNext(null)
                        this.koala.trackSavedPaymentMethod()
                    }

            val error = saveCardNotification
                    .compose(errors())
                    .compose(ignoreValues())
                    .compose<Pair<Void, Boolean>>(combineLatestPair(modal))

            error
                    .filter { !it.second }
                    .map { it.first }
                    .subscribe(this.error)

            error
                    .filter { it.second }
                    .map { it.first }
                    .subscribe(this.modalError)

            saveCardNotification
                    .compose(errors())
                    .subscribe { this.koala.trackFailedPaymentMethodCreation() }

            this.koala.trackViewedAddNewCard()
        }

        override fun card(card: Card?) {
            this.card.onNext(card)
        }

        override fun cardFocus(hasFocus: Boolean) {
            this.cardFocus.onNext(hasFocus)
        }

        override fun cardNumber(cardNumber: String) {
            this.cardNumber.onNext(cardNumber)
        }

        override fun name(name: String) {
            this.name.onNext(name)
        }

        override fun postalCode(postalCode: String) {
            this.postalCode.onNext(postalCode)
        }

        override fun reusable(reusable: Boolean) {
            this.reusable.onNext(reusable)
        }

        override fun saveCardClicked() {
            this.saveCardClicked.onNext(null)
        }

        override fun allowedCardWarning(): Observable<Pair<Int?, Project?>> = this.allowedCardWarning

        override fun allowedCardWarningIsVisible(): Observable<Boolean> = this.allowedCardWarningIsVisible

        override fun appBarLayoutHasElevation(): Observable<Boolean> = this.appBarLayoutHasElevation

        override fun cardWidgetFocusDrawable(): Observable<Int> = this.cardWidgetFocusDrawable

        override fun dividerIsVisible(): Observable<Boolean> = this.dividerIsVisible

        override fun error(): Observable<Void> = this.error

        override fun modalError(): Observable<Void> = this.modalError

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun reusableContainerIsVisible(): Observable<Boolean> = this.reusableContainerIsVisible

        override fun saveButtonIsEnabled(): Observable<Boolean> = this.saveButtonIsEnabled

        override fun success(): Observable<StoredCard> = this.success

        data class CardForm(val name: String, val card: Card?, val cardNumber: String, val postalCode: String, val reusable: Boolean) {

            fun isValid(project: Project?): Boolean {
                return this.name.isNotEmpty()
                        && this.postalCode.isNotEmpty()
                        && isValidCard(project)
            }

            private fun isValidCard(project: Project?): Boolean {
                return this.card != null && warning(this.cardNumber, project) == null && this.card.validateNumber() && this.card.validateExpiryDate() && card.validateCVC()
            }

            companion object {
                fun warning(cardNumber: String, project: Project?): Int? {
                    return if (cardNumber.length < 3)
                        null
                    else {
                        when (project) {
                            null -> when {
                                CardUtils.getPossibleCardType(cardNumber) !in allowedCardTypes -> R.string.Unsupported_card_type
                                else -> null
                            }
                            else -> when {
                                CardUtils.getPossibleCardType(cardNumber) !in getAllowedTypes(project) -> R.string.You_cant_use_this_credit_card_to_back_a_project_from_project_country
                                else -> null
                            }
                        }
                    }
                }

                private fun getAllowedTypes(project: Project): Array<String> {
                    return when {
                        project.currency() == Country.US.currencyCode -> usdCardTypes
                        else -> nonUsdCardTypes
                    }
                }

                private val allowedCardTypes = arrayOf(Card.CardBrand.AMERICAN_EXPRESS,
                        Card.CardBrand.DINERS_CLUB,
                        Card.CardBrand.DISCOVER,
                        Card.CardBrand.JCB,
                        Card.CardBrand.MASTERCARD,
                        Card.CardBrand.UNIONPAY,
                        Card.CardBrand.VISA)

                private val usdCardTypes = allowedCardTypes
                private val nonUsdCardTypes = arrayOf(Card.CardBrand.AMERICAN_EXPRESS,
                        Card.CardBrand.MASTERCARD,
                        Card.CardBrand.VISA)
            }
        }

        private fun createTokenAndSaveCard(cardAndReusable: Pair<Card, Boolean>): Observable<StoredCard> {
            return Observable.defer {
                val ps = PublishSubject.create<StoredCard>()
                this.stripe.createToken(cardAndReusable.first, object : ApiResultCallback<Token> {
                    override fun onSuccess(token: Token) {
                        saveCard(token, cardAndReusable.second, ps)
                    }

                    override fun onError(e: Exception) {
                        ps.onError(e)
                    }
                })
                return@defer ps
            }
                    .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

        private fun saveCard(token: Token, reusable:Boolean, ps: PublishSubject<StoredCard>) {
            token.card?.id?.apply {
                this@ViewModel.apolloClient.savePaymentMethod(PaymentTypes.CREDIT_CARD, token.id, this, reusable)
                        .subscribe({
                            ps.onCompleted()
                            this@ViewModel.success.onNext(it)
                            this@ViewModel.koala.trackSavedPaymentMethod()
                        }, { ps.onError(it) })
            }?: run {
                ps.onError(IllegalStateException("Card has no id"))
            }
        }
    }
}
