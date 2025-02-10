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
import com.statsig.androidsdk.StatsigUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

interface StatsigClientType {
    suspend fun init(application: KSApplication, sdkKey: String): InitializationDetails? = Statsig.initialize(application, sdkKey)
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

    fun initialize(scope: CoroutineScope, dispatcher: CoroutineDispatcher = Dispatchers.IO, errorCallback: (Exception) -> Unit) {
        this.scope = scope
        scope.launch(context = dispatcher) {
            try {
                val initDetails = init(
                    application = context as KSApplication,
                    sdkKey = getSDKKey()
                )

                if (initDetails?.success.isFalse()) {
                    initDetails?.failureDetails?.exception?.let { errorCallback.invoke(it) }
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
