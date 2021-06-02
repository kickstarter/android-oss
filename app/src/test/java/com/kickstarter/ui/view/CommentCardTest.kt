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
    private lateinit var commentActionGroup: Group
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
        assertFalse(commentBody.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(commentActionGroup.isVisible)
        assertTrue(retryButton.isVisible)
    }

    @Test
    fun testCommentWithoutReplyStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
        assertTrue(commentBody.isVisible)
        assertTrue(commentActionGroup.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
    }

    @Test
    fun testCommentWithReplyStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITH_REPLAY)
        assertTrue(commentBody.isVisible)
        assertTrue(commentActionGroup.isVisible)
        assertFalse(commentDeletedMessageGroup.isVisible)
        assertFalse(retryButton.isVisible)
    }
}
