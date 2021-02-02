package com.kickstarter.libs

import android.content.Context
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.utils.MapUtils
import com.kickstarter.models.User
import org.json.JSONException
import org.json.JSONObject

class LakeTrackingClient(
        @param:ApplicationContext private val context: Context,
        currentUser: CurrentUserType,
        build: Build,
        currentConfig: CurrentConfigType,
        optimizely: ExperimentsClientType) : TrackingClient(context, currentUser, build, currentConfig, optimizely) {
    private var loggedInUser: User? = null
    private var config: Config? = null

    init {

        // Cache the most recent config for default Lake properties.
        this.currentConfig.observable().subscribe { c -> this.config = c }
    }

    override fun type(): Type {
        return Type.LAKE
    }

    @Throws(JSONException::class)
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>): String {
        val data = JSONObject()
        data.put("event", eventName)

        val compactProperties = MapUtils.compact(newProperties)
        val propertiesJSON = JSONObject()
        for ((key, value) in compactProperties) {
            propertiesJSON.put(key, value)
        }
        data.put("properties", propertiesJSON)

        val record = JSONObject()
        record.put("partition-key", this.loggedInUser?.id()?.toString() ?: deviceDistinctId())
        record.put("data", data)
        return record.toString()
    }

}
