package com.kickstarter.libs

import android.content.Context
import com.kickstarter.libs.featureflag.StatsigClient
import com.statsig.androidsdk.EvaluationDetails
import com.statsig.androidsdk.EvaluationReason
import com.statsig.androidsdk.FeatureGate
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

class MockStatsigClient(
    context: Context,
    currentUser: CurrentUserTypeV2 = MockCurrentUserV2(),
    private val gateValues: Map<String, Boolean> = emptyMap()
) : StatsigClient(
    build = mockk<Build> { every { isRelease } returns false },
    context = context,
    currentUser = currentUser
) {
    override fun isInitialized(): Boolean = true

    override fun checkGate(gateName: String): Boolean =
        gateValues[gateName] ?: false

    override fun getFeatureGate(gateName: String): FeatureGate =
        FeatureGate(
            gateName,
            EvaluationDetails(EvaluationReason.Unrecognized, lcut = 0),
            false
        )

    override fun getSDKKey(): String = "test-sdk-key"

    override fun initialize(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
        errorCallback: (Throwable) -> Unit
    ) {}
}
