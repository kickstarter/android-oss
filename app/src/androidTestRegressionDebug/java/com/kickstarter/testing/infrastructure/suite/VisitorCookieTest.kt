package com.kickstarter.testing.infrastructure.suite

import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.ui.activities.DiscoveryActivity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.net.CookieStore
import java.net.URI

@SmallTest
class VisitorCookieTest {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(DiscoveryActivity::class.java)

    @Ignore
    @Test
    fun testVisitorCookieHasBeenSet() {
        val activity = this.activityRule.activity
        val environment = activity.environment()

        val cookieManager = environment.cookieManager()
        val cookieStore = cookieManager.cookieStore
        val webUri = URI.create(Secrets.WebEndpoint.PRODUCTION)
        val apiUri = URI.create(ApiEndpoint.PRODUCTION.url())

        val deviceId = FirebaseInstanceId.getInstance().id
        assertNotNull(deviceId)

        assertTrue(hasVisitorCookieForURI(cookieStore, webUri, deviceId))
        assertTrue(hasVisitorCookieForURI(cookieStore, apiUri, deviceId))
    }

    private fun hasVisitorCookieForURI(cookieStore: CookieStore, uri: URI, deviceId: String): Boolean {
        return cookieStore.get(uri).stream().anyMatch { httpCookie -> httpCookie.name == KEY_VIS && httpCookie.value == deviceId }
    }

    companion object {
        private const val KEY_VIS = "vis"
    }
}
