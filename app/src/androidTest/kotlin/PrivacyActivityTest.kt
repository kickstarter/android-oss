
import android.content.Intent
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.kickstarter.R
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.ui.activities.PrivacyActivity
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import utils.Events


@RunWith(AndroidJUnit4::class)
@SmallTest
class PrivacyActivityTest {

    private val events = Events()

    @Rule @JvmField
    val activityRule = IntentsTestRule(PrivacyActivity::class.java)

    @Test
    fun testSettingsDeleteAccountClick() {
        events.clickOnView(R.id.settings_delete_account)
        intended(Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(Secrets.Privacy.DELETE_ACCOUNT)))
    }

    @Test
    fun testSettingsRequestDataClick() {
        events.clickOnView(R.id.settings_request_data)
        intended(Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(Secrets.Privacy.REQUEST_DATA)))
    }
}
