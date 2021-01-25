package com.kickstarter

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kickstarter.libs.*
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.mock.services.MockWebClient
import com.stripe.android.Stripe
import junit.framework.TestCase
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber

@RunWith(KSRobolectricGradleTestRunner::class)
@Config(shadows = [ShadowAndroidXMultiDex::class], sdk = [KSRobolectricGradleTestRunner.DEFAULT_SDK])
abstract class KSRobolectricTestCase : TestCase() {
    private val application: Application = ApplicationProvider.getApplicationContext()
    private lateinit var environment: Environment

    lateinit var experimentsTest: TestSubscriber<String>
    lateinit var lakeTest: TestSubscriber<String>
    lateinit var koalaTest: TestSubscriber<String>


    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()

        val mockCurrentConfig = MockCurrentConfig()
        val experimentsClientType = experimentsClient()
        val koalaTrackingClient = koalaTrackingClient(mockCurrentConfig, experimentsClientType)
        val lakeTrackingClient = lakeTrackingClient(mockCurrentConfig, experimentsClientType)

        val component = DaggerApplicationComponent.builder()
                .applicationModule(TestApplicationModule(application()))
                .build()

        environment = component.environment().toBuilder()
                .apiClient(MockApiClient())
                .apolloClient(MockApolloClient())
                .currentConfig(mockCurrentConfig)
                .webClient(MockWebClient())
                .stripe(Stripe(context(), Secrets.StripePublishableKey.STAGING))
                .koala(Koala(koalaTrackingClient))
                .lake(Koala(lakeTrackingClient))
                .optimizely(experimentsClientType)
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

    private fun experimentsClient(): MockExperimentsClientType {
        experimentsTest = TestSubscriber()
        val experimentsClientType = MockExperimentsClientType()
        experimentsClientType.eventKeys.subscribe(experimentsTest)
        return experimentsClientType
    }

    private fun koalaTrackingClient(mockCurrentConfig: MockCurrentConfig, experimentsClientType: MockExperimentsClientType): MockTrackingClient {
        koalaTest = TestSubscriber()
        val koalaTrackingClient = MockTrackingClient(MockCurrentUser(), mockCurrentConfig, TrackingClientType.Type.KOALA, experimentsClientType)
        koalaTrackingClient.eventNames.subscribe(koalaTest)
        return koalaTrackingClient
    }

    private fun lakeTrackingClient(mockCurrentConfig: MockCurrentConfig, experimentsClientType: MockExperimentsClientType): MockTrackingClient {
        lakeTest = TestSubscriber()
        val lakeTrackingClient = MockTrackingClient(MockCurrentUser(),
                mockCurrentConfig, TrackingClientType.Type.LAKE, experimentsClientType)
        lakeTrackingClient.eventNames.subscribe(lakeTest)
        return lakeTrackingClient
    }
}