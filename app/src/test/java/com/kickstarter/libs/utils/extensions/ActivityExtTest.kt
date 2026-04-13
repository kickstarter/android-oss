package com.kickstarter.libs.utils.extensions

import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.PreLaunchProjectPageActivity
import com.kickstarter.ui.activities.ProjectPageActivity
import com.kickstarter.ui.extensions.startPreLaunchProjectActivity
import com.kickstarter.ui.extensions.startProjectActivity
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

class ActivityExtTest : KSRobolectricTestCase() {

    @Test
    fun testStartPreLaunchProjectActivity_verifyIntentAndExtras() {
        val activity = Robolectric.buildActivity(PreLaunchProjectPageActivity::class.java).setup().get()
        val project = ProjectFactory.project()
        val refTag = RefTag.search()

        activity.startPreLaunchProjectActivity(
            project = project,
            refTag = refTag,
            previousScreen = "search"
        )

        // Get the intent that was actually started
        val shadowActivity = shadowOf(activity)
        val intent = shadowActivity.nextStartedActivity

        // Assertions
        assertNotNull(intent)
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.PreLaunchProjectPageActivity")
        assertEquals(intent.getStringExtra(IntentKey.PREVIOUS_SCREEN), "search")
        assertEquals(intent.getParcelableExtra<RefTag>(IntentKey.REF_TAG), refTag)
    }

    @Test
    fun testStartPreLaunchProjectActivity_withUri_setsData() {
        val activity = Robolectric.buildActivity(PreLaunchProjectPageActivity::class.java).setup().get()
        val project = ProjectFactory.project()
        val testUri = Uri.parse("https://www.kickstarter.com/projects/123/test-project")

        activity.startPreLaunchProjectActivity(
            uri = testUri,
            project = project
        )

        val shadowActivity = shadowOf(activity)
        val intent = shadowActivity.nextStartedActivity

        assertNotNull(intent)
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.PreLaunchProjectPageActivity")
        assertEquals(intent.getStringExtra(IntentKey.PREVIOUS_SCREEN), null)
        assertEquals(intent.getParcelableExtra<RefTag>(IntentKey.REF_TAG), UrlUtils.refTag(testUri.toString()))
    }

    @Test
    fun testStartProjectActivity_StartsActivityWithAllExtras() {
        val activity = Robolectric.buildActivity(ProjectPageActivity::class.java).setup().get()
        val project = ProjectFactory.project()
        val refTag = RefTag.discovery()
        val previousScreen = "search"

        activity.startProjectActivity(project, refTag, previousScreen)

        val shadowActivity = shadowOf(activity)
        val intent = shadowActivity.nextStartedActivity

        assertNotNull(intent)
        assertEquals("com.kickstarter.ui.activities.ProjectPageActivity", intent.component?.className)
        assertEquals(project, intent.getParcelableExtra(IntentKey.PROJECT))
        assertEquals(refTag, intent.getParcelableExtra(IntentKey.REF_TAG))
        assertEquals(previousScreen, intent.getStringExtra(IntentKey.PREVIOUS_SCREEN))
    }

    @Test
    fun testStartProjectActivity_withoutPreviousScreen() {
        val activity = Robolectric.buildActivity(ProjectPageActivity::class.java).setup().get()
        val project = ProjectFactory.project()
        val refTag = RefTag.search()

        activity.startProjectActivity(project, refTag, null)

        val shadowActivity = shadowOf(activity)
        val intent = shadowActivity.nextStartedActivity

        assertNotNull(intent)
        assertFalse(intent.hasExtra(IntentKey.PREVIOUS_SCREEN) && intent.getStringExtra(IntentKey.PREVIOUS_SCREEN) != null)
    }
}
