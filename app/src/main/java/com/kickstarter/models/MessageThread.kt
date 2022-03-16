package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MessageThread private constructor(
    private val backing: Backing?,
    private val closed: Boolean,
    private val id: Long,
    private val lastMessage: Message?,
    private val participant: User?,
    private val project: Project?,
    private val unreadMessagesCount: Int
) : Parcelable {
    fun backing() = this.backing
    fun closed() = this.closed
    fun id() = this.id
    fun lastMessage() = this.lastMessage
    fun participant() = this.participant
    fun project() = this.project
    fun unreadMessagesCount() = this.unreadMessagesCount

    @Parcelize
    data class Builder(
        private var backing: Backing? = null,
        private var closed: Boolean = false,
        private var id: Long = 0L,
        private var lastMessage: Message? = null,
        private var participant: User? = null,
        private var project: Project? = null,
        private var unreadMessagesCount: Int = 0
    ) : Parcelable {
        fun backing(backing: Backing?) = apply { this.backing = backing }
        fun closed(closed: Boolean) = apply { this.closed = closed }
        fun id(id: Long) = apply { this.id = id ?: 0L }
        fun lastMessage(lastMessage: Message?) = apply { this.lastMessage = lastMessage }
        fun participant(participant: User?) = apply { this.participant = participant }
        fun project(project: Project?) = apply { this.project = project }
        fun unreadMessagesCount(unreadMessagesCount: Int) = apply { this.unreadMessagesCount = unreadMessagesCount }

        fun build() = MessageThread(
            backing = this.backing,
            closed = this.closed,
            id = this.id,
            lastMessage = this.lastMessage,
            participant = this.participant,
            project = this.project,
            unreadMessagesCount = this.unreadMessagesCount
        )
    }

    fun toBuilder() = Builder(
        backing = backing,
        closed = closed,
        id = id,
        lastMessage = lastMessage,
        participant = participant,
        project = project,
        unreadMessagesCount = unreadMessagesCount
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is MessageThread) {
            equals = backing() == obj.backing() &&
                closed() == obj.closed() &&
                id() == obj.id() &&
                lastMessage() == obj.lastMessage() &&
                participant() == obj.participant() &&
                project() == obj.project() &&
                unreadMessagesCount() == obj.unreadMessagesCount()
        }
        return equals
    }
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
