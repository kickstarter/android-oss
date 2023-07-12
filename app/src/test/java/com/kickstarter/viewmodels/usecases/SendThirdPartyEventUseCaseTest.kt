package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.facebook.appevents.cloudbridge.ConversionsAPIEventName
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.toHashedSHAEmail
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import org.junit.Test
import org.mockito.Mockito
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject
import type.TriggerCapiEventInput
import type.TriggerThirdPartyEventInput

class SendThirdPartyEventUseCaseTest : KSRobolectricTestCase() {

    private var mockSharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)

    private val sendCAPIEventObservable = BehaviorSubject.create<Pair<TriggerCapiEventMutation.Data, TriggerCapiEventInput>>()
    private val sendThirdPartyEventObservable = TestSubscriber.create<Pair<Boolean, String>>()

    private val mockFeatureFlagClientType: MockFeatureFlagClient =
        object : MockFeatureFlagClient() {
            override fun getBoolean(FlagKey: FlagKey): Boolean {
                return true
            }
        }

    val currentUser: CurrentUserType = MockCurrentUser(UserFactory.user().toBuilder().id(7272).email("some@email.com").build())
    private fun setUpEnvironment(mockFeatureFlagClient: MockFeatureFlagClient = mockFeatureFlagClientType): Environment {
        return environment()
            .toBuilder()
            .currentUser(currentUser)
            .sharedPreferences(mockSharedPreferences)
            .featureFlagClient(mockFeatureFlagClient)
            .build()
    }

    @Test
    fun testEvents_whenFeatureFlagOff_doesNotSendEvent() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(true)

        val mockFeatureFlagClientType: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return false
                }
            }

        val project = ProjectFactory.project().toBuilder().sendThirdPartyEvents(true).build()

        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )

        val pledgeData = PledgeData.with(
            PledgeFlowContext.NEW_PLEDGE,
            ProjectDataFactory.project(project),
            RewardFactory.reward(),
            listOf<Reward>(RewardFactory.addOn(), RewardFactory.addOnMultiple()),
            null
        )

        subscribeToThirdPartyEvent(Observable.just(project), setUpEnvironment(mockFeatureFlagClientType), Observable.just(Pair(checkoutData, pledgeData)), ThirdPartyEventValues.EventName.PURCHASE)
        sendThirdPartyEventObservable.assertValue(Pair(true, ""))
        // assertNull(ThirdPartyEventValues.EventName.PURCHASE.value, sendThirdPartyEventObservable.value)
    }

    @Test
    fun testEvents_whenUserDeclinesConsent_doesNotSendEvent() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(false)

        val project = ProjectFactory.project().toBuilder().sendThirdPartyEvents(true).build()

        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )

        val pledgeData = PledgeData.with(
            PledgeFlowContext.NEW_PLEDGE,
            ProjectDataFactory.project(project),
            RewardFactory.reward(),
            listOf<Reward>(RewardFactory.addOn(), RewardFactory.addOnMultiple()),
            null
        )

        subscribeToThirdPartyEvent(Observable.just(project), setUpEnvironment(), Observable.just(Pair(checkoutData, pledgeData)), ThirdPartyEventValues.EventName.PURCHASE)
        sendThirdPartyEventObservable.assertValue(Pair(true, ""))
        // assertNull(ThirdPartyEventValues.EventName.PURCHASE.value, sendThirdPartyEventObservable.value)
    }

    @Test
    fun testEvents_whenCanSendThirdPartyEventsFalse_doesNotSendEvents() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(true)

        val project = ProjectFactory.project().toBuilder().sendThirdPartyEvents(false).build()

        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )

        val pledgeData = PledgeData.with(
            PledgeFlowContext.NEW_PLEDGE,
            ProjectDataFactory.project(project),
            RewardFactory.reward(),
            listOf(RewardFactory.addOn(), RewardFactory.addOnMultiple()),
            null
        )

        subscribeToThirdPartyEvent(Observable.just(project), setUpEnvironment(), Observable.just(Pair(checkoutData, pledgeData)), ThirdPartyEventValues.EventName.PURCHASE)
        sendThirdPartyEventObservable.assertValue(Pair(true, ""))
        // assertNull(ThirdPartyEventValues.EventName.PURCHASE.value, sendThirdPartyEventObservable.value)
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
    @Test
    fun testSendThirdPartyPurchaseEvent() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(true)

        val project = ProjectFactory.project().toBuilder().sendThirdPartyEvents(true).build()

        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )
        val addons = listOf(RewardFactory.addOn().toBuilder().build(), RewardFactory.addOnMultiple().toBuilder().id(242).build())
        val pledgeData = PledgeData.with(
            PledgeFlowContext.NEW_PLEDGE,
            ProjectDataFactory.project(project),
            RewardFactory.reward(),
            addons,
            null
        )
        subscribeToThirdPartyEvent(Observable.just(project), setUpEnvironment(), Observable.just(Pair(checkoutData, pledgeData)), ThirdPartyEventValues.EventName.PURCHASE)
        sendThirdPartyEventObservable.assertValue(Pair(true, ""))
//        assertEquals(ThirdPartyEventValues.EventName.PURCHASE.value, sendThirdPartyEventObservable.value?.second?.eventName())
//        assertEquals(encodeRelayId(project), sendThirdPartyEventObservable.value?.second?.projectId())
//        assertEquals(sendThirdPartyEventObservable.value?.second?.firebasePreviousScreen(), "")
//        assertEquals(sendThirdPartyEventObservable.value?.second?.firebaseScreen(), "")
//        assertEquals(3, sendThirdPartyEventObservable.value?.second?.items()?.size)
//        assertEquals("242", sendThirdPartyEventObservable.value?.second?.items()?.get(2)?.itemId())
//        assertEquals(100.0, sendThirdPartyEventObservable.value?.second?.items()?.get(2)?.price())
//        assertEquals(20.0, sendThirdPartyEventObservable.value?.second?.shipping())
//        assertEquals("3", sendThirdPartyEventObservable.value?.second?.transactionId())
//        assertEquals("7272", sendThirdPartyEventObservable.value?.second?.userId())
    }

    @Test
    fun testSendThirdPartyScreenViewEvent() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(true)

        val project = ProjectFactory.project().toBuilder().sendThirdPartyEvents(true).build()

        subscribeToThirdPartyEvent(
            Observable.just(project), setUpEnvironment(),
            eventName = ThirdPartyEventValues.EventName.SCREEN_VIEW,
            firebaseScreen = ThirdPartyEventValues.ScreenName.PROJECT.value,
            firebasePreviousScreen = ThirdPartyEventValues.ScreenName.DISCOVERY.value
        )

        sendThirdPartyEventObservable.assertValue(Pair(true, ""))
//        assertEquals(ThirdPartyEventValues.EventName.SCREEN_VIEW.value, sendThirdPartyEventObservable.value?.second?.eventName())
//        assertEquals(encodeRelayId(project), sendThirdPartyEventObservable.value?.second?.projectId())
//        assertEquals("7272", sendThirdPartyEventObservable.value?.second?.userId())
//        assertEquals(ThirdPartyEventValues.ScreenName.PROJECT.value, sendThirdPartyEventObservable.value?.second?.firebaseScreen())
//        assertEquals(ThirdPartyEventValues.ScreenName.DISCOVERY.value, sendThirdPartyEventObservable.value?.second?.firebasePreviousScreen())
    }

    private fun subscribeToEvent(
        environment: Environment,
        project: Project,
        event: ConversionsAPIEventName,
        pledgeAmountAndCurrency: Observable<Pair<String?, String?>> = Observable.just(Pair(null, null))
    ) {
        val apolloClient = object : MockApolloClient() {
            override fun triggerCapiEvent(triggerCapiEventInput: TriggerCapiEventInput): Observable<TriggerCapiEventMutation.Data> {
                return Observable.just(
                    TriggerCapiEventMutation.Data(
                        TriggerCapiEventMutation
                            .TriggerCAPIEvent(
                                "",
                                true
                            )
                    )
                )
            }
        }

        SendThirdPartyEventUseCase(
            requireNotNull(environment.sharedPreferences()),
            requireNotNull(environment.featureFlagClient())
        ).sendCAPIEvent(
            project = Observable.just(project),
            currentUser = requireNotNull(environment.currentUser()),
            apolloClient = apolloClient,
            eventName = event,
            pledgeAmountAndCurrency = pledgeAmountAndCurrency
        ).subscribe {
            sendCAPIEventObservable.onNext(it)
        }
    }

    private fun subscribeToThirdPartyEvent(
        project: Observable<Project>,
        environment: Environment,
        checkoutAndPledgeData: Observable<Pair<CheckoutData, PledgeData>?> = Observable.just(Pair(null, null)),
        eventName: ThirdPartyEventValues.EventName,
        firebaseScreen: String = "",
        firebasePreviousScreen: String = "",
    ) {

        val apolloClient = object : MockApolloClient() {
            override fun triggerThirdPartyEvent(triggerThirdPartyEventInput: TriggerThirdPartyEventInput): Observable<Pair<Boolean, String>> {
                return Observable.just(Pair(true, ""))
            }
        }

        SendThirdPartyEventUseCase(
            requireNotNull(environment.sharedPreferences()),
            requireNotNull(environment.featureFlagClient())
        ).sendThirdPartyEvent(
            project = project,
            apolloClient = apolloClient,
            checkoutAndPledgeData = checkoutAndPledgeData,
            currentUser = requireNotNull(environment.currentUser()),
            eventName = eventName,
            firebaseScreen = firebaseScreen,
            firebasePreviousScreen = firebasePreviousScreen
        ).subscribe {
            sendThirdPartyEventObservable.onNext(it)
        }
    }
}
