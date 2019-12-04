package com.kickstarter.libs

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.utils.ConfigUtils
import com.kickstarter.libs.utils.MapUtils
import com.kickstarter.models.User
import com.kickstarter.services.LakeBackgroundService
import com.kickstarter.services.firebase.dispatchJob
import com.kickstarter.ui.IntentKey
import io.fabric.sdk.android.Fabric
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class LakeTrackingClient(
        @param:ApplicationContext private val context: Context,
        @set:Inject var currentUser: CurrentUserType,
        @set:Inject var build: Build,
        @set:Inject var currentConfig: CurrentConfigType) : TrackingClientType() {
    private var loggedInUser: User? = null
    private var config: Config? = null

    init {

        // Cache the most recent logged in user for default Lake properties.
        this.currentUser.observable().subscribe { u -> this.loggedInUser = u }

        // Cache the most recent config for default Lake properties.
        this.currentConfig.observable().subscribe { c -> this.config = c }
    }

    override fun track(eventName: String, additionalProperties: Map<String, Any>) {
        try {
            val trackingData = getTrackingData(eventName, combinedProperties(additionalProperties))
            val bundle = Bundle()
            bundle.putString(IntentKey.LAKE_EVENT_NAME, eventName)
            bundle.putString(IntentKey.LAKE_EVENT, trackingData)

            val uniqueJobName = LakeBackgroundService.BASE_JOB_NAME + System.currentTimeMillis()
            dispatchJob(this.context, LakeBackgroundService::class.java, uniqueJobName, bundle)
            if (this.build.isDebug) {
                Log.d(TAG, "Queued event:$trackingData")
            }
        } catch (e: JSONException) {
            if (this.build.isDebug) {
                Timber.e("Failed to encode event: $eventName")
            }
            Fabric.getLogger().e(TAG, "Failed to encode event: $eventName")
        }

    }

    @Throws(JSONException::class)
    private fun getTrackingData(eventName: String, newProperties: Map<String, Any>): String {
        val data = JSONObject()
        data.put("event", eventName)

        val compactProperties = MapUtils.compact(newProperties)
        val propertiesJSON = JSONObject()
        for ((key, value) in compactProperties) {
            propertiesJSON.put(key, value)
        }
        data.put("properties", propertiesJSON)

        val record = JSONObject()
        record.put("partition-key", UUID.randomUUID().toString())
        record.put("data", data)
        return record.toString()
    }

    override fun androidUUID(): String {
        return FirebaseInstanceId.getInstance().id
    }

    override fun brand(): String {
        return android.os.Build.BRAND
    }

    override fun cleanPropertiesOnly(): Boolean = true

    /**
     * Derives the device's orientation (portrait/landscape) from the `context`.
     */
    override fun deviceOrientation(): String {
        return if (this.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            "landscape"
        } else "portrait"
    }

    override fun deviceFormat(): String {
        return if (this.context.resources.getBoolean(R.bool.isTablet)) "tablet" else "phone"
    }

    override fun enabledFeatureFlags(): JSONArray? {
        return ConfigUtils.enabledFeatureFlags(this.config)
    }

    /**
     * Derives the availability of google play services from the `context`.
     */
    override fun isGooglePlayServicesAvailable(): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.context) == ConnectionResult.SUCCESS
    }

    override fun isTalkBackOn(): Boolean {
        val am = this.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
        return am?.isTouchExplorationEnabled ?: false
    }

    override fun manufacturer(): String {
        return android.os.Build.MANUFACTURER
    }

    override fun model(): String {
        return android.os.Build.MODEL
    }

    override fun OSVersion(): String {
        return android.os.Build.VERSION.RELEASE
    }

    override fun time(): Long {
        return System.currentTimeMillis() / 1000
    }

    override fun loggedInUser(): User? {
        return this.loggedInUser
    }

    override fun versionName(): String {
        return BuildConfig.VERSION_NAME
    }

    companion object {
        private val TAG = LakeTrackingClient::class.java.simpleName
    }

}
