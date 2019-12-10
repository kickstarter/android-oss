package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
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
                    .starredProjectsCount(10)
                    .build()

}
