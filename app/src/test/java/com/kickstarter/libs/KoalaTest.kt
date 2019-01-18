package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import org.joda.time.DateTime
import org.junit.Test
import rx.subjects.BehaviorSubject

class KoalaTest : KSRobolectricTestCase() {

    private val propertiesTest = BehaviorSubject.create<Map<String, Any>>()

    @Test
    fun testDefaultProperties() {
        val client = MockTrackingClient(MockCurrentUser())
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackAppOpen()

        this.koalaTest.assertValue("App Open")

        assertDefaultProperties(null)
    }

    @Test
    fun testDefaultProperties_LoggedInUser() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user))
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackAppOpen()

        this.koalaTest.assertValue("App Open")

        assertDefaultProperties(user)
        val expectedProperties = propertiesTest.value
        assertEquals(15L, expectedProperties["user_uid"])
        assertEquals(3, expectedProperties["user_backed_projects_count"])
        assertEquals(2, expectedProperties["user_created_projects_count"])
        assertEquals(10, expectedProperties["user_starred_projects_count"])
    }

    @Test
    fun testDiscoveryProperties() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user))
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        val params = DiscoveryParams.builder().staffPicks(true).category(CategoryFactory.artCategory()).build()

        koala.trackDiscovery(params, false)

        assertDefaultProperties(user)
        val expectedProperties = propertiesTest.value
        assertEquals(1L, expectedProperties["discover_category_id"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals(true, expectedProperties["discover_staff_picks"])
        assertEquals(false, expectedProperties["discover_starred"])
        assertEquals(null, expectedProperties["discover_term"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(1, expectedProperties["page"])
        assertEquals(15, expectedProperties["per_page"])
    }

    @Test
    fun testDiscoveryProperties_AllProjects() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user))
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        val params = DiscoveryParams.builder().build()

        koala.trackDiscovery(params, false)

        assertDefaultProperties(user)
        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["discover_category_id"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals(false, expectedProperties["discover_staff_picks"])
        assertEquals(false, expectedProperties["discover_starred"])
        assertEquals(null, expectedProperties["discover_term"])
        assertEquals(true, expectedProperties["discover_everything"])
        assertEquals(1, expectedProperties["page"])
        assertEquals(15, expectedProperties["per_page"])
    }

    @Test
    fun testDiscoveryProperties_NoCategory() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user))
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        val params = DiscoveryParams.builder().staffPicks(true).build()

        koala.trackDiscovery(params, false)

        assertDefaultProperties(user)
        val expectedProperties = propertiesTest.value
        assertNull(expectedProperties["discover_category_id"])
        assertEquals(false, expectedProperties["discover_recommended"])
        assertEquals(false, expectedProperties["discover_social"])
        assertEquals(true, expectedProperties["discover_staff_picks"])
        assertEquals(false, expectedProperties["discover_starred"])
        assertEquals(null, expectedProperties["discover_term"])
        assertEquals(false, expectedProperties["discover_everything"])
        assertEquals(1, expectedProperties["page"])
        assertEquals(15, expectedProperties["per_page"])
    }

    @Test
    fun testProjectProperties() {
        val project = project()

        val client = MockTrackingClient(MockCurrentUser())
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackProjectShow(project, RefTag.discovery(), RefTag.recommended())

        assertDefaultProperties(null)
        assertProjectProperties()
        this.koalaTest.assertValues("Project Page", "Viewed Project Page")
    }

    @Test
    fun testProjectProperties_LoggedInUser() {
        val project = project()
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user))
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackProjectShow(project, RefTag.discovery(), RefTag.recommended())

        assertDefaultProperties(user)
        assertProjectProperties()
        val expectedProperties = propertiesTest.value
        assertEquals(false, expectedProperties["user_is_project_creator"])
        assertEquals(false, expectedProperties["user_is_backer"])
        assertEquals(false, expectedProperties["user_has_starred"])

        this.koalaTest.assertValues("Project Page", "Viewed Project Page")
    }

    @Test
    fun testProjectProperties_LoggedInUser_IsBacker() {
        val project = project().toBuilder().isBacking(true).build()
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user))
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackProjectShow(project, RefTag.discovery(), RefTag.recommended())

        assertDefaultProperties(user)
        assertProjectProperties()
        val expectedProperties = propertiesTest.value
        assertEquals(false, expectedProperties["user_is_project_creator"])
        assertEquals(true, expectedProperties["user_is_backer"])
        assertEquals(false, expectedProperties["user_has_starred"])

        this.koalaTest.assertValues("Project Page", "Viewed Project Page")
    }

    @Test
    fun testProjectProperties_LoggedInUser_IsProjectCreator() {
        val project = project().toBuilder().build()
        val creator = creator()
        val client = MockTrackingClient(MockCurrentUser(creator))
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackProjectShow(project, RefTag.discovery(), RefTag.recommended())

        assertDefaultProperties(creator)
        assertProjectProperties()
        val expectedProperties = propertiesTest.value
        assertEquals(true, expectedProperties["user_is_project_creator"])
        assertEquals(false, expectedProperties["user_is_backer"])
        assertEquals(false, expectedProperties["user_has_starred"])

        this.koalaTest.assertValues("Project Page", "Viewed Project Page")
    }

    @Test
    fun testProjectProperties_LoggedInUser_HasStarred() {
        val project = project().toBuilder().isStarred(true).build()
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user))
        client.eventNames.subscribe(this.koalaTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackProjectShow(project, RefTag.discovery(), RefTag.recommended())

        assertDefaultProperties(user)
        assertProjectProperties()
        val expectedProperties = propertiesTest.value
        assertEquals(false, expectedProperties["user_is_project_creator"])
        assertEquals(false, expectedProperties["user_is_backer"])
        assertEquals(true, expectedProperties["user_has_starred"])

        this.koalaTest.assertValues("Project Page", "Viewed Project Page")
    }

    private fun assertDefaultProperties(user: User?) {
        val expectedProperties = propertiesTest.value
        assertEquals(false, expectedProperties["android_pay_capable"])
        assertEquals("uuid", expectedProperties["android_uuid"])
        assertEquals("9.9.9", expectedProperties["app_version"])
        assertEquals("Google", expectedProperties["brand"])
        assertEquals("android", expectedProperties["client_platform"])
        assertEquals("native", expectedProperties["client_type"])
        assertEquals("uuid", expectedProperties["device_fingerprint"])
        assertEquals("phone", expectedProperties["device_format"])
        assertEquals("portrait", expectedProperties["device_orientation"])
        assertEquals("uuid", expectedProperties["distinct_id"])
        assertEquals("unavailable", expectedProperties["google_play_services"])
        assertEquals("kickstarter_android", expectedProperties["koala_lib"])
        assertEquals("Google", expectedProperties["manufacturer"])
        assertEquals("Pixel 3", expectedProperties["model"])
        assertEquals("android", expectedProperties["mp_lib"])
        assertEquals("Android", expectedProperties["os"])
        assertEquals("9", expectedProperties["os_version"])
        assertEquals(DateTime.parse("2018-11-02T18:42:05Z").millis / 1000, expectedProperties["time"])
        assertEquals(user != null, expectedProperties["user_logged_in"])
    }

    private fun assertProjectProperties() {
        val expectedProperties = propertiesTest.value
        assertEquals(100, expectedProperties["project_backers_count"])
        assertEquals("US", expectedProperties["project_country"])
        assertEquals("USD", expectedProperties["project_currency"])
        assertEquals(100f, expectedProperties["project_goal"])
        assertEquals(true, expectedProperties["project_has_video"])
        assertEquals(4L, expectedProperties["project_pid"])
        assertEquals(50f, expectedProperties["project_pledged"])
        assertEquals(.5f, expectedProperties["project_percent_raised"])
        assertEquals("Ceramics", expectedProperties["project_category"])
        assertEquals("Art", expectedProperties["project_parent_category"])
        assertEquals("Brooklyn", expectedProperties["project_location"])
        assertEquals(10 * 24, expectedProperties["project_hours_remaining"])
        assertEquals(60 * 60 * 24 * 20, expectedProperties["project_duration"])
        assertEquals("discovery", expectedProperties["ref_tag"])
        assertEquals("recommended", expectedProperties["referrer_credit"])
        assertEquals(3L, expectedProperties["creator_uid"])
        assertEquals(17, expectedProperties["creator_backed_projects_count"])
        assertEquals(5, expectedProperties["creator_created_projects_count"])
        assertEquals(2, expectedProperties["creator_starred_projects_count"])
    }

    private fun project() =
            ProjectFactory.project().toBuilder()
                    .id(4)
                    .category(CategoryFactory.ceramicsCategory())
                    .location(LocationFactory.unitedStates())
                    .creator(creator())
                    .build()

    private fun creator() =
            UserFactory.creator().toBuilder()
                    .id(3)
                    .backedProjectsCount(17)
                    .starredProjectsCount(2)
                    .build()

    private fun user() =
            UserFactory.user()
                    .toBuilder()
                    .id(15)
                    .backedProjectsCount(3)
                    .createdProjectsCount(2)
                    .starredProjectsCount(10)
                    .build()

}
