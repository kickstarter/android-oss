package com.kickstarter

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockTrackingClient
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.mock.services.MockWebClient
import com.kickstarter.models.User
import com.stripe.android.Stripe
import junit.framework.TestCase
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import kotlin.jvm.Throws
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

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()

        val mockCurrentConfig = MockCurrentConfig()
        val mockCurrentConfigV2 = MockCurrentConfigV2()
        val mockFeatureFlagClient: MockFeatureFlagClient = MockFeatureFlagClient()
        val segmentTestClient = segmentTrackingClient(mockCurrentConfig, mockFeatureFlagClient)

        val component = DaggerApplicationComponent.builder()
            .applicationModule(TestApplicationModule(application()))
            .build()

        val config = ConfigFactory.config().toBuilder()
            .build()

        mockCurrentConfig.config(config)
        mockCurrentConfigV2.config(config)

        environment = component.environment().toBuilder()
            .ksCurrency(KSCurrency(mockCurrentConfig))
            .apiClient(MockApiClient())
            .apolloClient(MockApolloClient())
            .apolloClientV2(MockApolloClientV2())
            .currentConfig(mockCurrentConfig)
            .currentConfig2(mockCurrentConfigV2)
            .webClient(MockWebClient())
            .stripe(Stripe(context(), Secrets.StripePublishableKey.STAGING))
            .analytics(AnalyticEvents(listOf(segmentTestClient)))
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

    private fun segmentTrackingClient(mockCurrentConfig: MockCurrentConfig, ffClient: FeatureFlagClientType): MockTrackingClient {
        segmentTrack = TestSubscriber()
        segmentIdentify = TestSubscriber()
        val segmentTrackingClient = MockTrackingClient(
            MockCurrentUser(),
            mockCurrentConfig,
            TrackingClientType.Type.SEGMENT,
            ffClient
        )
        segmentTrackingClient.eventNames.subscribe(segmentTrack)
        segmentTrackingClient.identifiedUser.subscribe(segmentIdentify)
        return segmentTrackingClient
    }
}
