package com.kickstarter.ui.view

import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentReplyCard
import org.junit.Before
import org.junit.Test

class CommentReplyCardTest : KSRobolectricTestCase() {
    private lateinit var commentCard: CommentReplyCard
    private lateinit var commentBody: AppCompatTextView
    private lateinit var retryButton: AppCompatButton

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        commentCard = (LayoutInflater.from(context()).inflate(R.layout.item_comment_reply, null) as ConstraintLayout)
            .findViewById(R.id.comment_reply_card_view)
        commentBody = commentCard.findViewById(R.id.comment_body)
        retryButton = commentCard.findViewById(R.id.retry_button)
    }

    @Test
    fun testFailedSendCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.FAILED_TO_SEND_COMMENT)
        assertTrue(commentBody.isVisible)
        assertTrue(retryButton.isVisible)
    }

    @Test
    fun testSuccessSendCommentStatus() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITH_REPLAY)
        assertTrue(commentBody.isVisible)
        assertFalse(retryButton.isVisible)
    }

}