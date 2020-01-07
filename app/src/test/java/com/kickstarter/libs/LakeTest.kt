package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import org.joda.time.DateTime
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

        assertDefaultProperties(null)
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

        assertDefaultProperties(user)
        val expectedProperties = propertiesTest.value
        assertEquals(15L, expectedProperties["user_uid"])
        assertEquals(3, expectedProperties["user_backed_projects_count"])
        assertEquals("NG", expectedProperties["user_country"])
        assertEquals(false, expectedProperties["user_facebook_account"])
        assertEquals(false, expectedProperties["user_is_admin"])
        assertEquals(2, expectedProperties["user_launched_projects_count"])
        assertEquals(10, expectedProperties["user_watched_projects_count"])
    }

    private fun assertDefaultProperties(user: User?) {
        val expectedProperties = propertiesTest.value
        assertEquals(DateTime.parse("2018-11-02T18:42:05Z").millis / 1000, expectedProperties["time"])
        assertEquals(user != null, expectedProperties["user_logged_in"])
    }

    private fun mockCurrentConfig() = MockCurrentConfig().apply {
        config(ConfigFactory.configWithFeatureEnabled("android_example_feature"))
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
