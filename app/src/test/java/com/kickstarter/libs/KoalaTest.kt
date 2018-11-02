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
        val client = MockTrackingClient(MockCurrentUser(UserFactory.user()))
        client.eventNames.subscribe(this.namesTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val koala = Koala(client)

        koala.trackAppOpen()
        namesTest.assertValue("App Open")
        val expectedProperties = getDefaultExpectedProperties()
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
        expectedProperties["time"] = System.currentTimeMillis()
        expectedProperties["user_logged_in"] = false
        return expectedProperties
    }

}
