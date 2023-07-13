package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.junit.Test
import org.mockito.Mockito
import rx.observers.TestSubscriber

class SendThirdPartyEventUseCaseV2Test : KSRobolectricTestCase() {

    private var mockSharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)

    private val disposables = CompositeDisposable()

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
        sendThirdPartyEventObservable.assertNoValues()
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
        sendThirdPartyEventObservable.assertNoValues()
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
        sendThirdPartyEventObservable.assertNoValues()
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
    }

    @Test
    fun testInputForPurchaseEvent() {
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

        val user = UserFactory
            .user()
            .toBuilder()
            .id(7272)
            .build()

        val useCase = SendThirdPartyEventUseCaseV2(mockSharedPreferences, mockFeatureFlagClientType)

        // - The input is built and sent to the Mutation before any network call happens, test here the proper values for the input
        val input: TPEventInputData = useCase.buildInput(
            eventName = ThirdPartyEventValues.EventName.PURCHASE,
            canSendEventFlag = true,
            rawData = Pair(Pair(project, user), Pair(checkoutData, pledgeData))
        )

        assertEquals(ThirdPartyEventValues.EventName.PURCHASE.value, input.eventName)
        assertEquals(encodeRelayId(project), input.projectId)
        assertEquals(null, input.firebaseScreen)
        assertEquals(null, input.firebaseScreen)
        assertEquals(3, input.items.size)
        assertEquals("242", input.items?.get(2)?.itemId)
        assertEquals(100.0, input.items?.get(2)?.price)
        assertEquals(20.0, input.shipping)
        assertEquals("3", input.transactionId)
        assertEquals("7272", input.userId)

        assertEquals(true, input.appData.androidConsent)
        assertEquals(false, input.appData.iOSConsent)
        assertEquals("a2", input.appData.extInfo.first())
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
    }

    fun testInputForScreenViewEvent() {
        Mockito.`when`(
            mockSharedPreferences
                .getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)
        )
            .thenReturn(true)

        val project = ProjectFactory.project().toBuilder().sendThirdPartyEvents(true).build()

        val user = UserFactory.user()
        val useCase = SendThirdPartyEventUseCaseV2(mockSharedPreferences, mockFeatureFlagClientType)

        // - The input is built and sent to the Mutation before any network call happens, test here the proper values for the input
        val input: TPEventInputData = useCase.buildInput(
            eventName = ThirdPartyEventValues.EventName.SCREEN_VIEW,
            firebaseScreen = ThirdPartyEventValues.ScreenName.PROJECT.value,
            firebasePreviousScreen = ThirdPartyEventValues.ScreenName.DISCOVERY.value,
            canSendEventFlag = true,
            rawData = Pair(Pair(project, user), Pair(null, null))
        )

        assertEquals(ThirdPartyEventValues.EventName.SCREEN_VIEW.value, input.eventName)
        assertEquals(encodeRelayId(project), input.projectId)
        assertEquals("7272", input.userId)
        assertEquals(ThirdPartyEventValues.ScreenName.PROJECT.value, input.firebaseScreen)
        assertEquals(ThirdPartyEventValues.ScreenName.DISCOVERY.value, input.firebasePreviousScreen)

        assertEquals(true, input.appData.androidConsent)
        assertEquals(false, input.appData.iOSConsent)
        assertEquals("a2", input.appData.extInfo.first())
    }

    private fun subscribeToThirdPartyEvent(
        project: Observable<Project>,
        environment: Environment,
        checkoutAndPledgeData: Observable<Pair<CheckoutData, PledgeData>?> = Observable.just(Pair(null, null)),
        eventName: ThirdPartyEventValues.EventName,
        firebaseScreen: String = "",
        firebasePreviousScreen: String = "",
    ) {

        val apolloClient = object : MockApolloClientV2() {
            override fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> {
                return Observable.just(Pair(true, ""))
            }
        }

        SendThirdPartyEventUseCaseV2(
            requireNotNull(environment.sharedPreferences()),
            requireNotNull(environment.featureFlagClient())
        ).sendThirdPartyEvent(
            project = project,
            apolloClient = apolloClient,
            checkoutAndPledgeData = checkoutAndPledgeData,
            currentUser = requireNotNull(environment.currentUserV2()),
            eventName = eventName,
            firebaseScreen = firebaseScreen,
            firebasePreviousScreen = firebasePreviousScreen
        ).subscribe {
            sendThirdPartyEventObservable.onNext(it)
        }
            .addToDisposable(disposables)
    }
}
