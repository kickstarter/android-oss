
import android.content.Intent
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.kickstarter.R
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.HelpNewActivity
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import utils.Events
import utils.Matchers


@RunWith(AndroidJUnit4::class)
class HelpActivityViewModelTest {

    private val events = Events()
    private val checkThat = Matchers()

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(HelpNewActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testCookiePolicyClick() {
        events.clickOnView(R.id.cookie_policy_card)
        checkThat.nextOpenActivityIs(HelpActivity.CookiePolicy::class.java)
    }

    @Test
    fun testHelpCenterClick() {
        events.clickOnView(R.id.help_center_card)
        intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData("https://kickstarter.com/help")))
    }

    @Test
    fun testHowKSWorksClick() {
        events.clickOnView(R.id.how_kickstarter_works_card)
        checkThat.nextOpenActivityIs(HelpActivity.HowItWorks::class.java)
    }

    @Test
    fun testPrivacyPolicyClick() {
        events.clickOnView(R.id.privacy_card)
        checkThat.nextOpenActivityIs(HelpActivity.Privacy::class.java)
    }

    @Test
    fun testTermsClick() {
        events.clickOnView(R.id.terms_card)
        checkThat.nextOpenActivityIs(HelpActivity.Terms::class.java)
    }
}
