package com.kickstarter.libs

import android.content.Context
import com.kickstarter.libs.featureflag.StatsigClient
import com.statsig.androidsdk.DynamicConfig
import com.statsig.androidsdk.EvalDetails
import com.statsig.androidsdk.EvalReason
import com.statsig.androidsdk.EvalSource
import com.statsig.androidsdk.FeatureGate
import io.mockk.every
import io.mockk.mockk

/**
 * A test-only [StatsigClient] that never touches the real Statsig SDK.
 *
 * Starts in a ready state ([isReady] emits `true` immediately), so downstream consumers
 * like `DiscoveryFragmentViewModel` can evaluate gates without waiting for initialization.
 *
 * Usage:
 * ```
 * val client = MockStatsigClient(
 *     context = application(),
 *     gateMap = mapOf(StatsigGateKey.ANDROID_VIDEO_FEED.key to true)
 * )
 * val environment = environment().toBuilder()
 *     .statsigClient(client)
 *     .build()
 * ```
 *
 * @param context an Android [Context], typically obtained via `application()` in Robolectric tests.
 * @param currentUser the current user observable; defaults to [MockCurrentUserV2] (logged-out).
 * @param gateMap a map of gate names to their boolean values. Used by both [checkGate] (returns
 *   the boolean directly) and [getFeatureGate] (wraps the value in a [FeatureGate] with
 *   [EvaluationReason.Unrecognized]). Gates absent from the map default to `false`.
 */
open class MockStatsigClient(
    context: Context,
    currentUser: CurrentUserTypeV2 = MockCurrentUserV2(),
    segmentTrackingClient: SegmentTrackingClient = mockk<SegmentTrackingClient>(),
    private val gateMap: Map<String, Boolean> = emptyMap(),
    private val experimentMap: Map<String, Map<String, Any>> = emptyMap(),
    startReady: Boolean = true
) : StatsigClient(
    build = mockk<Build> { every { isRelease } returns false },
    context = context,
    currentUser = currentUser,
    segmentTrackingClient = segmentTrackingClient,
    sdkInitializer = { null }
) {
    /**
     * When true, simulates the async `updateUser` reload window: [configReady] is `false` and gate
     * reads come back with reason `Loading:Unrecognized` (value `false`), exactly like the real SDK
     * between a user change and the new values landing.
     */
    private var reloadingValues = false

    init {
        if (startReady) {
            _isReady.value = true
            _configReady.value = true
        }
    }

    /**
     * Simulates a successful cold-start init: the SDK is ready ([isReady]) and values for the
     * initial user have settled ([configReady]). In production both flip to `true` once the first
     * init/updateUser completes; use [beginUserReload]/[completeUserReload] to model a later
     * user-change reload window where only [configReady] toggles.
     */
    fun triggerReady() {
        _isReady.value = true
        _configReady.value = true
    }

    /**
     * Simulates the start of a user change (login/logout): values for the new user are not loaded
     * yet, so [configReady] flips to `false` and gate reads become `Loading:Unrecognized`. Mirrors
     * production [StatsigClient.handleObservedUserData] before `updateUser` completes.
     */
    fun beginUserReload() {
        reloadingValues = true
        _configReady.value = false
    }

    /** Simulates the reload finishing: values have settled for the current user. */
    fun completeUserReload() {
        reloadingValues = false
        _configReady.value = true
    }

    override fun isInitialized(): Boolean = true

    override fun checkGate(gateName: String): Boolean =
        if (reloadingValues) false else gateMap[gateName] ?: false

    override fun getFeatureGate(gateName: String): FeatureGate =
        if (reloadingValues) {
            FeatureGate(
                gateName,
                EvalDetails(EvalSource.Loading, EvalReason.Unrecognized),
                false
            )
        } else {
            FeatureGate(
                gateName,
                EvalDetails(EvalSource.Network, EvalReason.Recognized),
                gateMap[gateName] ?: false
            )
        }

    override fun getExperiment(experimentName: String): DynamicConfig =
        DynamicConfig(
            name = experimentName,
            EvalDetails(EvalSource.NoValues, EvalReason.Unrecognized),
            experimentMap[experimentName] ?: emptyMap()
        )

    override fun getSDKKey(): String = "test-sdk-key"

    override fun getStableId(): String? = "11111111-1111-1111-1111-111111111111"
}
