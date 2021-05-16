package com.kickstarter.models

import com.kickstarter.mock.factories.CommentThreadFactory
import com.kickstarter.mock.factories.UserFactory
import junit.framework.TestCase

class CommentThreadTest : TestCase() {

    fun testEquals_whenSecondCommentThreadDifferentId_returnFalse() {
        val commentA = CommentThreadFactory.comment()
        val commentB = CommentThreadFactory.comment().toBuilder().id(2).build()

        assertFalse(commentA == commentB)
    }

    fun testEquals_whenSecondCommentThreadDifferentUser_returnFalse() {
        val commentA = CommentThreadFactory.comment()
        val commentB = CommentThreadFactory.comment().toBuilder().author(
            UserFactory.user()
                .toBuilder()
                .id(2)
                .build()
        ).build()

        assertFalse(commentA == commentB)
    }

    fun testEquals_whenSecondCommentThreadDifferentParentId_returnFalse() {
        val commentA = CommentThreadFactory.comment()
        val commentB = CommentThreadFactory.comment()
            .toBuilder()
            .parentId(2)
            .build()

        assertFalse(commentA == commentB)
    }

    fun testEquals_whenCommentThreadEquals_returnTrue() {
        val commentA = CommentThreadFactory.comment()
        val commentB = CommentThreadFactory.comment()

        assertTrue(commentA == commentB)
    }
}
