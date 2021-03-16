package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.BooleanUtils.isTrue
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isLocationGermany
import com.kickstarter.ui.activities.NewsletterActivity
import com.kickstarter.ui.data.Newsletter
import rx.Notification
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<NewsletterActivity>(environment), Inputs, Errors, Outputs {

        private val client = environment.apiClient()
        private val currentUser = environment.currentUser()

        private val newsletterInput = PublishSubject.create<Pair<Boolean, Newsletter>>()
        private val userInput = PublishSubject.create<User>()
        private val updateSuccess = PublishSubject.create<Void>()

        private val showOptInPrompt = BehaviorSubject.create<Newsletter>()
        private val subscribeAll = BehaviorSubject.create<Boolean>()
        private val userOutput = BehaviorSubject.create<User>()

        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        init {

            this.client.fetchCurrentUser()
                .retry(2)
                .compose(Transformers.neverError())
                .compose(bindToLifecycle())
                .subscribe(this.currentUser::refresh)

            val currentUser = this.currentUser.observable()
                .filter { ObjectUtils.isNotNull(it) }

            currentUser
                .take(1)
                .compose(bindToLifecycle())
                .subscribe(this.userOutput::onNext)

            currentUser
                .compose<Pair<User, Pair<Boolean, Newsletter>>>(takePairWhen<User, Pair<Boolean, Newsletter>>(this.newsletterInput))
                .filter { us -> requiresDoubleOptIn(us.first, us.second.first) }
                .map { us -> us.second.second }
                .compose(bindToLifecycle())
                .subscribe(this.showOptInPrompt)

            currentUser
                .map { isSubscribedToAllNewsletters(it) }
                .compose(bindToLifecycle())
                .subscribe(this.subscribeAll::onNext)

            val updateUserNotification = this.userInput
                .concatMap<Notification<User>> { this.updateSettings(it) }

            updateUserNotification
                .compose(values())
                .compose(bindToLifecycle())
                .subscribe { this.success(it) }

            updateUserNotification
                .compose(errors())
                .compose(bindToLifecycle())
                .subscribe(this.unableToSavePreferenceError)

            this.userInput
                .compose(bindToLifecycle())
                .subscribe(this.userOutput)

            this.userOutput
                .window(2, 1)
                .flatMap<List<User>> { it.toList() }
                .map<User> { ListUtils.first(it) }
                .compose<User>(takeWhen<User, Throwable>(this.unableToSavePreferenceError))
                .compose(bindToLifecycle())
                .subscribe(this.userOutput)
        }

        override fun sendAllNewsletter(checked: Boolean) {
            this.userInput.onNext(
                this.userOutput.value.toBuilder()
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
            )
            this.newsletterInput.onNext(Pair(checked, Newsletter.ALL))
        }

        override fun sendAlumniNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().alumniNewsletter(checked).build())
            this.newsletterInput.onNext(Pair(checked, Newsletter.ALUMNI))
        }

        override fun sendArtsNewsNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().artsCultureNewsletter(checked).build())
            this.newsletterInput.onNext(Pair(checked, Newsletter.ARTS))
        }

        override fun sendFilmsNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().filmNewsletter(checked).build())
            this.newsletterInput.onNext(Pair(checked, Newsletter.FILMS))
        }

        override fun sendGamesNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().gamesNewsletter(checked).build())
            this.newsletterInput.onNext(Pair(checked, Newsletter.GAMES))
        }

        override fun sendHappeningNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().happeningNewsletter(checked).build())
            this.newsletterInput.onNext(Pair(checked, Newsletter.HAPPENING))
        }

        override fun sendInventNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().inventNewsletter(checked).build())
            this.newsletterInput.onNext(Pair(checked, Newsletter.INVENT))
        }

        override fun sendMusicNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().musicNewsletter(checked).build())
            this.newsletterInput.onNext(Pair(checked, Newsletter.MUSIC))
        }

        override fun sendPromoNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().promoNewsletter(checked).build())
            this.newsletterInput.onNext(Pair(checked, Newsletter.PROMO))
        }

        override fun sendReadsNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().publishingNewsletter(checked).build())
            this.newsletterInput.onNext(Pair(checked, Newsletter.READS))
        }

        override fun sendWeeklyNewsletter(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().weeklyNewsletter(checked).build())
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

        private fun isSubscribedToAllNewsletters(@NonNull user: User): Boolean {
            return isTrue(user.alumniNewsletter()) && isTrue(user.artsCultureNewsletter()) &&
                isTrue(user.filmNewsletter()) && isTrue(user.gamesNewsletter()) &&
                isTrue(user.happeningNewsletter()) && isTrue(user.inventNewsletter()) &&
                isTrue(user.musicNewsletter()) && isTrue(user.promoNewsletter()) &&
                isTrue(user.publishingNewsletter()) && isTrue(user.weeklyNewsletter())
        }

        private fun requiresDoubleOptIn(user: User, checked: Boolean) = user.isLocationGermany() && checked

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(null)
        }

        private fun updateSettings(user: User): Observable<Notification<User>> {
            return this.client.updateUserSettings(user)
                .materialize()
                .share()
        }
    }
}
