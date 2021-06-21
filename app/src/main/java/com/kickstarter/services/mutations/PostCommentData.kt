package com.kickstarter.services.mutations

data class PostCommentData(
    val body: String,
    val parentId: String?,
    val commentableId: String,
    val clientMutationId: String?
)
