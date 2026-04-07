package com.kickstarter.libs.featureflag

import android.content.Context
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isTrue
import com.statsig.androidsdk.InitializationDetails
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

class StatsigException(cause: Throwable) : Exception(cause)
interface StatsigClientType {
    fun getSDKKey(): String
    fun isInitialized(): Boolean = Statsig.isInitialized()
    fun checkGate(gateName: String) = Statsig.checkGate(gateName) // TODO: For feature flags, will expand in the future
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

    fun initialize(scope: CoroutineScope, dispatcher: CoroutineDispatcher = Dispatchers.IO, errorCallback: (Throwable) -> Unit) {
        this.scope = scope
        var details: InitializationDetails? = null
        val options = StatsigOptions().apply {
            setTier(
                if (build.isRelease && Build.isExternal()) Tier.PRODUCTION
                else Tier.STAGING
            )
        }
        scope.launch(context = dispatcher) {
            try {
                async {
                    details = Statsig.initialize(
                        application = context as KSApplication,
                        "client-gMosuzVPIQ4U1y6WTCjBM1HF3Y4nIouVqUfciWzH729",
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
