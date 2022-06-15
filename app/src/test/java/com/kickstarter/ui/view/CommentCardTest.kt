package com.kickstarter.ui.view

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.views.CommentCard
import com.kickstarter.ui.views.CommentCardStatus
import org.junit.Before
import org.junit.Test

class CommentCardTest : KSRobolectricTestCase() {
    private lateinit var commentCard: CommentCard
    private lateinit var removedMessage: AppCompatTextView
    private lateinit var commentBody: AppCompatTextView
    private lateinit var replyButton: AppCompatButton
    private lateinit var repliesButton: AppCompatTextView
    private lateinit var retryButton: View
    private lateinit var postingButton: AppCompatButton
    private lateinit var postedButton: AppCompatButton
    private lateinit var showCanceledPledgeComment: AppCompatTextView
    private lateinit var infoIcon: AppCompatImageView
    private lateinit var flaggedMessage: AppCompatTextView

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        commentCard = (LayoutInflater.from(context()).inflate(R.layout.item_comment_card, null) as ConstraintLayout)
            .findViewById(R.id.comments_card_view)
        // - Specify Feature Flag enabled
        commentCard.setCommentEnabledThreads(true)

        commentBody = commentCard.findViewById(R.id.comment_body)
        removedMessage = commentCard.findViewById(R.id.removed_message)
        flaggedMessage = commentCard.findViewById(R.id.flagged_message)
        replyButton = commentCard.findViewById(R.id.reply_button)
        retryButton = commentCard.findViewById(R.id.retry_button)
        repliesButton = commentCard.findViewById(R.id.replies)
        postingButton = commentCard.findViewById(R.id.posting_button)
        postedButton = commentCard.findViewById(R.id.posted_button)
        showCanceledPledgeComment = commentCard.findViewById(R.id.canceled_pledge_message)
        infoIcon = commentCard.findViewById(R.id.info_button)
    }

    @Test
    fun testDeleteCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.DELETED_COMMENT)
        assertTrue(commentBody.isGone)
        assertFalse(retryButton.isVisible)
        assertFalse(replyButton.isVisible)
        assertTrue(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertTrue(infoIcon.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }

    @Test
    fun testFailedSendCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.FAILED_TO_SEND_COMMENT)
        assertTrue(commentBody.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(replyButton.isVisible)
        assertTrue(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }

    @Test
    fun testCommentWithoutReplyStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
        assertTrue(commentBody.isVisible)
        assertTrue(replyButton.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun testCommentWithReplyStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITH_REPLIES)
        assertTrue(commentBody.isVisible)
        assertTrue(replyButton.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }

    @Test
    fun setCommentActionGroupVisibility_whenFalse_setToInvisible() {
        commentCard.setReplyButtonVisibility(false)
        assertTrue(commentBody.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }

    @Test
    fun setCommentActionGroupVisibility_whenTrue_setToInvisible() {
        commentCard.setReplyButtonVisibility(true)
        assertTrue(commentBody.isVisible)
        assertTrue(replyButton.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
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
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }

    @Test
    fun testCommentViewReplyStatus_No_Replies() {
        commentCard.setCommentReplies(0)
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)

        assertTrue(commentBody.isVisible)
        assertTrue(replyButton.isVisible)
        assertFalse(repliesButton.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }

    @Test
    fun testVisibilityFeatureFlagOff() {
        commentCard.setCommentEnabledThreads(false)

        commentCard.setCommentReplies(10)
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITH_REPLIES)

        assertTrue(commentBody.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(repliesButton.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }
    @Test
    fun testTryingToPostCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.RE_TRYING_TO_POST)
        assertTrue(commentBody.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertTrue(postingButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }

    @Test
    fun testSuccessfullyPostCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY)
        assertTrue(commentBody.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(retryButton.isVisible)
        assertTrue(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
        assertFalse(postingButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }

    @Test
    fun testShowCanceledPledgeCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.CANCELED_PLEDGE_COMMENT)
        assertTrue(commentBody.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
    }

    @Test
    fun testshowCanceledPledgeMessageStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.CANCELED_PLEDGE_MESSAGE)
        assertFalse(commentBody.isVisible)
        assertTrue(showCanceledPledgeComment.isVisible)
        assertFalse(removedMessage.isVisible)
        assertFalse(flaggedMessage.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(infoIcon.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }

    @Test
    fun testShowFlaggedMessageStatus(){
        commentCard.setCommentCardStatus(CommentCardStatus.FLAGGED_COMMENT)
        assertFalse(commentBody.isVisible)
        assertFalse(showCanceledPledgeComment.isVisible)
        assertFalse(removedMessage.isVisible)
        assertTrue(flaggedMessage.isVisible)
        assertTrue(infoIcon.isVisible)
        assertFalse(replyButton.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(postedButton.isVisible)
        assertFalse(postingButton.isVisible)
    }
}
