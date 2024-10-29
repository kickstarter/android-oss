package com.kickstarter.services

import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.models.Comment
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.services.transformers.commentTransformer
import rx.subjects.PublishSubject

private fun createPageInfoObject(pageFr: fragment.PageInfo?): PageInfoEnvelope {
    return PageInfoEnvelope.builder()
        .endCursor(pageFr?.endCursor() ?: "")
        .hasNextPage(pageFr?.hasNextPage() ?: false)
        .hasPreviousPage(pageFr?.hasPreviousPage() ?: false)
        .startCursor(pageFr?.startCursor() ?: "")
        .build()
}

private fun createCommentEnvelop(responseData: GetRepliesForCommentQuery.Data): CommentEnvelope {
    val replies = (responseData.commentable() as? GetRepliesForCommentQuery.AsComment)?.replies()
    val listOfComments = replies?.nodes()?.map { commentFragment ->
        commentTransformer(commentFragment.fragments().comment())
    } ?: emptyList()
    val totalCount = replies?.totalCount() ?: 0
    val pageInfo = createPageInfoObject(replies?.pageInfo()?.fragments()?.pageInfo())

    return CommentEnvelope.builder()
        .comments(listOfComments)
        .pageInfoEnvelope(pageInfo)
        .totalCount(totalCount)
        .build()
}

private fun mapGetCommentQueryResponseToComment(responseData: GetCommentQuery.Data): Comment {
    val commentFragment =
        (responseData.commentable() as? GetCommentQuery.AsComment)?.fragments()?.comment()
    return commentTransformer(commentFragment)
}

private fun <T : Any?> handleResponse(it: T, ps: PublishSubject<T>) {
    when {
        it.isNull() -> {
            ps.onError(Exception())
        }
        else -> {
            ps.onNext(it)
            ps.onCompleted()
        }
    }
}
