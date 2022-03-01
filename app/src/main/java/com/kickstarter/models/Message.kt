package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class Message internal constructor(
    private val body: String,
    private val createdAt: DateTime,
    private val id: Long,
    private val recipient: User,
    private val sender: User,
) : Parcelable {

    fun body() = this.body
    fun createdAt() = this.createdAt
    fun id() = this.id
    fun recipient() = this.recipient
    fun sender() = this.sender

    @Parcelize
    data class Builder(
        private var body: String = "",
        private var createdAt: DateTime = DateTime.now(),
        private var id: Long = 0L,
        private var recipient: User = User.builder().build(),
        private var sender: User = User.builder().build()
    ) : Parcelable {
        fun body(body: String?) = apply { this.body = body ?: "" }
        fun createdAt(createdAt: DateTime?) = apply { createdAt?.let { this.createdAt = it } }
        fun id(id: Long?) = apply { this.id = id ?: 0L }
        fun recipient(recipient: User?) = apply { recipient?.let { this.recipient = it } }
        fun sender(sender: User?) = apply { sender?.let { this.sender = it } }
        fun build() = Message(
            body = body,
            createdAt = createdAt,
            id = id,
            recipient = recipient,
            sender = sender
        )
    }

    fun toBuilder() = Builder(
        body = body,
        createdAt = createdAt,
        id = id,
        recipient = recipient,
        sender = sender
    )

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Message) {
            equals = body() == other.body() &&
                createdAt() == other.createdAt() &&
                id() == other.id() &&
                recipient() == other.recipient() &&
                sender() == other.sender()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
