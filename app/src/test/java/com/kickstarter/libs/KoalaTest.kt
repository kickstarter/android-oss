package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test
import rx.observers.TestSubscriber

class KoalaTest : KSRobolectricTestCase() {

    private val namesTest = TestSubscriber.create<String>()
    private val propertiesTest = TestSubscriber.create<Map<String, Any>>()

    @Test
    fun testDefaultProperties() {
        val client = MockTrackingClient(MockCurrentUser())
        client.eventNames.subscribe(this.namesTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackAppOpen()
        val expectedProperties = getDefaultExpectedProperties()
        namesTest.assertValue("App Open")
        propertiesTest.assertValue(expectedProperties)
    }

    @Test
    fun testDefaultPropertiesWithLoggedInUser() {
        val user = UserFactory.user()
                .toBuilder()
                .id(15)
                .backedProjectsCount(3)
                .createdProjectsCount(2)
                .starredProjectsCount(10)
                .build()
        val client = MockTrackingClient(MockCurrentUser(user))
        client.eventNames.subscribe(this.namesTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackAppOpen()
        val expectedProperties = getDefaultExpectedProperties()
        expectedProperties["user_logged_in"] = true
        expectedProperties["user_uid"] = 15
        expectedProperties["user_backed_projects_count"] = 3
        expectedProperties["user_created_projects_count"] = 2
        expectedProperties["user_starred_projects_count"] = 10

        namesTest.assertValue("App Open")
        propertiesTest.assertValue(expectedProperties)
    }

    private fun getDefaultExpectedProperties(): HashMap<String, Any> {
        val expectedProperties = HashMap<String, Any>()
        expectedProperties["android_pay_capable"] = false
        expectedProperties["android_uuid"] = "uuid"
        expectedProperties["app_version"] = "9.9.9"
        expectedProperties["brand"] = "Google"
        expectedProperties["client_platform"] = "android"
        expectedProperties["client_type"] = "native"
        expectedProperties["device_fingerprint"] = "uuid"
        expectedProperties["device_format"] = "phone"
        expectedProperties["device_orientation"] = "portrait"
        expectedProperties["distinct_id"] = "uuid"
        expectedProperties["google_play_services"] = "unavailable"
        expectedProperties["koala_lib"] = "kickstarter_android"
        expectedProperties["manufacturer"] = "Google"
        expectedProperties["model"] = "Pixel 3"
        expectedProperties["mp_lib"] = "android"
        expectedProperties["os"] = "Android"
        expectedProperties["os_version"] = "9"
        expectedProperties["time"] = MockTrackingClient.DEFAULT_TIME
        expectedProperties["user_logged_in"] = false
        return expectedProperties
    }

}
