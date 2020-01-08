package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
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
    }

    @Test
    fun testDiscoveryProperties_AllProjects() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        val params = DiscoveryParams
                .builder()
                .sort(DiscoveryParams.Sort.HOME)
                .build()

        koala.trackDiscovery(params, false)

        assertSessionProperties(user)
        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["discover_category_id"])
        assertNull(expectedProperties["discover_category_name"])
        assertEquals(true, expectedProperties["discover_everything"])
        assertEquals(false, expectedProperties["discover_pwl"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals("discovery", expectedProperties["discover_ref_tag"])
        assertEquals(null, expectedProperties["discover_search_term"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("home", expectedProperties["discover_sort"])
        assertNull(expectedProperties["discover_subcategory_id"])
        assertNull(expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])
    }

    @Test
    fun testDiscoveryProperties_NoCategory() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        val params = DiscoveryParams
                .builder()
                .sort(DiscoveryParams.Sort.POPULAR)
                .staffPicks(true)
                .build()

        koala.trackDiscovery(params, false)

        assertSessionProperties(user)
        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["discover_category_id"])
        assertNull(expectedProperties["discover_category_name"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(true, expectedProperties["discover_pwl"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals("recommended_popular", expectedProperties["discover_ref_tag"])
        assertEquals(null, expectedProperties["discover_search_term"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("popularity", expectedProperties["discover_sort"])
        assertNull(expectedProperties["discover_subcategory_id"])
        assertNull(expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])
    }

    @Test
    fun testDiscoveryProperties_Category() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        val params = DiscoveryParams
                .builder()
                .category(CategoryFactory.ceramicsCategory())
                .sort(DiscoveryParams.Sort.NEWEST)
                .build()

        koala.trackDiscovery(params, false)

        assertSessionProperties(user)
        val expectedProperties = propertiesTest.value
        assertEquals(1L, expectedProperties["discover_category_id"])
        assertEquals("Art", expectedProperties["discover_category_name"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(false, expectedProperties["discover_pwl"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals("category_newest", expectedProperties["discover_ref_tag"])
        assertEquals(null, expectedProperties["discover_search_term"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("newest", expectedProperties["discover_sort"])
        assertEquals(287L, expectedProperties["discover_subcategory_id"])
        assertEquals("Ceramics", expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])
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
                    .starredProjectsCount(10)
                    .build()

}
