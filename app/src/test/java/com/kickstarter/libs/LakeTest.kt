package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.*
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import rx.subjects.BehaviorSubject

class LakeTest : KSRobolectricTestCase() {

    private val propertiesTest = BehaviorSubject.create<Map<String, Any>>()

    @Test
    fun testDefaultProperties() {
        val client = client(null)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        lake.trackAppOpen()

        this.lakeTest.assertValue("App Open")

        assertSessionProperties(null)
        assertContextProperties()
    }

    @Test
    fun testDefaultProperties_LoggedInUser() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        lake.trackAppOpen()

        this.lakeTest.assertValue("App Open")

        assertSessionProperties(user)
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertEquals("15", expectedProperties["user_uid"])
        assertEquals("NG", expectedProperties["user_country"])
    }

    @Test
    fun testDiscoveryProperties_AllProjects() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .sort(DiscoveryParams.Sort.MAGIC)
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
        assertEquals("magic", expectedProperties["discover_sort"])
        assertNull(expectedProperties["discover_subcategory_id"])
        assertNull(expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])
    }

    @Test
    fun testDiscoveryProperties_NoCategory() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

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
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .category(CategoryFactory.ceramicsCategory())
            .sort(DiscoveryParams.Sort.NEWEST)
            .build()

        lake.trackExplorePageViewed(params)

        assertSessionProperties(user)
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertEquals("1", expectedProperties["discover_category_id"])
        assertEquals("Art", expectedProperties["discover_category_name"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(false, expectedProperties["discover_pwl"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals("category_newest", expectedProperties["discover_ref_tag"])
        assertEquals(null, expectedProperties["discover_search_term"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("newest", expectedProperties["discover_sort"])
        assertEquals("287", expectedProperties["discover_subcategory_id"])
        assertEquals("Ceramics", expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])
    }

    @Test
    fun testProjectProperties_loggedOutUser() {
        val project = project()

        val client = client(null)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        lake.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(null)
        assertContextProperties()
        assertProjectProperties(project)

        val expectedProperties = propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        lake.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
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
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        lake.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), null)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(true, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser_IsProjectCreator() {
        val project = project().toBuilder().build()
        val creator = creator()
        val client = client(creator)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        lake.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), null)

        assertSessionProperties(creator)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = this.propertiesTest.value
        assertNull(expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(true, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser_HasStarred() {
        val project = project().toBuilder().isStarred(true).build()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        lake.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = this.propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(true, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser_NotBacked() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())
        lake.trackProjectPagePledgeButtonClicked(projectData, PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Pledge Button Clicked")
    }

    @Test
    fun testPledgeProperties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        lake.trackSelectRewardButtonClicked(PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward()))

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()

        val expectedProperties = propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Select Reward Button Clicked")
    }

    @Test
    fun testCheckoutProperties_whenNewPledge() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        lake.trackPledgeSubmitButtonClicked(
            CheckoutDataFactory.checkoutData(20.0, 30.0),
            PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward())
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertCheckoutProperties()

        val expectedProperties = this.propertiesTest.value
        assertNull(expectedProperties["checkout_id"])
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Pledge Submit Button Clicked")
    }

    @Test
    fun testCheckoutProperties_whenFixingPledge() {
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
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        lake.trackPledgeSubmitButtonClicked(
            CheckoutDataFactory.checkoutData(20.0, 30.0),
            PledgeData.with(PledgeFlowContext.FIX_ERRORED_PLEDGE, projectData, reward())
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertCheckoutProperties()

        val expectedProperties = this.propertiesTest.value
        assertNull(expectedProperties["checkout_id"])
        assertEquals("fix_errored_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(true, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Pledge Submit Button Clicked")
    }

    @Test
    fun testSuccessfulCheckoutProperties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        lake.trackThanksPageViewed(
            CheckoutDataFactory.checkoutData(3L, 20.0, 30.0),
            PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward())
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertCheckoutProperties()

        val expectedProperties = this.propertiesTest.value
        assertEquals("3", expectedProperties["checkout_id"])
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Thanks Page Viewed")
    }

    @Test
    fun testOptimizelyProperties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = AnalyticEvents(listOf(client))

        lake.trackProjectPagePledgeButtonClicked(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertOptimizelyProperties()

        val expectedProperties = propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.lakeTest.assertValues("Project Page Pledge Button Clicked")
    }

    private fun client(user: User?) = MockTrackingClient(
        user?.let { MockCurrentUser(it) }
            ?: MockCurrentUser(),
        mockCurrentConfig(),
        TrackingClientType.Type.LAKE,
        object : MockExperimentsClientType() {
            override fun enabledFeatures(user: User?): List<String> {
                return listOf("optimizely_feature")
            }
        }
    )

    private fun assertCheckoutProperties() {
        val expectedProperties = this.propertiesTest.value
        assertEquals(30.0, expectedProperties["checkout_amount"])
        assertEquals("credit_card", expectedProperties["checkout_payment_type"])
        assertEquals(50.0, expectedProperties["checkout_amount_total_usd"])
        assertEquals(20.0, expectedProperties["checkout_shipping_amount"])
    }

    private fun assertContextProperties() {
        val expectedProperties = this.propertiesTest.value
        assertEquals(DateTime.parse("2018-11-02T18:42:05Z").millis / 1000, expectedProperties["context_timestamp"])
    }

    private fun assertOptimizelyProperties() {
        val expectedProperties = this.propertiesTest.value
        assertEquals(OptimizelyEnvironment.DEVELOPMENT.sdkKey, expectedProperties["optimizely_api_key"])
        assertEquals(OptimizelyEnvironment.DEVELOPMENT.environmentKey, expectedProperties["optimizely_environment_key"])
        assertNotNull(expectedProperties["optimizely_experiments"])
        val experiments = expectedProperties["optimizely_experiments"] as JSONArray
        val experiment = experiments[0] as JSONObject
        assertEquals("test_experiment", experiment["optimizely_experiment_slug"])
        assertEquals("unknown", experiment["optimizely_variant_id"])
    }

    private fun assertPledgeProperties() {
        val expectedProperties = this.propertiesTest.value
        assertEquals(DateTime.parse("2019-03-26T19:26:09Z"), expectedProperties["checkout_reward_estimated_delivery_on"])
        assertEquals(false, expectedProperties["checkout_reward_has_items"])
        assertEquals("2", expectedProperties["checkout_reward_id"])
        assertEquals(false, expectedProperties["checkout_reward_is_limited_time"])
        assertEquals(false, expectedProperties["checkout_reward_is_limited_quantity"])
        assertEquals(10.0, expectedProperties["checkout_reward_minimum"])
        assertEquals(true, expectedProperties["checkout_reward_shipping_enabled"])
        assertEquals("unrestricted", expectedProperties["checkout_reward_shipping_preference"])
        assertEquals("Digital Bundle", expectedProperties["checkout_reward_title"])
    }

    private fun assertProjectProperties(project: Project) {
        val expectedProperties = this.propertiesTest.value
        assertEquals(100, expectedProperties["project_backers_count"])
        assertEquals("Ceramics", expectedProperties["project_subcategory"])
        assertEquals("Art", expectedProperties["project_category"])
        assertEquals(3, expectedProperties["project_comments_count"])
        assertEquals("US", expectedProperties["project_country"])
        assertEquals("3", expectedProperties["project_creator_uid"])
        assertEquals("USD", expectedProperties["project_currency"])
        assertEquals(50.0, expectedProperties["project_current_pledge_amount"])
        assertEquals(50.0, expectedProperties["project_current_amount_pledged_usd"])
        assertEquals(project.deadline(), expectedProperties["project_deadline"])
        assertEquals(60 * 60 * 24 * 20, expectedProperties["project_duration"])
        assertEquals(100.0, expectedProperties["project_goal"])
        assertEquals(100.0, expectedProperties["project_goal_usd"])
        assertEquals(true, expectedProperties["project_has_video"])
        assertEquals(10 * 24, expectedProperties["project_hours_remaining"])
        assertEquals(true, expectedProperties["project_is_repeat_creator"])
        assertEquals(project.launchedAt(), expectedProperties["project_launched_at"])
        assertEquals("Brooklyn", expectedProperties["project_location"])
        assertEquals("Some Name", expectedProperties["project_name"])
        assertEquals(.5f, expectedProperties["project_percent_raised"])
        assertEquals("4", expectedProperties["project_pid"])
        assertEquals(50.0, expectedProperties["project_current_pledge_amount"])
        assertEquals(2, expectedProperties["project_rewards_count"])
        assertEquals("live", expectedProperties["project_state"])
        assertEquals(1.0f, expectedProperties["project_static_usd_rate"])
        assertEquals(5, expectedProperties["project_updates_count"])
        assertEquals("discovery", expectedProperties["session_ref_tag"])
        assertEquals("recommended", expectedProperties["session_referrer_credit"])
    }

    private fun assertSessionProperties(user: User?) {
        val expectedProperties = this.propertiesTest.value
        assertEquals(9999, expectedProperties["session_app_build_number"])
        assertEquals("9.9.9", expectedProperties["session_app_release_version"])
        assertEquals("native_android", expectedProperties["session_platform"])
        assertEquals("native", expectedProperties["session_client"])
        assertEquals(JSONArray().put("android_example_experiment[control]"), expectedProperties["session_current_variants"])
        assertEquals("uuid", expectedProperties["session_device_distinct_id"])
        assertEquals("phone", expectedProperties["session_device_type"])
        assertEquals("Google", expectedProperties["session_device_manufacturer"])
        assertEquals("Pixel 3", expectedProperties["session_device_model"])
        assertEquals("Portrait", expectedProperties["session_device_orientation"])
        assertEquals("en", expectedProperties["session_display_language"])
        assertEquals(JSONArray().put("optimizely_feature").put("android_example_feature"), expectedProperties["session_enabled_features"])
        assertEquals(false, expectedProperties["session_is_voiceover_running"])
        assertEquals("kickstarter_android", expectedProperties["session_mp_lib"])
        assertEquals("android", expectedProperties["session_os"])
        assertEquals("9", expectedProperties["session_os_version"])
        assertEquals("agent", expectedProperties["session_user_agent"])
        assertEquals(user != null, expectedProperties["session_user_is_logged_in"])
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

    private fun reward() =
        RewardFactory.rewardWithShipping().toBuilder()
            .id(2)
            .minimum(10.0)
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
