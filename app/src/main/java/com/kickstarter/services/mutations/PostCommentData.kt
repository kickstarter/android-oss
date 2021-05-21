package com.kickstarter.services.mutations

import com.kickstarter.models.Project

data class PostCommentData(
    val body: String,
    val parentId: String?,
    val project: Project,
    val clientMutationId: String?
)
