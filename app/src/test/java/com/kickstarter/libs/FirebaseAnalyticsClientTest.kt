package com.kickstarter.libs

import android.content.SharedPreferences
import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.ui.SharedPreferenceKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class FirebaseAnalyticsClientTest : KSRobolectricTestCase() {

    private val disposables = CompositeDisposable()

    private lateinit var eventNameSubscriber: TestSubscriber<String>
    private lateinit var eventPropertySubscriber: TestSubscriber<Bundle>

    private class Event constructor(
        val name: String,
        val properties: Bundle
    )

    private class MockFirebaseClient(
        ffClient: FeatureFlagClientType,
        sharedPreferences: SharedPreferences
    ) : FirebaseAnalyticsClient(sharedPreferences, null) {

        private val events = PublishSubject.create<Event>()

        fun events(): Observable<Event> {
            return this.events
        }

        val eventName: Observable<String> = this.events.map { it.name }
        val eventProperties: Observable<Bundle> = this.events.map { it.properties }

        override fun trackEvent(eventName: String, parameters: Bundle) {
            this.events.onNext(Event(eventName, parameters))
        }
    }

    @Test
    fun testFirebaseAnalyticsEvent() {
        eventNameSubscriber = TestSubscriber()
        eventPropertySubscriber = TestSubscriber()

        val mockFirebaseClient =
            MockFirebaseClient(MockFeatureFlagClient(), MockSharedPreferences())

        disposables.add(mockFirebaseClient.eventName.subscribe { this.eventNameSubscriber.onNext(it) })
        disposables.add(mockFirebaseClient.eventProperties.subscribe { this.eventPropertySubscriber.onNext(it) })

        val eventProps = Bundle()
        eventProps.putBoolean("event_property", true)
        mockFirebaseClient.trackEvent("event_name", eventProps)

        assertEquals(eventNameSubscriber.values().size, 1)
        assertEquals(eventNameSubscriber.values()[0], "event_name")
        assertEquals(eventPropertySubscriber.values().size, 1)
        assertTrue(eventPropertySubscriber.values()[0].getBoolean("event_property"))
    }

    @Test
    fun testIsEnabled_whenConsentFalse_returnFalse() {
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }

        val mockSharedPreferences: SharedPreferences = MockSharedPreferences()
        mockSharedPreferences.edit()
            .putBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false).commit()

        val mockFirebaseClient = MockFirebaseClient(mockFeatureFlagClient, mockSharedPreferences)

        assertFalse(mockFirebaseClient.isEnabled())
    }

    @Test
    fun testIsEnabled_whenConsentTrue_returnTrue() {
        val mockFeatureFlagClient = object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }

        val mockSharedPreferences: SharedPreferences = MockSharedPreferences()
        mockSharedPreferences.edit()
            .putBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, true).commit()

        val mockFirebaseClient = MockFirebaseClient(mockFeatureFlagClient, mockSharedPreferences)

        assertTrue(mockFirebaseClient.isEnabled())
    }

    @After
    fun breakDown() {
        disposables.clear()
    }
}
