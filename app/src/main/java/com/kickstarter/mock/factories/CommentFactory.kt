package com.kickstarter.mock.factories

import com.kickstarter.models.Avatar
import com.kickstarter.models.Comment
import com.kickstarter.models.User
import com.kickstarter.ui.data.CommentCardData
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

        fun liveComment(comment: String = "Some Comment", createdAt: DateTime): Comment {
            return Comment.builder()
                .body(comment)
                .parentId(-1)
                .authorBadges(listOf())
                .createdAt(createdAt)
                .cursor("")
                .deleted(false)
                .id(-1)
                .repliesCount(0)
                .author(
                    UserFactory.user()
                        .toBuilder()
                        .id(1)
                        .avatar(AvatarFactory.avatar())
                        .build()
                )
                .build()
        }

        fun liveCommentCardData(comment: String = "Some Comment", createdAt: DateTime, currentUser: User): CommentCardData {
            return CommentCardData(
                    Comment.builder()
                            .body(comment)
                            .parentId(-1)
                            .authorBadges(listOf())
                            .createdAt(createdAt)
                            .cursor("")
                            .deleted(false)
                            .id(-1)
                            .repliesCount(0)
                            .author(currentUser)
                            .build(),
                0,
                ProjectFactory.project().toBuilder().creator(UserFactory.creator().toBuilder().id(278438049).build()).build()

            )
        }
    }
}
