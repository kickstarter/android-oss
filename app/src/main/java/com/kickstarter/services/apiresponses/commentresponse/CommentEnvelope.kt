package com.kickstarter.services.apiresponses.commentresponse

import com.kickstarter.models.Comment

class CommentEnvelope private constructor(
    val comments: List<Comment>?,
    val pageInfoEnvelope: PageInfoEnvelope?,
    val totalCount: Int?
) {

    data class Builder(
        var comments: List<Comment>? = null,
        var pageInfoEnvelope: PageInfoEnvelope? = null,
        var totalCount: Int? = 0
    ) {

        fun comments(comments: List<Comment>?) = apply { this.comments = comments }
        fun pageInfoEnvelope(pageInfoEnvelope: PageInfoEnvelope?) = apply { this.pageInfoEnvelope = pageInfoEnvelope }
        fun totalCount(totalCount: Int?) = apply { this.totalCount = totalCount }
        fun build() = CommentEnvelope(comments, pageInfoEnvelope, totalCount)
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(this.comments, this.pageInfoEnvelope, this.totalCount)
}
