package com.kickstarter.mock.factories

import com.kickstarter.models.CommentThread
import org.joda.time.DateTime

class CommentThreadFactory {

    companion object {
        fun comment(): CommentThread {
            return CommentThread.builder()
                .id(1)
                .author(
                    UserFactory.user()
                        .toBuilder()
                        .id(1)
                        .build()
                )
                .parentId(1)
                .replies(listOf())
                .cursor("")
                .authorBadges(listOf())
                .deleted(false)
                .createdAt(DateTime.parse("2021-01-01T00:00:00Z"))
                .body("Some Comment")
                .build()
        }
    }
}
