package com.kickstarter.models

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.StringDef
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Parcelize
class Activity internal constructor(
    @Category
    private val category: String?,
    private val createdAt: DateTime,
    private val id: Long,
    private val project: Project?,
    private val update: Update?,
    private val updatedAt: DateTime?,
    private val user: User?
) : Parcelable {
    fun category() = this.category
    fun createdAt() = this.createdAt
    fun id() = this.id
    fun project() = this.project
    fun update() = this.update
    fun updatedAt() = this.updatedAt
    fun user() = this.user

    @Parcelize
    data class Builder(
        private var category: String? = "",
        private var createdAt: DateTime = DateTime.now(),
        private var id: Long = 0L,
        private var project: Project? = null,
        private var update: Update? = null,
        private var updatedAt: DateTime? = null,
        private var user: User? = null
    ) : Parcelable {
        fun category(@Category category: String?) = apply { this.category = category ?: "" }
        fun createdAt(createdAt: DateTime?) = apply { createdAt?.let { this.createdAt = it } }
        fun id(id: Long?) = apply { this.id = id ?: 0L }
        fun project(project: Project?) = apply { this.project = project }
        fun update(update: Update?) = apply { this.update = update }
        fun updatedAt(updatedAt: DateTime?) = apply { this.updatedAt = updatedAt }
        fun user(user: User?) = apply { this.user = user }
        fun build() = Activity(
            category = category,
            createdAt = createdAt,
            id = id,
            project = project,
            update = update,
            updatedAt = updatedAt,
            user = user
        )
    }

    fun toBuilder() = Builder(
        category = category,
        createdAt = createdAt,
        id = id,
        project = project,
        update = update,
        updatedAt = updatedAt,
        user = user
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Activity) {
            equals = category() == other.category() &&
                createdAt() == other.createdAt() &&
                id() == other.id() &&
                project() == other.project() &&
                update() == other.update() &&
                updatedAt() == other.updatedAt() &&
                user() == other.user()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef(
        CATEGORY_WATCH,
        CATEGORY_UPDATE,
        CATEGORY_COMMENT_PROJECT,
        CATEGORY_BACKING,
        CATEGORY_COMMENT_POST,
        CATEGORY_CANCELLATION,
        CATEGORY_SUCCESS,
        CATEGORY_SUSPENSION,
        CATEGORY_LAUNCH,
        CATEGORY_FAILURE,
        CATEGORY_FUNDING,
        CATEGORY_BACKING_CANCELED,
        CATEGORY_BACKING_DROPPED,
        CATEGORY_BACKING_REWARD,
        CATEGORY_BACKING_AMOUNT,
        CATEGORY_COMMENT_PROPOSAL,
        CATEGORY_FOLLOW
    )
    annotation class Category

    fun projectUpdateUrl(): String {
        return Uri.parse(project()?.webProjectUrl()).buildUpon()
            .appendEncodedPath("posts")
            .appendPath(update()?.id().toString())
            .toString()
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()

        const val CATEGORY_WATCH = "watch"
        const val CATEGORY_UPDATE = "update"
        const val CATEGORY_COMMENT_PROJECT = "comment-project"
        const val CATEGORY_BACKING = "backing"
        const val CATEGORY_COMMENT_POST = "comment-post"
        const val CATEGORY_CANCELLATION = "cancellation"
        const val CATEGORY_SUCCESS = "success"
        const val CATEGORY_SUSPENSION = "suspension"
        const val CATEGORY_LAUNCH = "launch"
        const val CATEGORY_FAILURE = "failure"
        const val CATEGORY_FUNDING = "funding"
        const val CATEGORY_BACKING_CANCELED = "backing-canceled"
        const val CATEGORY_BACKING_DROPPED = "backing-dropped"
        const val CATEGORY_BACKING_REWARD = "backing-reward"
        const val CATEGORY_BACKING_AMOUNT = "backing-amount"
        const val CATEGORY_COMMENT_PROPOSAL = "comment-proposal"
        const val CATEGORY_FOLLOW = "follow"
    }
}
