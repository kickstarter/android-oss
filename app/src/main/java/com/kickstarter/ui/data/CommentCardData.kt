package com.kickstarter.ui.data

import android.os.Parcelable
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import kotlinx.android.parcel.Parcelize

@Parcelize
class CommentCardData(
    val comment: Comment?,
    val commentCardState: Int,
    val commentableId: String?,
    val project: Project?
) : Parcelable {

    @Parcelize
    data class Builder(
        var comment: Comment? = null,
        var commentCardState: Int = 0,
        var commentableId: String? = null,
        var project: Project? = null
    ) : Parcelable {
        fun comment(comment: Comment?) = apply { this.comment = comment }
        fun commentCardState(commentCardState: Int) = apply { this.commentCardState = commentCardState }
        fun project(project: Project?) = apply { this.project = project }
        fun commentableId(commentableId: String?) = apply { this.commentableId = commentableId }
        fun build() = CommentCardData(comment, commentCardState, commentableId, project)
    }

    companion object {
        fun builder() = CommentCardData.Builder()
    }

    fun toBuilder() = CommentCardData.Builder(this.comment, this.commentCardState, this.commentableId, this.project)
}
