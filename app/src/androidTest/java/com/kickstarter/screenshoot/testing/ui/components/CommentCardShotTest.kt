package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.kickstarter.R
import com.kickstarter.screenshoot.testing.ScreenshotTestBase
import com.kickstarter.ui.views.CommentCard
import com.kickstarter.ui.views.CommentCardStatus
import org.junit.Before
import org.junit.Test

class CommentCardShotTest : ScreenshotTestBase() {
    private lateinit var commentCard: CommentCard
    private val testDevices = getViewConfigCombos(WRAP_CONTENT)

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        commentCard = (LayoutInflater.from(context).inflate(R.layout.item_comment_card, null) as ConstraintLayout)
            .findViewById(R.id.comments_card_view)
    }

    @Test
    fun commentCardScreenshotTest() {
        commentCard.setCommentCardStatus(CommentCardStatus.DELETED_COMMENT)
        record(commentCard, testDevices.first())
    }
}
