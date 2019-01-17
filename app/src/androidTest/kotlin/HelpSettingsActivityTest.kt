
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.kickstarter.R
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.HelpSettingsActivity
import junit.framework.Assert.*
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
        val uri = Uri.parse("https://kickstarter.com/privacy")
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(activityRule.activity, uri)
        val intent = customTabsIntent.intent
        assertNotNull(intent)
        assertNull(customTabsIntent.startAnimationBundle)

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertTrue(intent.hasExtra(CustomTabsIntent.EXTRA_SESSION))
        if (Build.VERSION.SDK_INT >= 18) {
            assertNull(intent.extras.getBinder(CustomTabsIntent.EXTRA_SESSION))
        }
        assertNull(intent.component)

        intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData("https://kickstarter.com/privacy")))
    }

    @Test
    fun testTermsClick() {
        events.clickOnView(R.id.terms_of_use)
        checkThat.nextOpenActivityIs(HelpActivity.Terms::class.java)
    }

    @Test
    fun testToolbarColor() {
        val color = Color.RED
        val intent = CustomTabsIntent.Builder().setToolbarColor(color).build().intent
        assertTrue(intent.hasExtra(CustomTabsIntent.EXTRA_TOOLBAR_COLOR))
        assertEquals(color, intent.getIntExtra(CustomTabsIntent.EXTRA_TOOLBAR_COLOR, 0))
    }
}
