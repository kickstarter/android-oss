package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.NotificationsActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface NotificationsViewModel {
    interface Inputs {

        /** Call when the notify mobile of pledge activity toggle changes.  */
        fun notifyMobileOfBackings(checked: Boolean)

        /** Call when the notify mobile of new comments toggle changes.  */
        fun notifyMobileOfComments(checked: Boolean)

        /** Call when the notify mobile of new comments toggle changes.  */
        fun notifyMobileOfCreatorEdu(checked: Boolean)

        /** Call when the notify mobile of new followers toggle changes.  */
        fun notifyMobileOfFollower(checked: Boolean)

        /** Call when the notify mobile of friend backs a project toggle changes.  */
        fun notifyMobileOfFriendActivity(checked: Boolean)

        /** Call when the notify mobile of messages toggle changes.  */
        fun notifyMobileOfMessages(checked: Boolean)

        /** Call when the notify mobile of project updates toggle changes.  */
        fun notifyMobileOfPostLikes(checked: Boolean)

        /** Call when the notify mobile of project updates toggle changes.  */
        fun notifyMobileOfUpdates(checked: Boolean)

        /** Call when the notify of pledge activity toggle changes.  */
        fun notifyOfBackings(checked: Boolean)

        /** Call when the notify of new comments toggle changes.  */
        fun notifyOfComments(checked: Boolean)

        /** Call when the email frequency spinner selection changes.  */
        fun notifyOfCreatorDigest(checked: Boolean)

        /** Call when the notify of creator tips toggle changes.  */
        fun notifyOfCreatorEdu(checked: Boolean)

        /** Call when the notify of new followers toggle changes.  */
        fun notifyOfFollower(checked: Boolean)

        /** Call when the notify of friend backs a project toggle changes.  */
        fun notifyOfFriendActivity(checked: Boolean)

        /** Call when the notify of messages toggle changes.  */
        fun notifyOfMessages(checked: Boolean)

        /** Call when the notify of post likes toggle changes.  */
        fun notifyOfPostLikes(checked: Boolean)

        /** Call when the notify of project updates toggle changes.  */
        fun notifyOfUpdates(checked: Boolean)
    }

    interface Outputs {

        /** Emission determines whether we should be hiding backings frequency emails settings.  */
        fun creatorDigestFrequencyIsGone(): Observable<Boolean>

        /** Emission determines whether we should be hiding creator notification settings.   */
        fun creatorNotificationsAreGone(): Observable<Boolean>

        /** Emits user containing settings state.  */
        fun user(): Observable<User>
    }

    interface Errors {
        fun unableToSavePreferenceError(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<NotificationsActivity>(environment), Inputs, Outputs, Errors {
        private val userInput = PublishSubject.create<User>()

        private val creatorDigestFrequencyIsGone : Observable<Boolean>
        private val creatorNotificationsAreGone : Observable<Boolean>
        private val userOutput = BehaviorSubject.create<User>()
        private val updateSuccess = PublishSubject.create<Void>()

        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        private val client: ApiClientType = environment.apiClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        init {

            this.client.fetchCurrentUser()
                    .retry(2)
                    .compose(Transformers.neverError())
                    .compose(bindToLifecycle())
                    .subscribe { this.currentUser.refresh(it) }

            val currentUser = this.currentUser.observable()

            currentUser
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe({ this.userOutput.onNext(it) })

            this.creatorDigestFrequencyIsGone = currentUser
                    .compose(bindToLifecycle())
                    .map { it.notifyOfBackings() != true  }
                    .distinctUntilChanged()

            this.creatorNotificationsAreGone = currentUser
                    .compose(bindToLifecycle())
                    .map { IntegerUtils.isZero(it.createdProjectsCount()?: 0) }
                    .distinctUntilChanged()

            this.userInput
                    .concatMap<User>({ this.updateSettings(it) })
                    .compose(bindToLifecycle())
                    .subscribe({ this.success(it) })

            this.userInput
                    .compose(bindToLifecycle())
                    .subscribe(this.userOutput)

            this.userOutput
                    .window(2, 1)
                    .flatMap<List<User>>({ it.toList() })
                    .map<User>({ ListUtils.first(it) })
                    .compose<User>(takeWhen<User, Throwable>(this.unableToSavePreferenceError))
                    .compose(bindToLifecycle())
                    .subscribe(this.userOutput)
        }

        override fun notifyMobileOfBackings(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfBackings(checked).build())
        }

        override fun notifyMobileOfComments(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfComments(checked).build())
        }

        override fun notifyMobileOfCreatorEdu(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfCreatorEdu(checked).build())
        }

        override fun notifyMobileOfFollower(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfFollower(checked).build())
        }

        override fun notifyMobileOfFriendActivity(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfFriendActivity(checked).build())
        }

        override fun notifyMobileOfMessages(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfMessages(checked).build())
        }

        override fun notifyMobileOfPostLikes(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfPostLikes(checked).build())
        }

        override fun notifyMobileOfUpdates(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfUpdates(checked).build())
        }

        override fun notifyOfBackings(checked: Boolean) {
            val userBuilder = this.userOutput.value.toBuilder().notifyOfBackings(checked)
            if (!checked) {
                userBuilder.notifyOfCreatorDigest(false)
            }
            this.userInput.onNext(userBuilder.build())
        }

        override fun notifyOfComments(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfComments(checked).build())
        }

        override fun notifyOfCreatorDigest(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfCreatorDigest(checked).build())
        }

        override fun notifyOfCreatorEdu(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfCreatorEdu(checked).build())
        }

        override fun notifyOfFollower(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfFollower(checked).build())
        }

        override fun notifyOfFriendActivity(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfFriendActivity(checked).build())
        }

        override fun notifyOfMessages(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfMessages(checked).build())
        }

        override fun notifyOfPostLikes(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfPostLikes(checked).build())
        }

        override fun notifyOfUpdates(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfUpdates(checked).build())
        }

        override fun creatorDigestFrequencyIsGone() = this.creatorDigestFrequencyIsGone

        override fun creatorNotificationsAreGone() = this.creatorNotificationsAreGone

        override fun user(): Observable<User> = this.userOutput

        override fun unableToSavePreferenceError(): Observable<String> =  this.unableToSavePreferenceError
                    .takeUntil(this.updateSuccess)
                    .map { _ -> null }

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(null)
        }

        private fun updateSettings(user: User): Observable<User> {
            return this.client.updateUserSettings(user)
                    .compose(Transformers.pipeErrorsTo(this.unableToSavePreferenceError))
        }
    }
}
