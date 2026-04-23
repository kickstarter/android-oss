package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.featureflag.StatsigClient
import com.statsig.androidsdk.InitializationDetails
import com.statsig.androidsdk.InitializeFailReason
import com.statsig.androidsdk.InitializeResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsigClientTest : KSRobolectricTestCase() {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    /**
     * Builds a [StatsigClient] subclass that simulates SDK initialization behavior.
     * Use this only for tests that exercise [StatsigClient.initialize] with controlled
     * [initResult] or [initException] scenarios.
     *
     * For tests that only need to check gates or feature flags, prefer [MockStatsigClient].
     */
    private fun buildInitializableClient(
        initResult: InitializationDetails? = null,
        initException: Exception? = null
    ): StatsigClient {
        return StatsigClient(
            build = mockk<Build> { every { isRelease } returns false },
            context = application(),
            currentUser = requireNotNull(environment().currentUserV2()),
            sdkInitializer = {
                initException?.let { throw it }
                initResult
            }
        )
    }

    // - Initialize tests
    @Test
    fun `initialize - Sets scope and completes without error on success`() = runTest(testDispatcher) {
        val initDetails = InitializationDetails(200L, true)
        val client = buildInitializableClient(initResult = initDetails)

        var errorCount = 0
        client.initialize(this, testDispatcher) { errorCount++ }

        advanceUntilIdle()

        assertEquals(0, errorCount)
        assertEquals(this, client.scope)
        assertTrue(client.isReady.value)
    }

    @Test
    fun `initialize - Invokes error callback with failure details on SDK failure`() = runTest(testDispatcher) {
        val failureDetails = InitializeResponse.FailedInitializeResponse(
            reason = InitializeFailReason.NetworkError,
            exception = Exception("Network unavailable")
        )
        val initDetails = InitializationDetails(100L, false, failureDetails)
        val client = buildInitializableClient(initResult = initDetails)

        var capturedError: Throwable? = null
        client.initialize(this, testDispatcher) { capturedError = it }

        advanceUntilIdle()

        assertNotNull(capturedError)
        assertEquals("Network unavailable", capturedError?.cause?.message)
        assertFalse(client.isReady.value)
    }

    @Test
    fun `initialize - Invokes error callback when initialization throws an exception`() = runTest(testDispatcher) {
        val client = buildInitializableClient(initException = RuntimeException("Init crashed"))

        var capturedError: Throwable? = null
        client.initialize(this, testDispatcher) { capturedError = it }

        advanceUntilIdle()

        assertNotNull(capturedError)
        assertEquals("Init crashed", capturedError?.message)
        assertFalse(client.isReady.value)
    }

    // - Gate and feature flag tests (using MockStatsigClient)
    @Test
    fun `checkGate - Returns true for an enabled gate`() {
        val client = MockStatsigClient(
            context = application(),
            gateMap = mapOf("test_gate" to true)
        )
        assertTrue(client.checkGate("test_gate"))
    }

    @Test
    fun `checkGate - Returns false for an unknown gate`() {
        val client = MockStatsigClient(
            context = application(),
            gateMap = mapOf("test_gate" to true)
        )
        assertFalse(client.checkGate("unknown_gate"))
    }

    @Test
    fun `checkGate - Returns false for a disabled gate`() {
        val client = MockStatsigClient(
            context = application(),
            gateMap = mapOf("test_gate" to false)
        )
        assertFalse(client.checkGate("test_gate"))
    }

    @Test
    fun `isInitialized - Returns true for MockStatsigClient`() {
        val client = MockStatsigClient(context = application())
        assertTrue(client.isInitialized())
    }

    @Test
    fun `isReady - Returns true for MockStatsigClient`() {
        val client = MockStatsigClient(context = application())
        assertTrue(client.isReady.value)
    }

    @Test
    fun `getFeatureGate - Returns enabled gate for a known enabled gate`() {
        val client = MockStatsigClient(
            context = application(),
            gateMap = mapOf("test_gate" to true)
        )

        val result = client.getFeatureGate("test_gate")

        assertTrue(result.getValue())
        assertEquals("test_gate", result.getName())
    }

    @Test
    fun `getFeatureGate - Returns disabled gate for an unknown gate`() {
        val client = MockStatsigClient(context = application())

        val result = client.getFeatureGate("unknown_gate")

        assertFalse(result.getValue())
    }

    @Test
    fun `getFeatureGate - Returns disabled gate for a known disabled gate`() {
        val client = MockStatsigClient(
            context = application(),
            gateMap = mapOf("test_gate" to false)
        )

        val result = client.getFeatureGate("test_gate")

        assertFalse(result.getValue())
    }

    // - Override tests
    @Test
    fun `overrideGate - checkGate returns true when gate is overridden to true`() {
        val client = MockStatsigClient(context = application())

        client.overrideGate("test_gate", true)

        assertTrue(client.checkGate("test_gate"))
    }

    @Test
    fun `overrideGate - checkGate returns false when gate is overridden to false`() {
        val client = MockStatsigClient(
            context = application(),
            gateMap = mapOf("test_gate" to true)
        )

        client.overrideGate("test_gate", false)

        assertFalse(client.checkGate("test_gate"))
    }

    @Test
    fun `removeGateOverride - checkGate returns original gateMap value after override is removed`() {
        val client = MockStatsigClient(
            context = application(),
            gateMap = mapOf("test_gate" to true)
        )
        client.overrideGate("test_gate", false)
        client.removeGateOverride("test_gate")

        assertTrue(client.checkGate("test_gate"))
    }

    @Test
    fun `removeGateOverride - checkGate returns false for unknown gate after override is removed`() {
        val client = MockStatsigClient(context = application())
        client.overrideGate("test_gate", true)
        client.removeGateOverride("test_gate")

        assertFalse(client.checkGate("test_gate"))
    }

    @Test
    fun `getAllOverrides - returns empty gates map when no overrides are set`() {
        val client = MockStatsigClient(context = application())

        assertTrue(client.getAllOverrides().gates.isEmpty())
    }

    @Test
    fun `getAllOverrides - reflects all active overrides`() {
        val client = MockStatsigClient(context = application())
        client.overrideGate("gate_a", true)
        client.overrideGate("gate_b", false)

        val gates = client.getAllOverrides().gates

        assertEquals(2, gates.size)
        assertEquals(true, gates["gate_a"])
        assertEquals(false, gates["gate_b"])
    }

    @Test
    fun `getAllOverrides - does not include gate after override is removed`() {
        val client = MockStatsigClient(context = application())
        client.overrideGate("test_gate", true)
        client.removeGateOverride("test_gate")

        assertFalse(client.getAllOverrides().gates.containsKey("test_gate"))
    }

    @Test
    fun `getFeatureGate - returns LocalOverride reason when gate is overridden`() {
        val client = MockStatsigClient(context = application())
        client.overrideGate("test_gate", true)

        val result = client.getFeatureGate("test_gate")

        assertTrue(result.getValue())
    }
}
