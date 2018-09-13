import android.content.Intent
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.kickstarter.R
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.HelpSettingsActivity
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import utils.Events
import utils.Matchers


@RunWith(AndroidJUnit4::class)
class HelpSettingsActivityTest {

    private val events = Events()
    private val checkThat = Matchers()

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(HelpSettingsActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testContactClick() {
        events.clickOnView(R.id.contact)
        intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_CHOOSER)))
    }

    @Test
    fun testCookiePolicyClick() {
        events.clickOnView(R.id.cookie_policy)
        checkThat.nextOpenActivityIs(HelpActivity.CookiePolicy::class.java)
    }

    @Test
    fun testHelpCenterClick() {
        events.clickOnView(R.id.help_center)
        intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData("https://kickstarter.com/help")))
    }

    @Test
    fun testPrivacyPolicyClick() {
        events.clickOnView(R.id.privacy_policy)
        checkThat.nextOpenActivityIs(HelpActivity.Privacy::class.java)
    }

    @Test
    fun testTermsClick() {
        events.clickOnView(R.id.terms_of_use)
        checkThat.nextOpenActivityIs(HelpActivity.Terms::class.java)
    }
}