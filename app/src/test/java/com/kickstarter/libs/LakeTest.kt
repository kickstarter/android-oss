package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import org.json.JSONArray
import org.junit.Test
import rx.subjects.BehaviorSubject

class LakeTest : KSRobolectricTestCase() {

    private val propertiesTest = BehaviorSubject.create<Map<String, Any>>()

    @Test
    fun testDefaultProperties() {
        val client = MockTrackingClient(MockCurrentUser(), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        lake.trackAppOpen()

        this.lakeTest.assertValue("App Open")

        assertSessionProperties(null)
    }

    @Test
    fun testDefaultProperties_LoggedInUser() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        lake.trackAppOpen()

        this.lakeTest.assertValue("App Open")

        assertSessionProperties(user)
        val expectedProperties = propertiesTest.value
        assertEquals(15L, expectedProperties["user_uid"])
        assertEquals(3, expectedProperties["user_backed_projects_count"])
        assertEquals("NG", expectedProperties["user_country"])
        assertEquals(false, expectedProperties["user_facebook_account"])
        assertEquals(false, expectedProperties["user_is_admin"])
        assertEquals(2, expectedProperties["user_launched_projects_count"])
        assertEquals(10, expectedProperties["user_watched_projects_count"])
    }

    private fun assertSessionProperties(user: User?) {
        val expectedProperties = propertiesTest.value
        assertEquals(9999, expectedProperties["session_app_build_number"])
        assertEquals("9.9.9", expectedProperties["session_app_release_version"])
        assertEquals("native", expectedProperties["session_client_type"])
        assertEquals(JSONArray().put("android_example_experiment[control]"), expectedProperties["session_current_variants"])
        assertEquals("uuid", expectedProperties["session_device_distinct_id"])
        assertEquals("phone", expectedProperties["session_device_format"])
        assertEquals("Google", expectedProperties["session_device_manufacturer"])
        assertEquals("Pixel 3", expectedProperties["session_device_model"])
        assertEquals("Portrait", expectedProperties["session_device_orientation"])
        assertEquals("en", expectedProperties["session_display_language"])
        assertEquals(JSONArray().put("android_example_feature"), expectedProperties["session_enabled_features"])
        assertEquals(false, expectedProperties["session_is_voiceover_running"])
        assertEquals("kickstarter_android", expectedProperties["session_mp_lib"])
        assertEquals("Android 9", expectedProperties["session_os_version"])
        assertEquals("agent", expectedProperties["session_user_agent"])
        assertEquals(user != null, expectedProperties["session_user_logged_in"])
        assertEquals(false, expectedProperties["session_wifi_connection"])
    }

    private fun mockCurrentConfig() = MockCurrentConfig().apply {
        val config = ConfigFactory.configWithFeatureEnabled("android_example_feature")
                .toBuilder()
                .abExperiments(mapOf(Pair("android_example_experiment", "control")))
                .build()
        config(config)
    }

    private fun user() =
            UserFactory.user()
                    .toBuilder()
                    .id(15)
                    .backedProjectsCount(3)
                    .createdProjectsCount(2)
                    .location(LocationFactory.nigeria())
                    .starredProjectsCount(10)
                    .build()

}
