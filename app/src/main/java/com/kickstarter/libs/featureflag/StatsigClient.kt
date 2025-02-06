package com.kickstarter.libs.featureflag

import android.content.Context
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.User
import com.statsig.androidsdk.Statsig
import com.statsig.androidsdk.StatsigUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

interface StatsigClientType {
    fun initialize(applicationScope: CoroutineScope)
    fun isInitialized(): Boolean = Statsig.isInitialized()
    fun checkGate(gateName: String) = Statsig.checkGate(gateName)
    fun getExperiment(experimentName: String) = Statsig.getExperiment(experimentName) // TODO: for test MOCK statsig DynamicConfig type or expose to the rest of the app just the json values, will explore in the future
}

class StatsigClient(
    internal val build: Build,
    private val context: Context,
    private val currentUser: CurrentUserTypeV2,
) : StatsigClientType {

    private var loggedInUser: User? = null
    private var scope: CoroutineScope? = null

    override fun initialize(applicationScope: CoroutineScope) {
        scope = applicationScope
        scope?.launch {

            if (context.isKSApplication()) {
                Statsig.initialize(
                    application = context as KSApplication,
                    sdkKey =
                    if (build.isRelease && Build.isExternal()) Secrets.Statsig.PRODUCTION
                    else Secrets.Statsig.STAGING,
                    // TODO: Build the user adding the rest of the information required
                    // userAgent, locale ...
                    user = if (loggedInUser.isNotNull()) StatsigUser(
                        userID = loggedInUser?.id().toString()
                    ) else null
                )
            }

            currentUser.loggedInUser().asFlow().distinctUntilChanged().collectLatest {
                loggedInUser = it

                // TODO: Build the user adding the rest of the information required
                // userAgent, locale ...
                Statsig.updateUser(StatsigUser(loggedInUser?.id().toString()))
            }
        }
    }
}
