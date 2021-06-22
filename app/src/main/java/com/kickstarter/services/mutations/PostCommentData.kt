package com.kickstarter.services.mutations

import com.kickstarter.models.Comment
import org.joda.time.DateTime

data class PostCommentData(
    val body: String,
    val parent: Comment?,
    val commentableId: String,
    val clientMutationId: String?
){
//    fun parent(): Comment? = this.parentId?.let { it1 ->
//        Comment.builder().id(it1)
//            .deleted(false)
//            .createdAt(DateTime.now())
//            .body("")
//            .authorBadges(emptyList())
//            .cursor("")
//            .author(null)
//            .repliesCount(0)
//            .build()
//    }
}
