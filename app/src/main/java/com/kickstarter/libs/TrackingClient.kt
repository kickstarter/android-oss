package com.kickstarter.libs

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.accessibility.AccessibilityManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.utils.WebUtils
import com.kickstarter.libs.utils.extensions.currentVariants
import com.kickstarter.models.User
import com.kickstarter.ui.SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE
import org.json.JSONException
import timber.log.Timber
import javax.inject.Inject

abstract class TrackingClient(
    @param:ApplicationContext private val context: Context,
    @set:Inject var currentUser: CurrentUserTypeV2,
    @set:Inject var build: Build,
    @set:Inject var currentConfig: CurrentConfigTypeV2,
    @set:Inject var ffClient: FeatureFlagClientType,
    @set:Inject var sharedPreferences: SharedPreferences
) : TrackingClientType() {

    override val isGooglePlayServicesAvailable: Boolean
        get() = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.context) == ConnectionResult.SUCCESS

    override val isTalkBackOn: Boolean
        get() {
            val am = this.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
            return am?.isTouchExplorationEnabled ?: false
        }

    override fun track(eventName: String, additionalProperties: Map<String, Any>) {
        if (isEnabled()) {
            try {
                trackingData(eventName, combinedProperties(additionalProperties))

                if (this.build.isDebug) {
                    val dataForLogs = combinedProperties(additionalProperties).toString()
                    Timber.d("Queued ${type().tag} $eventName event: $dataForLogs")
                }
            } catch (e: JSONException) {
                if (this.build.isDebug) {
                    Timber.e("Failed to encode ${type().tag} event: $eventName")
                }
                FirebaseCrashlytics.getInstance().log("E/${TrackingClient::class.java.simpleName}: Failed to encode ${type().tag} event: $eventName")
            }
        }
    }

    override fun isEnabled(): Boolean {
        return if (ffClient.getBoolean(FlagKey.ANDROID_CONSENT_MANAGEMENT)) {
            sharedPreferences.getBoolean(CONSENT_MANAGEMENT_PREFERENCE, false)
        } else true
    }

    override fun reset() {
        if (isEnabled()) this.loggedInUser = null
    }

    override fun identify(user: User) {
        if (isEnabled()) this.loggedInUser = user
    }

    /**
     * Send data to the Tracking clients.
     * implementation differs between Lake and Segment
     * Segment will call a third party dependency, while Lake will send the event to a concrete
     * endpoint.
     */
    abstract fun trackingData(eventName: String, newProperties: Map<String, Any?>)

    // Default property values
    override fun brand(): String = android.os.Build.BRAND

    override fun buildNumber(): Int = BuildConfig.VERSION_CODE

    override fun currentVariants(): Array<String>? = this.config?.currentVariants()

    override fun deviceDistinctId(): String = FirebaseHelper.identifier

    override fun deviceFormat(): String =
        if (this.context.resources.getBoolean(R.bool.isTablet)) "tablet"
        else "phone"

    /**
     * Derives the device's orientation (portrait/landscape) from the `context`.
     */
    override fun deviceOrientation(): String =
        if (this.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "landscape"
        else "portrait"

    override fun manufacturer(): String = android.os.Build.MANUFACTURER

    override fun model(): String = android.os.Build.MODEL

    override fun OSVersion(): String = android.os.Build.VERSION.RELEASE

    override fun time() = System.currentTimeMillis() / 1000

    override fun loggedInUser(): User? = this.loggedInUser

    override fun userAgent(): String = WebUtils.userAgent(this.build)

    override fun userCountry(user: User): String = user.location()?.country() ?: this.config?.countryCode() ?: ""

    override fun sessionCountry(): String = this.config?.countryCode() ?: ""

    override fun versionName(): String = BuildConfig.VERSION_NAME

    override fun wifiConnection(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cm?.activeNetwork?.let {
            cm.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } ?: false
    }

    override fun sessionForceDarkMode(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    true
                }
                else -> false
            }
        } else {
            false
        }
    }
}
