package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.featureflag.StatsigClient
import com.statsig.androidsdk.EvaluationDetails
import com.statsig.androidsdk.EvaluationReason
import com.statsig.androidsdk.FeatureGate
import com.statsig.androidsdk.InitializationDetails
import com.statsig.androidsdk.InitializeFailReason
import com.statsig.androidsdk.InitializeResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsigClientTest : KSRobolectricTestCase() {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    /**
     * Creates a [StatsigClient] subclass that never touches the real [com.statsig.androidsdk.Statsig]
     * singleton. Every SDK-facing method is overridden so tests run without a live Application
     * context, network access, or Statsig initialization.
     *
     * @param initResult the [InitializationDetails] that [initialize] will return to simulate SDK responses
     * @param initException when non-null, [initialize] will throw this instead of returning [initResult],
     *                      simulating a crash during SDK initialization
     * @param gateValues a map of gate names to their boolean values used by [checkGate];
     *                   any gate not present in the map defaults to false
     * @param featureGates a map of gate names to [FeatureGate] objects used by [getFeatureGate];
     *                     gates absent from the map return a default disabled [FeatureGate] with
     *                     an empty rule ID and [EvaluationReason.Unrecognized]
     * @param initialized the value returned by [isInitialized], controlling whether the client
     *                    reports itself as ready
     */
    fun buildClient(
        initResult: InitializationDetails? = null,
        initException: Exception? = null,
        gateValues: Map<String, Boolean> = emptyMap(),
        featureGates: Map<String, FeatureGate> = emptyMap(),
        initialized: Boolean = true
    ): StatsigClient {
        val mockCurrentUser: CurrentUserTypeV2 = requireNotNull(environment().currentUserV2())
        val mockBuild = mockk<Build> {
            every { isRelease } returns false
        }

        return object : StatsigClient(
            build = mockBuild,
            context = application(),
            currentUser = mockCurrentUser
        ) {
            override fun isInitialized(): Boolean = initialized

            override fun checkGate(gateName: String): Boolean =
                gateValues[gateName] ?: false

            override fun getFeatureGate(gateName: String): FeatureGate =
                featureGates[gateName] ?: FeatureGate(
                    gateName,
                    EvaluationDetails(EvaluationReason.Unrecognized, lcut = 0),
                    false
                )

            override fun getSDKKey(): String = "test-sdk-key"

            override fun initialize(
                scope: CoroutineScope,
                dispatcher: CoroutineDispatcher,
                errorCallback: (Throwable) -> Unit
            ) {
                this.scope = scope
                scope.launch(context = dispatcher) {
                    try {
                        val details = initException?.let { throw it } ?: initResult
                        if (details?.success == false) {
                            errorCallback.invoke(Throwable(details.failureDetails?.exception))
                        }
                    } catch (e: Exception) {
                        errorCallback.invoke(e)
                    }
                }
            }
        }
    }

    @Test
    fun `initialize - Sets scope and completes without error on success`() = runTest(testDispatcher) {
        val initDetails = InitializationDetails(200L, true)
        val client = buildClient(initResult = initDetails)

        var errorCount = 0
        client.initialize(this, testDispatcher) { errorCount++ }

        advanceUntilIdle()

        assertEquals(0, errorCount)
        assertEquals(this, client.scope)
    }

    @Test
    fun `initialize - Invokes error callback with failure details on SDK failure`() = runTest(testDispatcher) {
        val failureDetails = InitializeResponse.FailedInitializeResponse(
            reason = InitializeFailReason.NetworkError,
            exception = Exception("Network unavailable")
        )
        val initDetails = InitializationDetails(100L, false, failureDetails)
        val client = buildClient(initResult = initDetails)

        var capturedError: Throwable? = null
        client.initialize(this, testDispatcher) { capturedError = it }

        advanceUntilIdle()

        assertNotNull(capturedError)
        assertEquals("Network unavailable", capturedError?.cause?.message)
    }

    @Test
    fun `initialize - Invokes error callback when initialization throws an exception`() = runTest(testDispatcher) {
        val client = buildClient(initException = RuntimeException("Init crashed"))

        var capturedError: Throwable? = null
        client.initialize(this, testDispatcher) { capturedError = it }

        advanceUntilIdle()

        assertNotNull(capturedError)
        assertEquals("Init crashed", capturedError?.message)
    }

    @Test
    fun `checkGate - Returns true for an enabled gate`() {
        val client = buildClient(gateValues = mapOf("test_gate" to true))
        assertTrue(client.checkGate("test_gate"))
    }

    @Test
    fun `checkGate - Returns false for an unknown gate`() {
        val client = buildClient(gateValues = mapOf("test_gate" to true))
        assertFalse(client.checkGate("unknown_gate"))
    }

    @Test
    fun `checkGate - Returns false for a disabled gate`() {
        val client = buildClient(gateValues = mapOf("test_gate" to false))
        assertFalse(client.checkGate("test_gate"))
    }

    @Test
    fun `isInitialized - Returns true when initialized and false when not`() {
        val initializedClient = buildClient(initialized = true)
        assertTrue(initializedClient.isInitialized())

        val uninitializedClient = buildClient(initialized = false)
        assertFalse(uninitializedClient.isInitialized())
    }

    @Test
    fun `getFeatureGate - Returns gate value and matching rule ID for a known gate`() {
        val gate = FeatureGate(
            "test_gate",
            EvaluationDetails(EvaluationReason.Network, lcut = 0),
            true,
            "targeting_rule_us_users"
        )
        val client = buildClient(featureGates = mapOf("test_gate" to gate))

        val result = client.getFeatureGate("test_gate")

        assertTrue(result.getValue())
        assertEquals("targeting_rule_us_users", result.getRuleID())
        assertEquals("test_gate", result.getName())
    }

    @Test
    fun `getFeatureGate - Returns disabled gate with empty rule ID for an unknown gate`() {
        val client = buildClient()

        val result = client.getFeatureGate("unknown_gate")

        assertFalse(result.getValue())
        assertEquals("", result.getRuleID())
    }

    @Test
    fun `getFeatureGate - Returns correct evaluation reason from gate details`() {
        val gate = FeatureGate(
            "cached_gate",
            EvaluationDetails(EvaluationReason.Cache, lcut = 0),
            true,
            "cache_rule"
        )
        val client = buildClient(featureGates = mapOf("cached_gate" to gate))

        val result = client.getFeatureGate("cached_gate")

        assertEquals(EvaluationReason.Cache, result.getEvaluationDetails()?.reason)
    }
}
