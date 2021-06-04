package com.kickstarter.ui.view

import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.views.CommentCard
import com.kickstarter.ui.views.CommentCardStatus
import org.junit.Before
import org.junit.Test

class CommentCardTest : KSRobolectricTestCase() {
    private lateinit var commentCard: CommentCard
    private lateinit var commentDeletedMessageGroup: Group
    private lateinit var commentBody: AppCompatTextView
    private lateinit var replyButton: AppCompatButton
    private lateinit var repliesButton: AppCompatTextView
    private lateinit var retryButton: AppCompatButton
    private lateinit var postingButton: AppCompatButton
    private lateinit var postedButton: AppCompatButton

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        commentCard = (LayoutInflater.from(context()).inflate(R.layout.item_comment_card, null) as ConstraintLayout)
            .findViewById(R.id.comments_card_view)
        // - Specify Feature Flag enabled
        commentCard.setCommentEnabledThreads(true)

        commentBody = commentCard.findViewById(R.id.comment_body)
        commentDeletedMessageGroup = commentCard.findViewById(R.id.comment_deleted_message_group)
        replyButton = commentCard.findViewById(R.id.reply_button)
        retryButton = commentCard.findViewById(R.id.retry_button)
        repliesButton = commentCard.findViewById(R.id.replies)
        postingButton = commentCard.findViewById(R.id.posting_button)
        postedButton = commentCard.findViewById(R.id.posted_button)
    }

    @Test
    fun testDeleteCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.DELETED_COMMENT)
        assertFalse(commentBody.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(replyButton.isVisible)
        assertTrue(commentDeletedMessageGroup.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun testFailedSendCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.FAILED_TO_SEND_COMMENT)
        assertTrue(commentBody.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertTrue(replyButton.isVisible)
        assertTrue(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun testCommentWithoutReplyStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
        assertTrue(commentBody.isVisible)
        assertTrue(replyButton.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun testCommentWithReplyStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITH_REPLIES)
        assertTrue(commentBody.isVisible)
        assertTrue(replyButton.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun setCommentActionGroupVisibility_whenFalse_setToInvisible() {
        commentCard.setReplyButtonVisibility(false)
        assertTrue(commentBody.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun setCommentActionGroupVisibility_whenTrue_setToInvisible() {
        commentCard.setReplyButtonVisibility(true)
        assertTrue(commentBody.isVisible)
        assertTrue(replyButton.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun testCommentViewReplyStatus_With_Replies() {
        commentCard.setCommentReplies(1)
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITH_REPLIES)

        assertTrue(commentBody.isVisible)
        assertTrue(replyButton.isVisible)
        assertTrue(repliesButton.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun testCommentViewReplyStatus_No_Replies() {
        commentCard.setCommentReplies(0)
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)

        assertTrue(commentBody.isVisible)
        assertTrue(replyButton.isVisible)
        assertFalse(repliesButton.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun testVisibilityFeatureFlagOff() {
        commentCard.setCommentEnabledThreads(false)

        commentCard.setCommentReplies(10)
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITH_REPLIES)

        assertTrue(commentBody.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(repliesButton.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
    }
    @Test
    fun testTryingToPostCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.RE_TRYING_TO_POST)
        assertTrue(commentBody.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertTrue(postingButton.isVisible)
    }

    @Test
    fun testSuccessfullyPostCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY)
        assertTrue(commentBody.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(retryButton.isVisible)
        assertTrue(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }
}
