package com.kickstarter.services.mutations

import com.kickstarter.models.Comment
import org.joda.time.DateTime

data class PostCommentData(
    val body: String,
    val parent: Comment?,
    val commentableId: String,
    val clientMutationId: String?
)
