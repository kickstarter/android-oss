package com.kickstarter.libs

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.utils.MapUtils
import com.kickstarter.models.User
import com.kickstarter.services.LakeBackgroundService
import com.kickstarter.services.firebase.dispatchJob
import com.kickstarter.ui.IntentKey
import io.fabric.sdk.android.Fabric
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class LakeTrackingClient(
        @param:ApplicationContext private val context: Context,
        currentUser: CurrentUserType,
        build: Build,
        currentConfig: CurrentConfigType) : TrackingClient(context, currentUser, build, currentConfig) {
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

    override fun cleanPropertiesOnly(): Boolean = true

    companion object {
        private val TAG = LakeTrackingClient::class.java.simpleName
    }

}
