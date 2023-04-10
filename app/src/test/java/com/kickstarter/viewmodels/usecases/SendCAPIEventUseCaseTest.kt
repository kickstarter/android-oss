package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.facebook.appevents.cloudbridge.ConversionsAPIEventName
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.utils.extensions.toHashedSHAEmail
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.SharedPreferenceKey
import org.junit.Test
import org.mockito.Mockito
import rx.Observable
import rx.subjects.BehaviorSubject
import type.TriggerCapiEventInput

class SendCAPIEventUseCaseTest : KSRobolectricTestCase() {

    private var mockSharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)

    private val sendCAPIEventObservable = BehaviorSubject.create<Pair<TriggerCapiEventMutation.Data, TriggerCapiEventInput>>()

    val mockExperimentsClientType: MockExperimentsClientType =
        object : MockExperimentsClientType() {
            override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                return true
            }
        }

    private val mockFeatureFlagClientType: MockFeatureFlagClient =
        object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }

    val currentUser: CurrentUserType = MockCurrentUser()
    private fun setUpEnvironment(): Environment {
        return environment()
            .toBuilder()
            .currentUser(currentUser)
            .sharedPreferences(mockSharedPreferences)
            .optimizely(mockExperimentsClientType)
            .featureFlagClient(mockFeatureFlagClientType)
            .build()
    }

    @Test
    fun testSendCapiEventViewProject() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(true)

        val project = ProjectFactory.project().toBuilder().sendMetaCapiEvents(true).build()
        val event = ConversionsAPIEventName.VIEWED_CONTENT

        subscribeToEvent(setUpEnvironment(), project, event)

        assertEquals(event.rawValue, sendCAPIEventObservable.value?.second?.eventName())
        assertEquals(encodeRelayId(project), sendCAPIEventObservable.value?.second?.projectId())
        assertEquals("some@email.com".toHashedSHAEmail(), sendCAPIEventObservable.value?.second?.userEmail())
        assertEquals(null, sendCAPIEventObservable.value?.second?.customData()?.currency())
        assertEquals(null, sendCAPIEventObservable.value?.second?.customData()?.value())
        assertEquals("a2", sendCAPIEventObservable.value?.second?.appData()?.extinfo()?.first())
        assertEquals("", sendCAPIEventObservable.value?.second?.externalId())
    }

    @Test
    fun testSendCapiEventInitialCheckout() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(true)

        val project = ProjectFactory.project().toBuilder().sendMetaCapiEvents(true).build()
        val event = ConversionsAPIEventName.INITIATED_CHECKOUT

        subscribeToEvent(setUpEnvironment(), project, event)

        assertEquals(event.rawValue, sendCAPIEventObservable.value?.second?.eventName())
        assertEquals(encodeRelayId(project), sendCAPIEventObservable.value?.second?.projectId())
        assertEquals("some@email.com".toHashedSHAEmail(), sendCAPIEventObservable.value?.second?.userEmail())
        assertEquals(null, sendCAPIEventObservable.value?.second?.customData()?.currency())
        assertEquals(null, sendCAPIEventObservable.value?.second?.customData()?.value())
        assertEquals("a2", sendCAPIEventObservable.value?.second?.appData()?.extinfo()?.first())
        assertEquals("", sendCAPIEventObservable.value?.second?.externalId())
    }

    @Test
    fun testSendCapiEventAddPaymentInfo() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(true)

        val project = ProjectFactory.project().toBuilder().sendMetaCapiEvents(true).build()
        val event = ConversionsAPIEventName.ADDED_PAYMENT_INFO

        subscribeToEvent(setUpEnvironment(), project, event)

        assertEquals(event.rawValue, sendCAPIEventObservable.value?.second?.eventName())
        assertEquals(encodeRelayId(project), sendCAPIEventObservable.value?.second?.projectId())
        assertEquals("some@email.com".toHashedSHAEmail(), sendCAPIEventObservable.value?.second?.userEmail())
        assertEquals(null, sendCAPIEventObservable.value?.second?.customData()?.currency())
        assertEquals(null, sendCAPIEventObservable.value?.second?.customData()?.value())
        assertEquals("a2", sendCAPIEventObservable.value?.second?.appData()?.extinfo()?.first())
        assertEquals("", sendCAPIEventObservable.value?.second?.externalId())
    }

    @Test
    fun testSendCapiEventPurchased() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(true)

        val project = ProjectFactory.project().toBuilder().sendMetaCapiEvents(true).build()
        val event = ConversionsAPIEventName.PURCHASED

        subscribeToEvent(setUpEnvironment(), project, event, Observable.just(Pair("10", "USD")))

        assertEquals(event.rawValue, sendCAPIEventObservable.value?.second?.eventName())
        assertEquals(encodeRelayId(project), sendCAPIEventObservable.value?.second?.projectId())
        assertEquals("some@email.com".toHashedSHAEmail(), sendCAPIEventObservable.value?.second?.userEmail())
        assertEquals("USD", sendCAPIEventObservable.value?.second?.customData()?.currency())
        assertEquals("10", sendCAPIEventObservable.value?.second?.customData()?.value())
        assertEquals("a2", sendCAPIEventObservable.value?.second?.appData()?.extinfo()?.first())
        assertEquals("", sendCAPIEventObservable.value?.second?.externalId())
    }

    private fun subscribeToEvent(
        environment: Environment,
        project: Project,
        event: ConversionsAPIEventName,
        pledgeAmountAndCurrency: Observable<Pair<String?, String?>> = Observable.just(Pair(null, null))
    ) {
        SendCAPIEventUseCase(
            requireNotNull(environment.sharedPreferences()),
            requireNotNull(environment.featureFlagClient())
        ).sendCAPIEvent(
            project = Observable.just(project),
            apolloClient = requireNotNull(environment.apolloClient()),
            eventName = event,
            pledgeAmountAndCurrency = pledgeAmountAndCurrency
        ).subscribe {
            sendCAPIEventObservable.onNext(it)
        }
    }
}
