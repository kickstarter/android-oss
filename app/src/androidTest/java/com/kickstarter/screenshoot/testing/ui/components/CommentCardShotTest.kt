package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import com.karumi.shot.ScreenshotTest
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.ui.views.CommentCard
import com.kickstarter.ui.views.CommentCardStatus
import org.joda.time.DateTime
import org.junit.Test

class CommentCardShotTest : ScreenshotTest {
    private val context = ApplicationProvider.getApplicationContext<KSApplication>()

    @Test
    fun commentCardScreenshotTest_COMMENT_FOR_LOGIN_BACKED_USERS() {
        var commentCard: CommentCard = (LayoutInflater.from(context).inflate(R.layout.item_comment_card, null) as ConstraintLayout)
            .findViewById(R.id.comments_card_view)

        val user = UserFactory.user()
        commentCard.setAvatarUrl(null) // -> internal network call to picasso we need to extract that.
        commentCard.setReplyButtonVisibility(true)
        commentCard.setViewRepliesVisibility(true)
        commentCard.setCommentUserName(user.name())
        commentCard.setCommentBody("Message here for the Screenshot test lets see how it behaves ....")
        commentCard.setCommentPostTime(DateTime.now().toString())

        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
        compareScreenshot(commentCard)
    }
}
