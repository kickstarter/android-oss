package com.kickstarter.mock.factories

import com.kickstarter.models.Avatar
import com.kickstarter.models.Comment
import org.joda.time.DateTime

class CommentFactory {

    companion object {
        fun comment(
            avatar: Avatar = AvatarFactory.avatar(),
            name: String = "joe",
            body: String = "Some Comment",
            repliesCount: Int = 0,
            isDelete: Boolean = false,
        ): Comment {
            return Comment.builder()
                .id(1)
                .author(
                    UserFactory.user()
                        .toBuilder()
                        .id(1).name(name).avatar(avatar)
                        .build()
                )
                .parentId(1)
                .repliesCount(repliesCount)
                .cursor("")
                .authorBadges(listOf())
                .deleted(isDelete)
                .createdAt(DateTime.parse("2021-01-01T00:00:00Z"))
                .body(body)
                .build()
        }
    }
}
