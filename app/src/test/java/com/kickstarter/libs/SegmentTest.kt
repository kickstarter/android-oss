package com.kickstarter.libs

import android.content.Context
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_CTA
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_LOCATION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_PAGE
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_TYPE
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ACTIVITY_FEED
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN_SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.MANAGE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.THANKS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.TWO_FACTOR_AUTH
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.UPDATE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_FILTER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_SORT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SEARCH
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SIGN_UP_INITIATE
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.ALL
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.PWL
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.RECOMMENDED
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.RESULTS
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.CURATED
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.DISCOVER_ADVANCED
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.DISCOVER_OVERLAY
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.GLOBAL_NAV
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.SEARCH_RESULTS
import com.kickstarter.libs.utils.EventName.CTA_CLICKED
import com.kickstarter.libs.utils.EventName.PAGE_VIEWED
import com.kickstarter.libs.utils.EventName.VIDEO_PLAYBACK_COMPLETED
import com.kickstarter.libs.utils.EventName.VIDEO_PLAYBACK_STARTED
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
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

    lateinit var build: Build
    lateinit var context: Context

    override fun setUp() {
        super.setUp()
        build = environment().build()
        context = application()
    }

    class MockSegmentTrackingClient(
        build: Build,
        context: Context,
        currentConfig: CurrentConfigType,
        currentUser: CurrentUserType,
        opt: ExperimentsClientType
    ) : SegmentTrackingClient(build, context, currentConfig, currentUser, opt) {
        override fun initialize() {
            this.isInitialized = true
        }

        override fun isEnabled() = this.isInitialized
    }

    @Test
    fun testSegmentClientTest() {
        val user = UserFactory.user()
        val mockOptimizely = object : MockExperimentsClientType() {
            override fun enabledFeatures(user: User?): List<String> {
                return listOf("optimizely_feature")
            }

            override fun getTrackingProperties(): Map<String, Array<String>> {
                return getOptimizelySession()
            }
        }

        val mockClient = MockSegmentTrackingClient(build, context, mockCurrentConfig(), MockCurrentUser(user), mockOptimizely)
        mockClient.initialize()
        assertNotNull(mockClient)
        assertTrue(mockClient.isEnabled())
    }

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
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        segment.trackAppOpen()

        this.segmentTrack.assertValue("App Open")
        this.segmentIdentify.assertValue(user)

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
        client.identifiedUser.subscribe(this.segmentIdentify)
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
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        segment.trackActivityFeedPageViewed()

        val expectedProperties = propertiesTest.value
        assertEquals(0, expectedProperties["user_backed_projects_count"])
        assertEquals(false, expectedProperties["user_is_admin"])
        assertEquals(0, expectedProperties["user_launched_projects_count"])
        assertEquals("12", expectedProperties["user_uid"])
        assertEquals("US", expectedProperties["user_country"])
        assertEquals(0, expectedProperties["user_created_projects_count"])
        assertEquals(false, expectedProperties["user_facebook_connected"])
        assertEquals(0, expectedProperties["user_watched_projects_count"])
    }

    @Test
    fun testCampaignDetailsCta_Properties() {
        val user = user()
        val project = project()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackCampaignDetailsCTAClicked(projectData)
        this.segmentIdentify.assertValue(user)

        assertSessionProperties(user)
        assertContextProperties()
        assertProjectProperties(projectData.project())
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertEquals("campaign_details", expectedProperties["context_cta"])
        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testDiscoveryProperties_AllProjects() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()

        segment.trackDiscoveryPageViewed(params)
        this.segmentIdentify.assertValue(user)

        assertSessionProperties(user)
        assertContextProperties()
        assertDiscoverProperties()
    }

    @Test
    fun testDiscoveryPageViewed() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()

        segment.trackDiscoveryPageViewed(params)
        this.segmentIdentify.assertValue(user)

        assertSessionProperties(user)
        assertContextProperties()
        assertDiscoverProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value

        assertEquals("magic", expectedProperties[DISCOVER_SORT.contextName])
        assertEquals(DISCOVER.contextName, expectedProperties[CONTEXT_PAGE.contextName])
        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun testDiscoveryProjectCtaClickedProperties_AllProjects() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val project = project().toBuilder().build()

        val params = DiscoveryParams
            .builder()
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()

        segment.trackDiscoverProjectCtaClicked(params, ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()))

        assertSessionProperties(user)
        assertContextProperties()
        assertDiscoverProperties()
        assertUserProperties(false)
        assertProjectProperties(project)

        val expectedProperties = propertiesTest.value

        assertEquals(PROJECT.contextName, expectedProperties[CONTEXT_CTA.contextName])
        assertEquals(DISCOVER_ADVANCED.contextName, expectedProperties[CONTEXT_LOCATION.contextName])
        assertEquals(RESULTS.contextName, expectedProperties[CONTEXT_TYPE.contextName])
        assertEquals(DISCOVER.contextName, expectedProperties[CONTEXT_PAGE.contextName])
        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testDiscoveryProjectCtaClickedProperties_Recommended() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val project = project().toBuilder().build()

        val params = DiscoveryParams
            .builder()
            .recommended(true)
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()

        segment.trackDiscoverProjectCtaClicked(params, ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()))

        assertSessionProperties(user)
        assertContextProperties()

        assertUserProperties(false)
        assertProjectProperties(project)

        val expectedProperties = propertiesTest.value

        // test custom discover properties
        assertNull(expectedProperties["discover_category_id"])
        assertNull(expectedProperties["discover_category_name"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(false, expectedProperties["discover_pwl"])
        assertEquals(true, expectedProperties["discover_recommended"])
        assertEquals("discovery", expectedProperties["discover_ref_tag"])
        assertEquals(null, expectedProperties["discover_search_term"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("magic", expectedProperties["discover_sort"])
        assertNull(expectedProperties["discover_subcategory_id"])
        assertNull(expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])

        assertEquals(PROJECT.contextName, expectedProperties[CONTEXT_CTA.contextName])
        assertEquals(DISCOVER_ADVANCED.contextName, expectedProperties[CONTEXT_LOCATION.contextName])
        assertEquals(RECOMMENDED.contextName, expectedProperties[CONTEXT_TYPE.contextName])
        assertEquals(DISCOVER.contextName, expectedProperties[CONTEXT_PAGE.contextName])
        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testSearchResultCTAClicked_Properties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .term("test")
            .sort(DiscoveryParams.Sort.POPULAR)
            .staffPicks(true)
            .build()

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())
        segment.trackDiscoverSearchResultProjectCATClicked(params, projectData, 200, DiscoveryParams.Sort.POPULAR)

        assertSessionProperties(user)
        assertProjectProperties(projectData.project())
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertEquals("test", expectedProperties["discover_search_term"])
        assertEquals(200, expectedProperties["discover_search_results_count"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(true, expectedProperties["discover_pwl"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals("recommended_popular", expectedProperties["discover_ref_tag"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("popular", expectedProperties["discover_sort"])
        assertNull(expectedProperties["discover_subcategory_id"])
        assertNull(expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])

        assertEquals(PROJECT.contextName, expectedProperties[CONTEXT_CTA.contextName])
        assertEquals(SEARCH.contextName, expectedProperties[CONTEXT_PAGE.contextName])
        assertEquals(SEARCH_RESULTS.contextName, expectedProperties[CONTEXT_LOCATION.contextName])
        assertEquals(RESULTS.contextName, expectedProperties[CONTEXT_TYPE.contextName])

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testThanksActivityRecommendedProjectCATClicked_Properties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackThanksActivityProjectCardClicked(
            projectData,
            CheckoutDataFactory.checkoutData(20.0, 30.0),
            PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward(), listOfAddons())
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertCheckoutProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value

        assertEquals(PROJECT.contextName, expectedProperties[CONTEXT_CTA.contextName])
        assertEquals(THANKS.contextName, expectedProperties[CONTEXT_PAGE.contextName])
        assertEquals(CURATED.contextName, expectedProperties[CONTEXT_LOCATION.contextName])
        assertEquals(RECOMMENDED.contextName, expectedProperties[CONTEXT_TYPE.contextName])

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testSearchResultPageViewed_Properties() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .term("test")
            .sort(DiscoveryParams.Sort.POPULAR)
            .staffPicks(true)
            .build()

        segment.trackSearchResultPageViewed(params, 200, DiscoveryParams.Sort.POPULAR)

        assertSessionProperties(user)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertEquals("test", expectedProperties["discover_search_term"])
        assertEquals(200, expectedProperties["discover_search_results_count"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(true, expectedProperties["discover_pwl"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals("recommended_popular", expectedProperties["discover_ref_tag"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("popular", expectedProperties["discover_sort"])
        assertNull(expectedProperties["discover_subcategory_id"])
        assertNull(expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun testDiscoveryProperties_NoCategory() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .sort(DiscoveryParams.Sort.POPULAR)
            .staffPicks(true)
            .build()

        segment.trackDiscoveryPageViewed(params)

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

        this.segmentIdentify.assertValue(user)
    }

    @Test
    fun testDiscoveryActivity_CTA_Clicked_Properties() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoverProjectCTAClicked()

        assertSessionProperties(user)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertEquals(DISCOVER.contextName, expectedProperties[CONTEXT_CTA.contextName])
        assertEquals(ACTIVITY_FEED.contextName, expectedProperties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testDiscoveryProperties_Category() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .category(CategoryFactory.ceramicsCategory())
            .sort(DiscoveryParams.Sort.NEWEST)
            .build()

        segment.trackDiscoveryPageViewed(params)

        assertSessionProperties(user)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertEquals("1", expectedProperties["discover_category_id"])
        assertEquals("categoryName", expectedProperties["discover_category_name"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(false, expectedProperties["discover_pwl"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals("category_newest", expectedProperties["discover_ref_tag"])
        assertEquals(null, expectedProperties["discover_search_term"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("newest", expectedProperties["discover_sort"])
        assertEquals("287", expectedProperties["discover_subcategory_id"])
        assertEquals("subcategoryName", expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])

        this.segmentIdentify.assertValue(user)
    }

    @Test
    fun testSearchCta_Properties() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        val params = DiscoveryParams
            .builder()
            .category(CategoryFactory.ceramicsCategory())
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()

        segment.trackSearchCTAButtonClicked(params)

        assertSessionProperties(user)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value

        assertEquals("1", expectedProperties["discover_category_id"])
        assertEquals("categoryName", expectedProperties["discover_category_name"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(false, expectedProperties["discover_pwl"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals("category", expectedProperties["discover_ref_tag"])
        assertEquals(null, expectedProperties["discover_search_term"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals("magic", expectedProperties["discover_sort"])
        assertEquals("287", expectedProperties["discover_subcategory_id"])
        assertEquals("subcategoryName", expectedProperties["discover_subcategory_name"])
        assertEquals(null, expectedProperties["discover_tag"])
        assertEquals(false, expectedProperties["discover_watched"])

        assertEquals(SEARCH.contextName, expectedProperties[CONTEXT_CTA.contextName])
        assertEquals(GLOBAL_NAV.contextName, expectedProperties[CONTEXT_LOCATION.contextName])

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testProjectProperties_loggedOutUser() {
        val project = project()

        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), EventContextValues.ContextSectionName.OVERVIEW.contextName)

        assertSessionProperties(null)
        assertContextProperties()
        assertProjectProperties(project)

        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["user_uid"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)

        this.segmentIdentify.assertNoValues()
    }

    @Test
    fun testProjectProperties_hasAddOns() {
        val project = ProjectFactory.projectWithAddOns()

        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

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

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

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
        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )
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
        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )
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
        client.identifiedUser.subscribe(this.segmentIdentify)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        // assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)
        this.segmentIdentify.assertValue(user)
    }

    @Test
    fun testProjectProperties_LoggedInUser_IsBacker() {
        val project = ProjectFactory.backedProject()
            .toBuilder()
            .id(4)
            .tags(listOfTags())
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

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(true, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)
    }

    @Test
    fun testProjectProperties_LoggedInUser_IsProjectCreator() {
        val project = project().toBuilder().build()
        val creator = creator()
        val client = client(creator)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        assertSessionProperties(creator)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = this.propertiesTest.value

        assertNull(expectedProperties["context_pledge_flow"])

        assertEquals(17, expectedProperties["user_backed_projects_count"])
        assertEquals(false, expectedProperties["user_is_admin"])
        assertEquals(5, expectedProperties["user_launched_projects_count"])
        assertEquals("3", expectedProperties["user_uid"])
        assertEquals("US", expectedProperties["user_country"])

        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(true, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)
    }

    @Test
    fun testProjectProperties_LoggedInUser_HasStarred() {
        val project = project().toBuilder().isStarred(true).build()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = this.propertiesTest.value
        assertEquals(true, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)
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
        segment.trackPledgeInitiateCTA(projectData)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value
        // assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues(CTA_CLICKED.eventName)
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

        segment.trackSelectRewardCTA(PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward(), listOfAddons()))

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

        this.segmentTrack.assertValues(CTA_CLICKED.eventName)
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

        segment.trackPledgeSubmitCTA(
            CheckoutDataFactory.checkoutData(20.0, 30.0),
            PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward(), listOfAddons())
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

        this.segmentTrack.assertValues(CTA_CLICKED.eventName)
    }

    @Test
    fun testManagePledgePageViewed() {
        val project = ProjectFactory.backedProject()
            .toBuilder()
            .id(4)
            .tags(listOfTags())
            .category(CategoryFactory.ceramicsCategory())
            .commentsCount(3)
            .creator(creator())
            .location(LocationFactory.unitedStates())
            .updatesCount(5)
            .build()

        val addOn1 = RewardFactory.addOn()
        val addOn2 = RewardFactory.addOnMultiple()

        val backing = BackingFactory.backing().toBuilder()
            .project(project)
            .addOns(listOf(addOn1, addOn2))
            .bonusAmount(35.0)
            .shippingAmount(20f)
            .location(LocationFactory.germany())
            .locationId(LocationFactory.germany().id())
            .build()

        val creator = creator()
        val client = client(creator)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackManagePledgePageViewed(backing = backing, projectData = projectData)

        assertSessionProperties(creator)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = this.propertiesTest.value

        // - we test asserting this properties all methods in SharedFunctions.kt
        assertEquals(10.0, expectedProperties["checkout_amount"])
        assertEquals("credit_card", expectedProperties["checkout_payment_type"])
        assertEquals(10.0, expectedProperties["checkout_amount_total_usd"])
        assertEquals(20.0, expectedProperties["checkout_shipping_amount_usd"])
        assertEquals(5, expectedProperties["checkout_add_ons_count_total"])
        assertEquals(2, expectedProperties["checkout_add_ons_count_unique"])
        assertEquals(100.0, expectedProperties["checkout_add_ons_minimum_usd"])
        assertEquals(35.0, expectedProperties["checkout_bonus_amount_usd"])
        assertEquals(MANAGE_PLEDGE.contextName, expectedProperties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun testUpdatePledgePageViewed() {
        val project = ProjectFactory.backedProject()
            .toBuilder()
            .id(4)
            .category(CategoryFactory.ceramicsCategory())
            .commentsCount(3)
            .creator(creator())
            .location(LocationFactory.unitedStates())
            .tags(listOfTags())
            .updatesCount(5)
            .build()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackUpdatePledgePageViewed(
            CheckoutDataFactory.checkoutData(20.0, 30.0),
            PledgeData.with(PledgeFlowContext.MANAGE_REWARD, projectData, reward(), listOfAddons())
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertCheckoutProperties()
        assertUserProperties(false)

        val expectedProperties = this.propertiesTest.value
        assertEquals(UPDATE_PLEDGE.contextName, expectedProperties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun testCheckoutProperties_whenFixingPledge() {
        val project = ProjectFactory.backedProject()
            .toBuilder()
            .id(4)
            .tags(listOfTags())
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

        val rewardTest = reward()
        segment.trackPledgeSubmitCTA(
            CheckoutDataFactory.checkoutData(20.0, 30.0),
            PledgeData.with(PledgeFlowContext.FIX_ERRORED_PLEDGE, projectData, reward(), listOfAddons())
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

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
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

        segment.trackThanksScreenViewed(
            CheckoutDataFactory.checkoutData(3L, 20.0, 30.0),
            PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward(), listOfAddons())
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

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)
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

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)
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
        assertPageContextProperty(TWO_FACTOR_AUTH.contextName)

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)
    }

//    @Test
//    fun testOptimizelyProperties() {
//        val project = project()
//        val user = user()
//        val client = client(user)
//        client.eventNames.subscribe(this.segmentTrack)
//        client.eventProperties.subscribe(this.propertiesTest)
//        val segment = AnalyticEvents(listOf(client))
//
//        segment.trackPledgeInitiateCTA(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()))
//
//        assertSessionProperties(user)
//        assertProjectProperties(project)
//        assertContextProperties()
//        assertOptimizelyProperties()
//        assertUserProperties(false)
//
//        val expectedProperties = propertiesTest.value
//        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
//        assertEquals(false, expectedProperties["project_user_has_watched"])
//        assertEquals(false, expectedProperties["project_user_is_backer"])
//        assertEquals(false, expectedProperties["project_user_is_project_creator"])
//
//        this.segmentTrack.assertValues("Project Page Pledge Button Clicked")
//    }

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

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        segment.trackVideoStarted(project, videoLength, videoStartedPosition)

        assertVideoProperties(videoLength, videoStartedPosition)

        segment.trackVideoCompleted(project, videoLength, videoCompletedPosition)

        assertVideoProperties(videoLength, videoCompletedPosition)

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName, VIDEO_PLAYBACK_STARTED.eventName, VIDEO_PLAYBACK_COMPLETED.eventName)
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

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
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
        assertEquals("categoryName", properties["discover_category_name"])
        assertEquals("subcategoryName", properties["discover_subcategory_name"])
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

        assertEquals(DISCOVER_ADVANCED.contextName, properties[CONTEXT_LOCATION.contextName])
        assertEquals(DISCOVER.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(DISCOVER_SORT.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals("ending_soon", properties[CONTEXT_TYPE.contextName])
        assertEquals("popular", properties["discover_sort"])
    }

    @Test
    fun testSignUpInitiateCtaClicked_Properties() {
        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)

        val segment = AnalyticEvents(listOf(client))
        segment.trackSignUpInitiateCtaClicked()

        assertSessionProperties(null)
        assertContextProperties()

        val properties = this.propertiesTest.value
        assertNull(properties["user_uid"])
        assertEquals(LOGIN_SIGN_UP.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(SIGN_UP_INITIATE.contextName, properties[CONTEXT_CTA.contextName])

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testSignUpPageViewed_Properties() {

        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)

        val segment = AnalyticEvents(listOf(client))
        segment.trackSignUpPageViewed()

        assertSessionProperties(null)
        assertContextProperties()

        val properties = this.propertiesTest.value
        assertNull(properties["user_uid"])
        assertEquals(SIGN_UP.contextName, properties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun testLoginOrSignUpPageViewed_Properties() {

        val client = client(null)
        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)

        val segment = AnalyticEvents(listOf(client))
        segment.trackLoginOrSignUpPagedViewed()

        assertSessionProperties(null)
        assertContextProperties()

        val properties = this.propertiesTest.value
        assertNull(properties["user_uid"])
        assertEquals(LOGIN_SIGN_UP.contextName, properties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun trackDiscoverFilterCTA_whenFilterPresent_returnsCorrectFilter() {
        val user = user()
        val client = client(user)

        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)

        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoverFilterCTA(discoveryParams())

        val properties = this.propertiesTest.value

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)

        assertEquals(DISCOVER_FILTER.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals(DISCOVER_OVERLAY.contextName, properties[CONTEXT_LOCATION.contextName])
        assertEquals(DISCOVER.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(PWL.contextName, properties[CONTEXT_TYPE.contextName])
        assertEquals("categoryName", properties["discover_category_name"])
        assertEquals(false, properties["discover_everything"])
        assertEquals(true, properties["discover_pwl"])
        assertEquals(false, properties["discover_recommended"])
        assertEquals(true, properties["discover_social"])
        assertEquals(true, properties["discover_watched"])
        assertEquals("category_ending_soon", properties["discover_ref_tag"])
        assertEquals("hello world", properties["discover_search_term"])
        assertEquals("ending_soon", properties["discover_sort"])
        assertEquals("287", properties["discover_subcategory_id"])
        assertEquals("subcategoryName", properties["discover_subcategory_name"])
        assertEquals(123, properties["discover_tag"])
    }

    @Test
    fun trackDiscoverFilterCTA_whenFilterNotPresent_returnsAllProjectsFilter() {
        val user = user()
        val client = client(user)
        val discoveryParams =
            discoveryParams()
                .toBuilder()
                .staffPicks(false)
                .recommended(false)
                .starred(0)
                .social(0)
                .category(null)
                .build()

        client.eventNames.subscribe(this.segmentTrack)
        client.eventProperties.subscribe(this.propertiesTest)

        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoverFilterCTA(discoveryParams)

        val properties = this.propertiesTest.value

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)

        assertEquals(DISCOVER_FILTER.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals(DISCOVER_OVERLAY.contextName, properties[CONTEXT_LOCATION.contextName])
        assertEquals(DISCOVER.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(ALL.contextName, properties[CONTEXT_TYPE.contextName])
        assertEquals(false, properties["discover_everything"])
        assertEquals(false, properties["discover_pwl"])
        assertEquals(false, properties["discover_recommended"])
        assertEquals(false, properties["discover_social"])
        assertEquals(false, properties["discover_watched"])
        assertEquals("city", properties["discover_ref_tag"])
        assertEquals("hello world", properties["discover_search_term"])
        assertEquals("ending_soon", properties["discover_sort"])
        assertEquals(123, properties["discover_tag"])
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

            override fun getTrackingProperties(): Map<String, Array<String>> {
                return getOptimizelySession()
            }
        }
    )

    private fun getOptimizelySession(): Map<String, Array<String>> {
        val array = arrayOf("suggested_no_reward_amount[variation_3]")
        return mapOf("variants_optimizely" to array)
    }

    private fun assertCheckoutProperties() {
        val expectedProperties = this.propertiesTest.value
        assertEquals(30.0, expectedProperties["checkout_amount"])
        assertEquals("credit_card", expectedProperties["checkout_payment_type"])
        assertEquals(30.0, expectedProperties["checkout_amount_total_usd"])
        assertEquals(20.0, expectedProperties["checkout_shipping_amount"])
        assertEquals(20.0, expectedProperties["checkout_shipping_amount_usd"])
        assertEquals(6, expectedProperties["checkout_add_ons_count_total"])
        assertEquals(2, expectedProperties["checkout_add_ons_count_unique"])
        assertEquals(110.71, expectedProperties["checkout_add_ons_minimum_usd"])
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

    private fun assertDiscoverProperties() {
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
        assertEquals(6, expectedProperties["checkout_add_ons_count_total"])
        assertEquals(2, expectedProperties["checkout_add_ons_count_unique"])
        assertEquals(110.71, expectedProperties["checkout_add_ons_minimum_usd"])
    }

    private fun assertProjectProperties(project: Project) {
        val expectedProperties = this.propertiesTest.value
        assertEquals(100, expectedProperties["project_backers_count"])
        assertEquals("subcategoryName", expectedProperties["project_subcategory"])
        assertEquals("categoryName", expectedProperties["project_category"])
        assertEquals(3, expectedProperties["project_comments_count"])
        assertEquals("US", expectedProperties["project_country"])
        assertEquals("3", expectedProperties["project_creator_uid"])
        assertEquals("USD", expectedProperties["project_currency"])
        assertNotNull(expectedProperties["project_prelaunch_activated"])
        assertEquals(50.0, expectedProperties["project_current_pledge_amount"])
        assertEquals(50.0, expectedProperties["project_current_amount_pledged_usd"])
        assertEquals(project.deadline(), expectedProperties["project_deadline"])
        assertEquals(20, expectedProperties["project_duration"])
        assertEquals(100.0, expectedProperties["project_goal"])
        assertEquals(100.0, expectedProperties["project_goal_usd"])
        assertEquals(true, expectedProperties["project_has_video"])
        assertEquals(10 * 24, expectedProperties["project_hours_remaining"])
        assertEquals(true, expectedProperties["project_is_repeat_creator"])
        assertEquals(project.launchedAt(), expectedProperties["project_launched_at"])
        assertEquals("Brooklyn", expectedProperties["project_location"])
        assertEquals("Some Name", expectedProperties["project_name"])
        assertEquals(50, expectedProperties["project_percent_raised"])
        assertEquals("4", expectedProperties["project_pid"])
        assertEquals(50.0, expectedProperties["project_current_pledge_amount"])
        assertEquals(1, expectedProperties["project_rewards_count"])
        assertEquals("live", expectedProperties["project_state"])
        assertEquals(1.0f, expectedProperties["project_static_usd_rate"])
        assertEquals(5, expectedProperties["project_updates_count"])
        assertEquals("tag1, tag2, tag3", expectedProperties["project_tags"])
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
        assertEquals("portrait", expectedProperties["session_device_orientation"])
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
        assertEquals(6, expectedProperties["user_launched_projects_count"])
        assertEquals(9, expectedProperties["user_created_projects_count"])
        assertEquals(true, expectedProperties["user_facebook_connected"])
        assertEquals(10, expectedProperties["user_watched_projects_count"])
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
            .tags(listOfTags())
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
            .draftProjectsCount(3)
            .facebookConnected(true)
            .createdProjectsCount(6)
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

    private fun listOfAddons(): java.util.List<Reward>? =
        listOf(RewardFactory.addOnSingle().toBuilder().minimum(10.06).build(), RewardFactory.addOnMultiple().toBuilder().minimum(20.13).build()) as java.util.List<Reward>?

    private fun listOfTags(): List<String> = listOf("tag1", "tag2", "tag3")
}
