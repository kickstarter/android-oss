package com.kickstarter.ui.view

import android.view.LayoutInflater
import android.view.View
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
    private lateinit var commentActionGroup: Group
    private lateinit var repliesContainer: View
    private lateinit var retryButton: AppCompatButton

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        commentCard = (LayoutInflater.from(context()).inflate(R.layout.item_comment_card, null) as ConstraintLayout)
            .findViewById(R.id.comments_card_view)
        commentBody = commentCard.findViewById(R.id.comment_body)
        commentDeletedMessageGroup = commentCard.findViewById(R.id.comment_deleted_message_group)
        commentActionGroup = commentCard.findViewById(R.id.comment_action_group)
        retryButton = commentCard.findViewById(R.id.retry_button)
        repliesContainer = commentCard.findViewById(R.id.replies)
    }

    @Test
    fun testDeleteCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.DELETED_COMMENT)
        assertFalse(commentBody.isVisible)
        assertFalse(retryButton.isVisible)
        assertFalse(commentActionGroup.isVisible)
        assertTrue(commentDeletedMessageGroup.isVisible)
    }

    @Test
    fun testFailedSendCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.FAILED_TO_SEND_COMMENT)
        assertTrue(commentBody.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertTrue(commentActionGroup.isVisible)
        assertTrue(retryButton.isVisible)
    }

    @Test
    fun testCommentWithoutReplyStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITHOUT_REPLIES)
        assertTrue(commentBody.isVisible)
        assertTrue(commentActionGroup.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
    }

    @Test
    fun testCommentWithReplyStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITH_REPLIES)
        assertTrue(commentBody.isVisible)
        assertTrue(commentActionGroup.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
    }

    @Test
    fun setCommentActionGroupVisibility_whenFalse_setToInvisible() {
        commentCard.setCommentActionGroupVisibility(false)
        assertTrue(commentBody.isVisible)
        assertFalse(commentActionGroup.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
    }

    @Test
    fun setCommentActionGroupVisibility_whenTrue_setToInvisible() {
        commentCard.setCommentActionGroupVisibility(true)
        assertTrue(commentBody.isVisible)
        assertTrue(commentActionGroup.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
    }

    @Test
    fun testCommentViewReplyStatus_With_Replies() {
        commentCard.setCommentReplies(1)
        assertTrue(commentBody.isVisible)
        assertTrue(commentActionGroup.isVisible)
        assertTrue(repliesContainer.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
    }

    @Test
    fun testCommentViewReplyStatus_No_Replies() {
        commentCard.setCommentReplies(0)
        assertTrue(commentBody.isVisible)
        assertTrue(commentActionGroup.isVisible)
        assertFalse(repliesContainer.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
    }
}
