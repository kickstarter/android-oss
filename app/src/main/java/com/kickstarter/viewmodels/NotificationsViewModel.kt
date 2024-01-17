package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isZero
import com.kickstarter.models.User
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

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

        /** Call when the notify mobile of marketing updates toggle changes.  */
        fun notifyMobileOfMarketingUpdate(checked: Boolean)

        /** Call when the notify of pledge activity toggle changes.  */
        fun notifyOfBackings(checked: Boolean)

        /** Call when the notify of new comments toggle changes.  */
        fun notifyOfComments(checked: Boolean)

        /** Call when the notify of new comment reply toggle changes.  */
        fun notifyOfCommentReplies(checked: Boolean)

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

    class NotificationsViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs, Errors {
        private val userInput = PublishSubject.create<User>()

        private val creatorDigestFrequencyIsGone: Observable<Boolean>
        private val creatorNotificationsAreGone: Observable<Boolean>
        private val userOutput = BehaviorSubject.create<User>()
        private val updateSuccess = PublishSubject.create<Unit>()

        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        private val client = requireNotNull(environment.apiClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())

        private val disposables = CompositeDisposable()

        init {

            this.client.fetchCurrentUser()
                .retry(2)
                .compose(neverErrorV2())
                .subscribe { this.currentUser.refresh(it) }
                .addToDisposable(disposables)

            val currentUser = this.currentUser.observable()
                .filter { it.getValue().isNotNull() }
                .map { it.getValue()!! }

            currentUser
                .take(1)
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            this.creatorDigestFrequencyIsGone = Observable.merge(currentUser, this.userInput)
                .map { !it.notifyOfBackings() }
                .distinctUntilChanged()

            this.creatorNotificationsAreGone = currentUser
                .map { (it.createdProjectsCount()).isZero() }
                .distinctUntilChanged()

            val updateSettingsNotification = this.userInput
                .concatMap { this.updateSettings(it) }

            updateSettingsNotification
                .compose(valuesV2())
                .subscribe { this.success(it) }
                .addToDisposable(disposables)

            updateSettingsNotification
                .compose(errorsV2())
                .subscribe {
                    this.unableToSavePreferenceError.onNext(
                        it ?: Throwable("update notifications settings error")
                    )
                }
                .addToDisposable(disposables)

            this.userInput
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            this.userOutput
                .window(2, 1)
                .flatMap<List<User>> { it.toList().toObservable() }
                .map { it.first() }
                .compose(takeWhenV2(this.unableToSavePreferenceError))
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun notifyMobileOfBackings(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyMobileOfBackings(checked).build())
            }
        }

        override fun notifyMobileOfComments(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyMobileOfComments(checked).build())
            }
        }

        override fun notifyMobileOfCreatorEdu(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyMobileOfCreatorEdu(checked).build())
            }
        }

        override fun notifyMobileOfFollower(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyMobileOfFollower(checked).build())
            }
        }

        override fun notifyMobileOfFriendActivity(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyMobileOfFriendActivity(checked).build())
            }
        }

        override fun notifyMobileOfMessages(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyMobileOfMessages(checked).build())
            }
        }

        override fun notifyMobileOfPostLikes(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyMobileOfPostLikes(checked).build())
            }
        }

        override fun notifyMobileOfUpdates(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyMobileOfUpdates(checked).build())
            }
        }

        override fun notifyOfBackings(checked: Boolean) {
            this.userOutput.value?.let {
                val userBuilder = it.toBuilder().notifyOfBackings(checked)
                if (!checked) {
                    userBuilder.notifyOfCreatorDigest(false)
                }
                this.userInput.onNext(userBuilder.build())
            }
        }

        override fun notifyMobileOfMarketingUpdate(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyMobileOfMarketingUpdate(checked).build())
            }
        }

        override fun notifyOfComments(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyOfComments(checked).build())
            }
        }

        override fun notifyOfCommentReplies(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyOfCommentReplies(checked).build())
            }
        }

        override fun notifyOfCreatorDigest(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyOfCreatorDigest(checked).build())
            }
        }

        override fun notifyOfCreatorEdu(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyOfCreatorEdu(checked).build())
            }
        }

        override fun notifyOfFollower(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyOfFollower(checked).build())
            }
        }

        override fun notifyOfFriendActivity(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyOfFriendActivity(checked).build())
            }
        }

        override fun notifyOfMessages(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyOfMessages(checked).build())
            }
        }

        override fun notifyOfUpdates(checked: Boolean) {
            this.userOutput.value?.let {
                this.userInput.onNext(it.toBuilder().notifyOfUpdates(checked).build())
            }
        }

        override fun creatorDigestFrequencyIsGone() = this.creatorDigestFrequencyIsGone

        override fun creatorNotificationsAreGone() = this.creatorNotificationsAreGone

        override fun user(): Observable<User> = this.userOutput

        override fun unableToSavePreferenceError(): Observable<String> =
            this.unableToSavePreferenceError
                .takeUntil(this.updateSuccess)
                .map { it.message ?: "Unable to save preference" }

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(Unit)
        }

        private fun updateSettings(user: User): Observable<Notification<User>> {
            return this.client.updateUserSettings(user)
                .materialize()
                .share()
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationsViewModel(environment) as T
        }
    }
}
