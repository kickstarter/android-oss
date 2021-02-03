package com.kickstarter.libs

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.accessibility.AccessibilityManager
import androidx.work.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.utils.WebUtils
import com.kickstarter.models.User
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.libs.utils.extensions.currentVariants
import com.kickstarter.libs.utils.extensions.enabledFeatureFlags
import org.json.JSONArray
import org.json.JSONException
import timber.log.Timber
import javax.inject.Inject

abstract class TrackingClient(@param:ApplicationContext private val context: Context,
                     @set:Inject var currentUser: CurrentUserType,
                     @set:Inject var build: Build,
                     @set:Inject var currentConfig: CurrentConfigType,
                     @set:Inject var optimizely: ExperimentsClientType) : TrackingClientType() {

    private var loggedInUser: User? = null
    private var config: Config? = null

    init {

        // Cache the most recent logged in user for default Lake properties.
        this.currentUser.observable()
                .distinctUntilChanged()
                .subscribe { u ->
                    this.loggedInUser = u
                    this.loggedInUser?.let { identify(it) }
                }

        // Cache the most recent config for default Lake properties.
        this.currentConfig.observable().subscribe { c -> this.config = c }
    }

    override val isGooglePlayServicesAvailable: Boolean
        get() = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.context) == ConnectionResult.SUCCESS

    override val isTalkBackOn: Boolean
        get() {
            val am = this.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
            return am?.isTouchExplorationEnabled ?: false
        }

    override fun track(eventName: String, additionalProperties: Map<String, Any>) {
        try {
            val eventData = trackingData(eventName, combinedProperties(additionalProperties))

            if (this.build.isDebug) {
                Timber.d("Queued ${type().tag} $eventName event: $eventData")
            }
        } catch (e: JSONException) {
            if (this.build.isDebug) {
                Timber.e("Failed to encode ${type().tag} event: $eventName")
            }
            FirebaseCrashlytics.getInstance().log("E/${TrackingClient::class.java.simpleName}: Failed to encode ${type().tag} event: $eventName")
        }
    }

    override fun identify(user: User) {
        this.loggedInUser = user
        if (this.build.isDebug) {
            user.apply {
                Timber.d("Queued ${type().tag} Identify userName: ${this.name()} userId: ${ this.id()}")
            }
        }
    }

    override fun optimizely(): ExperimentsClientType = this.optimizely

    /**
     * Send data to the Tracking clients.
     * implementation differs between Lake and Segment
     * Segment will call a third party dependency, while Lake will send the event to a concrete
     * endpoint.
     */
    abstract fun trackingData(eventName: String, newProperties: Map<String, Any?>)

    //Default property values
    override fun brand(): String = android.os.Build.BRAND

    override fun buildNumber(): Int = BuildConfig.VERSION_CODE

    override fun currentVariants(): JSONArray? = this.config?.currentVariants()

    override fun deviceDistinctId(): String = FirebaseInstanceId.getInstance().id

    override fun deviceFormat(): String =
            if (this.context.resources.getBoolean(R.bool.isTablet)) "tablet"
            else "phone"

    /**
     * Derives the device's orientation (portrait/landscape) from the `context`.
     */
    override fun deviceOrientation(): String =
            if (this.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "Landscape"
            else "Portrait"

    override fun enabledFeatureFlags(): JSONArray {
        return JSONArray(this.optimizely.enabledFeatures(this.loggedInUser))
                .apply {
                    val configFlags = this@TrackingClient.config?.enabledFeatureFlags()
                    configFlags?.let {
                        for (index in 0 until it.length()) {
                            put(it.get(index))
                        }
                    }
                }
    }

    override fun manufacturer(): String = android.os.Build.MANUFACTURER

    override fun model(): String = android.os.Build.MODEL

    override fun OSVersion(): String = android.os.Build.VERSION.RELEASE

    override fun time() = System.currentTimeMillis() / 1000

    override fun loggedInUser(): User? = this.loggedInUser

    override fun userAgent(): String = WebUtils.userAgent(this.build)

    override fun userCountry(user: User): String =  user.location()?.country() ?: this.config?.countryCode() ?: ""

    override fun versionName(): String =  BuildConfig.VERSION_NAME

    override fun wifiConnection(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cm?.activeNetwork?.let {
            cm.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } ?: false
    }
}
