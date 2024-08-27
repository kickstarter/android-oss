package com.kickstarter.viewmodels

import android.content.SharedPreferences
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.MessageThread
import com.kickstarter.ui.SharedPreferenceKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

interface MessageThreadHolderViewModel {
    interface Inputs {
        /** Call to configure with a MessageThread.  */
        fun configureWith(messageThread: MessageThread)

        /** Call when the the message thread card view has been clicked.  */
        fun messageThreadCardViewClicked()
    }

    interface Outputs {
        /** Emits when the card view should be elevated.  */
        fun cardViewIsElevated(): Observable<Boolean>

        /** Emits the date to display.  */
        fun dateDateTime(): Observable<DateTime>

        /** Emits a boolean determining if the date typeface is bold.  */
        fun dateTextViewIsBold(): Observable<Boolean>

        /** Emits the message body to display.  */
        fun messageBodyTextViewText(): Observable<String>

        /** Emits a boolean determining if the message body typeface is bold.  */
        fun messageBodyTextIsBold(): Observable<Boolean>

        /** Emits the participant's avatar url to display.  */
        fun participantAvatarUrl(): Observable<String>

        /** Emits a boolean determining if the participant name typeface is bold.  */
        fun participantNameTextViewIsBold(): Observable<Boolean>

        /** Emits the participant name to display.  */
        fun participantNameTextViewText(): Observable<String>

        /** Emits when we want to start the [com.kickstarter.ui.activities.MessagesActivity].  */
        fun startMessagesActivity(): Observable<MessageThread>

        /** Emits a boolean to determine if the unread count text view should be gone.  */
        fun unreadCountTextViewIsGone(): Observable<Boolean>

        /** Emits the unread count text view text to be displayed.  */
        fun unreadCountTextViewText(): Observable<String>
    }

    class ViewModel(val environment: Environment) : androidx.lifecycle.ViewModel(), Inputs, Outputs {

        private val sharedPreferences: SharedPreferences?
        private val messageThread = PublishSubject.create<MessageThread>()
        private val messageThreadCardViewClicked = PublishSubject.create<Unit>()
        private val cardViewIsElevated: Observable<Boolean>
        private val dateDateTime: Observable<DateTime>
        private val dateTextViewIsBold: Observable<Boolean>
        private val messageBodyTextIsBold: Observable<Boolean>
        private val messageBodyTextViewText: Observable<String>
        private val participantAvatarUrl: Observable<String>
        private val participantNameTextViewIsBold: Observable<Boolean>
        private val participantNameTextViewText: Observable<String>
        private val startMessagesActivity: Observable<MessageThread>
        private val unreadCountTextViewIsGone: Observable<Boolean>
        private val unreadCountTextViewText: Observable<String>

        val inputs: Inputs = this
        val outputs: Outputs = this
        val disposables = CompositeDisposable()

        init {
            sharedPreferences = requireNotNull(environment.sharedPreferences())

            // Store the correct initial hasUnreadMessages value.
            messageThread
                .compose(Transformers.observeForUIV2())
                .filter { it.isNotNull() }
                .subscribe { thread: MessageThread ->
                    setHasUnreadMessagesPreference(
                        thread,
                        sharedPreferences
                    )
                }.addToDisposable(disposables)

            val hasUnreadMessages = Observable.merge(
                messageThread.map { thread: MessageThread ->
                    hasUnreadMessages(
                        thread,
                        sharedPreferences
                    )
                },
                messageThreadCardViewClicked.map { false }
            )

            val lastMessage = messageThread.map { it.lastMessage() }
                .filter { it.isNotNull() }
                .map { it }

            val participant = messageThread.map { it.participant() }
                .filter { it.isNotNull() }
                .map { it }

            cardViewIsElevated = hasUnreadMessages

            dateDateTime = lastMessage.map { it.createdAt() }

            dateTextViewIsBold = hasUnreadMessages

            messageBodyTextIsBold = hasUnreadMessages

            messageBodyTextViewText = lastMessage.map { it.body() }

            participantAvatarUrl = participant.map { it.avatar().medium() }

            participantNameTextViewIsBold = hasUnreadMessages

            participantNameTextViewText = participant.map { it.name() }

            startMessagesActivity = messageThread.compose(
                Transformers.takeWhenV2(
                    messageThreadCardViewClicked
                )
            )
            unreadCountTextViewIsGone = hasUnreadMessages.map { it.negate() }

            unreadCountTextViewText = messageThread
                .map { it.unreadMessagesCount() }
                .map {
                    NumberUtils.format(
                        it
                    )
                }

            messageThread
                .compose(Transformers.takeWhenV2(messageThreadCardViewClicked))
                .subscribe { markedAsRead(it, sharedPreferences) }
                .addToDisposable(disposables)
        }

        override fun configureWith(messageThread: MessageThread) {
            this.messageThread.onNext(messageThread)
        }

        override fun messageThreadCardViewClicked() {
            messageThreadCardViewClicked.onNext(Unit)
        }

        override fun cardViewIsElevated(): Observable<Boolean> = cardViewIsElevated

        override fun dateDateTime(): Observable<DateTime> = dateDateTime

        override fun dateTextViewIsBold(): Observable<Boolean> = dateTextViewIsBold

        override fun messageBodyTextViewText(): Observable<String> = messageBodyTextViewText

        override fun messageBodyTextIsBold(): Observable<Boolean> = messageBodyTextIsBold

        override fun participantAvatarUrl(): Observable<String> = participantAvatarUrl

        override fun participantNameTextViewIsBold(): Observable<Boolean> = participantNameTextViewIsBold

        override fun participantNameTextViewText(): Observable<String> = participantNameTextViewText

        override fun startMessagesActivity(): Observable<MessageThread> = startMessagesActivity

        override fun unreadCountTextViewIsGone(): Observable<Boolean> = unreadCountTextViewIsGone

        override fun unreadCountTextViewText(): Observable<String> = unreadCountTextViewText

        companion object {
            private fun cacheKey(messageThread: MessageThread): String {
                return SharedPreferenceKey.MESSAGE_THREAD_HAS_UNREAD_MESSAGES + messageThread.id()
            }

            private fun hasUnreadMessages(
                messageThread: MessageThread,
                sharedPreferences: SharedPreferences
            ): Boolean {
                return sharedPreferences.getBoolean(
                    cacheKey(messageThread),
                    messageThread.unreadMessagesCount() > 0
                )
            }

            private fun markedAsRead(
                messageThread: MessageThread,
                sharedPreferences: SharedPreferences
            ) {
                val editor = sharedPreferences.edit()
                editor.putBoolean(cacheKey(messageThread), false)
                editor.apply()
            }

            private fun setHasUnreadMessagesPreference(
                messageThread: MessageThread,
                sharedPreferences: SharedPreferences
            ) {
                val editor = sharedPreferences.edit()
                editor.putBoolean(cacheKey(messageThread), messageThread.unreadMessagesCount() > 0)
                editor.apply()
            }
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }
}
