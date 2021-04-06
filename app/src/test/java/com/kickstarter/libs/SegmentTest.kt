package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.libs.utils.ContextPropertyKeyName
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_CTA
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_PAGE
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ACTIVITY_FEED
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.EventName.VIDEO_PLAYBACK_COMPLETED
import com.kickstarter.libs.utils.EventName.VIDEO_PLAYBACK_STARTED
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
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

class SegmentTest : KSRobolectricTestCase() {

    private val propertiesTest = BehaviorSubject.create<Map<String, Any>>()

    @Test
    fun testDefaultProperties() {
        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackAppOpen()

        this.segmentTrack.assertValue("App Open")

        assertSessionProperties(null)
        assertContextProperties()
    }

    @Test
    fun testDefaultProperties_LoggedInUser() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedId.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        segment.trackAppOpen()

        this.segmentTrack.assertValue("App Open")
        this.segmentIdentify.assertValue(user.id())

        assertSessionProperties(user)
        assertContextProperties()
        assertUserProperties(false)
    }

    @Test
    fun testDefaultProperties_LoggedInUser_isAdmin() {
        val user = user().toBuilder().isAdmin(true).build()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedId.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        segment.trackAppOpen()

        assertUserProperties(true)
    }

    @Test
    fun testDefaultProperties_loggedInUser_nullProperties() {
        val user =
            User.builder()
                .avatar(AvatarFactory.avatar())
                .name("Kickstarter")
                .id(12)
                .build()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedId.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        segment.trackActivityFeedPageViewed()

        val expectedProperties = propertiesTest.value
        assertEquals(0, expectedProperties["user_backed_projects_count"])
        assertEquals(false, expectedProperties["user_is_admin"])
        assertEquals(0, expectedProperties["user_launched_projects_count"])
        assertEquals("12", expectedProperties["user_uid"])
        assertEquals("US", expectedProperties["user_country"])
    }

    @Test
    fun testDiscoveryProperties_AllProjects() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedId.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()

        segment.trackExplorePageViewed(params)
        this.segmentIdentify.assertValue(user.id())

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
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedId.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .sort(DiscoveryParams.Sort.POPULAR)
            .staffPicks(true)
            .build()

        segment.trackExplorePageViewed(params)

        assertSessionProperties(user)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["discover_category_id"])
        assertNull(expectedProperties["discover_category_name"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(true, expectedProperties["discover_pwl"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals("recommended_popular", expectedProperties["discover_ref_tag"])
        assertEquals(null, expectedProperties["discover_search_term"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("popular", expectedProperties["discover_sort"])
        assertNull(expectedProperties["discover_subcategory_id"])
        assertNull(expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])

        this.segmentIdentify.assertValue(user.id())
    }

    @Test
    fun testDiscoveryProperties_Category() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedId.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .category(CategoryFactory.ceramicsCategory())
            .sort(DiscoveryParams.Sort.NEWEST)
            .build()

        segment.trackExplorePageViewed(params)

        assertSessionProperties(user)
        assertContextProperties()
        assertUserProperties(false)

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

        this.segmentIdentify.assertValue(user.id())
    }

    @Test
    fun testProjectProperties_loggedOutUser() {
        val project = project()

        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(null)
        assertContextProperties()
        assertProjectProperties(project)

        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["user_uid"])
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Project Page Viewed")

        this.segmentIdentify.assertNoValues()
    }

    @Test
    fun testProjectProperties_hasAddOns() {
        val project = ProjectFactory.projectWithAddOns()

        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        val expectedProperties = this.propertiesTest.value
        assertNull(expectedProperties["user_uid"])
        assertEquals(true, expectedProperties["project_has_add_ons"])
    }

    @Test
    fun testProjectProperties_hasProject_prelaunch_activated() {
        val project = ProjectFactory.projectWithAddOns()

        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        val expectedProperties = this.propertiesTest.value
        assertNotNull(expectedProperties["project_prelaunch_activated"])
    }

    @Test
    fun testProjectProperties_hasProject_prelaunch_activated_true() {
        val project = ProjectFactory.projectWithAddOns()
            .toBuilder()
            .prelaunchActivated(true)
            .build()
        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))
        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)
        val expectedProperties = this.propertiesTest.value
        assertNotNull(expectedProperties["project_prelaunch_activated"])
        assertEquals(true, expectedProperties["project_prelaunch_activated"])
    }

    @Test
    fun testProjectProperties_hasProject_prelaunch_activated_false() {
        val project = ProjectFactory.projectWithAddOns()
            .toBuilder()
            .prelaunchActivated(false)
            .build()
        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))
        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)
        val expectedProperties = this.propertiesTest.value
        assertNotNull(expectedProperties["project_prelaunch_activated"])
        assertEquals(false, expectedProperties["project_prelaunch_activated"])
    }

    @Test
    fun testProjectProperties_LoggedInUser() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedId.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Project Page Viewed")
        this.segmentIdentify.assertValue(user.id())
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
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), null)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(true, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser_IsProjectCreator() {
        val project = project().toBuilder().build()
        val creator = creator()
        val client = client(creator)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), null)

        assertSessionProperties(creator)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = this.propertiesTest.value

        assertNull(expectedProperties["context_pledge_flow"])

        assertEquals(17, expectedProperties["user_backed_projects_count"])
        assertEquals(false, expectedProperties["user_is_admin"])
        assertEquals(10, expectedProperties["user_launched_projects_count"])
        assertEquals("3", expectedProperties["user_uid"])
        assertEquals("US", expectedProperties["user_country"])

        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(true, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser_HasStarred() {
        val project = project().toBuilder().isStarred(true).build()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = this.propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(true, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Project Page Viewed")
    }

    @Test
    fun testProjectProperties_LoggedInUser_NotBacked() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())
        segment.trackProjectPagePledgeButtonClicked(projectData, PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Project Page Pledge Button Clicked")
    }

    @Test
    fun testPledgeProperties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackSelectRewardButtonClicked(PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward()))

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Select Reward Button Clicked")
    }

    @Test
    fun testCheckoutProperties_whenNewPledge() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackPledgeSubmitButtonClicked(
            CheckoutDataFactory.checkoutData(20.0, 30.0),
            PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward())
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertCheckoutProperties()
        assertUserProperties(false)

        val expectedProperties = this.propertiesTest.value
        assertNull(expectedProperties["checkout_id"])
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Pledge Submit Button Clicked")
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
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackPledgeSubmitButtonClicked(
            CheckoutDataFactory.checkoutData(20.0, 30.0),
            PledgeData.with(PledgeFlowContext.FIX_ERRORED_PLEDGE, projectData, reward())
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertCheckoutProperties()
        assertUserProperties(false)

        val expectedProperties = this.propertiesTest.value
        assertNull(expectedProperties["checkout_id"])
        assertEquals("fix_errored_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(true, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Pledge Submit Button Clicked")
    }

    @Test
    fun testSuccessfulCheckoutProperties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackThanksPageViewed(
            CheckoutDataFactory.checkoutData(3L, 20.0, 30.0),
            PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward())
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertCheckoutProperties()
        assertUserProperties(false)

        val expectedProperties = this.propertiesTest.value
        assertEquals("3", expectedProperties["checkout_id"])
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Thanks Page Viewed")
    }

    @Test
    fun testActivityFeedsProperties() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackActivityFeedPageViewed()

        assertSessionProperties(user)
        assertContextProperties()
        assertPageContextProperty(ACTIVITY_FEED.contextName)
        assertUserProperties(false)

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTwoFactorAuthProperties() {
        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackTwoFactorAuthPageViewed()

        assertSessionProperties(null)
        assertContextProperties()
        assertPageContextProperty(EventContextValues.ContextPageName.TWO_FACTOR_AUTH.contextName)

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testOptimizelyProperties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPagePledgeButtonClicked(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), PledgeFlowContext.NEW_PLEDGE)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertOptimizelyProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues("Project Page Pledge Button Clicked")
    }

    @Test
    fun testVideoProperties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)

        val videoLength = 100L
        val videoStartedPosition = 0L
        val videoCompletedPosition = 50L

        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPageViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), null)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        segment.trackVideoStarted(project, videoLength, videoStartedPosition)

        assertVideoProperties(videoLength, videoStartedPosition)

        segment.trackVideoCompleted(project, videoLength, videoCompletedPosition)

        assertVideoProperties(videoLength, videoCompletedPosition)

        this.segmentTrack.assertValues("Project Page Viewed", VIDEO_PLAYBACK_STARTED.eventName, VIDEO_PLAYBACK_COMPLETED.eventName)
    }

    @Test
    fun testLoginPageViewed() {

        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)

        val segment = AnalyticEvents(listOf(client))
        segment.trackLoginPagedViewed()

        assertSessionProperties(null)
        assertContextProperties()

        val properties = this.propertiesTest.value
        assertNull(properties["user_uid"])
        assertEquals(LOGIN.contextName, properties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun discoveryParamProperties_whenAllFieldsPopulated_shouldReturnExpectedProps() {
        val user = user()
        val client = client(user)

        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)

        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoveryPageViewed(discoveryParams())

        val properties = this.propertiesTest.value

        assertEquals(false, properties["discover_everything"])
        assertEquals(true, properties["discover_pwl"])
        assertEquals(false, properties["discover_recommended"])
        assertEquals("category_ending_soon", properties["discover_ref_tag"])
        assertEquals("hello world", properties["discover_search_term"])
        assertEquals(true, properties["discover_social"])
        assertEquals("ending_soon", properties["discover_sort"])
        assertEquals(123, properties["discover_tag"])
        assertEquals(true, properties["discover_watched"])
        assertEquals("Art", properties["discover_category_name"])
        assertEquals("Ceramics", properties["discover_subcategory_name"])
    }

    @Test
    fun testTrackDiscoverSortCTA() {
        val user = user()
        val client = client(user)

        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)

        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoverSortCTA(DiscoveryParams.Sort.POPULAR, discoveryParams())

        val properties = this.propertiesTest.value

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)

        assertEquals(EventContextValues.LocationContextName.DISCOVER_ADVANCED.contextName, properties[ContextPropertyKeyName.CONTEXT_LOCATION.contextName])
        assertEquals(EventContextValues.CtaContextName.DISCOVER.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(EventContextValues.CtaContextName.DISCOVER_SORT.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals(DiscoveryParams.Sort.ENDING_SOON.toString(), properties[ContextPropertyKeyName.CONTEXT_TYPE.contextName])
        assertEquals("popular", properties["discover_sort"])
    }

    private fun client(user: User?) = MockTrackingClient(
        user?.let { MockCurrentUser(it) }
            ?: MockCurrentUser(),
        mockCurrentConfig(),
        TrackingClientType.Type.SEGMENT,
        object : MockExperimentsClientType() {
            override fun enabledFeatures(user: User?): List<String> {
                return listOf("optimizely_feature")
            }

            override fun getTrackingProperties(): Map<String, Array<Map<String, String>>> {
                return getOptimizelySession()
            }
        }
    )

    private fun getOptimizelySession(): Map<String, Array<Map<String, String>>> {
        val experiment1 = mapOf("suggested_no_reward_amount" to "variation_3")
        val array = arrayOf(experiment1)
        return mapOf("variants_optimizely" to array)
    }

    private fun assertCheckoutProperties() {
        val expectedProperties = this.propertiesTest.value
        assertEquals(30.0, expectedProperties["checkout_amount"])
        assertEquals("credit_card", expectedProperties["checkout_payment_type"])
        assertEquals(50.0, expectedProperties["checkout_amount_total_usd"])
        assertEquals(20.0, expectedProperties["checkout_shipping_amount"])
        assertEquals(20.0, expectedProperties["checkout_shipping_amount_usd"])
        assertEquals(0, expectedProperties["checkout_add_ons_count_total"])
        assertEquals(0, expectedProperties["checkout_add_ons_count_unique"])
        assertEquals(0.0, expectedProperties["checkout_add_ons_minimum_usd"])
        assertEquals(0.0, expectedProperties["checkout_bonus_amount_usd"])
    }

    private fun assertContextProperties() {
        val expectedProperties = this.propertiesTest.value
        assertEquals(DateTime.parse("2018-11-02T18:42:05Z").millis / 1000, expectedProperties["context_timestamp"])
    }

    // TODO: will be deleted on https://kickstarter.atlassian.net/browse/EP-187
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
        assertEquals(10.0, expectedProperties["checkout_reward_minimum_usd"])
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
        assertNotNull(expectedProperties["project_prelaunch_activated"])
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
        assertEquals(false, expectedProperties["project_has_add_ons"])
    }

    private fun assertSessionProperties(user: User?) {
        val expectedProperties = this.propertiesTest.value
        assertEquals(9999, expectedProperties["session_app_build_number"])
        assertEquals("9.9.9", expectedProperties["session_app_release_version"])
        assertEquals("native_android", expectedProperties["session_platform"])
        assertEquals("native", expectedProperties["session_client"])
        assertEquals("US", expectedProperties["session_country"])
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
        assertEquals(getOptimizelySession()["variants_optimizely"]?.first(), (expectedProperties["session_variants_optimizely"] as Array<*>).first())
        assertEquals("android_example_experiment[control]", (expectedProperties["session_variants_internal"] as Array<*>).first())
    }

    private fun assertUserProperties(isAdmin: Boolean) {
        val expectedProperties = this.propertiesTest.value
        assertEquals(3, expectedProperties["user_backed_projects_count"])
        assertEquals(5, expectedProperties["user_launched_projects_count"])
        assertEquals("15", expectedProperties["user_uid"])
        assertEquals("NG", expectedProperties["user_country"])
        assertEquals(isAdmin, expectedProperties["user_is_admin"])
    }

    private fun assertCtaContextProperty(contextName: String) {
        val expectedProperties = this.propertiesTest.value
        assertEquals(contextName, expectedProperties[CONTEXT_CTA.contextName])
    }

    private fun assertPageContextProperty(contextName: String) {
        val expectedProperties = this.propertiesTest.value
        assertEquals(contextName, expectedProperties[CONTEXT_PAGE.contextName])
    }

    private fun assertVideoProperties(videoLength: Long, videoPosition: Long) {
        val expectedProperties = this.propertiesTest.value
        assertEquals(videoLength, expectedProperties["video_length"])
        assertEquals(videoPosition, expectedProperties["video_position"])
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
            .memberProjectsCount(5)
            .createdProjectsCount(2)
            .location(LocationFactory.nigeria())
            .starredProjectsCount(10)
            .build()

    private fun discoveryParams() =
        DiscoveryParams
            .builder()
            .staffPicks(true)
            .recommended(false)
            .location(LocationFactory.germany())
            .starred(1)
            .term("hello world")
            .social(2)
            .sort(DiscoveryParams.Sort.ENDING_SOON)
            .tagId(123)
            .category(CategoryFactory.ceramicsCategory())
            .build()
}
