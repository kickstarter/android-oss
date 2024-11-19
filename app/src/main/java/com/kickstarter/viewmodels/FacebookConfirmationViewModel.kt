package com.kickstarter.viewmodels

import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.FacebookUser
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.usecases.LoginUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface FacebookConfirmationViewModel {
    interface Inputs {
        /** Call when the create new account button has been clicked.  */
        fun createNewAccountClick()

        /** Call when the send newsletter switch has been toggled.  */
        fun sendNewslettersClick(b: Boolean)
    }

    interface Outputs {
        /** Fill the view's email address.  */
        fun prefillEmail(): Observable<String>

        /** Emits a string to display when sign up fails.  */
        fun signupError(): Observable<String>

        /** Finish Facebook confirmation activity with OK result.  */
        fun signupSuccess(): Observable<Unit>

        /** Emits a boolean to check send newsletter switch.  */
        fun sendNewslettersIsChecked(): Observable<Boolean>
    }

    class FacebookConfirmationViewModel(environment: Environment) :
        ViewModel(), Inputs, Outputs {
        private val client = requireNotNull(environment.apiClientV2())
        private val loginUserCase = LoginUseCase(environment)
        private val currentConfig = requireNotNull(environment.currentConfigV2())

        private fun registerWithFacebookSuccess(envelope: AccessTokenEnvelope) {
            loginUserCase.setToken(envelope.accessToken())
            loginUserCase.setUser(envelope.user())
            signupSuccess.onNext(Unit)
        }

        private val createNewAccountClick: PublishSubject<Unit> = PublishSubject.create()
        private val sendNewslettersClick: PublishSubject<Boolean> = PublishSubject.create()

        private val prefillEmail: BehaviorSubject<String> = BehaviorSubject.create()
        private val signupError: PublishSubject<String> = PublishSubject.create()
        private val signupSuccess: PublishSubject<Unit> = PublishSubject.create()
        private val sendNewslettersIsChecked: BehaviorSubject<Boolean> = BehaviorSubject.create()
        private val intent = PublishSubject.create<Intent>()
        private val disposables = CompositeDisposable()

        @JvmField
        val inputs: Inputs = this
        @JvmField
        val outputs: Outputs = this

        init {
            val intentObservable = intent.share()

            val facebookAccessToken = intentObservable
                .map { i: Intent -> i.getStringExtra(IntentKey.FACEBOOK_TOKEN) }
                .ofType(String::class.java)

            val tokenAndNewsletter = facebookAccessToken
                .compose(Transformers.combineLatestPair(this.sendNewslettersIsChecked))

            intentObservable
                .filter { it.getParcelableExtra<Parcelable>(IntentKey.FACEBOOK_USER).isNotNull() }
                .map { it.getParcelableExtra<Parcelable>(IntentKey.FACEBOOK_USER) }
                .filter { it is FacebookUser }
                .map { it as FacebookUser }
                .map { it.email() }
                .subscribe { prefillEmail.onNext(it) }
                .addToDisposable(disposables)

            val createNewAccountNotification = tokenAndNewsletter
                .compose(Transformers.takeWhenV2(this.createNewAccountClick))
                .flatMap {
                    client.registerWithFacebook(
                        it.first, it.second
                    )
                }
                .materialize()

            createNewAccountNotification
                .compose(Transformers.errorsV2())
                .map { ErrorEnvelope.fromThrowable(it) }
                .map { it.errorMessage() }
                .takeUntil(this.signupSuccess)
                .subscribe { this.signupError.onNext(it) }
                .addToDisposable(disposables)

            createNewAccountNotification
                .compose(Transformers.valuesV2())
                .ofType(AccessTokenEnvelope::class.java)
                .subscribe { envelope: AccessTokenEnvelope ->
                    this.registerWithFacebookSuccess(
                        envelope
                    )
                }
                .addToDisposable(disposables)

            sendNewslettersClick
                .subscribe { v: Boolean -> sendNewslettersIsChecked.onNext(v) }
                .addToDisposable(disposables)

            currentConfig.observable()
                .take(1)
                .map { false }
                .subscribe { v: Boolean -> sendNewslettersIsChecked.onNext(v) }
                .addToDisposable(disposables)
        }

        override fun createNewAccountClick() {
            createNewAccountClick.onNext(Unit)
        }

        override fun sendNewslettersClick(b: Boolean) {
            sendNewslettersClick.onNext(b)
        }

        override fun prefillEmail(): Observable<String> {
            return this.prefillEmail
        }

        override fun signupError(): Observable<String> {
            return this.signupError
        }

        override fun signupSuccess(): Observable<Unit> {
            return this.signupSuccess
        }

        override fun sendNewslettersIsChecked(): Observable<Boolean> {
            return this.sendNewslettersIsChecked
        }

        fun provideIntent(intent: Intent) {
            this.intent.onNext(intent)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FacebookConfirmationViewModel(environment) as T
        }
    }
}
