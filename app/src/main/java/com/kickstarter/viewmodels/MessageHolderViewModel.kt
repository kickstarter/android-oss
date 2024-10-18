package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Message
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface MessageHolderViewModel {
    interface Inputs {
        /** Call to configure the view model with a message.  */
        fun configureWith(message: Message)

        /** Call to let view model know if message view holder is in the last adapter position.  */
        fun isLastPosition(isLastPosition: Boolean)
    }

    interface Outputs {
        /** Emits a boolean to determine whether the delivery status text view should be gone.  */
        fun deliveryStatusTextViewIsGone(): Observable<Boolean>

        /** Emits a boolean to determine whether the message recipient card view should be gone.  */
        fun messageBodyRecipientCardViewIsGone(): Observable<Boolean>

        /** Emits the recipient's message body text view text.  */
        fun messageBodyRecipientTextViewText(): Observable<String>

        /** Emits a boolean to determine whether the message sender card view should be gone.  */
        fun messageBodySenderCardViewIsGone(): Observable<Boolean>

        /** Emits the sender's message body text view text.  */
        fun messageBodySenderTextViewText(): Observable<String>

        /** Emits a boolean that determines whether the participant's avatar image should be hidden.  */
        fun participantAvatarImageHidden(): Observable<Boolean>

        /** Emits the url for the participant's avatar image.  */
        fun participantAvatarImageUrl(): Observable<String>
    }

    class ViewModel(environment: Environment) :
        Inputs,
        Outputs {
        private val currentUser: CurrentUserTypeV2?
        private val isLastPosition = PublishSubject.create<Boolean>()
        private val message = PublishSubject.create<Message>()
        private val deliveryStatusTextViewIsGone: Observable<Boolean>
        private val messageBodyRecipientCardViewIsGone: Observable<Boolean>
        private val messageBodyRecipientTextViewText: Observable<String>
        private val messageBodySenderCardViewIsGone: Observable<Boolean>
        private val messageBodySenderTextViewText: Observable<String>
        private val participantAvatarImageHidden: Observable<Boolean>
        private val participantAvatarImageUrl: Observable<String>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            currentUser = requireNotNull(environment.currentUserV2())

            val messageAndCurrentUserIsSender =
                Observable.combineLatest<Message, User, Pair<Message, User>>(
                    message,
                    currentUser.loggedInUser()
                ) { a: Message?, b: User? -> Pair.create(a, b) }
                    .map { mu: Pair<Message, User> ->
                        Pair.create(
                            mu.first,
                            mu.first.sender().id() == mu.second.id()
                        )
                    }

            messageBodyRecipientCardViewIsGone = messageAndCurrentUserIsSender
                .map { PairUtils.second(it) }

            messageBodyRecipientTextViewText = messageAndCurrentUserIsSender
                .filter { !it.second }
                .map { it.first.body() }

            messageBodySenderCardViewIsGone = messageBodyRecipientCardViewIsGone
                .map { it.negate() }

            messageBodySenderTextViewText = messageAndCurrentUserIsSender
                .filter { it.second }
                .map { it.first.body() }

            deliveryStatusTextViewIsGone = Observable.zip<Boolean, Boolean, Pair<Boolean, Boolean>>(
                isLastPosition,
                messageBodySenderCardViewIsGone
            ) { a: Boolean?, b: Boolean? -> Pair.create(a, b) }
                .map { !it.first || it.second }

            participantAvatarImageHidden = messageBodyRecipientCardViewIsGone
            participantAvatarImageUrl = messageAndCurrentUserIsSender
                .filter { !it.second }
                .map { it.first.sender().avatar().medium() }
        }
        override fun configureWith(message: Message) {
            this.message.onNext(message)
        }

        override fun isLastPosition(isLastPosition: Boolean) {
            this.isLastPosition.onNext(isLastPosition)
        }

        override fun deliveryStatusTextViewIsGone(): Observable<Boolean> = deliveryStatusTextViewIsGone

        override fun messageBodyRecipientCardViewIsGone(): Observable<Boolean> = messageBodyRecipientCardViewIsGone

        override fun messageBodySenderCardViewIsGone(): Observable<Boolean> = messageBodySenderCardViewIsGone

        override fun messageBodySenderTextViewText(): Observable<String> = messageBodySenderTextViewText

        override fun messageBodyRecipientTextViewText(): Observable<String> = messageBodyRecipientTextViewText

        override fun participantAvatarImageHidden(): Observable<Boolean> = participantAvatarImageHidden

        override fun participantAvatarImageUrl(): Observable<String> = participantAvatarImageUrl
    }
}
