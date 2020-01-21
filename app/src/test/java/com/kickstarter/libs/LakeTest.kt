package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.*
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import org.joda.time.DateTime
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
        assertContextProperties()
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
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertEquals(15L, expectedProperties["user_uid"])
        assertEquals(3, expectedProperties["user_backed_projects_count"])
        assertEquals("NG", expectedProperties["user_country"])
        assertEquals(false, expectedProperties["user_facebook_account"])
        assertEquals(false, expectedProperties["user_is_admin"])
        assertEquals(2, expectedProperties["user_launched_projects_count"])
        assertEquals(10, expectedProperties["user_watched_projects_count"])
    }

    @Test
    fun testDiscoveryProperties_AllProjects() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        val params = DiscoveryParams
                .builder()
                .sort(DiscoveryParams.Sort.HOME)
                .build()

        lake.trackExplorePageViewed(params)

        assertSessionProperties(user)
        assertContextProperties()

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
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        val params = DiscoveryParams
                .builder()
                .sort(DiscoveryParams.Sort.POPULAR)
                .staffPicks(true)
                .build()

        lake.trackExplorePageViewed(params)

        assertSessionProperties(user)
        assertContextProperties()

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
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        val params = DiscoveryParams
                .builder()
                .category(CategoryFactory.ceramicsCategory())
                .sort(DiscoveryParams.Sort.NEWEST)
                .build()

        lake.trackExplorePageViewed(params)

        assertSessionProperties(user)
        assertContextProperties()

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

    @Test
    fun testProjectProperties() {
        val project = project()

        val client = MockTrackingClient(MockCurrentUser(), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        lake.trackProjectPageViewed(project, RefTag.discovery(), RefTag.recommended())

        assertSessionProperties(null)
        assertContextProperties()
        assertProjectProperties(project)

        this.lakeTest.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser() {
        val project = project()
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        lake.trackProjectPageViewed(project, RefTag.discovery(), RefTag.recommended())

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser_IsBacker() {
        val project = ProjectFactory.backedProject()
                .toBuilder()
                .id(4)
                .category(CategoryFactory.ceramicsCategory())
                .commentsCount(3)
                .creator(creator())
                .location(LocationFactory.unitedStates())
                .updatesCount(5)
                .build()
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        lake.trackProjectPageViewed(project, RefTag.discovery(), RefTag.recommended())

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(true, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser_IsProjectCreator() {
        val project = project().toBuilder().build()
        val creator = creator()
        val client = MockTrackingClient(MockCurrentUser(creator), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        lake.trackProjectPageViewed(project, RefTag.discovery(), RefTag.recommended())

        assertSessionProperties(creator)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(true, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser_HasStarred() {
        val project = project().toBuilder().isStarred(true).build()
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        lake.trackProjectPageViewed(project, RefTag.discovery(), RefTag.recommended())

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertEquals(true, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Viewed")
    }

    private fun assertContextProperties() {
        val expectedProperties = propertiesTest.value
        assertEquals(DateTime.parse("2018-11-02T18:42:05Z").millis / 1000, expectedProperties["context_timestamp"])
    }

    private fun assertProjectProperties(project: Project) {
        val expectedProperties = propertiesTest.value
        assertEquals(100, expectedProperties["project_backers_count"])
        assertEquals("Ceramics", expectedProperties["project_subcategory"])
        assertEquals("Art", expectedProperties["project_category"])
        assertEquals(3, expectedProperties["project_comments_count"])
        assertEquals("US", expectedProperties["project_country"])
        assertEquals(3L, expectedProperties["project_creator_uid"])
        assertEquals("USD", expectedProperties["project_currency"])
        assertEquals(50.0, expectedProperties["project_current_pledge_amount"])
        assertEquals(50.0, expectedProperties["project_current_pledge_amount_usd"])
        assertEquals(project.deadline()?.millis?.let { it / 1000 }, expectedProperties["project_deadline"])
        assertEquals(60 * 60 * 24 * 20, expectedProperties["project_duration"])
        assertEquals(100.0, expectedProperties["project_goal"])
        assertEquals(100.0, expectedProperties["project_goal_usd"])
        assertEquals(true, expectedProperties["project_has_video"])
        assertEquals(10 * 24, expectedProperties["project_hours_remaining"])
        assertEquals(true, expectedProperties["project_is_repeat_creator"])
        assertEquals(project.launchedAt()?.millis?.let { it / 1000 }, expectedProperties["project_launched_at"])
        assertEquals("Brooklyn", expectedProperties["project_location"])
        assertEquals("Some Name", expectedProperties["project_name"])
        assertEquals(.5f, expectedProperties["project_percent_raised"])
        assertEquals(4L, expectedProperties["project_pid"])
        assertEquals(50.0, expectedProperties["project_current_pledge_amount"])
        assertEquals(2, expectedProperties["project_rewards_count"])
        assertEquals("live", expectedProperties["project_state"])
        assertEquals(1.0f, expectedProperties["project_static_usd_rate"])
        assertEquals(5, expectedProperties["project_updates_count"])
        assertEquals("discovery", expectedProperties["session_ref_tag"])
        assertEquals("recommended", expectedProperties["session_referrer_credit"])
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
        assertEquals("Android", expectedProperties["session_os"])
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

    private fun creator() =
            UserFactory.creator().toBuilder()
                    .id(3)
                    .backedProjectsCount(17)
                    .starredProjectsCount(2)
                    .build()

    private fun project() =
            ProjectFactory.project().toBuilder()
                    .id(4)
                    .category(CategoryFactory.ceramicsCategory())
                    .creator(creator())
                    .commentsCount(3)
                    .location(LocationFactory.unitedStates())
                    .updatesCount(5)
                    .build()

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
