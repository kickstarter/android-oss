package com.kickstarter.models.extensions

import com.kickstarter.models.Comment
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus

/**
 * Update the internal persisted list of comments with the successful response
 * from calling the Post Mutation
 */
fun Comment.updateCommentAfterSuccessfulPost(
    listOfComments: List<CommentCardData>
): List<CommentCardData> {

    val position = listOfComments.indexOfFirst { commentCardData ->
        (
            commentCardData.commentCardState == CommentCardStatus.TRYING_TO_POST.commentCardStatus ||
                commentCardData.commentCardState == CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus
            ) &&
            commentCardData.comment?.body() == this.body() &&
            commentCardData.comment?.author()?.id() == this.author().id()
    }

    if (position >= 0 && position < listOfComments.size) {
        return listOfComments.toMutableList().apply {
            this[position] = listOfComments[position].toBuilder()
                .commentCardState(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus)
                .comment(this@updateCommentAfterSuccessfulPost)
                .build()
        }
    }

    return listOfComments
}

/**
 * Update the internal persisted list of comments with the failed response
 * from calling the Post Mutation
 */
fun Comment.updateCommentFailedToPost(
    listOfComments: List<CommentCardData>
): List<CommentCardData> {

    val position = listOfComments.indexOfFirst { commentCardData ->
        commentCardData.commentCardState == CommentCardStatus.TRYING_TO_POST.commentCardStatus &&
            commentCardData.comment?.body() == this.body() &&
            commentCardData.comment?.author()?.id() == this.author().id()
    }

    if (position >= 0 && position < listOfComments.size) {
        return listOfComments.toMutableList().apply {
            this[position] = listOfComments[position].toBuilder()
                .commentCardState(CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus)
                .comment(this@updateCommentFailedToPost)
                .build()
        }
    }

    return listOfComments
}

/**
 * Update the internal persisted list of comments with the failed response
 * from calling the Post Mutation
 */
fun Comment.updateCanceledPledgeComment(
    listOfComments: List<CommentCardData>
): List<CommentCardData> {

    val position = listOfComments.indexOfFirst { commentCardData ->
        commentCardData.commentCardState == CommentCardStatus.CANCELED_PLEDGE_MESSAGE.commentCardStatus &&
            commentCardData.comment?.body() == this.body() &&
            commentCardData.comment?.author()?.id() == this.author().id()
    }

    if (position >= 0 && position < listOfComments.size) {
        return listOfComments.toMutableList().apply {
            this[position] = listOfComments[position].toBuilder()
                .commentCardState(CommentCardStatus.CANCELED_PLEDGE_COMMENT.commentCardStatus)
                .comment(this@updateCanceledPledgeComment)
                .build()
        }
    }

    return listOfComments
}

fun Comment.cardStatus() = when {
    this.deleted() ?: false -> CommentCardStatus.DELETED_COMMENT
    this.authorCanceledPledge() ?: false -> CommentCardStatus.CANCELED_PLEDGE_MESSAGE
    this.repliesCount() ?: 0 != 0 -> CommentCardStatus.COMMENT_WITH_REPLIES
    else -> CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS
}.commentCardStatus
