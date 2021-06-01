package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Comment
import com.kickstarter.ui.views.CommentCardStatus
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class CommentsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CommentsViewHolderViewModel.ViewModel

    private val commentCardStatus = TestSubscriber<CommentCardStatus>()
    private val commentAuthorName = TestSubscriber<String>()
    private val commentAuthorAvatarUrl = TestSubscriber<String>()
    private val commentMessageBody = TestSubscriber<String>()
    private val commentPostTime = TestSubscriber<DateTime>()
    private val openCommentGuideLines = TestSubscriber<Comment>()
    private val retrySendComment = TestSubscriber<Comment>()
    private val replyToComment = TestSubscriber<Comment>()
    private val flagComment = TestSubscriber<Comment>()
    private val repliesCount = TestSubscriber<Int>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = CommentsViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.commentCardStatus().subscribe(this.commentCardStatus)
        this.vm.outputs.commentAuthorName().subscribe(this.commentAuthorName)
        this.vm.outputs.commentAuthorAvatarUrl().subscribe(this.commentAuthorAvatarUrl)
        this.vm.outputs.commentMessageBody().subscribe(this.commentMessageBody)
        this.vm.outputs.commentPostTime().subscribe(this.commentPostTime)
        this.vm.outputs.openCommentGuideLines().subscribe(this.openCommentGuideLines)
        this.vm.outputs.retrySendComment().subscribe(this.retrySendComment)
        this.vm.outputs.replyToComment().subscribe(this.replyToComment)
        this.vm.outputs.flagComment().subscribe(this.flagComment)
        this.vm.outputs.commentRepliesCount().subscribe(this.repliesCount)
    }

    @Test
    fun testOpenCommentGuideLinesClicked() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)
        this.vm.inputs.onCommentGuideLinesClicked()

        this.openCommentGuideLines.assertValue(comment)
    }

    @Test
    fun testReplyToCommentClicked() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)
        this.vm.inputs.onReplyButtonClicked()

        this.replyToComment.assertValue(comment)
    }

    @Test
    fun testRetrySendCommentClicked() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)
        this.vm.inputs.onRetryViewClicked()

        this.retrySendComment.assertValue(comment)
    }

    @Test
    fun testFlagCommentClicked() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)
        this.vm.inputs.onFlagButtonClicked()

        this.flagComment.assertValue(comment)
    }

    @Test
    fun testUserAvatarUrl() {
        setUpEnvironment(environment())
        val userAvatar = AvatarFactory.avatar()
        val currentUser = UserFactory.user().toBuilder().id(111).avatar(
            userAvatar
        ).build()
        this.vm.inputs.configureWith(CommentFactory.comment(currentUser.avatar()))

        this.commentAuthorAvatarUrl.assertValue(userAvatar.medium())
        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_WITHOUT_REPLAY)
    }

    @Test
    fun testCommentAuthorName() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)

        this.commentAuthorName.assertValue(comment.author().name())
        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_WITHOUT_REPLAY)
    }

    @Test
    fun testCommentMessageBody() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)

        this.commentMessageBody.assertValue(comment.body())
        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_WITHOUT_REPLAY)
    }

    @Test
    fun testDeletedComment() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment(isDelete = true)
        this.vm.inputs.configureWith(comment)

        this.commentCardStatus.assertValue(CommentCardStatus.DELETED_COMMENT)
    }

    @Test
    fun testCommentPostTime() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)

        this.commentPostTime.assertValue(comment.createdAt())
    }

    @Test
    fun testNoReplayCountForBindingCardStatus() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)
        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_WITHOUT_REPLAY)
    }

    @Test
    fun testReplayCountForBindingCardStatus() {
        setUpEnvironment(environment())
        val updatedComment = CommentFactory.comment(repliesCount = 10)
        this.vm.inputs.configureWith(updatedComment)
        this.commentCardStatus.assertValue(CommentCardStatus.COMMENT_WITH_REPLAY)
    }

    @Test
    fun testSetRepliesCount() {
        setUpEnvironment(environment())
        val comment = CommentFactory.comment(repliesCount = 1)
        this.vm.inputs.configureWith(comment)
        this.repliesCount.assertValue(comment.repliesCount())
    }
}
