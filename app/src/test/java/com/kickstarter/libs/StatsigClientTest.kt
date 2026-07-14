package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.featureflag.StatsigClient
import com.kickstarter.mock.factories.UserFactory
import com.statsig.androidsdk.EvalReason
import com.statsig.androidsdk.EvalSource
import com.statsig.androidsdk.InitializationDetails
import com.statsig.androidsdk.InitializeFailReason
import com.statsig.androidsdk.InitializeResponse
import com.statsig.androidsdk.Statsig
import com.statsig.androidsdk.StatsigOptions
import com.statsig.androidsdk.StatsigUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsigClientTest : KSRobolectricTestCase() {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private lateinit var segmentTrackingClient: SegmentTest.MockSegmentTrackingClient

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
        segmentTrackingClient = mockSegmentTrackingClient()
        return StatsigClient(
            build = mockk<Build> { every { isRelease } returns false },
            context = application(),
            currentUser = requireNotNull(environment().currentUserV2()),
            segmentTrackingClient = segmentTrackingClient,
            sdkInitializer = {
                initException?.let { throw it }
                initResult
            },
        )
    }

    private fun buildOfflineClientForOverrides(): StatsigClient {
        segmentTrackingClient = mockSegmentTrackingClient()
        return StatsigClient(
            build = mockk<Build> { every { isRelease } returns false },
            context = application(),
            currentUser = requireNotNull(environment().currentUserV2()),
            segmentTrackingClient = segmentTrackingClient,
            sdkInitializer = {
                if (initializationDetails == null) {
                    initializationDetails = Statsig.initialize(
                        application(),
                        sdkKey = "test-sdk-key",
                        user = null,
                        StatsigOptions(
                            initializeOffline = true,
                            loggingEnabled = false
                        )
                    )
                }
                initializationDetails
            },
        )
    }

    private fun mockSegmentTrackingClient(): SegmentTest.MockSegmentTrackingClient {
        val build = environment().build()!!
        val context = application()
        val currentConfigV2 = environment().currentConfigV2()!!
        val currentUserV2 = environment().currentUserV2()!!
        val sharedPreferences = MockSharedPreferences()
        return SegmentTest.MockSegmentTrackingClient(
            build, context, currentConfigV2, currentUserV2, sharedPreferences
        )
    }

    @After
    fun after() {
        Statsig.removeAllOverrides()
    }

    // - Initialize tests
    @Test
    fun `initialize - Sets scope and completes without error on success`() = runTest(testDispatcher) {
        val initDetails = InitializationDetails(200L, true, source = EvalSource.Network)
        val client = buildInitializableClient(initResult = initDetails)

        var errorCount = 0
        client.initialize(this, testDispatcher) { errorCount++ }

        advanceUntilIdle()

        assertEquals(0, errorCount)
        assertTrue(client.isReady.value)
    }

    @Test
    fun `initialize - Invokes error callback with failure details on SDK failure`() = runTest(testDispatcher) {
        val failureDetails = InitializeResponse.FailedInitializeResponse(
            reason = InitializeFailReason.NetworkError,
            exception = Exception("Network unavailable")
        )
        val initDetails = InitializationDetails(100L, false, failureDetails, source = EvalSource.Network)
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

    @Test
    fun `getExperiment - unrecognized`() = runTest {
        val standardTestDispatcher = StandardTestDispatcher(testScheduler)

        val client = buildOfflineClientForOverrides()

        var errorCount = 0
        client.initialize(this, standardTestDispatcher) { errorCount++ }

        advanceUntilIdle()

        val experimentName = "does_not_exist"
        val experiment = client.getExperiment(experimentName)
        val evalDetails = experiment.getEvalDetails()

        assertEquals(0, errorCount)
        assertEquals(evalDetails.reason, EvalReason.Unrecognized)
    }

    @Test
    fun `getExperiment - recognized override`() = runTest {
        val standardTestDispatcher = StandardTestDispatcher(testScheduler)

        val client = buildOfflineClientForOverrides()

        var errorCount = 0
        client.initialize(this, standardTestDispatcher) { errorCount++ }

        advanceUntilIdle()

        val experimentName = "experiment_that_exists"

        Statsig.overrideConfig(
            experimentName,
            mapOf()
        )

        val experiment = client.getExperiment(experimentName)
        val evalDetails = experiment.getEvalDetails()
        println(evalDetails)

        assertEquals(0, errorCount)
        assertEquals(EvalReason.LocalOverride, evalDetails.reason)
    }

    @Test
    fun `getExperiment - parameter exists with correct type`() = runTest {
        val standardTestDispatcher = StandardTestDispatcher(testScheduler)

        val client = buildOfflineClientForOverrides()

        var errorCount = 0
        client.initialize(this, standardTestDispatcher) { errorCount++ }

        advanceUntilIdle()

        val experimentName = "experiment_that_exists"

        Statsig.overrideConfig(
            experimentName,
            mapOf("number" to 1)
        )

        val experiment = client.getExperiment(experimentName)
        val evalDetails = experiment.getEvalDetails()
        val intValue = experiment.getIntIfPresent("number")

        assertEquals(0, errorCount)
        assertEquals(EvalReason.LocalOverride, evalDetails.reason)
        assertEquals(1, intValue)
    }

    @Test
    fun `getExperiment - parameter does not exist`() = runTest {
        val standardTestDispatcher = StandardTestDispatcher(testScheduler)

        val client = buildOfflineClientForOverrides()

        var errorCount = 0
        client.initialize(this, standardTestDispatcher) { errorCount++ }

        advanceUntilIdle()

        val experimentName = "experiment_that_exists"

        Statsig.overrideConfig(
            experimentName,
            mapOf("number" to 1)
        )

        val experiment = client.getExperiment(experimentName)
        val evalDetails = experiment.getEvalDetails()
        val stringValue = experiment.getStringIfPresent("string")

        assertEquals(0, errorCount)
        assertEquals(EvalReason.LocalOverride, evalDetails.reason)
        assertNull(stringValue)
    }

    @Test
    fun `test user data observation`() = runTest {
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))

        val data = mutableListOf<ObservedData>()

        val currentUser = MockCurrentUserV2()
        val segmentTrackingClient = mockSegmentTrackingClient()
        val statsigClient = object : MockStatsigClient(
            context = application(),
            currentUser = currentUser,
            segmentTrackingClient = segmentTrackingClient,
            startReady = false
        ) {
            override suspend fun handleObservedUserData(
                stableId: String?,
                segmentAnonymousId: String?,
                userId: String?,
                isAdmin: Boolean
            ) {
                data += ObservedData(stableId, segmentAnonymousId, userId, isAdmin)
            }
        }

        statsigClient.observeUserAndFetchConfigs(testScope)

        assertEquals(ObservedData(null, null, null, false), data.last())

        statsigClient.triggerReady()

        assertEquals(ObservedData(statsigClient.getStableId(), null, null, false), data.last())

        segmentTrackingClient.initialize()

        assertEquals(ObservedData(statsigClient.getStableId(), segmentTrackingClient.getAnonymousIdOrNull(), null, false), data.last())

        val user = UserFactory.user()
        currentUser.login(user)

        assertEquals(ObservedData(statsigClient.getStableId(), segmentTrackingClient.getAnonymousIdOrNull()!!, user.id().toString(), false), data.last())

        val adminUser = user.toBuilder().isAdmin(true).build()
        currentUser.login(adminUser)

        assertEquals(ObservedData(statsigClient.getStableId(), segmentTrackingClient.getAnonymousIdOrNull()!!, adminUser.id().toString(), true), data.last())
    }

    @Test
    fun `handleObservedUserData - forwards is_ksr_admin as a private attribute for logged-in users`() = runTest {
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))

        val currentUser = MockCurrentUserV2()
        val segmentTrackingClient = mockSegmentTrackingClient()

        val updatedUsers = mutableListOf<StatsigUser>()
        val statsigClient = object : MockStatsigClient(
            context = application(),
            currentUser = currentUser,
            segmentTrackingClient = segmentTrackingClient,
            startReady = false
        ) {
            override suspend fun updateUser(user: StatsigUser) { updatedUsers += user }
        }

        statsigClient.observeUserAndFetchConfigs(testScope)
        statsigClient.triggerReady()
        segmentTrackingClient.initialize()
        advanceUntilIdle()

        // Anonymous users carry no admin concept, so no admin attribute is attached.
        assertNull(updatedUsers.last().privateAttributes)

        // Logged-in non-admin: is_ksr_admin = false.
        val user = UserFactory.user()
        currentUser.login(user)
        advanceUntilIdle()
        assertEquals(user.id().toString(), updatedUsers.last().userID)
        assertEquals(mapOf(StatsigClient.KEY_IS_KSR_ADMIN to false), updatedUsers.last().privateAttributes)

        // Admin (same id, only isAdmin flips): is_ksr_admin = true is forwarded to Statsig. Also
        // guards against distinctUntilChanged swallowing an admin-status change on an unchanged id.
        val adminUser = user.toBuilder().isAdmin(true).build()
        currentUser.login(adminUser)
        advanceUntilIdle()
        assertEquals(mapOf(StatsigClient.KEY_IS_KSR_ADMIN to true), updatedUsers.last().privateAttributes)
    }

    @Test
    fun `updateUser - StatsigUser is mutated when successful`() = runTest {
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))

        val currentUser = MockCurrentUserV2()
        val segmentTrackingClient = mockSegmentTrackingClient()
        val statsigClient = object : MockStatsigClient(
            context = application(),
            currentUser = currentUser,
            segmentTrackingClient = segmentTrackingClient,
            startReady = false
        ) {
            override suspend fun updateUser(user: StatsigUser) {}
        }

        var expectedStatsigUser = StatsigUser()
        assertEquals(expectedStatsigUser, statsigClient.statsigUser.value)

        statsigClient.observeUserAndFetchConfigs(testScope)
        statsigClient.triggerReady()
        segmentTrackingClient.initialize()

        expectedStatsigUser = StatsigUser().apply {
            customIDs = mapOf(StatsigClient.KEY_SEGMENT_ANONYMOUS_ID to segmentTrackingClient.getAnonymousIdOrNull()!!)
        }
        assertEquals(expectedStatsigUser, statsigClient.statsigUser.value)

        val user = UserFactory.user()
        currentUser.login(user)

        expectedStatsigUser = StatsigUser().apply {
            userID = user.id().toString()
            customIDs = mapOf(StatsigClient.KEY_SEGMENT_ANONYMOUS_ID to segmentTrackingClient.getAnonymousIdOrNull()!!)
        }
        assertEquals(expectedStatsigUser, statsigClient.statsigUser.value)
    }

    @Test
    fun `updateUser - StatsigUser is unchanged when failed`() = runTest {
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))

        val currentUser = MockCurrentUserV2()
        val segmentTrackingClient = mockSegmentTrackingClient()
        val statsigClient = object : MockStatsigClient(
            context = application(),
            currentUser = currentUser,
            segmentTrackingClient = segmentTrackingClient,
            startReady = false
        ) {
            override suspend fun updateUser(user: StatsigUser) { throw Exception() }
        }

        statsigClient.observeUserAndFetchConfigs(testScope)
        statsigClient.triggerReady()
        segmentTrackingClient.initialize()
        currentUser.login(UserFactory.user())

        assertEquals(StatsigUser(), statsigClient.statsigUser.value)
    }

    @Test
    fun `updateUser - method is not called when StatsigClient is not ready`() = runTest {
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))

        var count = 0

        val currentUser = MockCurrentUserV2()
        val segmentTrackingClient = mockSegmentTrackingClient()
        val statsigClient = object : MockStatsigClient(
            context = application(),
            currentUser = currentUser,
            segmentTrackingClient = segmentTrackingClient,
            startReady = false
        ) {
            override suspend fun updateUser(user: StatsigUser) { count++ }
        }

        statsigClient.observeUserAndFetchConfigs(testScope)
        segmentTrackingClient.initialize()
        currentUser.login(UserFactory.user())

        assertEquals(0, count)
    }

    /** Test-only mirror of the params passed to [StatsigClient.handleObservedUserData]. */
    private data class ObservedData(
        val stableId: String?,
        val segmentAnonymousId: String?,
        val userId: String?,
        val isAdmin: Boolean
    )

    companion object {
        var initializationDetails: InitializationDetails? = null
    }
}
