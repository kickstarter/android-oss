package com.kickstarter.services.apiresponses.commentresponse

import android.os.Parcelable
import com.kickstarter.libs.qualifiers.AutoGson
import com.kickstarter.models.Comment
import kotlinx.android.parcel.Parcelize

@Parcelize
@AutoGson
class CommentEnvelope(
    val comments: List<Comment>?,
    val commentableId: String?,
    val pageInfoEnvelope: PageInfoEnvelope?,
    val totalCount: Int?
) : Parcelable {

    @Parcelize
    @AutoGson
    data class Builder(
        var comments: List<Comment>? = null,
        var commentableId: String? = null,
        var pageInfoEnvelope: PageInfoEnvelope? = null,
        var totalCount: Int? = 0
    ) : Parcelable {

        fun comments(comments: List<Comment>?) = apply { this.comments = comments }
        fun commentableId(commentableId: String?) = apply { this.commentableId = commentableId }
        fun pageInfoEnvelope(pageInfoEnvelope: PageInfoEnvelope?) = apply { this.pageInfoEnvelope = pageInfoEnvelope }
        fun totalCount(totalCount: Int?) = apply { this.totalCount = totalCount }
        fun build() = CommentEnvelope(comments, commentableId, pageInfoEnvelope, totalCount)
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(this.comments, this.commentableId, this.pageInfoEnvelope, this.totalCount)
}
