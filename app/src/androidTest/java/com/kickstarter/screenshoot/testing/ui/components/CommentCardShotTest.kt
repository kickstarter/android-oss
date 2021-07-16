package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.karumi.shot.ScreenshotTest
import com.kickstarter.R
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.screenshoot.testing.InstrumentedApp
import com.kickstarter.ui.views.CommentCard
import com.kickstarter.ui.views.CommentCardStatus
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class CommentCardShotTest : ScreenshotTest {

    @Before
    fun setup() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as InstrumentedApp
        // - Dagger component
        val component = app.component()
    }

    @Test
    fun commentCardScreenshotTest_COMMENT_FOR_LOGIN_BACKED_USERS() {
        var commentCard: CommentCard = (LayoutInflater.from(getInstrumentation().targetContext).inflate(R.layout.item_comment_card, null) as ConstraintLayout)
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
