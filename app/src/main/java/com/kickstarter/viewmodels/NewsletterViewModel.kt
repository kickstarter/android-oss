package com.kickstarter.viewmodels

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isLocationGermany
import com.kickstarter.ui.activities.Newsletter
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface NewsletterViewModel {

    interface Inputs {
        /** Call when the user toggles the subscribe all switch.  */
        fun sendAllNewsletter(checked: Boolean)

        /** Call when the user toggles the alumni switch. */
        fun sendAlumniNewsletter(checked: Boolean)

        /** Call when the user toggles the arts & news switch. */
        fun sendArtsNewsNewsletter(checked: Boolean)

        /** Call when the user toggles the films switch. */
        fun sendFilmsNewsletter(checked: Boolean)

        /** Call when the user toggles the games switch. */
        fun sendGamesNewsletter(checked: Boolean)

        /** Call when the user toggles the Happening newsletter switch.  */
        fun sendHappeningNewsletter(checked: Boolean)

        /** Call when the user toggles the invent switch */
        fun sendInventNewsletter(checked: Boolean)

        /** Call when the user toggles the music switch */
        fun sendMusicNewsletter(checked: Boolean)

        /** Call when the user toggles the Kickstarter News & Events newsletter switch.  */
        fun sendPromoNewsletter(checked: Boolean)

        /** Call when the user toggles the reads switch. */
        fun sendReadsNewsletter(checked: Boolean)

        /** Call when the user toggles the Projects We Love newsletter switch.  */
        fun sendWeeklyNewsletter(checked: Boolean)
    }

    interface Outputs {
        /** Show a dialog to inform the user that their newsletter subscription must be confirmed via email.  */
        fun showOptInPrompt(): Observable<Newsletter>

        /** Emits a Boolean notifying the subscribe all switch that all newsletters are checked */
        fun subscribeAll(): Observable<Boolean>

        /** Emits user containing settings state. */
        fun user(): Observable<User>
    }

    interface Errors {
        /** Emits when there is an error updating the user preferences. */
        fun unableToSavePreferenceError(): Observable<String>
    }

    class NewsletterViewModel(val environment: Environment) : ViewModel(), Inputs, Errors, Outputs {

        private val client = requireNotNull(environment.apiClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())

        private val newsletterInput = PublishSubject.create<Pair<Boolean, Newsletter>>()
        private val userInput = PublishSubject.create<User>()
        private val updateSuccess = PublishSubject.create<Unit>()

        private val showOptInPrompt = BehaviorSubject.create<Newsletter>()
        private val subscribeAll = BehaviorSubject.create<Boolean>()
        private val userOutput = BehaviorSubject.create<User>()

        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()
        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        init {

            this.client.fetchCurrentUser()
                .retry(2)
                .compose(Transformers.neverErrorV2())
                .subscribe { this.currentUser.refresh(it) }
                .addToDisposable(disposables)

            val currentUser = this.currentUser.observable()
                .filter { it.getValue().isNotNull() }
                .map { requireNotNull(it.getValue()) }

            currentUser
                .take(1)
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            currentUser
                .compose<Pair<User, Pair<Boolean, Newsletter>>>(takePairWhenV2<User, Pair<Boolean, Newsletter>>(this.newsletterInput))
                .filter { us -> requiresDoubleOptIn(us.first, us.second.first) }
                .map { us -> us.second.second }
                .subscribe { this.showOptInPrompt.onNext(it) }
                .addToDisposable(disposables)

            currentUser
                .map { isSubscribedToAllNewsletters(it) }
                .subscribe { this.subscribeAll.onNext(it) }
                .addToDisposable(disposables)

            val updateUserNotification = this.userInput
                .distinctUntilChanged()
                .switchMap {
                    this.updateSettings(it)
                }
                .share()

            updateUserNotification
                .compose(valuesV2())
                .subscribe {
                    this.success(it)
                }
                .addToDisposable(disposables)

            updateUserNotification
                .compose(errorsV2())
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .subscribe { this.unableToSavePreferenceError.onNext(it) }
                .addToDisposable(disposables)

            this.userInput
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            this.userOutput
                .window(2, 1)
                .switchMap { it }
                .compose(takeWhenV2(this.unableToSavePreferenceError))
                .subscribe {
                    this.userOutput.onNext(it)
                }
                .addToDisposable(disposables)
        }

        override fun sendAllNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                val updatedUser = this.toBuilder()
                    .alumniNewsletter(checked)
                    .artsCultureNewsletter(checked)
                    .filmNewsletter(checked)
                    .gamesNewsletter(checked)
                    .happeningNewsletter(checked)
                    .inventNewsletter(checked)
                    .musicNewsletter(checked)
                    .promoNewsletter(checked)
                    .publishingNewsletter(checked)
                    .weeklyNewsletter(checked)
                    .build()

                userInput.onNext(updatedUser)
                newsletterInput.onNext(Pair(checked, Newsletter.ALL))
            }
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        override fun sendAlumniNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().alumniNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.ALUMNI))
        }

        override fun sendArtsNewsNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().artsCultureNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.ARTS))
        }

        override fun sendFilmsNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().filmNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.FILMS))
        }

        override fun sendGamesNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().gamesNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.GAMES))
        }

        override fun sendHappeningNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().happeningNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.HAPPENING))
        }

        override fun sendInventNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().inventNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.INVENT))
        }

        override fun sendMusicNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().musicNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.MUSIC))
        }

        override fun sendPromoNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().promoNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.PROMO))
        }

        override fun sendReadsNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().publishingNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.READS))
        }

        override fun sendWeeklyNewsletter(checked: Boolean) {
            this.userOutput.value?.apply {
                userInput.onNext(this.toBuilder().weeklyNewsletter(checked).build())
            }
            this.newsletterInput.onNext(Pair(checked, Newsletter.WEEKLY))
        }

        override fun showOptInPrompt(): Observable<Newsletter> = this.showOptInPrompt

        override fun subscribeAll(): Observable<Boolean> = this.subscribeAll

        override fun user(): Observable<User> = this.userOutput

        override fun unableToSavePreferenceError(): Observable<String> {
            return this.unableToSavePreferenceError
                .takeUntil(this.updateSuccess)
                .map { _ -> null }
        }

        private fun isSubscribedToAllNewsletters(user: User): Boolean {
            return user.alumniNewsletter().isTrue() && user.artsCultureNewsletter().isTrue() &&
                user.filmNewsletter().isTrue() && user.gamesNewsletter().isTrue() &&
                user.happeningNewsletter().isTrue() && user.inventNewsletter().isTrue() &&
                user.musicNewsletter().isTrue() && user.promoNewsletter().isTrue() &&
                user.publishingNewsletter().isTrue() && user.weeklyNewsletter().isTrue()
        }

        private fun requiresDoubleOptIn(user: User, checked: Boolean) = user.isLocationGermany() && checked

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(Unit)
        }

        private fun updateSettings(user: User): Observable<Notification<User>> {
            return this.client.updateUserSettings(user)
                .materialize()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NewsletterViewModel(environment) as T
        }
    }
}
