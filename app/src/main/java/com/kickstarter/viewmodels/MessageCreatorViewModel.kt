package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isPresent
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface MessageCreatorViewModel {
    interface Inputs {
        /** Call when the message edit text changes.  */
        fun messageBodyChanged(messageBody: String)

        /** Call when the send message button has been clicked.  */
        fun sendButtonClicked()
    }

    interface Outputs {
        /** Emits the creator's name to display in the message edit text hint.  */
        fun creatorName(): Observable<String>

        /** Emits when the progress bar should be visible.  */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits a boolean that determines if the send button should be enabled.  */
        fun sendButtonIsEnabled(): Observable<Boolean>

        /** Emits the MessageThread that the successfully sent message belongs to. */
        fun showMessageThread(): Observable<MessageThread>

        /** Emits when the message failed to successfully.  */
        fun showSentError(): Observable<Int>

        /** Emits when the message was sent successfully but the thread failed to load.  */
        fun showSentSuccess(): Observable<Int>
    }

    class MessageCreatorViewModel(private val environment: Environment, private val intent: Intent? = null) : ViewModel(), Inputs, Outputs {

        private val messageBodyChanged = PublishSubject.create<String>()
        private val sendButtonClicked = PublishSubject.create<Unit>()

        private val creatorName = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val sendButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val showMessageThread = BehaviorSubject.create<MessageThread>()
        private val showSentError = BehaviorSubject.create<Int>()
        private val showSentSuccess = BehaviorSubject.create<Int>()

        private val apiClient = requireNotNull(environment.apiClientV2())
        private val apolloClient = requireNotNull(environment.apolloClientV2())

        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()

        private fun getProjectFromIntent(intent: Intent?) = intent?.getParcelableExtra(IntentKey.PROJECT) as Project?
        init {
            val project = intent()
                .filter { getProjectFromIntent(it) != null }
                .map { requireNotNull(getProjectFromIntent(it)) }

            project
                .filter { it.creator().name().isNotNull() }
                .map { it.creator().name() }
                .subscribe {
                    this.creatorName.onNext(it)
                }
                .addToDisposable(disposables)

            val sendMessage = Observable.combineLatest(project, this.messageBodyChanged.startWith("")) { p, u -> SendMessage(p, u) }

            this.messageBodyChanged
                .map { it.isPresent() }
                .distinctUntilChanged()
                .subscribe {
                    this.sendButtonIsEnabled.onNext(it)
                }
                .addToDisposable(disposables)

            val sendMessageNotification = sendMessage
                .compose(takeWhenV2<SendMessage, Unit>(this.sendButtonClicked))
                .switchMap { sendMessage(it).materialize() }
                .share()

            sendMessageNotification
                .compose(errorsV2())
                .map { R.string.social_error_could_not_send_message_backer }
                .subscribe {
                    this.showSentError.onNext(it)
                    this.progressBarIsVisible.onNext(false)
                    this.sendButtonIsEnabled.onNext(true)
                }
                .addToDisposable(disposables)

            sendMessageNotification
                .compose(valuesV2())
                .switchMap { fetchThread(it) }
                .filter { it.isNotNull() }
                .subscribe {
                    this.showMessageThread.onNext(it)
                }
                .addToDisposable(disposables)
        }

        private fun fetchThread(conversationId: Long): Observable<MessageThread> {
            val fetchThreadNotification = this.apiClient.fetchMessagesForThread(conversationId)
                .materialize()
                .share()

            fetchThreadNotification
                .compose(errorsV2())
                .map { R.string.Your_message_has_been_sent }
                .subscribe {
                    this.showSentSuccess.onNext(it)
                }
                .addToDisposable(disposables)

            return fetchThreadNotification
                .compose(valuesV2())
                .filter { it.messageThread().isNotNull() }
                .map { it.messageThread() }
        }

        private fun sendMessage(sendMessage: SendMessage): Observable<Long> {
            return this.apolloClient.sendMessage(sendMessage.project, sendMessage.project.creator(), sendMessage.body)
                .doOnSubscribe {
                    this.progressBarIsVisible.onNext(true)
                    this.sendButtonIsEnabled.onNext(false)
                }
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        override fun messageBodyChanged(messageBody: String) {
            this.messageBodyChanged.onNext(messageBody)
        }

        override fun sendButtonClicked() {
            this.sendButtonClicked.onNext(Unit)
        }

        override fun creatorName(): Observable<String> = this.creatorName

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun sendButtonIsEnabled(): Observable<Boolean> = this.sendButtonIsEnabled

        override fun showMessageThread(): Observable<MessageThread> = this.showMessageThread

        override fun showSentError(): Observable<Int> = this.showSentError

        override fun showSentSuccess(): Observable<Int> = this.showSentSuccess

        data class SendMessage(val project: Project, val body: String)
    }

    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MessageCreatorViewModel(environment, intent) as T
        }
    }
}
