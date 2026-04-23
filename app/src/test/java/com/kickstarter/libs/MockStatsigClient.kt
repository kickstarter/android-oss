package com.kickstarter.libs

import android.content.Context
import com.kickstarter.libs.featureflag.StatsigClient
import com.statsig.androidsdk.EvaluationDetails
import com.statsig.androidsdk.EvaluationReason
import com.statsig.androidsdk.FeatureGate
import com.statsig.androidsdk.StatsigOverrides
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.ConcurrentHashMap

/**
 * A test-only [StatsigClient] that never touches the real Statsig SDK.
 *
 * Starts in a ready state ([isReady] emits `true` immediately), so downstream consumers
 * like `DiscoveryFragmentViewModel` can evaluate gates without waiting for initialization.
 *
 * Supports local overrides via [overrideGate] and [removeGateOverride]. Overrides take
 * precedence over [gateMap] values, mirroring real SDK behaviour.
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
 * @param gateMap a map of gate names to their baseline boolean values. Overrides applied via
 *   [overrideGate] take precedence. Gates absent from both maps default to `false`.
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
    private val overrideMap: ConcurrentHashMap<String, Boolean> = ConcurrentHashMap()

    init {
        _isReady.value = true
    }

    override fun isInitialized(): Boolean = true

    override fun checkGate(gateName: String): Boolean =
        overrideMap[gateName] ?: gateMap[gateName] ?: false

    override fun getFeatureGate(gateName: String): FeatureGate {
        val isOverridden = overrideMap.containsKey(gateName)
        val value = overrideMap[gateName] ?: gateMap[gateName] ?: false
        val reason = if (isOverridden) EvaluationReason.LocalOverride else EvaluationReason.Unrecognized
        return FeatureGate(gateName, EvaluationDetails(reason, lcut = 0), value)
    }

    override fun overrideGate(gateName: String, value: Boolean) {
        overrideMap[gateName] = value
    }

    override fun removeGateOverride(gateName: String) {
        overrideMap.remove(gateName)
    }

    override fun getAllOverrides(): StatsigOverrides =
        StatsigOverrides(ConcurrentHashMap(overrideMap), ConcurrentHashMap(), ConcurrentHashMap())

    override fun getSDKKey(): String = "test-sdk-key"
}
