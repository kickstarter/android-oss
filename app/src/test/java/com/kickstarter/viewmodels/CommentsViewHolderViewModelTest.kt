package com.kickstarter
    .viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Comment
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import org.joda.time.DateTime
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class CommentsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CommentsViewHolderViewModel.ViewModel

    private val commentCardStatus = TestSubscriber<CommentCardStatus>()
    private val commentAuthorName = TestSubscriber<String>()
    private val commentAuthorAvatarUrl = TestSubscriber<String>()
    private val commentMessageBody = TestSubscriber<String>()
    private val commentPostTime = TestSubscriber<DateTime>()
    private val isActionGroupVisible = TestSubscriber<Boolean>()
    private val openCommentGuideLines = TestSubscriber<Comment>()
    private val retrySendComment = TestSubscriber<Comment>()
    private val replyToComment = TestSubscriber<Comment>()
    private val flagComment = TestSubscriber<Comment>()
    private val repliesCount = TestSubscriber<Int>()

    private val createdAt = DateTime.now()
    private val currentUser = UserFactory.user().toBuilder().id(1).avatar(
        AvatarFactory.avatar()
    ).name("joe").build()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = CommentsViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.commentCardStatus().subscribe(this.commentCardStatus)
        this.vm.outputs.commentAuthorName().subscribe(this.commentAuthorName)
        this.vm.outputs.commentAuthorAvatarUrl().subscribe(this.commentAuthorAvatarUrl)
        this.vm.outputs.commentMessageBody().subscribe(this.commentMessageBody)
        this.vm.outputs.commentPostTime().subscribe(this.commentPostTime)
        this.vm.outputs.isCommentActionGroupVisible().subscribe(this.isActionGroupVisible)
        this.vm.outputs.openCommentGuideLines().subscribe(this.openCommentGuideLines)
        this.vm.outputs.retrySendComment().subscribe(this.retrySendComment)
        this.vm.outputs.replyToComment().subscribe(this.replyToComment)
        this.vm.outputs.flagComment().subscribe(this.flagComment)
        this.vm.outputs.commentRepliesCount().subscribe(this.repliesCount)
    }

    @Test
    fun testOpenCommentGuideLinesClicked() {
        setUpEnvironment(environment())
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)

        this.vm.inputs.configureWith(commentCardData)
        this.vm.inputs.onCommentGuideLinesClicked()

        this.openCommentGuideLines.assertValue(commentCardData.comment)
    }

    @Test
    fun testReplyToCommentClicked() {
        setUpEnvironment(environment())
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)

        this.vm.inputs.configureWith(commentCardData)
        this.vm.inputs.onReplyButtonClicked()

        this.replyToComment.assertValue(commentCardData.comment)
    }

    @Test
    fun testRetrySendCommentClicked() {
        setUpEnvironment(environment())
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)

        this.vm.inputs.configureWith(commentCardData)
        this.vm.inputs.onRetryViewClicked()

        this.retrySendComment.assertValue(commentCardData.comment)
    }

    @Test
    fun testFlagCommentClicked() {
        setUpEnvironment(environment())
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)
        this.vm.inputs.configureWith(commentCardData)
        this.vm.inputs.onFlagButtonClicked()

        this.flagComment.assertValue(commentCardData.comment)
    }

    @Test
    fun testUserAvatarUrl() {
        setUpEnvironment(environment())
        val userAvatar = AvatarFactory.avatar()
        val currentUser = UserFactory.user().toBuilder().id(111).avatar(
            userAvatar
        ).build()
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)
        this.vm.inputs.configureWith(commentCardData)

        this.commentAuthorAvatarUrl.assertValue(userAvatar.medium())
        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
    }

    @Test
    fun testCommentAuthorName() {
        setUpEnvironment(environment())
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)

        this.vm.inputs.configureWith(commentCardData)

        this.commentAuthorName.assertValue(commentCardData.comment?.author()?.name())
        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
    }

    @Test
    fun testCommentMessageBody() {
        setUpEnvironment(environment())
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)

        this.vm.inputs.configureWith(commentCardData)

        this.commentMessageBody.assertValue(commentCardData.comment?.body())
        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
    }

    @Test
    fun testDeletedComment() {
        setUpEnvironment(environment())
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser, isDelete = true)

        this.vm.inputs.configureWith(commentCardData)

        this.commentCardStatus.assertValue(CommentCardStatus.DELETED_COMMENT)
    }

    @Test
    fun testCommentPostTime() {
        setUpEnvironment(environment())
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser, isDelete = true)

        this.vm.inputs.configureWith(commentCardData)

        this.commentPostTime.assertValue(commentCardData.comment?.createdAt())
    }

    @Test
    fun testNoReplyCountForBindingCardStatus() {
        setUpEnvironment(environment())
        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)

        this.vm.inputs.configureWith(commentCardData)

        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
    }

    @Test
    fun testReplyCountForBindingCardStatus() {
        setUpEnvironment(environment())

        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser, repliesCount = 20)
        this.vm.inputs.configureWith(commentCardData)
        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_WITH_REPLIES)
    }

    @Test
    fun testCommentActionGroupVisibility_whenUserLoggedInAndProjectBacked_shouldSendTrue() {
        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build())
        val comment = CommentFactory.comment()
        val commentCardData = CommentCardData.builder().comment(comment).project(ProjectFactory.backedProject()).build()
        this.vm.inputs.configureWith(commentCardData)
        this.isActionGroupVisible.assertValue(true)
    }

    @Test
    fun testCommentActionGroupVisibility_whenUserLoggedInAndProjectNotBacked_shouldSendFalse() {
        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build())
        val comment = CommentFactory.comment()
        val commentCardData = CommentCardData.builder().comment(comment).project(ProjectFactory.project()).build()
        this.vm.inputs.configureWith(commentCardData)
        this.isActionGroupVisible.assertValue(false)
    }

    @Test
    fun testCommentActionGroupVisibility_whenUserNotLoggedIn_shouldSendFalse() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment()
        val commentCardData = CommentCardData.builder().comment(comment).project(ProjectFactory.project()).build()
        this.vm.inputs.configureWith(commentCardData)
        this.isActionGroupVisible.assertValue(false)
    }

    @Test
    fun testCommentActionGroupVisibility_whenProjectNotBackedAndUserIsCreator_shouldSendTrue() {
        val user = UserFactory.creator().toBuilder().id(2).build()
        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(user)).build())
        val comment = CommentFactory.comment()
        val commentCardData = CommentCardData.builder().comment(comment).project(ProjectFactory.project().toBuilder().creator(user).build()).build()
        this.vm.inputs.configureWith(commentCardData)
        this.isActionGroupVisible.assertValue(true)
    }

    @Test
    fun testSetRepliesCount() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment(repliesCount = 1)
        val commentData = CommentCardData.builder().comment(comment).project(ProjectFactory.project()).build()
        this.vm.inputs.configureWith(commentData)
        this.repliesCount.assertValue(comment.repliesCount())
    }

    @Test
    fun testRetrySendCommentErrorClicked() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun createComment(comment: PostCommentData): Observable<Comment> {
                return Observable.error(Throwable())
            }
        }).build()
        val currentUser = UserFactory.user().toBuilder().id(1).build()
        env.toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        setUpEnvironment(env)

        val comment = CommentFactory.comment()
        val commentCardData = CommentCardData.builder().comment(comment).project(ProjectFactory.initialProject()).build()
        this.vm.inputs.configureWith(commentCardData)
        this.vm.inputs.onRetryViewClicked()

        this.retrySendComment.assertValue(comment)
        this.commentCardStatus.assertValues(
            CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS,
            CommentCardStatus.TRYING_TO_POST,
            CommentCardStatus.FAILED_TO_SEND_COMMENT
        )

        this.vm.inputs.onRetryViewClicked()

        this.commentCardStatus.assertValues(
            CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS,
            CommentCardStatus.TRYING_TO_POST,
            CommentCardStatus.FAILED_TO_SEND_COMMENT,
            CommentCardStatus.TRYING_TO_POST,
            CommentCardStatus.FAILED_TO_SEND_COMMENT
        )
    }
}
