package com.kickstarter.libs.utils

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.kickstarter.KSApplication
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentConfigTypeV2
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Logout
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.syncUserFeatureFlagsFromPref
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.fromThrowable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ApplicationLifecycleUtil(private val application: KSApplication) :
    ActivityLifecycleCallbacks,
    ComponentCallbacks2 {

    @Inject
    lateinit var client: ApiClientTypeV2

    @Inject
    lateinit var config: CurrentConfigTypeV2

    @Inject
    lateinit var currentUser: CurrentUserTypeV2

    @Inject
    lateinit var logout: Logout

    @Inject
    lateinit var build: Build

    @JvmField
    @Inject
    var featuresFlagPreference: StringPreferenceType? = null
    private var isInBackground = true
    private val disposables = CompositeDisposable()

    init {
        application.component().inject(this)
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        if (isInBackground) {
            // Facebook: logs 'install' and 'app activate' App Events.
            AppEventsLogger.activateApp(activity.application)
            refreshConfigFile()
            refreshUser()
            isInBackground = false
        }
    }

    /**
     * Refresh the config file.
     */
    private fun refreshConfigFile() {
        client.config()
            .materialize()
            .share()
            .subscribe { notification ->
                notification.value?.let {
                    // sync save features flags in the config object
                    if (build.isDebug || Build.isInternal()) {
                        it.syncUserFeatureFlagsFromPref(featuresFlagPreference!!)
                    }
                    config.config(it)
                }

                notification.error?.let {
                    handleConfigApiError(fromThrowable(it))
                }
            }.addToDisposable(disposables)
    }

    /**
     * Handles a config API error by logging the user out in the case of a 401. We will interpret
     * 401's on the config request as meaning the user's current access token is no longer valid,
     * as that endpoint should never 401 othewise.
     */
    private fun handleConfigApiError(error: ErrorEnvelope) {
        if (error.httpCode() == 401) {
            forceLogout("config_api_error")
        }
    }

    /**
     * Forces the current user session to be logged out.
     */
    private fun forceLogout(context: String) {
        logout.execute()
        ApplicationUtils.startNewDiscoveryActivity(application)
        val params = Bundle()
        params.putString("location", context)
        FirebaseAnalytics.getInstance(application).logEvent("force_logout", params)
    }

    /**
     * Refreshes the user object if there is not a user logged in with a non-null access token.
     */
    private fun refreshUser() {
        val accessToken = currentUser.accessToken ?: ""

        if (accessToken.isNotNull() && accessToken.isNotEmpty()) {
            client.fetchCurrentUser()
                .doOnError {
                    forceLogout(it.message ?: "")
                }
                .subscribe { user ->
                    currentUser.refresh(user)
                }.addToDisposable(disposables)
        }
    }

    override fun onActivityPaused(activity: Activity) { disposables.clear() }
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onConfigurationChanged(configuration: Configuration) {}
    override fun onLowMemory() {}

    /**
     * Memory availability callback. TRIM_MEMORY_UI_HIDDEN means the app's UI is no longer visible.
     * This is triggered when the user navigates out of the app and primarily used to free resources used by the UI.
     * http://developer.android.com/training/articles/memory.html
     */
    override fun onTrimMemory(i: Int) {
        if (i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            isInBackground = true
        }
    }
}
