package com.kickstarter.ui.data

import android.os.Parcelable
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import kotlinx.android.parcel.Parcelize

@Parcelize
class CommentCardData(
    val comment: Comment?,
    val commentCardState: Int,
    val project: Project?
) : Parcelable {

    @Parcelize
    data class Builder(
        var comment: Comment? = null,
        var commentCardState: Int = 0,
        var project: Project? = null
    ) : Parcelable {
        fun comment(comment: Comment?) = apply { this.comment = comment }
        fun commentCardState(commentCardState: Int) = apply { this.commentCardState = commentCardState }
        fun project(project: Project?) = apply { this.project = project }
        fun build() = CommentCardData(comment, commentCardState, project)
    }

    companion object {
        fun builder() = CommentCardData.Builder()
    }

    fun toBuilder() = CommentCardData.Builder(this.comment, this.commentCardState, this.project)

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)

        if (other is CommentCardData) {
            val obj: CommentCardData = other
            equals = (
                obj.comment?.equals(this.comment) == true &&
                    obj.commentCardState == this.commentCardState &&
                    obj.project?.equals(this.project) == true
                )
        }

        return equals
    }
}
