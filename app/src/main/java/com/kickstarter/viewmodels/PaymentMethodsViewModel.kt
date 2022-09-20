package com.kickstarter.viewmodels

import DeletePaymentSourceMutation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
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

    /** Call when the user has introduced a  new paymentOption via PaymentSheet */
    fun savePaymentOption()
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
    fun success(): Observable<String>

    /** Emits after calling CreateSetupIntent mutation with the SetupClientId. */
    fun presentPaymentSheet(): Observable<String>

    /** Emits in case something went wrong with CreateSetupIntent mutation  */
    fun showError(): Observable<String>
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
    private val success = BehaviorSubject.create<String>()
    private val presentPaymentSheet = PublishSubject.create<String>()
    private val showError = PublishSubject.create<String>()

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
                    it.paymentSourceDelete()?.clientMutationId() ?: ""
                }
                .subscribe {
                    this.refreshCards.onNext(Unit)
                    this.success.onNext(it)
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
                    intentClientSecret = it
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
                }
        )

        compositeDisposable.add(
            savedPaymentOption
                .compose(errorsV2())
                .subscribe {
                    this.showError.onNext(it?.localizedMessage ?: "")
                }
        )
    }

    override fun onCleared() {
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

    // - Outputs
    override fun cards(): Observable<List<StoredCard>> = this.cards

    override fun dividerIsVisible(): Observable<Boolean> = this.dividerIsVisible

    override fun error(): Observable<String> = this.error

    override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible.distinctUntilChanged()

    override fun showDeleteCardDialog(): Observable<Unit> = this.showDeleteCardDialog

    override fun success(): Observable<String> = this.success

    @Override
    override fun presentPaymentSheet(): Observable<String> =
        this.presentPaymentSheet

    @Override
    override fun showError(): Observable<String> =
        this.showError

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PaymentMethodsViewModel(environment) as T
        }
    }
}
