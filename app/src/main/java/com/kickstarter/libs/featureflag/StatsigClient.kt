package com.kickstarter.libs.featureflag

import android.content.Context
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isTrue
import com.statsig.androidsdk.FeatureGate
import com.statsig.androidsdk.InitializationDetails
import com.statsig.androidsdk.Statsig
import com.statsig.androidsdk.StatsigOptions
import com.statsig.androidsdk.StatsigUser
import com.statsig.androidsdk.Tier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StatsigGateKey(val key: String) {
    ANDROID_VIDEO_FEED("android_video_feed")
}

class StatsigException(cause: Throwable) : Exception(cause)
interface StatsigClientType {
    fun getSDKKey(): String
    fun isInitialized(): Boolean = Statsig.isInitialized()
    fun checkGate(gateName: String) = Statsig.checkGate(gateName)

    /**
     * Returns the full [FeatureGate] object for [gateName], including the evaluated boolean value,
     * the matched [com.statsig.androidsdk.FeatureGate.getRuleID] (which rule in the Statsig
     * console was responsible for the result), and [com.statsig.androidsdk.EvaluationDetails]
     * describing how the value was resolved (network, cache, etc.).
     *
     * Prefer this over [checkGate] when you need more than just the boolean — e.g. for logging
     * which rule was evaluated or for debugging targeting behaviour.
     */
    fun getFeatureGate(gateName: String): FeatureGate = Statsig.getFeatureGate(gateName)
    fun getExperiment(experimentName: String) = Statsig.getExperiment(experimentName) // TODO: for test MOCK statsig DynamicConfig type or expose to the rest of the app just the json values, will explore in the future
    suspend fun updateUser(id: String) = Statsig.updateUser(StatsigUser(id))
    fun getStableId() = Statsig.getStableID()

    /** Emits true once the SDK has been successfully initialized. */
    val isReady: StateFlow<Boolean>
}

/**
 * @param build the app [Build] info, used to determine production vs staging environment.
 * @param context the application [Context], cast to [KSApplication] during SDK initialization.
 * @param currentUser provides the current user observable for updating the Statsig user on login/logout.
 * @param sdkInitializer a suspend lambda that performs the actual SDK initialization and returns
 *   [InitializationDetails]. The default calls [Statsig.initialize] with the appropriate
 *   environment tier and SDK key. Tests can inject a fake lambda (e.g. `{ null }`) to avoid
 *   hitting the real Statsig SDK.
 */
open class StatsigClient @JvmOverloads constructor(
    internal val build: Build,
    private val context: Context,
    private val currentUser: CurrentUserTypeV2,
    private val sdkInitializer: suspend () -> InitializationDetails? = {
        val isProduction = build.isRelease && Build.isExternal()
        val options = StatsigOptions().apply {
            setTier(if (isProduction) Tier.PRODUCTION else Tier.STAGING)
        }
        Statsig.initialize(
            application = context as KSApplication,
            if (isProduction) Secrets.Statsig.PRODUCTION else Secrets.Statsig.STAGING,
            StatsigUser(),
            options = options
        )
    }
) : StatsigClientType {

    lateinit var scope: CoroutineScope

    protected val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    override fun getSDKKey() =
        if (build.isRelease && Build.isExternal()) Secrets.Statsig.PRODUCTION
        else Secrets.Statsig.STAGING

    /**
     * Initializes the Statsig SDK by invoking [sdkInitializer], a suspend lambda that
     * encapsulates the actual SDK call. Production code uses the default lambda which calls
     * [Statsig.initialize]; tests can inject a fake lambda to avoid the real SDK.
     *
     * On success, [_isReady] is set to `true`, signalling downstream consumers (via [isReady])
     * that gate checks will return accurate values.
     *
     * @param scope the [CoroutineScope] used to launch initialization; stored for later use by [updateExperimentUser]
     * @param dispatcher the [CoroutineDispatcher] for the initialization coroutine, defaults to [Dispatchers.IO]
     * @param errorCallback invoked with the error when initialization fails or throws
     */
    fun initialize(scope: CoroutineScope, dispatcher: CoroutineDispatcher = Dispatchers.IO, errorCallback: (Throwable) -> Unit) {
        this.scope = scope
        scope.launch(context = dispatcher) {
            try {
                val details = sdkInitializer()

                _isReady.value = details?.success.isTrue()

                if (details?.success.isFalse()) {
                    errorCallback.invoke(Throwable(details?.failureDetails?.exception))
                }
            } catch (e: Exception) {
                errorCallback.invoke(e)
            }
        }
    }
}
