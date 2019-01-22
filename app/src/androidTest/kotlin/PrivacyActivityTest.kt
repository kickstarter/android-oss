
import android.content.Intent
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
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
