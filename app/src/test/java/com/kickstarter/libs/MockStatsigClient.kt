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
    init {
        if (startReady) _isReady.value = true
    }

    fun triggerReady() {
        _isReady.value = true
    }

    override fun isInitialized(): Boolean = true

    override fun checkGate(gateName: String): Boolean =
        gateMap[gateName] ?: false

    override fun getFeatureGate(gateName: String): FeatureGate =
        FeatureGate(
            gateName,
            EvalDetails(EvalSource.NoValues, EvalReason.Unrecognized),
            gateMap[gateName] ?: false
        )

    override fun getExperiment(experimentName: String): DynamicConfig =
        DynamicConfig(
            name = experimentName,
            EvalDetails(EvalSource.NoValues, EvalReason.Unrecognized),
            experimentMap[experimentName] ?: emptyMap()
        )

    override fun getSDKKey(): String = "test-sdk-key"

    override fun getStableId(): String? = "11111111-1111-1111-1111-111111111111"
}
