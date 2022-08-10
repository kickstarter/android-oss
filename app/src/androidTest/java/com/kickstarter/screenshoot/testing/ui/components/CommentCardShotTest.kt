package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
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

    @Before
    fun setup() {
        // - Test Application
        val app = getInstrumentation().targetContext.applicationContext as InstrumentedApp
        // - Test Dagger component for injecting on environment Mock Objects
        component = app.component()

        commentCard = (LayoutInflater.from(getInstrumentation().targetContext).inflate(R.layout.item_comment_card, null) as ConstraintLayout)
            .findViewById(R.id.comments_card_view)

        val user = UserFactory.user()
        commentCard.setAvatarUrl(null) // -> internal network call to picasso we need to wrap Picasso into our own client to be able to mock on testing.
        commentCard.setCommentUserName(user.name())
        commentCard.setCommentBody("Message here for the Screenshot test lets see how it behaves ...")

        val relativeTime = DateTime.now().minusMinutes(5)
        val commentPostedRelativeTime = DateTimeUtils.relative(getInstrumentation().targetContext, requireNotNull(component.environment().ksString()), relativeTime)
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

    @Test
    fun commentCardScreenshotTest_CANCELED_PLEDGE_MESSAGE() {
        commentCard.setCommentCardStatus(CommentCardStatus.CANCELED_PLEDGE_MESSAGE)
        commentCard.setCancelPledgeMessage(

            getInstrumentation().targetContext.getString(R.string.This_person_canceled_their_pledge)

        )
        compareScreenshot(commentCard)
    }

    @Test
    fun commentCardScreenshotTest_FLAGGED_MESSAGE() {
        commentCard.setCommentCardStatus(CommentCardStatus.FLAGGED_COMMENT)
        commentCard.setFlaggedMessage(
            getInstrumentation().targetContext.getString(R.string.This_comment_is_under_review_for_potentially_violating_kickstarters_community_guidelines)
        )
        compareScreenshot(commentCard)
    }
}
