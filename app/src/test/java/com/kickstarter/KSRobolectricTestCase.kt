package com.kickstarter

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.AttributionEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.MockTrackingClient
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.User
import com.stripe.android.Stripe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import junit.framework.TestCase
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(KSRobolectricGradleTestRunner::class)
@Config(
    shadows = [ShadowAndroidXMultiDex::class],
    sdk = [KSRobolectricGradleTestRunner.DEFAULT_SDK],
    instrumentedPackages = [
        "androidx.loader.content"
    ]
)
abstract class KSRobolectricTestCase : TestCase() {
    private val application: Application = ApplicationProvider.getApplicationContext()
    private lateinit var environment: Environment

    lateinit var segmentTrack: TestSubscriber<String>
    lateinit var segmentIdentify: TestSubscriber<User>
    private val disposables = CompositeDisposable()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        // - clean subscriptions
        disposables.clear()
        super.setUp()

        val mockApolloClientV2 = MockApolloClientV2()
        val mockCurrentConfigV2 = MockCurrentConfigV2()
        val mockFeatureFlagClient = MockFeatureFlagClient()
        val segmentTestClient = segmentTrackingClient(mockCurrentConfigV2, mockFeatureFlagClient)

        val component = DaggerApplicationComponent.builder()
            .applicationModule(TestApplicationModule(application()))
            .build()

        val config = ConfigFactory.configForUSUser()

        mockCurrentConfigV2.config(config)

        environment = component.environment().toBuilder()
            .apolloClientV2(mockApolloClientV2)
            .currentConfig2(mockCurrentConfigV2)
            .ksCurrency(KSCurrency(mockCurrentConfigV2))
            .stripe(Stripe(context(), Secrets.StripePublishableKey.STAGING))
            .analytics(AnalyticEvents(listOf(segmentTestClient)))
            .attributionEvents(AttributionEvents(mockApolloClientV2))
            .featureFlagClient(mockFeatureFlagClient)
            .build()
    }

    protected fun application() = this.application

    @After
    @Throws(Exception::class)
    public override fun tearDown() {
        super.tearDown()
        DateTimeUtils.setCurrentMillisSystem()
    }

    protected fun context(): Context = this.application().applicationContext

    protected fun environment() = environment

    protected fun ksString() = KSString(application().packageName, application().resources)

    private fun segmentTrackingClient(mockCurrentConfig: MockCurrentConfigV2, ffClient: FeatureFlagClientType): MockTrackingClient {
        segmentTrack = TestSubscriber()
        segmentIdentify = TestSubscriber()
        val segmentTrackingClient = MockTrackingClient(
            MockCurrentUserV2(),
            mockCurrentConfig,
            TrackingClientType.Type.SEGMENT
        )
        segmentTrackingClient.eventNames.subscribe { segmentTrack.onNext(it) }.addToDisposable(disposables)
        segmentTrackingClient.identifiedUser.subscribe { segmentIdentify.onNext(it) }.addToDisposable(disposables)
        return segmentTrackingClient
    }
}
