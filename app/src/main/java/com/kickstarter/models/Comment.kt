package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class Comment private constructor(
    private val author: User,
    private val body: String,
    private val createdAt: DateTime?,
    private val deleted: Boolean,
    private val hasFlaggings: Boolean,
    private val sustained: Boolean,
    private val authorCanceledPledge: Boolean,
    private val cursor: String,
    private val repliesCount: Int,
    private val authorBadges: List<String>?,
    private val id: Long,
    private val parentId: Long
) : Parcelable, Relay {
    fun author() = this.author
    fun body() = this.body
    fun createdAt() = this.createdAt
    fun deleted() = this.deleted
    fun hasFlaggings() = this.hasFlaggings
    fun sustained() = this.sustained
    fun authorCanceledPledge() = this.authorCanceledPledge
    fun cursor() = this.cursor
    fun repliesCount() = this.repliesCount
    fun authorBadges() = this.authorBadges
    override fun id() = this.id
    fun parentId() = this.parentId

    @Parcelize
    data class Builder(
        private var author: User = User.builder().build(),
        private var body: String = "",
        private var createdAt: DateTime? = null,
        private var deleted: Boolean = false,
        private var hasFlaggings: Boolean = false,
        private var sustained: Boolean = false,
        private var authorCanceledPledge: Boolean = false,
        private var cursor: String = "",
        private var repliesCount: Int = 0,
        private var authorBadges: List<String>? = emptyList(),
        private var id: Long = -1,
        private var parentId: Long = -1
    ) : Parcelable {
        fun author(author: User?) = apply { author?.let { this.author = it } }
        fun cursor(cursor: String?) = apply { this.cursor = cursor ?: "" }
        fun authorBadges(authorBadges: List<String>?) = apply { this.authorBadges = authorBadges }
        fun repliesCount(repliesCount: Int?) = apply { this.repliesCount = repliesCount ?: 0 }
        fun body(body: String?) = apply { this.body = body ?: "" }
        fun createdAt(createdAt: DateTime?) = apply { this.createdAt = createdAt }
        fun deleted(deleted: Boolean?) = apply { this.deleted = deleted ?: false }
        fun hasFlaggings(hasFlaggings: Boolean?) = apply { this.hasFlaggings = hasFlaggings ?: false }
        fun sustained(sustained: Boolean?) = apply { this.sustained = sustained ?: false }
        fun authorCanceledPledge(authorCanceledPledge: Boolean?) = apply { this.authorCanceledPledge = authorCanceledPledge ?: false }
        fun id(id: Long?) = apply { this.id = id ?: -1 }
        fun parentId(parentId: Long?) = apply { this.parentId = parentId ?: -1 }
        fun build() = Comment(
            author = author,
            cursor = cursor,
            authorBadges = authorBadges,
            repliesCount = repliesCount,
            body = body,
            createdAt = createdAt,
            deleted = deleted,
            hasFlaggings = hasFlaggings,
            sustained = sustained,
            authorCanceledPledge = authorCanceledPledge,
            id = id,
            parentId = parentId
        )
    }

    fun toBuilder() = Builder(
        author = author,
        cursor = cursor,
        authorBadges = authorBadges,
        repliesCount = repliesCount,
        body = body,
        createdAt = createdAt,
        deleted = deleted,
        hasFlaggings = hasFlaggings,
        sustained = sustained,
        authorCanceledPledge = authorCanceledPledge,
        id = id,
        parentId = parentId
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Comment) {
            equals = id() == other.id() &&
                body() == other.body() &&
                author() == other.author() &&
                authorBadges() == other.authorBadges() &&
                cursor() == other.cursor() &&
                deleted() == other.deleted() &&
                hasFlaggings() == other.hasFlaggings() &&
                sustained() == other.sustained() &&
                repliesCount() == other.repliesCount() &&
                authorCanceledPledge() == other.authorCanceledPledge() &&
                createdAt() == other.createdAt() &&
                parentId() == other.parentId()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
