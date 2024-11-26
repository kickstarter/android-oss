package com.kickstarter.viewmodels

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.DeletePaymentSourceMutation
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.models.StoredCard
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.ui.viewholders.PaymentMethodsViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface Inputs {

    /** Call when the user hits add new card button. */
    fun newCardButtonClicked()

    /** Call when the user confirms they want to delete card. */
    fun confirmDeleteCardClicked()

    /** Call when the user clicks the delete icon. */
    fun deleteCardClicked(paymentSourceId: String)

    /** Call when a card has been added or removed and the list needs to be updated. */
    fun refreshCards()

    /** Call when the user has introduced a new paymentOption via PaymentSheet */
    fun savePaymentOption()

    /** Loading state taking place between PaymentSheet confirmation and PaymentSheetResult */
    fun confirmedLoading(isLoading: Boolean)
}

interface Outputs {
    /** Emits a list of stored cards for a user. */
    fun cards(): Observable<List<StoredCard>>

    /** Emits when the divider should be visible (if there are cards). */
    fun dividerIsVisible(): Observable<Boolean>

    /** Emits whenever there is an error deleting a stored card.  */
    fun error(): Observable<String>

    /** Emits when the progress bar should be visible (during a network call). */
    fun progressBarIsVisible(): Observable<Boolean>

    /** Emits whenever the user tries to delete a card.  */
    fun showDeleteCardDialog(): Observable<Unit>

    /** Emits when the card was successfully deleted. */
    fun successDeleting(): Observable<String>

    /** Emits after calling CreateSetupIntent mutation with the SetupClientId. */
    fun presentPaymentSheet(): Observable<Pair<String, String>>

    /** Emits in case something went wrong with CreateSetupIntent mutation  */
    fun showError(): Observable<String>

    /** Emits in case SavePaymentMethod returns success output  */
    fun successSaving(): Observable<String>
}

class PaymentMethodsViewModel(environment: Environment) : ViewModel(), PaymentMethodsAdapter.Delegate, Inputs, Outputs {

    private val confirmDeleteCardClicked = PublishSubject.create<Unit>()
    private val deleteCardClicked = PublishSubject.create<String>()
    private val refreshCards = PublishSubject.create<Unit>()
    private val newCardButtonPressed = PublishSubject.create<Unit>()
    private val savePaymentOption = PublishSubject.create<Unit>()

    private val cards = BehaviorSubject.create<List<StoredCard>>()
    private val dividerIsVisible = BehaviorSubject.create<Boolean>()
    private val error = BehaviorSubject.create<String>()
    private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
    private val showDeleteCardDialog = BehaviorSubject.create<Unit>()
    private val successDeleting = BehaviorSubject.create<String>()
    private val successSaving = BehaviorSubject.create<String>()
    private val presentPaymentSheet = PublishSubject.create<Pair<String, String>>()
    private val showError = PublishSubject.create<String>()
    private val loadingConfirmed = PublishSubject.create<Boolean>()

    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val compositeDisposable = CompositeDisposable()

    val inputs: Inputs = this
    val outputs: Outputs = this

    init {

        compositeDisposable.add(
            getListOfStoredCards()
                .subscribe { this.cards.onNext(it) }
        )

        compositeDisposable.add(
            this.cards
                .map { it.isNotEmpty() }
                .subscribe { this.dividerIsVisible.onNext(it) }
        )

        compositeDisposable.add(
            this.deleteCardClicked
                .subscribe {
                    this.showDeleteCardDialog.onNext(Unit)
                }
        )

        val deleteCardNotification = this.deleteCardClicked
            .compose<String>(takeWhenV2(this.confirmDeleteCardClicked))
            .switchMap { deletePaymentSource(it).materialize() }
            .share()

        compositeDisposable.add(
            deleteCardNotification
                .compose(valuesV2())
                .map {
                    it.paymentSourceDelete?.clientMutationId ?: ""
                }
                .subscribe {
                    this.refreshCards.onNext(Unit)
                    this.successDeleting.onNext(it)
                }
        )

        compositeDisposable.add(
            deleteCardNotification
                .compose(errorsV2())
                .subscribe {
                    this.error.onNext(it?.localizedMessage ?: "")
                }
        )

        compositeDisposable.add(
            this.refreshCards
                .switchMap { getListOfStoredCards() }
                .subscribe { this.cards.onNext(it) }
        )

        val shouldPresentPaymentSheet = this.newCardButtonPressed
            .switchMap {
                createSetupIntent()
            }

        compositeDisposable.add(
            shouldPresentPaymentSheet
                .compose(valuesV2())
                .compose(combineLatestPair(userEmail()))
                .subscribe {
                    this.presentPaymentSheet.onNext(it)
                }
        )

        compositeDisposable.add(
            shouldPresentPaymentSheet
                .compose(errorsV2())
                .subscribe {
                    this.showError.onNext(it?.localizedMessage ?: "")
                }
        )

        val savedPaymentOption = this.savePaymentOption
            .withLatestFrom(this.presentPaymentSheet) { _, setupClientId ->
                setupClientId
            }
            .map {
                SavePaymentMethodData(
                    reusable = true,
                    intentClientSecret = it.first

                )
            }
            .switchMap {
                savePaymentMethod(it)
            }

        compositeDisposable.add(
            savedPaymentOption
                .compose(valuesV2())
                .subscribe {
                    this.refreshCards.onNext(Unit)
                    this.successSaving.onNext(it.toString())
                }
        )

        compositeDisposable.add(
            savedPaymentOption
                .compose(errorsV2())
                .subscribe {
                    this.showError.onNext(it?.localizedMessage ?: "")
                }
        )

        compositeDisposable.add(
            this.loadingConfirmed
                .subscribe {
                    this.progressBarIsVisible.onNext(it)
                }
        )
    }

    override fun onCleared() {
        apolloClient.cleanDisposables()
        compositeDisposable.clear()
        super.onCleared()
    }

    private fun createSetupIntent() =
        this.apolloClient.createSetupIntent()
            .doOnSubscribe {
                this.progressBarIsVisible.onNext(true)
            }
            .doAfterTerminate {
                this.progressBarIsVisible.onNext(false)
            }
            .materialize()
            .share()

    private fun savePaymentMethod(it: SavePaymentMethodData) =
        this.apolloClient.savePaymentMethod(it)
            .doOnSubscribe {
                this.progressBarIsVisible.onNext(true)
            }
            .doAfterTerminate {
                this.progressBarIsVisible.onNext(false)
            }
            .materialize()
            .share()

    private fun getListOfStoredCards(): Observable<List<StoredCard>> {
        return this.apolloClient.getStoredCards()
            .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
            .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
            .compose(neverErrorV2())
    }

    private fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return this.apolloClient.deletePaymentSource(paymentSourceId)
            .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
            .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
    }

    private fun userEmail(): Observable<String> {
        return this.apolloClient.userPrivacy()
            .compose(neverErrorV2())
            .map { it.email }
    }

    // - Inputs
    override fun newCardButtonClicked() = this.newCardButtonPressed.onNext(Unit)

    override fun savePaymentOption() = this.savePaymentOption.onNext(Unit)

    override fun deleteCardButtonClicked(paymentMethodsViewHolder: PaymentMethodsViewHolder, paymentSourceId: String) {
        deleteCardClicked(paymentSourceId)
    }

    @Override
    override fun confirmDeleteCardClicked() =
        this.confirmDeleteCardClicked.onNext(Unit)

    override fun deleteCardClicked(paymentSourceId: String) = this.deleteCardClicked.onNext(paymentSourceId)

    override fun refreshCards() = this.refreshCards.onNext(Unit)

    override fun confirmedLoading(isLoading: Boolean) = this.loadingConfirmed.onNext(isLoading)

    // - Outputs
    override fun cards(): Observable<List<StoredCard>> = this.cards

    override fun dividerIsVisible(): Observable<Boolean> = this.dividerIsVisible

    override fun error(): Observable<String> = this.error

    override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible.distinctUntilChanged()

    override fun showDeleteCardDialog(): Observable<Unit> = this.showDeleteCardDialog

    override fun successDeleting(): Observable<String> = this.successDeleting

    @Override
    override fun presentPaymentSheet(): Observable<Pair<String, String>> =
        this.presentPaymentSheet

    @Override
    override fun showError(): Observable<String> =
        this.showError

    @Override
    override fun successSaving(): Observable<String> =
        this.successSaving

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PaymentMethodsViewModel(environment) as T
        }
    }
}
