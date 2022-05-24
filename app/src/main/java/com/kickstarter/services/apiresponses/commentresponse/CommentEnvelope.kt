package com.kickstarter.services.apiresponses.commentresponse

import android.os.Parcelable
import com.kickstarter.models.ApolloEnvelope
import com.kickstarter.models.Comment
import kotlinx.android.parcel.Parcelize

@Parcelize
class CommentEnvelope(
    val comments: List<Comment>?,
    val commentableId: String?,
    val pageInfoEnvelope: PageInfoEnvelope?,
    val totalCount: Int?
) : Parcelable, ApolloEnvelope {

    @Parcelize
    data class Builder(
        var comments: List<Comment>? = null,
        var commentableId: String? = null,
        var pageInfoEnvelope: PageInfoEnvelope? = null,
        var totalCount: Int? = null
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
    override fun pageInfoEnvelope(): PageInfoEnvelope? = this.pageInfoEnvelope
}
