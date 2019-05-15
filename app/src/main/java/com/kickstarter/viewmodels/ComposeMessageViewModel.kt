package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.StringUtils
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ComposeMessageActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ComposeMessageViewModel {
    interface Inputs {
        /** Call when the message edit text changes.  */
        fun messageBodyChanged(messageBody: String)

        /** Call when the send message button has been clicked.  */
        fun sendButtonClicked()
    }

    interface Outputs {

        /**  */
        fun error(): Observable<String>

        fun sendButtonIsEnabled(): Observable<Boolean>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits a string to display as the message edit text hint.  */
        fun messageEditTextHint(): Observable<String>

        /**  */
        fun success(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<ComposeMessageActivity>(environment), Inputs, Outputs {

        private val messageBodyChanged = PublishSubject.create<String>()
        private val sendButtonClicked = PublishSubject.create<Void>()

        private val error = BehaviorSubject.create<String>()
        private val messageEditTextHint = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val sendButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            val project = intent()
                    .map { it.getParcelableExtra(IntentKey.PROJECT) as Project }

            project
                    .map { it.creator().name() }
                    .compose(bindToLifecycle())
                    .subscribe(this.messageEditTextHint)

            val sendMessage = Observable.combineLatest(project, this.messageBodyChanged)
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
                    .subscribe { this.error.onNext(it.localizedMessage) }

            sendMessageNotification
                    .compose(values())
                    .subscribe(this.success)
        }

        private fun sendMessage(sendMessage: SendMessage): Observable<String> {
            return this.apolloClient.sendMessage(sendMessage.project, sendMessage.project.creator(), sendMessage.body)
                    .doOnSubscribe {
                        this.progressBarIsVisible.onNext(true)
                        this.sendButtonIsEnabled.onNext(false)
                    }
                    .doAfterTerminate {
                        this.progressBarIsVisible.onNext(false)
                        this.sendButtonIsEnabled.onNext(true)
                    }
        }

        override fun messageBodyChanged(messageBody: String) {
            this.messageBodyChanged.onNext(messageBody)
        }

        override fun sendButtonClicked() {
            this.sendButtonClicked.onNext(null)
        }

        @NonNull
        override fun error(): Observable<String> = this.error

        @NonNull
        override fun messageEditTextHint(): Observable<String> = this.messageEditTextHint

        @NonNull
        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        @NonNull
        override fun sendButtonIsEnabled(): Observable<Boolean> = this.sendButtonIsEnabled

        @NonNull
        override fun success(): Observable<String> = this.success

        data class SendMessage(val project: Project, val body: String)
    }
}
