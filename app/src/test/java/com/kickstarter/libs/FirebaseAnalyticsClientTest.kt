package com.kickstarter.libs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.ui.SharedPreferenceKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic

class FirebaseAnalyticsClientTest : KSRobolectricTestCase() {

    lateinit var eventNameSubscriber: TestSubscriber<String>
    lateinit var eventPropertySubscriber: TestSubscriber<Bundle>
    private val mockSharedPreferences: SharedPreferences = MockSharedPreferences()
    lateinit var context: Context
    private val disposables = CompositeDisposable()

    private class Event constructor(
        val name: String,
        val properties: Bundle
    )

    private class MockFirebaseClient(
        private val optimizely: ExperimentsClientType,
        private val sharedPreferences: SharedPreferences
    ) : FirebaseAnalyticsClient(optimizely, sharedPreferences) {

        private val events = PublishSubject.create<Event>()

        fun events() : Observable<Event> { return this.events }
        val eventName : Observable<String> = this.events.map { it.name }
        val eventProperties : Observable<Bundle> = this.events.map { it.properties }

        override fun trackEvent(eventName: String, parameters: Bundle) {
            this.events.onNext(Event(eventName, parameters))
        }
    }

    @Test
    fun testFirebaseAnalyticsTrack_whenInitialized_registerOnChangeListener() {
        eventNameSubscriber = TestSubscriber()
        eventPropertySubscriber = TestSubscriber()
        val mockOptimizely = object : MockExperimentsClientType() {
            override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

        mockSharedPreferences.edit().putBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)

        val mockFirebaseClient = MockFirebaseClient(mockOptimizely, mockSharedPreferences)
        disposables.add(mockFirebaseClient.eventName.subscribe{ this.eventNameSubscriber.onNext(it)})
        disposables.add(mockFirebaseClient.eventProperties.subscribe{ this.eventPropertySubscriber.onNext(it)})

        val analyticEvents = FirebaseAnalyticsEvents(mockFirebaseClient)

        val eventProps = Bundle()
        eventProps.putBoolean("event_name", true)
        analyticEvents.track("event_name", eventProps)

        eventNameSubscriber.assertNoValues()

//        mockSharedPreferences.edit().putBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, true)

    }
}