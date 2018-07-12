package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhen
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import io.reactivex.rxkotlin.withLatestFrom
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.models.User
import com.kickstarter.ui.activities.NewsletterActivity
import com.kickstarter.ui.data.Newsletter
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.function.BiFunction

interface NewsletterViewModel {

    interface Inputs {
        /** Call when the user toggles the subscribe all switch.  */
        fun sendAllNewsletter(checked: Boolean)

        /** Call when the user toggles the Happening newsletter switch.  */
        fun sendHappeningNewsletter(checked: Boolean)

        /** Call when the user toggles the Kickstarter News & Events newsletter switch.  */
        fun sendPromoNewsletter(checked: Boolean)

        /** Call when the user toggles the Projects We Love newsletter switch.  */
        fun sendWeeklyNewsletter(checked: Boolean)

    }

    interface Outputs {
        /** Emits user containing settings state. */
        fun user(): Observable<User>
    }

    interface Errors {
        fun unableToSavePreferenceError(): Observable<String>
    }

    open class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<NewsletterActivity>(environment), Inputs, Errors, Outputs {
        private val client = environment.apiClient()
        private val currentUser = environment.currentUser()
        private val newsletterInput = PublishSubject.create<Pair<Boolean, Newsletter>>()
        private val userInput = PublishSubject.create<User>()
        private val updateSuccess = PublishSubject.create<Void>()
        private val userOutput = BehaviorSubject.create<User>()

        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        init {


            client.fetchCurrentUser()
                    .retry(2)
                    .compose(Transformers.neverError())
                    .compose(bindToLifecycle())
                    .subscribe(currentUser::refresh)

            currentUser.observable()
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe(userOutput::onNext)

            userInput
                    .concatMap { updateSettings(it) }
                    .compose(bindToLifecycle())
                    .subscribe { success(it) }

            userInput
                    .compose(bindToLifecycle())
                    .subscribe(userOutput)


            userOutput
                    .window(2, 1)
                    .flatMap<List<User>>({ it.toList() })
                    .map<User>({ ListUtils.first(it) })
                    .compose<User>(takeWhen<User, Throwable>(unableToSavePreferenceError))
                    .compose(bindToLifecycle())
                    .subscribe(userOutput)


            newsletterInput
                    .map { bs -> bs.first }
                    .compose(bindToLifecycle())
                    .subscribe(koala::trackNewsletterToggle)
        }

        private fun success(user: User) {
            currentUser.refresh(user)
            updateSuccess.onNext(null)
        }

        private fun updateSettings(user: User): Observable<User> {
            return client.updateUserSettings(user)
                    .compose(Transformers.pipeErrorsTo<User>(unableToSavePreferenceError))
        }


        override fun sendAllNewsletter(checked: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun sendHappeningNewsletter(checked: Boolean) {
            userInput.onNext(userOutput.value.toBuilder().happeningNewsletter(checked).build())
            newsletterInput.onNext(Pair(checked, Newsletter.HAPPENING))
        }

        override fun sendPromoNewsletter(checked: Boolean) {
            userInput.onNext(userOutput.value.toBuilder().promoNewsletter(checked).build());
            newsletterInput.onNext(Pair(checked, Newsletter.PROMO))
        }

        override fun sendWeeklyNewsletter(checked: Boolean) {
            userInput.onNext(userOutput.value.toBuilder().weeklyNewsletter(checked).build());
            newsletterInput.onNext(Pair(checked, Newsletter.WEEKLY))
        }

        override fun user() = userOutput

        override fun unableToSavePreferenceError() : Observable<String> {
           return unableToSavePreferenceError
                    .takeUntil(updateSuccess)
                    .map(null)
        }

    }
}
