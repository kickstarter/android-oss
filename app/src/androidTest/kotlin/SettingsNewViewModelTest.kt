import android.content.Intent
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasAction
import android.support.test.espresso.intent.matcher.IntentMatchers.hasData
import android.support.test.rule.ActivityTestRule
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.ui.activities.*
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import utils.Events
import utils.Matchers

class SettingsNewViewModelTest {

    private val events = Events()
    private val checkThat = Matchers()

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(SettingsActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testHelpClick() {
        events.clickOnView(R.id.help_card)
        checkThat.nextOpenActivityIs(HelpNewActivity::class.java)
    }

    @Test
    fun testLogoutClick() {
        events.clickOnView(R.id.log_out_card)
        checkThat.nextOpenActivityIs(DiscoveryActivity::class.java)
    }

    @Test
    fun testNewsletterClick() {
        events.clickOnView(R.id.newsletters_card)
        checkThat.nextOpenActivityIs(NewsletterActivity::class.java)
    }

    @Test
    fun testNotificationsClick() {
        events.clickOnView(R.id.notification_card)
        checkThat.nextOpenActivityIs(NotificationsActivity::class.java)
    }

    @Test
    fun testPrivacyClick() {
        events.clickOnView(R.id.privacy_card)
        checkThat.nextOpenActivityIs(PrivacyActivity::class.java)
    }

    @Test
    fun testRateUsClick() {
        events.clickOnView(R.id.rate_us_card)
        intended(allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("market://details?id=${BuildConfig.APPLICATION_ID}")))
    }
}
