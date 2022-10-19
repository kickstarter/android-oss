package com.kickstarter.libs.utils.extensions

import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.models.extensions.cardStatus
import com.kickstarter.ui.data.CommentCardData

fun List<Comment>.toCommentCardList(project: Project?, user: User?): List<CommentCardData> = this.map { comment: Comment ->
    CommentCardData.builder().comment(comment).commentCardState(comment.cardStatus(user)).project(project).build()
}
