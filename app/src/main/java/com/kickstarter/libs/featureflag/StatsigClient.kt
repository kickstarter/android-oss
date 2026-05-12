package com.kickstarter.libs.featureflag

import android.content.Context
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.SegmentTrackingClient
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isTrue
import com.segment.analytics.Analytics
import com.statsig.androidsdk.FeatureGate
import com.statsig.androidsdk.InitializationDetails
import com.statsig.androidsdk.Statsig
import com.statsig.androidsdk.Statsig.getInitializeResponseJson
import com.statsig.androidsdk.StatsigOptions
import com.statsig.androidsdk.StatsigUser
import com.statsig.androidsdk.Tier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import timber.log.Timber

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
     * the matched [FeatureGate.getRuleID] (which rule in the Statsig
     * console was responsible for the result), and [com.statsig.androidsdk.EvaluationDetails]
     * describing how the value was resolved (network, cache, etc.).
     *
     * Prefer this over [checkGate] when you need more than just the boolean — e.g. for logging
     * which rule was evaluated or for debugging targeting behaviour.
     */
    fun getFeatureGate(gateName: String): FeatureGate = Statsig.getFeatureGate(gateName)
    fun getExperiment(experimentName: String) = Statsig.getExperiment(experimentName) // TODO: for test MOCK statsig DynamicConfig type or expose to the rest of the app just the json values, will explore in the future
    suspend fun updateUser(user: StatsigUser) = Statsig.updateUser(user)
    fun getStableId() = Statsig.getStatsigMetadata().stableID

    /** Emits true once the SDK has been successfully initialized. */
    val isReady: StateFlow<Boolean>

    suspend fun handleObservedUserData(stableId: String?, segmentAnonymousId: String?, userId: String?)
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
    private val segmentTrackingClient: SegmentTrackingClient,
    private val getAnonymousId: () -> String? = {
        Analytics.with(context).analyticsContext.traits().anonymousId()
    },
    private val sdkInitializer: suspend StatsigClient.() -> InitializationDetails? = {
        val isProduction = build.isRelease && Build.isExternal()
        val options = StatsigOptions().apply {
            setTier(if (isProduction) Tier.PRODUCTION else Tier.STAGING)
        }
        val user = StatsigUser()
        val details = Statsig.initialize(
            application = context as KSApplication,
            if (isProduction) Secrets.Statsig.PRODUCTION else Secrets.Statsig.STAGING,
            user,
            options = options
        )
        Timber.d("Statsig.initialize():")
        Timber.d("- Stable ID: ${getStableId()}")
        val initializeResponseJson = Statsig.runCatching { getInitializeResponseJson() }.getOrNull()
        initializeResponseJson?.let {
            Timber.d("- EvaluationDetails: ${it.getEvalDetails()}")
            Timber.d("- JSON: ${it.getInitializeResponseJSON()}")
        }
        details
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
        this.scope = (scope + dispatcher)
        scope.launch {
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

    fun observeUserAndFetchConfigs(scope: CoroutineScope) {
        scope.launch {
            val statsigInitialization = isReady
            val segmentInitializationFlow = segmentTrackingClient.initialized
            val currentUserFlow = currentUser.observable().asFlow()
            combine(statsigInitialization, segmentInitializationFlow, currentUserFlow) { statsigIsReady, segmentIsReady, optionalUser ->
                Triple(statsigIsReady, segmentIsReady, optionalUser)
            }
                .map { (statsigIsReady, segmentIsReady, optionalUser) ->
                    val stableId = if (statsigIsReady) getStableId() else null
                    val segmentAnonymousId = if (segmentIsReady) getAnonymousId() else null
                    val userId = if (optionalUser.isPresent()) optionalUser.getValue()?.id().toString() else null
                    Triple(stableId, segmentAnonymousId, userId)
                }
                // TODO: consider debounce
                .collect {
                    val (stableId, segmentAnonymousId, userId) = it
                    handleObservedUserData(stableId, segmentAnonymousId, userId)
                }
        }
    }

    override suspend fun handleObservedUserData(stableId: String?, segmentAnonymousId: String?, userId: String?) {
        // `stableId` is automatically attached to `StatsigUser` internally, but included here for clarity, testing, and debugging
        Timber.d("handleObservedUserData(stableId: $stableId, segmentAnonymousId: $segmentAnonymousId, userId: $userId)")

        if (stableId == null && segmentAnonymousId == null && userId == null) return

        val statsigUser = StatsigUser()
        segmentAnonymousId?.let {
            statsigUser.customIDs = mapOf("segmentAnonymousId" to it)
        }
        userId?.let {
            statsigUser.userID = userId
        }

        try {
            Timber.d("Statsig.updateUser($statsigUser):")
            updateUser(statsigUser)
            val initializeResponseJson = Statsig.runCatching { getInitializeResponseJson() }.getOrNull()
            initializeResponseJson?.let {
                Timber.d("- EvaluationDetails: ${it.getEvalDetails()}")
                Timber.d("- JSON: ${it.getInitializeResponseJSON()}")
            }
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    override suspend fun updateUser(user: StatsigUser) {
        super.updateUser(user)
    }
}
