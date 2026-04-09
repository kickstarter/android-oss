package com.kickstarter.libs

import android.content.Context
import com.kickstarter.libs.featureflag.StatsigClient
import com.statsig.androidsdk.EvaluationDetails
import com.statsig.androidsdk.EvaluationReason
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
class MockStatsigClient(
    context: Context,
    currentUser: CurrentUserTypeV2 = MockCurrentUserV2(),
    private val gateMap: Map<String, Boolean> = emptyMap()
) : StatsigClient(
    build = mockk<Build> { every { isRelease } returns false },
    context = context,
    currentUser = currentUser,
    sdkInitializer = { null }
) {
    init {
        _isReady.value = true
    }

    override fun isInitialized(): Boolean = true

    override fun checkGate(gateName: String): Boolean =
        gateMap[gateName] ?: false

    override fun getFeatureGate(gateName: String): FeatureGate =
        FeatureGate(
            gateName,
            EvaluationDetails(EvaluationReason.Unrecognized, lcut = 0),
            gateMap[gateName] ?: false
        )

    override fun getSDKKey(): String = "test-sdk-key"
}
