package com.kickstarter.mock.factories

import com.kickstarter.models.Avatar
import com.kickstarter.models.Comment
import com.kickstarter.models.User
import com.kickstarter.ui.data.CommentCardData
import org.joda.time.DateTime

class CommentFactory {

    companion object {

        // - Comment created on the app that has not yet being send to the backend
        fun commentToPostWithUser(user: User): Comment {
            return Comment.builder()
                .id(-1)
                .author(user)
                .body("Some text here")
                .parentId(-1)
                .repliesCount(0)
                .cursor("")
                .authorBadges(listOf())
                .deleted(false)
                .authorCanceledPledge(false)
                .createdAt(DateTime.now())
                .build()
        }

        fun commentWithCanceledPledgeAuthor(user: User): Comment {
            return Comment.builder()
                .id(-1)
                .author(user)
                .body("Some text here")
                .parentId(-1)
                .repliesCount(0)
                .cursor("")
                .authorBadges(listOf())
                .deleted(false)
                .authorCanceledPledge(true)
                .createdAt(DateTime.now())
                .build()
        }

        fun commentFromCurrentUser(user: User, authorBadges: List<String>): Comment {
            return Comment.builder()
                .id(1)
                .author(user)
                .body("Some text here")
                .parentId(1)
                .repliesCount(0)
                .cursor("")
                .authorBadges(authorBadges)
                .authorCanceledPledge(false)
                .deleted(false)
                .createdAt(DateTime.now())
                .build()
        }

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
                        .id(1L).name(name).avatar(avatar)
                        .build()
                )
                .parentId(-1)
                .repliesCount(repliesCount)
                .cursor("")
                .authorBadges(listOf())
                .authorCanceledPledge(false)
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
                .authorCanceledPledge(false)
                .id(-1)
                .repliesCount(0)
                .author(
                    UserFactory.user()
                        .toBuilder()
                        .id(1L)
                        .avatar(AvatarFactory.avatar())
                        .build()
                )
                .build()
        }

        fun reply(comment: String = "Some Comment", createdAt: DateTime): Comment {
            return Comment.builder()
                .body(comment)
                .parentId(1)
                .authorBadges(listOf())
                .createdAt(createdAt)
                .cursor("")
                .deleted(false)
                .id(-1L)
                .repliesCount(0)
                .authorCanceledPledge(false)
                .author(
                    UserFactory.user()
                        .toBuilder()
                        .id(1L)
                        .avatar(AvatarFactory.avatar())
                        .build()
                )
                .build()
        }

        fun liveCommentCardData(comment: String = "Some Comment", createdAt: DateTime, currentUser: User, isDelete: Boolean = false, hasFlaggings: Boolean = false, sustained: Boolean = false, repliesCount: Int = 0): CommentCardData {
            val project = ProjectFactory.project().toBuilder().creator(UserFactory.creator().toBuilder().id(278438049L).build()).build()
            return CommentCardData(
                Comment.builder()
                    .body(comment)
                    .parentId(-1)
                    .authorBadges(listOf())
                    .createdAt(createdAt)
                    .cursor("")
                    .deleted(isDelete)
                    .hasFlaggings(hasFlaggings)
                    .sustained(sustained)
                    .id(-1)
                    .repliesCount(repliesCount)
                    .author(currentUser)
                    .authorCanceledPledge(false)
                    .build(),
                0,
                project.id().toString(),
                project

            )
        }

        fun liveCanceledPledgeCommentCardData(comment: String = "Some Comment", createdAt: DateTime, currentUser: User, isDelete: Boolean = false, repliesCount: Int = 0): CommentCardData {
            val project = ProjectFactory.project().toBuilder().creator(UserFactory.creator().toBuilder().id(278438049L).build()).build()
            return CommentCardData(
                Comment.builder()
                    .body(comment)
                    .parentId(-1)
                    .authorBadges(listOf())
                    .createdAt(createdAt)
                    .cursor("")
                    .deleted(isDelete)
                    .id(-1)
                    .authorCanceledPledge(true)
                    .repliesCount(repliesCount)
                    .author(currentUser)
                    .build(),
                0,
                project.id().toString(),
                project

            )
        }
    }
}
