package com.kickstarter.libs

import android.content.Context
import android.content.SharedPreferences
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCardFactory
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ContextPropertyKeyName
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_BODY
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_CHARACTER_COUNT
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_CTA
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_LOCATION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_PAGE
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_TYPE
import com.kickstarter.libs.utils.ContextPropertyKeyName.PROJECT_UPDATE_ID
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ACTIVITY_FEED
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN_SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.MANAGE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT_ALERTS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.THANKS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.TWO_FACTOR_AUTH
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.ADDRESS
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CONFIRM_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CONFIRM_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_FILTER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_SORT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.EDIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.FINALIZE_PLEDGE_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.FIX_PLEDGE_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.MESSAGE_CREATOR_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SEARCH
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SIGN_UP_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SURVEY_RESPONSE_INITIATE
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
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.PhotoFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.Urls
import com.kickstarter.models.User
import com.kickstarter.models.Web
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import org.junit.Test

class SegmentTest : KSRobolectricTestCase() {

    private val propertiesTest = BehaviorSubject.create<Map<String, Any>>()
    lateinit var build: Build
    lateinit var context: Context
    private val disposables = CompositeDisposable()

    private val mockShared: SharedPreferences = MockSharedPreferences()

    override fun setUp() {
        super.setUp()
        build = requireNotNull(environment().build())
        context = application()
    }

    class MockSegmentTrackingClient(
        build: Build,
        context: Context,
        currentConfig: CurrentConfigTypeV2,
        currentUser: CurrentUserTypeV2,
        ffClient: FeatureFlagClientType,
        mockSharedPref: SharedPreferences
    ) : SegmentTrackingClient(build, context, currentConfig, currentUser, ffClient, mockSharedPref) {

        override fun initialize() {
            this.isInitialized = true
        }
        override fun isEnabled() = this.isInitialized
    }

    @Test
    fun testSegmentClientIsEnabled_whenFeatureNotEnabled_returnIsEnabledTrue() {
        val user = UserFactory.user()
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return false
            }
        }

        val client = SegmentTrackingClient(build, context, mockCurrentConfig(), MockCurrentUserV2(user), mockFeatureFlagClient, mockShared)
        client.initialize()
        assertFalse(mockShared.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE))
        assertTrue(client.isEnabled())
    }

    @Test
    fun testSegmentClientIsEnabled_whenFeatureEnabledAndConsentNotPresent_returnIsEnabledFalse() {
        val user = UserFactory.user()
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }

        val client = SegmentTrackingClient(build, context, mockCurrentConfig(), MockCurrentUserV2(user), mockFeatureFlagClient, mockShared)
        client.initialize()
        assertFalse(mockShared.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE))
        assertFalse(client.isEnabled())
    }

    @Test
    fun testSegmentClientIsEnabled_whenFeatureEnabledAndConsentPrefTrue_returnIsEnabledTrue() {
        val user = UserFactory.user()
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }

        mockShared.edit().putBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, true)
        val client = SegmentTrackingClient(build, context, mockCurrentConfig(), MockCurrentUserV2(user), mockFeatureFlagClient, mockShared)
        client.initialize()
        assertTrue(mockShared.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE))
        assertTrue(client.isEnabled())
    }

    @Test
    fun testSegmentClientIsEnabled_whenFeatureEnabledAndConsentPrefFalse_returnIsEnabledFalse() {
        val user = UserFactory.user()
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }

        mockShared.edit().putBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)

        val client = SegmentTrackingClient(build, context, mockCurrentConfig(), MockCurrentUserV2(user), mockFeatureFlagClient, mockShared)
        client.initialize()
        assertTrue(mockShared.contains(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE))
        assertFalse(client.isEnabled())
    }

    @Test
    fun testSegmentClientTest() {
        val user = UserFactory.user()
        val mockFeatureFlagClient = MockFeatureFlagClient()

        val mockClient = MockSegmentTrackingClient(build, context, mockCurrentConfig(), MockCurrentUserV2(user), mockFeatureFlagClient, mockShared)
        mockClient.initialize()
        assertNotNull(mockClient)
        assertTrue(mockClient.isEnabled())
    }

    @Test
    fun testDefaultProperties() {
        val client = client(null)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackActivityFeedPageViewed()

        val expectedProperties = propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackCampaignDetailsCTAClicked(projectData)
        this.segmentIdentify.assertValue(user)

        assertSessionProperties(user)
        assertContextProperties()
        assertProjectProperties(projectData.project())
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value ?: mapOf()
        assertEquals("campaign_details", expectedProperties["context_cta"])
        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testDiscoveryProperties_AllProjects() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = propertiesTest.value ?: mapOf()

        assertEquals("magic", expectedProperties[DISCOVER_SORT.contextName])
        assertEquals(DISCOVER.contextName, expectedProperties[CONTEXT_PAGE.contextName])
        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun testDiscoveryProjectCtaClickedProperties_AllProjects() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = propertiesTest.value ?: mapOf()

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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = propertiesTest.value ?: mapOf()

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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = propertiesTest.value ?: mapOf()

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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoverProjectCTAClicked()

        assertSessionProperties(user)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value ?: mapOf()
        assertEquals(DISCOVER.contextName, expectedProperties[CONTEXT_CTA.contextName])
        assertEquals(ACTIVITY_FEED.contextName, expectedProperties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testDiscoveryProperties_Category() {
        val user = user()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = propertiesTest.value ?: mapOf()

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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()), EventContextValues.ContextSectionName.OVERVIEW.contextName)

        assertSessionProperties(null)
        assertContextProperties()
        assertProjectProperties(project)

        val expectedProperties = propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertNull(expectedProperties["user_uid"])
        assertEquals(true, expectedProperties["project_has_add_ons"])
    }

    @Test
    fun `testProjectProperties project_post_campaign_enabled=true and project_state=post_campaign when latePledges enabled`() {
        val project = ProjectFactory.projectWithAddOns()
            .toBuilder()
            .isInPostCampaignPledgingPhase(true)
            .postCampaignPledgingEnabled(true)
            .build()

        val client = client(null)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertEquals(true, expectedProperties["project_project_post_campaign_enabled"])
        assertEquals("post_campaign", expectedProperties["project_state"])
    }

    @Test
    fun `testProjectProperties project_post_campaign_enabled=false and project_state=live when latePledges disabled`() {
        val project = ProjectFactory.projectWithAddOns()
            .toBuilder()
            .state("live")
            .isInPostCampaignPledgingPhase(false)
            .postCampaignPledgingEnabled(true)
            .build()

        val client = client(null)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertEquals(false, expectedProperties["project_project_post_campaign_enabled"])
        assertEquals("live", expectedProperties["project_state"])
    }

    @Test
    fun testProjectProperties_hasProject_prelaunch_activated() {
        val project = ProjectFactory.projectWithAddOns()

        val client = client(null)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertNotNull(expectedProperties["project_prelaunch_activated"])
    }

    @Test
    fun testProjectProperties_tab_selection() {
        val project = ProjectFactory.projectWithAddOns()

        val client = client(null)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectPageTabChanged(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertNotNull(expectedProperties["project_prelaunch_activated"])
    }

    @Test
    fun testProjectProperties_hasProject_prelaunch_activated_true() {
        val project = ProjectFactory.projectWithAddOns()
            .toBuilder()
            .prelaunchActivated(true)
            .build()
        val client = client(null)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))
        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )
        val expectedProperties = this.propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))
        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )
        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertNotNull(expectedProperties["project_prelaunch_activated"])
        assertEquals(false, expectedProperties["project_prelaunch_activated"])
    }

    @Test
    fun testProjectProperties_LoggedInUser() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value ?: mapOf()
        // assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)
        this.segmentIdentify.assertValue(user)
    }

    @Test
    fun testProjectProperties_LoggedInUser_IsBacker() {
        val project = backedProject()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        assertSessionProperties(creator)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = this.propertiesTest.value ?: mapOf()

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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        segment.trackProjectScreenViewed(
            ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended()),
            EventContextValues.ContextSectionName.OVERVIEW.contextName
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = this.propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())
        segment.trackPledgeInitiateCTA(projectData)

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackSelectRewardCTA(PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward(), listOfAddons()))

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
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

        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertNull(expectedProperties["checkout_id"])
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues(CTA_CLICKED.eventName)
    }

    @Test
    fun testCheckoutProperties_whenConfirmPledge() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackPledgeConfirmCTA(
            CheckoutDataFactory.checkoutData(20.0, 30.0),
            PledgeData.with(PledgeFlowContext.NEW_PLEDGE, projectData, reward(), listOfAddons())
        )

        assertSessionProperties(user)
        assertProjectProperties(project)
        assertContextProperties()
        assertPledgeProperties()
        assertCheckoutProperties()
        assertUserProperties(false)

        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertNull(expectedProperties["checkout_id"])
        assertEquals("new_pledge", expectedProperties["context_pledge_flow"])
        assertEquals(false, expectedProperties["project_user_has_watched"])
        assertEquals(false, expectedProperties["project_user_is_backer"])
        assertEquals(false, expectedProperties["project_user_is_project_creator"])

        this.segmentTrack.assertValues(CTA_CLICKED.eventName)
    }

    @Test
    fun testManagePledgePageViewed() {
        val project = backedProject()

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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        val projectData = ProjectDataFactory.project(project, RefTag.discovery(), RefTag.recommended())

        segment.trackManagePledgePageViewed(backing = backing, projectData = projectData)

        assertSessionProperties(creator)
        assertProjectProperties(project)
        assertContextProperties()

        val expectedProperties = this.propertiesTest.value ?: mapOf()

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
        val project = backedProject()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

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

        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertEquals(EventContextValues.ContextPageName.UPDATE_PLEDGE.contextName, expectedProperties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun testCheckoutProperties_whenFixingPledge() {
        val project = backedProject()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

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

        val expectedProperties = this.propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

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

        val expectedProperties = this.propertiesTest.value ?: mapOf()
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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackTwoFactorAuthPageViewed()

        assertSessionProperties(null)
        assertContextProperties()
        assertPageContextProperty(TWO_FACTOR_AUTH.contextName)

        this.segmentTrack.assertValues(PAGE_VIEWED.eventName)
    }

    @Test
    fun testVideoProperties() {
        val project = project()
        val user = user()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))
        segment.trackLoginPagedViewed()

        assertSessionProperties(null)
        assertContextProperties()

        val properties = this.propertiesTest.value ?: mapOf()
        assertNull(properties["user_uid"])
        assertEquals(LOGIN.contextName, properties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun discoveryParamProperties_whenAllFieldsPopulated_shouldReturnExpectedProps() {
        val user = user()
        val client = client(user)

        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoveryPageViewed(discoveryParams())

        val properties = this.propertiesTest.value ?: mapOf()

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

        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoverSortCTA(DiscoveryParams.Sort.POPULAR, discoveryParams())

        val properties = this.propertiesTest.value ?: mapOf()

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
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))
        segment.trackSignUpInitiateCtaClicked()

        assertSessionProperties(null)
        assertContextProperties()

        val properties = this.propertiesTest.value ?: mapOf()
        assertNull(properties["user_uid"])
        assertEquals(LOGIN_SIGN_UP.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(SIGN_UP_INITIATE.contextName, properties[CONTEXT_CTA.contextName])

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testSignUpPageViewed_Properties() {

        val client = client(null)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))
        segment.trackSignUpPageViewed()

        assertSessionProperties(null)
        assertContextProperties()

        val properties = this.propertiesTest.value ?: mapOf()
        assertNull(properties["user_uid"])
        assertEquals(SIGN_UP.contextName, properties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun testTrackThreadCommentPageViewed_Properties() {
        val user = user()
        val project = project()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        val commentId = "1"
        segment.trackThreadCommentPageViewed(
            project,
            commentId
        )
        this.segmentIdentify.assertValue(user)

        assertSessionProperties(user)
        assertContextProperties()
        assertPageContextProperty(PROJECT.contextName)
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value ?: mapOf()
        assertEquals(commentId, expectedProperties[ContextPropertyKeyName.COMMENT_ROOT_ID.contextName])
        assertNull(expectedProperties[PROJECT_UPDATE_ID.contextName])
        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun testTrackCommentReplyCTA_Properties() {
        val user = user()
        val project = project()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        val reply = "comment"
        val commentID = "1"
        segment.trackCommentCTA(
            project,
            commentID,
            reply
        )
        this.segmentIdentify.assertValue(user)

        assertSessionProperties(user)
        assertContextProperties()
        assertPageContextProperty(PROJECT.contextName)
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value ?: mapOf()
        assertEquals(commentID, expectedProperties[ContextPropertyKeyName.COMMENT_ID.contextName])
        assertEquals(reply, expectedProperties[COMMENT_BODY.contextName])
        assertEquals(reply.length, expectedProperties[COMMENT_CHARACTER_COUNT.contextName])
        assertNull(expectedProperties[PROJECT_UPDATE_ID.contextName])
        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testTrackRootCommentReplyCTA_Properties() {
        val user = user()
        val project = project()
        val client = client(user)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)
        client.identifiedUser.subscribe { this.segmentIdentify.onNext(it) }.addToDisposable(disposables)
        val segment = AnalyticEvents(listOf(client))

        val reply = "comment"
        val commentID = "34879063"
        val rootCommentID = "1"
        segment.trackRootCommentReplyCTA(
            project,
            commentID,
            reply,
            rootCommentID
        )
        this.segmentIdentify.assertValue(user)

        assertSessionProperties(user)
        assertContextProperties()
        assertPageContextProperty(PROJECT.contextName)
        assertUserProperties(false)

        val expectedProperties = propertiesTest.value ?: mapOf()
        assertEquals(commentID, expectedProperties[ContextPropertyKeyName.COMMENT_ID.contextName])
        assertEquals(rootCommentID, expectedProperties[ContextPropertyKeyName.COMMENT_ROOT_ID.contextName])
        assertEquals(reply, expectedProperties[COMMENT_BODY.contextName])
        assertEquals(reply.length, expectedProperties[COMMENT_CHARACTER_COUNT.contextName])
        assertNull(expectedProperties[PROJECT_UPDATE_ID.contextName])
        this.segmentTrack.assertValue(CTA_CLICKED.eventName)
    }

    @Test
    fun testLoginOrSignUpPageViewed_Properties() {

        val client = client(null)
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))
        segment.trackLoginOrSignUpPagedViewed()

        assertSessionProperties(null)
        assertContextProperties()

        val properties = this.propertiesTest.value ?: mapOf()
        assertNull(properties["user_uid"])
        assertEquals(LOGIN_SIGN_UP.contextName, properties[CONTEXT_PAGE.contextName])

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)
    }

    @Test
    fun `test ppo page viewed event`() {
        val user = user()
        val client = client(user)

        val ppoCards = listOf(PPOCardFactory.confirmAddressCard(), PPOCardFactory.confirmAddressCard(), PPOCardFactory.fixPaymentCard())
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackPledgedProjectsOverviewPageViewed(ppoCards, 10)

        val properties = this.propertiesTest.value ?: mapOf()

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)
        val expectedProperties = this.propertiesTest.value ?: mapOf()

        this.segmentTrack.assertValue(PAGE_VIEWED.eventName)

        assertEquals(PROJECT_ALERTS.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(2, expectedProperties["notification_count_address_locks_soon"])
        assertEquals(1, expectedProperties["notification_count_payment_failed"])
        assertEquals(0, expectedProperties["notification_count_card_auth_required"])
        assertEquals(0, expectedProperties["notification_count_survey_available"])
        assertEquals(0, expectedProperties["notification_count_pledge_management"])
        assertEquals(10, expectedProperties["notification_count_total"])
    }

    @Test
    fun `test ppo message creator click event`() {
        val user = user()
        val client = client(user)

        val ppoCards = listOf(PPOCardFactory.confirmAddressCard(), PPOCardFactory.confirmAddressCard(), PPOCardFactory.fixPaymentCard())
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackPPOMessageCreatorCTAClicked("123123", ppoCards, 11, "09231")

        val properties = this.propertiesTest.value ?: mapOf()

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)
        val expectedProperties = this.propertiesTest.value ?: mapOf()

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)

        assertEquals(PROJECT_ALERTS.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(MESSAGE_CREATOR_INITIATE.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals("123123", expectedProperties["project_pid"])
        assertEquals("09231", expectedProperties["interaction_target_id"])
        assertEquals(2, expectedProperties["notification_count_address_locks_soon"])
        assertEquals(1, expectedProperties["notification_count_payment_failed"])
        assertEquals(0, expectedProperties["notification_count_card_auth_required"])
        assertEquals(0, expectedProperties["notification_count_survey_available"])
        assertEquals(0, expectedProperties["notification_count_pledge_management"])
        assertEquals(11, expectedProperties["notification_count_total"])
    }

    @Test
    fun `test ppo fix payment click event`() {
        val user = user()
        val client = client(user)

        val ppoCards = listOf(PPOCardFactory.confirmAddressCard(), PPOCardFactory.confirmAddressCard(), PPOCardFactory.fixPaymentCard())
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackPPOFixPaymentCTAClicked("123123", ppoCards, 11)

        val properties = this.propertiesTest.value ?: mapOf()

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)
        val expectedProperties = this.propertiesTest.value ?: mapOf()

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)

        assertEquals(PROJECT_ALERTS.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(FIX_PLEDGE_INITIATE.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals("123123", expectedProperties["project_pid"])
        assertEquals(2, expectedProperties["notification_count_address_locks_soon"])
        assertEquals(1, expectedProperties["notification_count_payment_failed"])
        assertEquals(0, expectedProperties["notification_count_card_auth_required"])
        assertEquals(0, expectedProperties["notification_count_survey_available"])
        assertEquals(0, expectedProperties["notification_count_pledge_management"])
        assertEquals(11, expectedProperties["notification_count_total"])
    }

    @Test
    fun `test ppo open survey click event`() {
        val user = user()
        val client = client(user)

        val ppoCards = listOf(PPOCardFactory.confirmAddressCard(), PPOCardFactory.confirmAddressCard(), PPOCardFactory.fixPaymentCard())
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackPPOOpenSurveyCTAClicked("123123", ppoCards, 11, "9023234")

        val properties = this.propertiesTest.value ?: mapOf()

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)
        val expectedProperties = this.propertiesTest.value ?: mapOf()

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)

        assertEquals(PROJECT_ALERTS.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(SURVEY_RESPONSE_INITIATE.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals("123123", expectedProperties["project_pid"])
        assertEquals("9023234", expectedProperties["survey_id"])
        assertEquals(2, expectedProperties["notification_count_address_locks_soon"])
        assertEquals(1, expectedProperties["notification_count_payment_failed"])
        assertEquals(0, expectedProperties["notification_count_card_auth_required"])
        assertEquals(0, expectedProperties["notification_count_survey_available"])
        assertEquals(0, expectedProperties["notification_count_pledge_management"])
        assertEquals(11, expectedProperties["notification_count_total"])
    }

    @Test
    fun `test ppo finalize pledge click event`() {
        val user = user()
        val client = client(user)

        val ppoCards = listOf(PPOCardFactory.pledgeManagementCard(), PPOCardFactory.pledgeManagementCard(), PPOCardFactory.fixPaymentCard())
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackPPOFinalizePledgeCTAClicked("123123", ppoCards, 12)

        val properties = this.propertiesTest.value ?: mapOf()

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)
        val expectedProperties = this.propertiesTest.value ?: mapOf()

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)

        assertEquals(PROJECT_ALERTS.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(FINALIZE_PLEDGE_INITIATE.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals("123123", expectedProperties["project_pid"])
        assertEquals(0, expectedProperties["notification_count_address_locks_soon"])
        assertEquals(1, expectedProperties["notification_count_payment_failed"])
        assertEquals(0, expectedProperties["notification_count_card_auth_required"])
        assertEquals(0, expectedProperties["notification_count_survey_available"])
        assertEquals(2, expectedProperties["notification_count_pledge_management"])
        assertEquals(12, expectedProperties["notification_count_total"])
    }

    @Test
    fun `test ppo confirm address initiate click event`() {
        val user = user()
        val client = client(user)

        val ppoCards = listOf(PPOCardFactory.confirmAddressCard(), PPOCardFactory.confirmAddressCard(), PPOCardFactory.fixPaymentCard())
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackPPOConfirmAddressInitiateCTAClicked("123123", ppoCards, 11)

        val properties = this.propertiesTest.value ?: mapOf()

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)
        val expectedProperties = this.propertiesTest.value ?: mapOf()

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)

        assertEquals(PROJECT_ALERTS.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(CONFIRM_INITIATE.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals(ADDRESS.contextName, properties[CONTEXT_TYPE.contextName])
        assertEquals("123123", expectedProperties["project_pid"])
        assertEquals(2, expectedProperties["notification_count_address_locks_soon"])
        assertEquals(1, expectedProperties["notification_count_payment_failed"])
        assertEquals(0, expectedProperties["notification_count_card_auth_required"])
        assertEquals(0, expectedProperties["notification_count_survey_available"])
        assertEquals(0, expectedProperties["notification_count_pledge_management"])
        assertEquals(11, expectedProperties["notification_count_total"])
    }

    @Test
    fun `test ppo confirm address submit click event`() {
        val user = user()
        val client = client(user)

        val ppoCards = listOf(PPOCardFactory.confirmAddressCard(), PPOCardFactory.confirmAddressCard(), PPOCardFactory.fixPaymentCard())
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackPPOConfirmAddressSubmitCTAClicked("123123", ppoCards, 11)

        val properties = this.propertiesTest.value ?: mapOf()

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)
        val expectedProperties = this.propertiesTest.value ?: mapOf()

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)

        assertEquals(PROJECT_ALERTS.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(CONFIRM_SUBMIT.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals(ADDRESS.contextName, properties[CONTEXT_TYPE.contextName])
        assertEquals("123123", expectedProperties["project_pid"])
        assertEquals(2, expectedProperties["notification_count_address_locks_soon"])
        assertEquals(1, expectedProperties["notification_count_payment_failed"])
        assertEquals(0, expectedProperties["notification_count_card_auth_required"])
        assertEquals(0, expectedProperties["notification_count_survey_available"])
        assertEquals(0, expectedProperties["notification_count_pledge_management"])
        assertEquals(11, expectedProperties["notification_count_total"])
    }

    @Test
    fun `test ppo confirm address edit click event`() {
        val user = user()
        val client = client(user)

        val ppoCards = listOf(PPOCardFactory.confirmAddressCard(), PPOCardFactory.confirmAddressCard(), PPOCardFactory.fixPaymentCard())
        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackPPOConfirmAddressEditCTAClicked("123123", ppoCards, 11)

        val properties = this.propertiesTest.value ?: mapOf()

        assertContextProperties()
        assertUserProperties(false)
        assertSessionProperties(user)
        val expectedProperties = this.propertiesTest.value ?: mapOf()

        this.segmentTrack.assertValue(CTA_CLICKED.eventName)

        assertEquals(PROJECT_ALERTS.contextName, properties[CONTEXT_PAGE.contextName])
        assertEquals(EDIT.contextName, properties[CONTEXT_CTA.contextName])
        assertEquals(ADDRESS.contextName, properties[CONTEXT_TYPE.contextName])
        assertEquals("123123", expectedProperties["project_pid"])
        assertEquals(2, expectedProperties["notification_count_address_locks_soon"])
        assertEquals(1, expectedProperties["notification_count_payment_failed"])
        assertEquals(0, expectedProperties["notification_count_card_auth_required"])
        assertEquals(0, expectedProperties["notification_count_survey_available"])
        assertEquals(0, expectedProperties["notification_count_pledge_management"])
        assertEquals(11, expectedProperties["notification_count_total"])
    }

    @Test
    fun trackDiscoverFilterCTA_whenFilterPresent_returnsCorrectFilter() {
        val user = user()
        val client = client(user)

        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoverFilterCTA(discoveryParams())

        val properties = this.propertiesTest.value ?: mapOf()

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

        client.eventNames.subscribe { this.segmentTrack.onNext(it) }.addToDisposable(disposables)
        client.eventProperties.subscribe { this.propertiesTest.onNext(it) }.addToDisposable(disposables)

        val segment = AnalyticEvents(listOf(client))

        segment.trackDiscoverFilterCTA(discoveryParams)

        val properties = this.propertiesTest.value ?: mapOf()

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
        user?.let { MockCurrentUserV2(it) }
            ?: MockCurrentUserV2(),
        mockCurrentConfig(),
        TrackingClientType.Type.SEGMENT,
        MockFeatureFlagClient()
    )

    private fun assertCheckoutProperties() {
        val expectedProperties = this.propertiesTest.value ?: mapOf()
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
        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertEquals(DateTime.parse("2018-11-02T18:42:05Z").millis / 1000, expectedProperties["context_timestamp"])
    }

    private fun assertDiscoverProperties() {
        val expectedProperties = propertiesTest.value ?: mapOf()
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
        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertEquals(DateTime.parse("2019-03-26T19:26:09Z"), expectedProperties["checkout_reward_estimated_delivery_on"])
        assertEquals(false, expectedProperties["checkout_reward_has_items"])
        assertEquals("2", expectedProperties["checkout_reward_id"])
        assertEquals(false, expectedProperties["checkout_reward_is_limited_time"])
        assertEquals(false, expectedProperties["checkout_reward_is_limited_quantity"])
        assertEquals(10.0, expectedProperties["checkout_reward_minimum"])
        assertEquals(10.0, expectedProperties["checkout_reward_minimum_usd"])
        assertEquals(true, expectedProperties["checkout_reward_shipping_enabled"])
        assertEquals("UNRESTRICTED", expectedProperties["checkout_reward_shipping_preference"])
        assertEquals(6, expectedProperties["checkout_add_ons_count_total"])
        assertEquals(2, expectedProperties["checkout_add_ons_count_unique"])
        assertEquals(110.71, expectedProperties["checkout_add_ons_minimum_usd"])
    }

    private fun assertNotificationProperties() {
        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertEquals(1, expectedProperties["notification_count_address_locks_soon"])
    }

    private fun assertProjectProperties(project: Project) {
        val expectedProperties = this.propertiesTest.value ?: mapOf()
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
        assertEquals(PhotoFactory.photo().full(), expectedProperties["project_image_url"])
        assertEquals("https://www.kickstarter.com/projects/${expectedProperties["project_creator_uid"]}/slug-1", expectedProperties["project_url"])
        assertEquals(false, expectedProperties["project_has_add_ons"])
    }

    private fun assertSessionProperties(user: User?) {
        val expectedProperties = this.propertiesTest.value ?: mapOf()
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
        assertEquals(null, expectedProperties["session_enabled_features"])
        assertEquals(false, expectedProperties["session_is_voiceover_running"])
        assertEquals("kickstarter_android", expectedProperties["session_mp_lib"])
        assertEquals("android", expectedProperties["session_os"])
        assertEquals("9", expectedProperties["session_os_version"])
        assertEquals("agent", expectedProperties["session_user_agent"])
        assertEquals(user != null, expectedProperties["session_user_is_logged_in"])
        assertEquals(false, expectedProperties["session_wifi_connection"])
        assertEquals("android_example_experiment[control]", (expectedProperties["session_variants_internal"] as Array<*>).first())
        assertEquals(false, expectedProperties["session_force_dark_mode"])
    }

    private fun assertUserProperties(isAdmin: Boolean) {
        val expectedProperties = this.propertiesTest.value ?: mapOf()
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
        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertEquals(contextName, expectedProperties[CONTEXT_CTA.contextName])
    }

    private fun assertPageContextProperty(contextName: String) {
        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertEquals(contextName, expectedProperties[CONTEXT_PAGE.contextName])
    }

    private fun assertVideoProperties(videoLength: Long, videoPosition: Long) {
        val expectedProperties = this.propertiesTest.value ?: mapOf()
        assertEquals(videoLength, expectedProperties["video_length"])
        assertEquals(videoPosition, expectedProperties["video_position"])
    }

    private fun mockCurrentConfig() = MockCurrentConfigV2().apply {
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

    private fun project(): Project {
        val creatorUser = creator()
        val slug = "slug-1"
        val projectUrl = "https://www.kickstarter.com/projects/" + creatorUser.id() + "/" + slug
        val web = Web.builder()
            .project(projectUrl)
            .rewards("$projectUrl/rewards")
            .updates("$projectUrl/posts")
            .build()
        return ProjectFactory.project().toBuilder()
            .id(4)
            .urls(Urls.builder().web(web).build())
            .category(CategoryFactory.ceramicsCategory())
            .creator(creatorUser)
            .commentsCount(3)
            .tags(listOfTags())
            .location(LocationFactory.unitedStates())
            .updatesCount(5)
            .build()
    }

    private fun backedProject(): Project {
        val creatorUser = creator()
        val slug = "slug-1"
        val projectUrl = "https://www.kickstarter.com/projects/" + creatorUser.id() + "/" + slug
        val web = Web.builder()
            .project(projectUrl)
            .rewards("$projectUrl/rewards")
            .updates("$projectUrl/posts")
            .build()
        return ProjectFactory.backedProject()
            .toBuilder()
            .id(4)
            .urls(Urls.builder().web(web).build())
            .category(CategoryFactory.ceramicsCategory())
            .commentsCount(3)
            .creator(creator())
            .location(LocationFactory.unitedStates())
            .tags(listOfTags())
            .updatesCount(5)
            .build()
    }
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

    private fun listOfAddons(): List<Reward>? =
        listOf(RewardFactory.addOnSingle().toBuilder().minimum(10.06).build(), RewardFactory.addOnMultiple().toBuilder().minimum(20.13).build())

    private fun listOfTags(): List<String> = listOf("tag1", "tag2", "tag3")
}
