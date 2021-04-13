package com.kickstarter.libs

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.braze.BrazeClient
import com.kickstarter.libs.utils.ConfigFeatureName
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import org.junit.Test

const val mockSenderId = "MockSender"
class BrazeClientTest : KSRobolectricTestCase() {

    lateinit var build: Build
    lateinit var context: Context

    override fun setUp() {
        super.setUp()
        build = environment().build()
        context = application()
    }

    @Test
    fun testInitialize_whenEnabledFeatureFlag() {
        val mockConfig = mockCurrentConfig(enabledFeatureFlag = true)
        val mockClient = MockBrazeClient(build, context, mockConfig)
        assertEquals(mockSenderId, mockClient.getIdSender())
        assertNotNull(mockClient.getLifeCycleCallbacks())
        assertTrue(mockClient.isInitialized)
    }

    @Test
    fun testInitialize_whenDisabledFeatureFlag() {
        val mockConfig = mockCurrentConfig(enabledFeatureFlag = false)
        val mockClient = MockBrazeClient(build, context, mockConfig)
        assertEquals(mockSenderId, "")
        assertFalse(mockClient.isInitialized)
    }

    @Test
    fun testHandleMessageNotBraze_whenEnabledFeatureFlag() {
        val mockConfig = mockCurrentConfig(enabledFeatureFlag = true)
        val mockClient = MockBrazeClient(build, context, mockConfig)
        assertEquals(mockSenderId, mockClient.getIdSender())
        assertNotNull(mockClient.getLifeCycleCallbacks())
        assertTrue(mockClient.isInitialized)

        val message: RemoteMessage = RemoteMessage(Bundle())
        assertFalse(mockClient.handleRemoteMessages(context, message))
    }

    @Test
    fun testHandleMessageNotBraze_whenDisabledFeatureFlag() {
        val mockConfig = mockCurrentConfig(enabledFeatureFlag = false)
        val mockClient = MockBrazeClient(build, context, mockConfig)
        assertEquals(mockSenderId, mockClient.getIdSender())
        assertNotNull(mockClient.getLifeCycleCallbacks())

        val message: RemoteMessage = RemoteMessage(Bundle())
        assertFalse(mockClient.handleRemoteMessages(context, message))
    }

    private fun mockCurrentConfig(enabledFeatureFlag: Boolean) = MockCurrentConfig().apply {
        var config =
                if (enabledFeatureFlag)
                    ConfigFactory.configWithFeatureEnabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
                else ConfigFactory.configWithFeatureDisabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
        config(config)
    }

    class MockBrazeClient(
        private val build: Build,
        private val context: Context,
        private val config: CurrentConfigType
    ) : BrazeClient(build = build, context = context, configuration = config) {
        private var initialized = false

        override val isInitialized: Boolean
            get() = this.initialized

        override fun getIdSender(): String {
            return mockSenderId
        }

        override fun init() {
            initialized = this.isSDKEnabled()
        }

        override fun getLifeCycleCallbacks(): Application.ActivityLifecycleCallbacks {
            return (
                object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    }

                    override fun onActivityStarted(activity: Activity) {
                    }

                    override fun onActivityResumed(activity: Activity) {
                    }

                    override fun onActivityPaused(activity: Activity) {
                    }

                    override fun onActivityStopped(activity: Activity) {
                    }

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    }

                    override fun onActivityDestroyed(activity: Activity) {
                    }
                }
                )
        }
    }
}
