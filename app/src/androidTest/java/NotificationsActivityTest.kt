
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.kickstarter.R
import com.kickstarter.ui.activities.NotificationsActivity
import com.kickstarter.ui.activities.ProjectNotificationSettingsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class NotificationsActivityTest {

    @Rule @JvmField
    val activityRule = IntentsTestRule(NotificationsActivity::class.java)

    @Test
    fun testClickingManageProjectNotifications_startsProjectNotificationSettingsActivity() {
        onView(withId(R.id.manage_project_notifications))
                .perform(click())

        intended(hasComponent(ProjectNotificationSettingsActivity::class.java.name))
    }
}
