package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.UpdateUserCurrencyMutation
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.UserPrivacy
import com.kickstarter.type.CurrencyCode
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface AccountViewModel {

    interface Inputs {
        /** Notifies when the spinner has been selected. */
        fun onSelectedCurrency(currencyCode: CurrencyCode)
    }

    interface Outputs {
        /** Emits the current user's chosen Currency. */
        fun chosenCurrency(): Observable<String>

        /** Emits the current user's Email. */
        fun email(): Observable<String>

        /** Emits whenever there is an error updating the user's currency.  */
        fun error(): Observable<String>

        /** Emits when the password required container should be visible. */
        fun passwordRequiredContainerIsVisible(): Observable<Boolean>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits a boolean determining when we should show the email error icon. */
        fun showEmailErrorIcon(): Observable<Boolean>

        /** Emits when the currency update was successful. */
        fun success(): Observable<String>
    }

    class AccountViewModel(val environment: Environment, private val intent: Intent? = null) : ViewModel(), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val onSelectedCurrency = PublishSubject.create<CurrencyCode>()

        private val chosenCurrency = BehaviorSubject.create<String>()
        private val email = BehaviorSubject.create<String>()
        private val passwordRequiredContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val showEmailErrorIcon = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()

        private val error = BehaviorSubject.create<String>()

        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val disposables = CompositeDisposable()

        init {

            val userPrivacy = this.apolloClient.userPrivacy()
                .compose(Transformers.neverErrorV2())

            userPrivacy
                .map { it.chosenCurrency }
                .subscribe { this.chosenCurrency.onNext(it.toString()) }
                .addToDisposable(disposables)

            userPrivacy
                .map { it.email }
                .subscribe { this.email.onNext(it) }
                .addToDisposable(disposables)

            userPrivacy
                .map { it.hasPassword }
                .subscribe { this.passwordRequiredContainerIsVisible.onNext(it) }
                .addToDisposable(disposables)

            userPrivacy
                .map { showEmailErrorImage(it) ?: false }
                .subscribe { this.showEmailErrorIcon.onNext(it) }
                .addToDisposable(disposables)

            val updateCurrencyNotification = this.onSelectedCurrency
                .compose(combineLatestPair<CurrencyCode, String>(this.chosenCurrency))
                .filter { it.first.rawValue != it.second }
                .map<CurrencyCode> { it.first }
                .switchMap { updateUserCurrency(it).materialize() }
                .share()

            updateCurrencyNotification
                .compose(valuesV2())
                .map { it.updateUserProfile?.user?.chosenCurrency ?: "" }
                .filter { it.isNotNull() }
                .subscribe {
                    this.chosenCurrency.onNext(it)
                    this.success.onNext(it)
                }
                .addToDisposable(disposables)

            updateCurrencyNotification
                .compose(Transformers.errorsV2())
                .subscribe { this.error.onNext(it?.localizedMessage ?: "") }
                .addToDisposable(disposables)
        }

        override fun onSelectedCurrency(currencyCode: CurrencyCode) {
            this.onSelectedCurrency.onNext(currencyCode)
        }

        override fun chosenCurrency(): BehaviorSubject<String> = this.chosenCurrency

        override fun email(): Observable<String> = this.email

        override fun error(): Observable<String> = this.error

        override fun passwordRequiredContainerIsVisible(): Observable<Boolean> = this.passwordRequiredContainerIsVisible

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun showEmailErrorIcon(): Observable<Boolean> = this.showEmailErrorIcon

        override fun success(): BehaviorSubject<String> = this.success

        private fun showEmailErrorImage(userPrivacy: UserPrivacy): Boolean {
            val creator = userPrivacy.isCreator
            val deliverable = userPrivacy.isDeliverable
            val isEmailVerified = userPrivacy.isEmailVerified

            return if (!deliverable) {
                return true
            } else if (creator && !isEmailVerified) {
                return true
            } else {
                false
            }
        }

        private fun updateUserCurrency(currencyCode: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
            return this.apolloClient.updateUserCurrencyPreference(currencyCode)
                .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountViewModel(environment, intent) as T
        }
    }
}
