package com.kickstarter.services.mutations

import com.kickstarter.models.Comment

data class PostCommentData(
    val body: String,
    val parent: Comment?,
    val commentableId: String,
    val clientMutationId: String?
)
