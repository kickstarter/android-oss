package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.models.User
import org.joda.time.DateTime
import org.junit.Assert
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
        Assert.assertEquals(15L, expectedProperties["user_uid"])
        Assert.assertEquals(3, expectedProperties["user_backed_projects_count"])
        Assert.assertEquals(2, expectedProperties["user_created_projects_count"])
        Assert.assertEquals(10, expectedProperties["user_starred_projects_count"])
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
        Assert.assertEquals(false, expectedProperties["user_is_project_creator"])
        Assert.assertEquals(false, expectedProperties["user_is_backer"])
        Assert.assertEquals(false, expectedProperties["user_has_starred"])

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
        Assert.assertEquals(false, expectedProperties["user_is_project_creator"])
        Assert.assertEquals(true, expectedProperties["user_is_backer"])
        Assert.assertEquals(false, expectedProperties["user_has_starred"])

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
        Assert.assertEquals(false, expectedProperties["user_is_project_creator"])
        Assert.assertEquals(false, expectedProperties["user_is_backer"])
        Assert.assertEquals(true, expectedProperties["user_has_starred"])

        this.koalaTest.assertValues("Project Page", "Viewed Project Page")
    }

    private fun assertDefaultProperties(user: User?) {
        val expectedProperties = propertiesTest.value
        Assert.assertEquals(false, expectedProperties["android_pay_capable"])
        Assert.assertEquals("uuid", expectedProperties["android_uuid"])
        Assert.assertEquals("9.9.9", expectedProperties["app_version"])
        Assert.assertEquals("Google", expectedProperties["brand"])
        Assert.assertEquals("android", expectedProperties["client_platform"])
        Assert.assertEquals("native", expectedProperties["client_type"])
        Assert.assertEquals("uuid", expectedProperties["device_fingerprint"])
        Assert.assertEquals("phone", expectedProperties["device_format"])
        Assert.assertEquals("portrait", expectedProperties["device_orientation"])
        Assert.assertEquals("uuid", expectedProperties["distinct_id"])
        Assert.assertEquals("unavailable", expectedProperties["google_play_services"])
        Assert.assertEquals("kickstarter_android", expectedProperties["koala_lib"])
        Assert.assertEquals("Google", expectedProperties["manufacturer"])
        Assert.assertEquals("Pixel 3", expectedProperties["model"])
        Assert.assertEquals("android", expectedProperties["mp_lib"])
        Assert.assertEquals("Android", expectedProperties["os"])
        Assert.assertEquals("9", expectedProperties["os_version"])
        Assert.assertEquals(DateTime.parse("2018-11-02T18:42:05Z").millis, expectedProperties["time"])
        Assert.assertEquals(user != null, expectedProperties["user_logged_in"])
    }

    private fun assertProjectProperties() {
        val expectedProperties = propertiesTest.value
        Assert.assertEquals(100, expectedProperties["project_backers_count"])
        Assert.assertEquals("US", expectedProperties["project_country"])
        Assert.assertEquals("USD", expectedProperties["project_currency"])
        Assert.assertEquals(100f, expectedProperties["project_goal"])
        Assert.assertEquals(true, expectedProperties["project_has_video"])
        Assert.assertEquals(4L, expectedProperties["project_pid"])
        Assert.assertEquals(50f, expectedProperties["project_pledged"])
        Assert.assertEquals(.5f, expectedProperties["project_percent_raised"])
        Assert.assertEquals("Ceramics", expectedProperties["project_category"])
        Assert.assertEquals("Art", expectedProperties["project_parent_category"])
        Assert.assertEquals("Brooklyn", expectedProperties["project_location"])
        Assert.assertEquals(10 * 24, expectedProperties["project_hours_remaining"])
        Assert.assertEquals(60 * 60 * 24 * 20, expectedProperties["project_duration"])
        Assert.assertEquals("discovery", expectedProperties["ref_tag"])
        Assert.assertEquals("recommended", expectedProperties["referrer_credit"])
        Assert.assertEquals(3L, expectedProperties["creator_uid"])
        Assert.assertEquals(17, expectedProperties["creator_backed_projects_count"])
        Assert.assertEquals(5, expectedProperties["creator_created_projects_count"])
        Assert.assertEquals(2, expectedProperties["creator_starred_projects_count"])
    }

    private fun project(): Project {
        return ProjectFactory.project().toBuilder()
                .id(4)
                .category(CategoryFactory.ceramicsCategory())
                .location(LocationFactory.unitedStates())
                .creator(UserFactory.creator().toBuilder().id(3).backedProjectsCount(17).starredProjectsCount(2).build())
                .build()
    }

    private fun user(): User {
        return UserFactory.user()
                .toBuilder()
                .id(15)
                .backedProjectsCount(3)
                .createdProjectsCount(2)
                .starredProjectsCount(10)
                .build()
    }

}
