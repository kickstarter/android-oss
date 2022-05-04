package com.kickstarter.models.pushdata

import android.os.Parcelable
import com.kickstarter.models.Activity.Category
import kotlinx.parcelize.Parcelize

@Parcelize
class Activity private constructor(
    @Category
    private val category: String,
    private val commentId: Long?,
    private val id: Long,
    private val projectId: Long?,
    private val projectPhoto: String?,
    private val userPhoto: String?,
    private val updateId: Long?
) : Parcelable {
    fun category() = this.category
    fun commentId() = this.commentId
    fun id() = id
    fun projectId() = projectId
    fun projectPhoto() = projectPhoto
    fun userPhoto() = userPhoto
    fun updateId() = updateId

    @Parcelize
    data class Builder(
        private var category: String = "",
        private var commentId: Long? = null,
        private var id: Long = 0L,
        private var projectId: Long? = null,
        private var projectPhoto: String? = null,
        private var userPhoto: String? = null,
        private var updateId: Long? = null
    ) : Parcelable {
        fun category(@Category category: String?) = apply { this.category = category ?: "" }
        fun commentId(commentId: Long?) = apply { commentId?.let { this.commentId = it } }
        fun id(id: Long?) = apply { this.id = id ?: 0L }
        fun projectId(projectId: Long?) = apply { this.projectId = projectId }
        fun projectPhoto(projectPhoto: String?) = apply { this.projectPhoto = projectPhoto }
        fun userPhoto(userPhoto: String?) = apply { this.userPhoto = userPhoto }
        fun updateId(updateId: Long?) = apply { this.updateId = updateId }
        fun build() = Activity(
            category = category,
            commentId = commentId,
            id = id,
            projectId = projectId,
            projectPhoto = projectPhoto,
            userPhoto = userPhoto,
            updateId = updateId
        )
    }

    fun toBuilder() = Builder(
        category = category,
        commentId = commentId,
        id = id,
        projectId = projectId,
        projectPhoto = projectPhoto,
        userPhoto = userPhoto,
        updateId = updateId
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Activity) {
            equals = category() == other.category() &&
                commentId() == other.commentId() &&
                id() == other.id() &&
                projectId() == other.projectId() &&
                projectPhoto() == other.projectPhoto() &&
                userPhoto() == other.userPhoto() &&
                updateId() == other.updateId()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
