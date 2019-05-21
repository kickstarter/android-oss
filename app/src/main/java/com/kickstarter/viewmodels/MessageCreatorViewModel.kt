package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.StringUtils
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.MessageCreatorActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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

    class ViewModel(environment: Environment) : ActivityViewModel<MessageCreatorActivity>(environment), Inputs, Outputs {

        private val messageBodyChanged = PublishSubject.create<String>()
        private val sendButtonClicked = PublishSubject.create<Void>()

        private val creatorName = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val sendButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val showMessageThread = BehaviorSubject.create<MessageThread>()
        private val showSentError = BehaviorSubject.create<Int>()
        private val showSentSuccess = BehaviorSubject.create<Int>()

        private val apiClient: ApiClientType = environment.apiClient()
        private val apolloClient: ApolloClientType = environment.apolloClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            val project = intent()
                    .map { it.getParcelableExtra(IntentKey.PROJECT) as Project }

            project
                    .map { it.creator().name() }
                    .compose(bindToLifecycle())
                    .subscribe(this.creatorName)

            val sendMessage = Observable.combineLatest(project, this.messageBodyChanged.startWith(""))
            { p, u -> SendMessage(p, u) }

            this.messageBodyChanged
                    .map { StringUtils.isPresent(it) }
                    .distinctUntilChanged()
                    .subscribe(this.sendButtonIsEnabled)

            val sendMessageNotification = sendMessage
                    .compose(takeWhen<SendMessage, Void>(this.sendButtonClicked))
                    .switchMap { sendMessage(it).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            sendMessageNotification
                    .compose(errors())
                    .map { R.string.social_error_could_not_send_message_backer }
                    .subscribe {
                        this.showSentError.onNext(it)
                        this.progressBarIsVisible.onNext(false)
                        this.sendButtonIsEnabled.onNext(true)
                    }

            sendMessageNotification
                    .compose(values())
                    .switchMap { fetchThread(it) }
                    .filter(ObjectUtils::isNotNull)
                    .subscribe(this.showMessageThread)

            project
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackViewedMessageCreatorModal(it) }

        }

        private fun fetchThread(conversationId: Long): Observable<MessageThread> {
            val fetchThreadNotification = this.apiClient.fetchMessagesForThread(conversationId)
                    .compose(bindToLifecycle())
                    .materialize()
                    .share()

            fetchThreadNotification
                    .compose(errors())
                    .map { R.string.Your_message_has_been_sent }
                    .subscribe(this.showSentSuccess)

            return fetchThreadNotification
                    .compose(values())
                    .map { it.messageThread() }
        }

        private fun sendMessage(sendMessage: SendMessage): Observable<Long> {
            return this.apolloClient.sendMessage(sendMessage.project, sendMessage.project.creator(), sendMessage.body)
                    .doOnSubscribe {
                        this.progressBarIsVisible.onNext(true)
                        this.sendButtonIsEnabled.onNext(false)
                    }
        }

        override fun messageBodyChanged(messageBody: String) {
            this.messageBodyChanged.onNext(messageBody)
        }

        override fun sendButtonClicked() {
            this.sendButtonClicked.onNext(null)
        }

        @NonNull
        override fun creatorName(): Observable<String> = this.creatorName

        @NonNull
        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        @NonNull
        override fun sendButtonIsEnabled(): Observable<Boolean> = this.sendButtonIsEnabled

        @NonNull
        override fun showMessageThread(): Observable<MessageThread> = this.showMessageThread

        @NonNull
        override fun showSentError(): Observable<Int> = this.showSentError

        @NonNull
        override fun showSentSuccess(): Observable<Int> = this.showSentSuccess

        data class SendMessage(val project: Project, val body: String)
    }
}
