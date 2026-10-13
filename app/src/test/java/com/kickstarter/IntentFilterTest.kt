package com.kickstarter

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.kickstarter.ui.activities.DiscoveryActivity
import com.kickstarter.ui.activities.SplashScreenActivity
import org.junit.Test

class IntentFilterTest : KSRobolectricTestCase() {

    private val context = application()

    private fun queryIntentActivities(url: String): Set<String> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            setPackage(context.packageName)
        }

        return context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .map { it.activityInfo.name }
            .toSet()
    }

    @Test
    fun `test SplashScreenActivity deep links`() {
        val matches = { url: String ->
            queryIntentActivities(url).contains(SplashScreenActivity::class.java.name)
        }

        assertTrue(matches("https://www.kickstarter.com/discover/advanced?category_id=34&sort=newest"))
        assertFalse(matches("https://kickstarter.com/discover/advanced?category_id=34&sort=newest"))
        assertFalse(matches("https://staging.kickstarter.com/discover/advanced?category_id=34&sort=newest"))
        assertFalse(matches("https://stg.ksr.io/discover/advanced?category_id=34&sort=newest"))
        assertTrue(matches("ksr://staging.kickstarter.com/discover/advanced?category_id=16&sort=newest"))
        assertTrue(matches("ksr://stg.ksr.io/discover/advanced?category_id=16&sort=newest"))

        assertTrue(matches("ksr://www.kickstarter.com/video-feed"))
        assertTrue(matches("ksr://environment.dev.kickstarter.com/video-feed"))
        assertTrue(matches("ksr://stg.ksr.io/video-feed"))

        assertFalse(matches("https://environment.dev.kickstarter.com/video-feed"))
        assertFalse(matches("https://stg.ksr.io/video-feed"))
        assertTrue(matches("https://environment.dev.kickstarter.com/projects/creator/project"))
        assertTrue(matches("https://stg.ksr.io/projects/creator/project"))
        assertTrue(matches("https://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true"))
        assertTrue(matches("https://stg.ksr.io/settings/notify_mobile_of_marketing_update/true"))
    }

    @Test
    fun `test DiscoveryActivity deep links`() {
        val matches = { url: String ->
            queryIntentActivities(url).contains(DiscoveryActivity::class.java.name)
        }

        assertTrue(matches("https://www.kickstarter.com/profile/verify_email"))
        assertTrue(matches("https://stg.ksr.io/profile/verify_email"))
    }
}
