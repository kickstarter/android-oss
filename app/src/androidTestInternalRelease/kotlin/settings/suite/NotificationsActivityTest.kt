package settings.suite
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.filters.SmallTest
import com.kickstarter.R
import com.kickstarter.ui.activities.NotificationsActivity
import com.kickstarter.ui.activities.ProjectNotificationSettingsActivity
import org.junit.Rule
import org.junit.Test
import utils.Events
import utils.Matchers

@SmallTest
class NotificationsActivityTest {

    private val events = Events()
    private val checkThat = Matchers()

    @Rule @JvmField
    val activityRule = IntentsTestRule(NotificationsActivity::class.java)

    @Test
    fun testClickingManageProjectNotifications_startsProjectNotificationSettingsActivity() {
        events.clickOnView(R.id.manage_project_notifications)

        checkThat.nextOpenActivityIs(ProjectNotificationSettingsActivity::class.java)
    }
}
