package com.kickstarter.libs.featureflag

import android.content.Context
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isTrue
import com.statsig.androidsdk.FeatureGate
import com.statsig.androidsdk.Statsig
import com.statsig.androidsdk.StatsigOptions
import com.statsig.androidsdk.StatsigUser
import com.statsig.androidsdk.Tier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

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
}

open class StatsigClient(
    internal val build: Build,
    private val context: Context,
    private val currentUser: CurrentUserTypeV2
) : StatsigClientType {

    lateinit var scope: CoroutineScope

    override fun getSDKKey() =
        if (build.isRelease && Build.isExternal()) Secrets.Statsig.PRODUCTION
        else Secrets.Statsig.STAGING

    /**
     * Initializes the Statsig SDK using the recommended `async { }.await()` pattern from the
     * Statsig Android documentation for structured concurrency.
     *
     * This method is `open` so that tests can override it to avoid calling the real [Statsig]
     * singleton, which requires a live Application context and network access. Test subclasses
     * should replace the body entirely to simulate success or failure scenarios.
     *
     * @param scope the [CoroutineScope] used to launch initialization; stored for later use by [updateExperimentUser]
     * @param dispatcher the [CoroutineDispatcher] for the initialization coroutine, defaults to [Dispatchers.IO]
     * @param errorCallback invoked with the error when initialization fails or throws
     */
    open fun initialize(scope: CoroutineScope, dispatcher: CoroutineDispatcher = Dispatchers.IO, errorCallback: (Throwable) -> Unit) {
        this.scope = scope
        scope.launch(context = dispatcher) {
            try {
                val options = StatsigOptions().apply {
                    setTier(
                        if (build.isRelease && Build.isExternal()) Tier.PRODUCTION
                        else Tier.STAGING
                    )
                }
                val details = async {
                    Statsig.initialize(
                        application = context as KSApplication,
                        getSDKKey(),
                        StatsigUser(),
                        options = options
                    )
                }.await()

                if (details?.success.isFalse()) {
                    errorCallback.invoke(Throwable(details?.failureDetails?.exception))
                }
            } catch (e: Exception) {
                errorCallback.invoke(e)
            }
        }
    }

    fun updateExperimentUser(dispatcher: CoroutineDispatcher = Dispatchers.IO, errorCallback: (Exception) -> Unit = {}) {
        // TODO: updateExperimentUser might be called before SDK gets initialized add queue for requests to this method happening before initialization
        if (isInitialized()) {
            scope.launch(dispatcher) {
                // TODO: Ideally concatenate with userprivacy to obtain email and other user details not available on V1
                currentUser.observable().asFlow().distinctUntilChanged()
                    .catch {
                        errorCallback.invoke(Exception(it))
                    }
                    .collectLatest { user ->
                        if (isInitialized()) {
                            try {
                                if (user?.isPresent().isTrue())
                                    updateUser(user?.getValue()?.id().toString())
                                else updateUser(getStableId()) // Logged out user use StableID from SDK to identify session
                            } catch (e: Exception) {
                                errorCallback.invoke(e)
                            }
                        }
                    }
            }
        }
    }
}
