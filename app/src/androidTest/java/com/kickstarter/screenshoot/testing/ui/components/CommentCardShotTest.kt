package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.screenshoot.testing.InstrumentedApp
import com.kickstarter.ui.views.CommentCard
import com.kickstarter.ui.views.CommentCardStatus
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class CommentCardShotTest : ScreenshotTest {

    lateinit var commentCard: CommentCard
    lateinit var component: ApplicationComponent
    val relativeTime = DateTime.now().minusMinutes(5)
    lateinit var commentPostedRelativeTime:DateTime

    @Before
    fun setup() {
        //val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as InstrumentedApp
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as KSApplication
        // - Test Dagger component for future mock objects
        component = app.component()

        commentCard = (LayoutInflater.from(getInstrumentation().targetContext).inflate(R.layout.item_comment_card, null) as ConstraintLayout)
            .findViewById(R.id.comments_card_view)

        val user = UserFactory.user()
        commentCard.setAvatarUrl(null) // -> internal network call to picasso we need to wrap Picasso into our own client to be able to mock on testing.
        commentCard.setCommentUserName(user.name())
        commentCard.setCommentBody("Message here for the Screenshot test lets see how it behaves ...")
        val commentPostedRelativeTime = DateTimeUtils.relative(getInstrumentation().targetContext, component.environment().ksString(), relativeTime)
        commentCard.setCommentPostTime(commentPostedRelativeTime)
    }

    @Test
    fun commentCardScreenshotTest_COMMENT_FOR_LOGIN_BACKED_USERS() {
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
        compareScreenshot(commentCard)
    }

    @Test
    fun commentCardScreenshotTest_COMMENT_WITH_REPLIES() {
        commentCard.setCommentEnabledThreads(true)
        commentCard.setCommentReplies(3)
        commentCard.setCommentCardStatus(CommentCardStatus.COMMENT_WITH_REPLIES)
        compareScreenshot(commentCard)
    }

    @Test
    fun commentCardScreenshotTest_FAILED_TO_SEND_COMMENT() {
        commentCard.setCommentCardStatus(CommentCardStatus.FAILED_TO_SEND_COMMENT)
        compareScreenshot(commentCard)
    }

    @Test
    fun commentCardScreenshotTest_DELETED_COMMENT() {
        commentCard.setCommentCardStatus(CommentCardStatus.DELETED_COMMENT)
        compareScreenshot(commentCard)
    }

    @Test
    fun commentCardScreenshotTest_RE_TRYING_TO_POST() {
        commentCard.setCommentCardStatus(CommentCardStatus.RE_TRYING_TO_POST)
        compareScreenshot(commentCard)
    }
}
