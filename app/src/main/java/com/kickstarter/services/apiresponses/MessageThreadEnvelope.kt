package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.Message
import com.kickstarter.models.MessageThread
import com.kickstarter.models.User
import kotlinx.parcelize.Parcelize

@Parcelize
class MessageThreadEnvelope private constructor(
    private val messages: List<Message> = emptyList(),
    private val messageThread: MessageThread?,
    private val participants: List<User> = emptyList(),
) : Parcelable {
    fun messages() = this.messages
    fun messageThread() = this.messageThread
    fun participants() = this.participants

    @Parcelize
    data class Builder(
        private var messages: List<Message> = emptyList(),
        private var messageThread: MessageThread? = null,
        private var participants: List<User> = emptyList()
    ) : Parcelable {
        fun messages(messages: List<Message>?) = apply { this.messages = messages ?: emptyList() }
        fun messageThread(messageThread: MessageThread?) = apply { this.messageThread = messageThread }
        fun participants(participants: List<User>) = apply { this.participants = participants }
        fun build() = MessageThreadEnvelope(
            messages = messages,
            messageThread = messageThread,
            participants = participants
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is MessageThreadEnvelope) {
            equals = messages() == obj.messages() &&
                messageThread() == obj.messageThread() &&
                participants() == obj.participants()
        }
        return equals
    }

    fun toBuilder() = Builder(
        messages = messages,
        messageThread = messageThread,
        participants = participants
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
